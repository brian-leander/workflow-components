package dk.dmi.lib.workflow.component.grib;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import dk.dmi.lib.geo.GeoPoint;
import dk.dmi.lib.grib.GribField;
import dk.dmi.lib.grib.GribServer;
import dk.dmi.lib.grib.GribServerException;
import dk.dmi.lib.grib.GridPoint;
import dk.dmi.lib.grib.HasNoValueException;
import dk.dmi.lib.grib.NotImplementedException;
import dk.dmi.lib.grib.OutsideAreaException;
import dk.dmi.lib.grib.Request;
import dk.dmi.lib.grib.Response;
import dk.dmi.lib.grib.TimeoutException;
import dk.dmi.lib.persistence.common.DatabasePersistenceConfiguration;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.persistence.database.processdb.publicc.entity.WorkflowActivity;
import dk.dmi.lib.util.DateTime;
import dk.dmi.lib.util.Period;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

/**
 * @author mlm
 *
 */
@Deprecated // Use component GetGribFields instead
@Component(
		name = "Get GRIB data", 
		category = "Grib",
		description = "Fetching GRIB fields from GRIB server",
        version = 1)
public class GetGribData extends BaseComponent {
	
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
  
  	@ArgumentListGetMethod(
  	    	argumentIndex = "3")
      public String[] getMaxBackwardModelRunList(String ignore) {
      	String[] maxBackwardModelRuns = {"1", "2", "3", "4"};
      	return maxBackwardModelRuns;
      }
  
	@ExecuteMethod(
			argumentDescriptions = {"Model name", "model time is analyze time plus length (in hours)", "Parameter names known by server", "Number of previous model run to attempt, where 1 is the latest"},
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, 
			    WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			returnDescription = "?Map with GriField's fetched. parameter name is key. null if fails")
	public Map<String, GribField> execute(String modelName, Date modelTime, List<String> gribParameterTags, int maxBackwardModelrun) throws IOException, TimeoutException, GribServerException
	{
	  //
	  // Get analyseTime and length
	  //
	  int nModelRuns = 4;  //Number modelruns each day  TODO get it from DB
	  int modelRunPeriodInHours = 24/nModelRuns;
	  Period modelRunPeriod = new Period(modelRunPeriodInHours,0, 0, 0);
	  DateTime forecastTime = new DateTime(modelTime);
	  DateTime analyseTime = forecastTime.floorUsingPeriod(modelRunPeriod);
	  Period lengthPeriod = forecastTime.subtract(analyseTime);
	  int length = lengthPeriod.getHours();
	  
	  //
	  // Find number of tries. 
	  // This is the number of analysis for whitch we will try to get data.
	  // We will start with the latest (and best) prognose,
	  // then the next latest and so on the maxNumberTries times.
	  //
	  //TODO calculate maxNumberTries
	  //
	  
	  int endLength = 55;       //TODO Get value fron DB
	  
	  int numberTries = 1;
	  boolean fieldsNotFetched = true;
	  Map<String, GribField> gribFieldMap = null;
	  
	  while (fieldsNotFetched && numberTries <= maxBackwardModelrun)
	  {
	    //
	    // Get GRIB fields if possible
	    //
	    gribFieldMap = getGribFields(modelName,
		                         analyseTime,
		                         length,
		                         gribParameterTags);

	    //
	    // test if requested fields are fetched
	    // If not make ready for another try with the nearest later analyse
	    //
	    if (gribFieldMap != null)
	    {
	      fieldsNotFetched = false;  // The fields are fetched
	    }
	    else
	    {
	      // Try again with this earlier analyse
	      analyseTime =  analyseTime.subtract(modelRunPeriod);
	    }	    
	    
	    numberTries++;
	  }

	  //TODO check condition latest available moodel (return null if fails)
	  //TODO check condition incomplete parameter responses (return null if fails)

	  return gribFieldMap;
	}
	
	////////////////////////////////////////////////////////////////////////
	/**
	 * Fetch GRIB fields for specified model, analyse time and length
	 * @param modelName The model name as known by the GRIB server
	 * @param analyseTime Tha iteration or analyse time
	 * @param length The length. This is the time offset from analyse time in hours
	 * @param gribParameterTags The parameter names as known by the GRIB server
	 * @param allowIncompleteParameterResponseList
	 * @return List of requested GRIB fields in same order as requested.
	 * @throws GribServerException Exception thrown if an error at server side occurs.
	 * @throws IOException
	 * @throws TimeoutException Exception thrown if timeout occurs
	 */
	private Map<String, GribField> getGribFields(String modelName,
	                                              DateTime analyseTime,
	                                              int length,
	                                              List<String> gribParameterTags) 
	                  throws GribServerException, IOException, TimeoutException
	{
	  //
	  // Make list of requests.
	  // Request all parameters for this modelrun and length
	  //
	  List<Request> requestList = new LinkedList<>();
	  for (String fieldName:gribParameterTags)
	  {
	    Request request = new Request(modelName, analyseTime, length, fieldName);
	    requestList.add(request);
	  }

	  //
	  // Get grib fields
	  //
	  int numberRequests = requestList.size();
	  System.out.println();
	  System.out.printf("Requesting %d fields from model %s length %d\n", numberRequests, modelName, length);
	  
	  
	  // #######################################################################

	  Map<String, GribField> gribFieldMap = new HashMap<>();
	  
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
			responseMap = gribServer.getResponses(requestList);
			
			//
			// Make Map of GribField's received from server
			//
			for (Request request : requestList)
			{
				Response response = responseMap.get(request);
				if (response.hasGribField())
				{
					String fieldName = response.getField();
					GribField gribField = response.getGribField();
					gribFieldMap.put(fieldName, gribField);
				}
				else
				{
					String fieldName = response.getField();
					String sAnalyseTime = response.getReferenceTime().getDateTimeString("yyyy-MM-dd HH:mm");
					System.out.printf("Missing field %s from model %s analyse time %s length %d\n", fieldName, modelName, sAnalyseTime, length);      
				}
			}
			
			//
			// Test for complete response. All requested fields must be fetched.
			// Return a Map of GribField's if all requested fields are fetched.
			//	  
			if (gribFieldMap.size() < requestList.size())
			{
				gribFieldMap = null;
			}
		}

		return gribFieldMap;
	}
	
	///////////////////////////////////////////////////////////////////////
	private int getMaxNumberTries(int length, int endLength, int numberModelRuns)
	{
	  int modelRunPeriodInHours = 24/numberModelRuns;
	  int maxNumberTries = (endLength - length)/modelRunPeriodInHours;
	    
	  return maxNumberTries;
	}
	///////////////////////////////////////////////////////////////////////
	public static void main(String[] args) throws ParseException, OutsideAreaException, NotImplementedException, IOException, TimeoutException, GribServerException, HasNoValueException
	{
	  String modelName = "HIR-S03";
	  List<String> gribParameterTags = Arrays.asList( "2MT", "10MWu", "10MWv");
	  DateTime modelDateTime = new DateTime("2016-04-09 15:00", "yyyy-MM-dd HH:mm");
	  Date modelTime = modelDateTime.getTime();
	  
	  GetGribData gribfieldfetcher = new GetGribData();
	  int maxBackwardModelruns = 1;
	  Map<String, GribField> gribFieldMap = gribfieldfetcher.execute(modelName, modelTime, gribParameterTags, maxBackwardModelruns);
	  
	  
	  
	  GeoPoint testPoint = new GeoPoint(56.0, 12.0);
	  for (String  fieldName : gribFieldMap.keySet())
	  {
	    GribField gribField = gribFieldMap.get(fieldName);
	    
	    
//	    GridPoint gridPoint = gribField.getNearestGridPoint(testPoint);
	    
//	    GridPoint gridPoint = gribField.getIndex(geoPoint);
	    GridPoint gridPoint = gribField.getGridPoint(23);//index
	    
//	    gribField.get
//	    
//	    gridPoint.g
	    
	    double value = gridPoint.getValue();
	    
	    
	    System.out.println(fieldName + ": gridPoint = " + gridPoint);
	  }
	}
	
	
}
