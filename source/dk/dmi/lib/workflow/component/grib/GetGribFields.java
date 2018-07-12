package dk.dmi.lib.workflow.component.grib;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import dk.dmi.lib.grib.GribField;
import dk.dmi.lib.grib.GribServer;
import dk.dmi.lib.grib.GribServerException;
import dk.dmi.lib.grib.Request;
import dk.dmi.lib.grib.Response;
import dk.dmi.lib.grib.TimeoutException;
import dk.dmi.lib.persistence.common.DatabasePersistenceConfiguration;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.persistence.database.processdb.publicc.entity.WorkflowActivity;
import dk.dmi.lib.util.DateTime;
import dk.dmi.lib.util.Period;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Deprecated // new version available
@Component(
		name = "Get GRIB fields", 
		category = "Grib",
		description = "Fetching GRIB fields from GRIB server, defined by model name, time of the model run, forecast time and a list of parameters. Null is returned if not all parameters for the given times are available.",
        version = 1)
public class GetGribFields extends BaseComponent {
  
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDescriptions = {"Model name", "Time of the model run", "Forecast time", "Parameter names known by grib server"},
			returnDescription = "Map with Grib Field's fetched. parameter name is key. null if fails")
	public Map<String, GribField> execute(String modelName, Date analyseDateTime, Date modelDateTime, List<String> gribParameterNames) throws IOException, TimeoutException, GribServerException {
		DateTime analyseTime = new DateTime(analyseDateTime);
		DateTime forecastTime = new DateTime(modelDateTime);
		Period lengthPeriod = forecastTime.subtract(analyseTime);
		int days = (int) lengthPeriod.getDays();
		int hours = lengthPeriod.getHours();
		int length = 24*days + hours;
		
		//
		// Work-around for ECM-REJS_GG when lenth exceeds 144
		// and timestep changes from 3h to 6h
		//
		if(modelName.equals("ECM-REJS_GG"))
		{
		  if (length > 144)
		  {
		    if (length % 6 == 3)
		      length = length-3;
		  }
		}
		
		Map<String, GribField> gribFieldMap = getGribFieldMap(modelName, analyseTime, length, gribParameterNames);

		// Return null if not all requested fields are fetched
		if (gribFieldMap != null && gribFieldMap.size() < gribParameterNames.size()) {
			gribFieldMap = null;
		}
		
		return gribFieldMap;
	}
	
	private Map<String, GribField> getGribFieldMap(String modelName, DateTime analyseTime, int length, List<String> gribParameterNames) throws IOException, TimeoutException, GribServerException {
	  List<Request> requestList = generateRequestList(modelName, analyseTime, length, gribParameterNames);
	  Map<Request, Response> responseMap = getGribFields(modelName, length, requestList);
	  Map<String, GribField> gribFieldMap = null;
	  
	  if(responseMap != null) {
		  gribFieldMap = CreateGribFieldMap(modelName, length, requestList, responseMap);
	  }
	  
	  return gribFieldMap;
	}
	
	List<Request> generateRequestList(String modelName, DateTime analyseTime, int length, List<String> gribParameterNames) {
		List<Request> requestList = new LinkedList<Request>();

		for (String fieldName : gribParameterNames) {
			Request request = new Request(modelName, analyseTime, length, fieldName);
			requestList.add(request);
		}
		
		return requestList;
	}

	Map<Request, Response> getGribFields(String modelName, int length,List<Request> requestList) throws IOException, TimeoutException {
		int numberRequests = requestList.size();
		System.out.printf("Requesting %d fields from model %s length %d\n", numberRequests, modelName, length);
		
		ResourceBundle gribServerResourceBundle = null;
		
		if(DatabasePersistenceConfiguration.getCurrentConnectedDatabaseId() == DatabasePersistenceConfiguration.TEST_DATABASE_ID) {
			File configFile = new File(DatabasePersistenceConfiguration.getTestDatabaseConfigurationFilePath()[0]+"/..");
			URL[] configUrls = new URL[]{configFile.toURI().toURL()};
			ClassLoader configClassLoader = new URLClassLoader(configUrls);
			gribServerResourceBundle = ResourceBundle.getBundle("GribServerTest", Locale.getDefault(), configClassLoader);
		} else if(DatabasePersistenceConfiguration.getCurrentConnectedDatabaseId() == DatabasePersistenceConfiguration.PROD_DATABASE_ID) {
			File configFile = new File(DatabasePersistenceConfiguration.getProdDatabaseConfigurationFilePath()[0]+"/..");
			URL[] configUrls = new URL[]{configFile.toURI().toURL()};
			ClassLoader configClassLoader = new URLClassLoader(configUrls);
			gribServerResourceBundle = ResourceBundle.getBundle("GribServerProd", Locale.getDefault(), configClassLoader);
		}
		
		Map<Request, Response> responseMap = null;
		
		if(gribServerResourceBundle != null) {
			Object workflowActivityObject = workflowContextController.getObjectForKey("_ACTIVITY");
			String workflowDefinitionName = "Undefined workflow";
			
			if(workflowActivityObject != null && workflowActivityObject instanceof WorkflowActivity) {
				WorkflowActivity workflowActivity = (WorkflowActivity) workflowActivityObject;
				workflowDefinitionName = workflowActivity.getWorkflow().getWorkflowDefinition().getName();
			}
			
			GribServer gribServer = new GribServer(gribServerResourceBundle, workflowDefinitionName);
			
			try {
				responseMap = gribServer.getResponses(requestList);
			} catch(Exception e) {
				System.out.println("Failed to get response from grib server!");
			}
		}
		
		return responseMap;
	}

	Map<String, GribField> CreateGribFieldMap(String modelName, int length, List<Request> requestList, Map<Request, Response> responseMap) throws GribServerException {
		Map<String, GribField> gribFieldMap = new HashMap<>();
		
		for (Request request : requestList) {
			Response response = responseMap.get(request);
			
			if (response.hasGribField()) {
				String fieldName = response.getField();
				GribField gribField = response.getGribField();
				gribFieldMap.put(fieldName, gribField);
			} else {
				String fieldName = response.getField();
				String sAnalyseTime = response.getReferenceTime().getDateTimeString("yyyy-MM-dd HH:mm");
				System.out.printf("Missing field %s from model %s analyse time %s length %d\n", fieldName, modelName, sAnalyseTime, length);
			}
		}
		
		return gribFieldMap;
	}
	
}
