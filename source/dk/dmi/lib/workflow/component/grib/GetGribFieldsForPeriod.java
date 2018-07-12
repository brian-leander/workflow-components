package dk.dmi.lib.workflow.component.grib;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.grib.GribField;
import dk.dmi.lib.grib.GribServer;
import dk.dmi.lib.grib.GribServerException;
import dk.dmi.lib.grib.Request;
import dk.dmi.lib.grib.Response;
import dk.dmi.lib.persistence.common.DatabasePersistenceConfiguration;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.persistence.database.processdb.publicc.entity.WorkflowActivity;
import dk.dmi.lib.util.DateTime;
import dk.dmi.lib.util.Period;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Get GRIB fields for period", 
		category = "Grib",
		description = "Fetching GRIB fields from GRIB server, defined by model name, time of the model run, forecast time from and to, and a list of parameters.",
        version = 1)
public class GetGribFieldsForPeriod extends BaseComponent {
  
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDescriptions = {"Model name", "Time of the model run", "Start forecast time", "End forecast time", "Parameter names known by grib server", "Models time step resolution", "Max number timesteps in one request"},
			returnDescription = "Map with Grib Field's fetched. parameter name is key. null if fails")
	public Map<Date, Map<String, GribField>> execute(String modelName, Date analyseDateTime, Date modelDateTimeFrom, Date modelDateTimeTo, List<String> gribParameterNames, int timeStep, int maxTimeStepsToFetch) throws Exception {
		//
		// convert from Date to DateTime
		//
		DateTime analyseTime = new DateTime(analyseDateTime);
		
		//
		// Find beginLength and endLength for the time interval to fetch data from
		//		
		int beginLength = getLength(analyseDateTime, modelDateTimeFrom);
		int endLength   = getLength(analyseDateTime, modelDateTimeTo);
		
	  	//
	  	// Make gribFieldsForPeriod and initiate it
	  	//
		Map<Date, Map<String, GribField>> gribFieldsForPeriod = getGribFields(modelName, analyseTime, beginLength, endLength, timeStep, gribParameterNames, maxTimeStepsToFetch);

		return gribFieldsForPeriod;
	}
	
	
	private Map<Date, Map<String, GribField>> getGribFields(String modelName, DateTime analysisTime, int beginLength, int endLength, int timeStep, List<String> paramNames, int maxTimeStepsToFetch) throws Exception
	{
	  //
	  // Workaround for ECM-REJS_GG 
	  // Special case: endLength > 144 and endLength %6 == 3
	  //
	  int lengthToRemove = -1;
	  if (modelName.equals("ECM-REJS_GG"))
	    if (endLength > 144 && endLength %6 == 3)
	    {
	      endLength += 3;
	      lengthToRemove = endLength;
	    }
	     
	  //
	  // Make Map to return and initiate it
	  // After initiation there will be a entry for each timestep.
	  // Key is Date prognosisTime and value is Map<String,GribField>
	  // The value map might be empty.
	  //
	  SortedMap<Date, Map<String, GribField>> gribFieldsForPeriod = new TreeMap<>();
	  initiate(gribFieldsForPeriod, analysisTime, beginLength, endLength, timeStep);
	  
	  List<List<Request>> requestListList = getRequests(modelName, analysisTime, beginLength, endLength, timeStep, paramNames, maxTimeStepsToFetch);
	  
	  for (List<Request> requestList : requestListList)
	  {
	    Map<Request, Response> responseMap = fetchGribFields(requestList);
	    boolean allFieldsFetched = fromMapToMap(requestList, responseMap, gribFieldsForPeriod);
	    if (! allFieldsFetched)
	      return null;
	  }
	  
	  //
	  // Workaround for ECM-REJS_GG:  Se start of method
	  // Problem: timestep change from 3 to 6 after length 144
	  // Special case: endLength > 144 and endLength %6 == 3:
	  //   remove latest entry for (endLength+3)
	  //
	  if (lengthToRemove > 0)
	  {
	    Date prognosisTime = getPrognosisTime(analysisTime, lengthToRemove);
	    gribFieldsForPeriod.remove(prognosisTime);	      
	  }
	  
	  return gribFieldsForPeriod;
	}
	
	
	
	private boolean fromMapToMap(List<Request> requestList, Map<Request, Response> responseMap, Map<Date, Map<String, GribField>> fieldsForPeriod)
	{
	  //
	  // boolwan telling if all fields are fetched from GRIB-server
	  //
	  boolean allFieldsFetched = true;
	  
	  //
	  // Put fetched fields from Map<Request,response> responseMap into Map<Date,Map<String,GribField>> fielsForPeriod
	  //
	  for (Request request : requestList)
	  {
		Response response = responseMap.get(request);
//		String modelname = response.getModel();
	  	DateTime analysisTime = response.getReferenceTime();
	  	int length = response.getLength();
	  	Date prognosisTime = getPrognosisTime(analysisTime, length);
		String fieldName = response.getField();
		
		if (response.hasGribField())
		{
		  	Map<String, GribField> fieldsForTimeStep = fieldsForPeriod.get(prognosisTime);
		  	
		  	GribField gribField = null;
		  	try
		  	{
		  	  gribField = response.getGribField();
			  fieldsForTimeStep.put(fieldName, gribField);
		  	}
		  	catch (GribServerException e)
		  	{
		  	  String modelName = response.getModel();
		  	  String sAnalyseTime = analysisTime.getDateTimeString("yyyy-MM-dd HH:mm");
		  	  LOGGER.error(String.format("Server error for model %s analyse time %s length %d field %s\n", fieldName, modelName, sAnalyseTime, length, fieldName), e);

		  	  allFieldsFetched = false;
		  	  return allFieldsFetched;
		  	}
		}
		else
		{
		  String modelName = response.getModel();
		  String sAnalyseTime = analysisTime.getDateTimeString("yyyy-MM-dd HH:mm");
		  LOGGER.warn(String.format("Missing field %10s from model %s analyse time %s length %d\n", fieldName, modelName, sAnalyseTime, length));
		  //
		  // Not for model ECM-REJS_GG length> 144 and length %6 = 3
		  // Those fields are expected to be missing 
		  //
		  if ( ! (modelName.equals("ECM-REJS_GG") && length > 144 && length %6 == 3 ))
		  {
		    allFieldsFetched = false;
		    return allFieldsFetched;
		  }
		}
	  }
	  //
	  // Work around for ECM-REJS_GG
	  // Problem: Timestep not constant for the model.
	  // Timestep is 3 hours for length <= 144.
	  // Timestep is 6 hours for length >  144.
	  //
	  // Solution
	  //
	  Request firstRequest = requestList.get(0);
	  String modelName = firstRequest.getModel();
	  if (modelName.equals("ECM-REJS_GG"))
	  {
	    doWorkAround(requestList, responseMap,fieldsForPeriod );
	  }
	  
	  return allFieldsFetched;
	}
	
	
	
	private void doWorkAround(List<Request> requestList, Map<Request, Response> responseMap, Map<Date, Map<String, GribField>> fieldsForPeriod)
	{
	  //
	  // Work around for ECM-REJS_GG
	  // Problem: Timestep not constant for the model.
	  // Timestep is 3 hours for length <= 144.
	  // Timestep is 6 hours for length >  144.
	  //
	  // Solution
	  //
	  for (Request request : requestList)
	  {
	    Response response = responseMap.get(request);
	    DateTime analysisTime = response.getReferenceTime();
	    int length = response.getLength();

	    if (length > 144 && length % 6 == 0)
	    {
	      Date prognosisTime = getPrognosisTime(analysisTime, length-3);  //WORKAROUND using length-3 for length
	      Map<String, GribField> fieldsForTimeStep = fieldsForPeriod.get(prognosisTime);
	      DateTime modelTime = new DateTime(prognosisTime);
	      LOGGER.info(String.format("Workaround: prognosisTime: %s length: %3d \n", modelTime.getDateTimeString("yyyy-MM-dd HH"), length-3));
	      if (response.hasGribField())
	      {
		String fieldName = response.getField();
		try
		{
		  GribField gribField = response.getGribField();
		  fieldsForTimeStep.put(fieldName, gribField);
		}
		catch (GribServerException e)
		{
		  String modelName = response.getModel();
		  String sAnalyseTime = analysisTime.getDateTimeString("yyyy-MM-dd HH:mm");
		  LOGGER.error(String.format("Server error for model %s analyse time %s length %d field %s\n", fieldName, modelName, sAnalyseTime, length, fieldName), e);
		}
	      }
	    }
	  }//for
	}
	
	
	/**
	 * get list of lists of request
	 * @param modelName
	 * @param analyseTime
	 * @param beginLength
	 * @param endLength
	 * @param timeStep
	 * @param gribParameterNames
	 * @param maxTimeStepsToFetch max number of timeSteps to fetch from server at a time
	 * @return
	 */
	List<List<Request>> getRequests(String modelName, DateTime analyseTime, int beginLength, int endLength, int timeStep, List<String> gribParameterNames, int maxTimeStepsToFetch) {
	  	//
	  	// Initiate
	  	//
	  	List<List<Request>> requestListList = new LinkedList<>();
		List<Request> requestList = new LinkedList<>();
		int nPar = gribParameterNames.size();
		int timeStepsCount = 0;
		
		for (int length=beginLength; length<=endLength; length+=timeStep)
		{
		  timeStepsCount ++;
		  
		  for (String fieldName : gribParameterNames) {
			Request request = new Request(modelName, analyseTime, length, fieldName);
			requestList.add(request);
		  }
		  
		  if (timeStepsCount >= maxTimeStepsToFetch)
		  {
		    // Save the requestList in the requestListList and make a new empty one
		    requestListList.add(requestList);
		    requestList = new LinkedList<>();
		    timeStepsCount = 0;
		  }
		}
		// The last requestList might not have been added to the requestListList
		int nReq = requestList.size();
		if (nReq > 0 && nReq < maxTimeStepsToFetch * nPar)
		{
		  requestListList.add(requestList); 
		}
		
		return requestListList;
	}

	
	
	
	
	Map<Request, Response> fetchGribFields(List<Request> requestList) throws Exception {
	  	Request firstRequest = requestList.get(0);
	  	Request lastRequest  = requestList.get(requestList.size()-1);
	  	
	  	DateTime analysisTime = firstRequest.getReferenceTime();
	  	int firstLength = firstRequest.getLength();
	  	int lastLength  = lastRequest.getLength();
	  	
	  	Date prognosisTime1 = getPrognosisTime(analysisTime, firstLength);
	  	Date prognosisTime2 = getPrognosisTime(analysisTime, lastLength);
	  	DateTime modelTime1 = new DateTime(prognosisTime1);
	  	DateTime modelTime2 = new DateTime(prognosisTime2);
	  	String mTime1 = modelTime1.getDateTimeString("yyyy-MM-dd HH");
	  	String mTime2 = modelTime2.getDateTimeString("yyyy-MM-dd HH");
	  	
		int numberRequests = requestList.size();
		LOGGER.info(String.format("\nRequesting %d fields for length from %d to %d equavalent to period from %s to %s", numberRequests, firstLength, lastLength, mTime1, mTime2));
		
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
				LOGGER.info(String.format("\nRecived %d responses from grib server.", responseMap.size()));				
			} catch(Exception e) {
				LOGGER.error(String.format("\nFailed to get responses from grib server!"), e);
			}
		}
		
		return responseMap;
	}

	
	
	
	private void initiate(Map<Date, Map<String, GribField>> gfForPeriod, DateTime analysisTime, int beginLength, int endLength, int timeStep)
	{
	  for (int length=beginLength; length<=endLength; length+=timeStep)
	  {
	    Date prognosisTime = getPrognosisTime(analysisTime, length);
	    Map<String, GribField> fieldsForTimeStep = new TreeMap<>();
	    gfForPeriod.put(prognosisTime, fieldsForTimeStep);
	  }
	}
	
	
	static int getLength(Date analysisTime, Date prognosisTime)
	{
	  //
	  // Convert to DateTime
	  //
	  DateTime aTime = new DateTime(analysisTime);
	  DateTime pTime = new DateTime(prognosisTime);
	  Period modelPeriod = pTime.subtract(aTime);
	  int hours = modelPeriod.getHours();
	  int days  = (int)modelPeriod.getDays();
	  
	  int periodInHours = 24*days + hours;
	  
	  return periodInHours;
	}
	
	
	
	Date getPrognosisTime(DateTime analysisTime, int length)
	{
	  long hours = length;
	  Period period = new Period(hours, 0, 0, 0);
	  DateTime modelTime = analysisTime.add(period);
	  Date prognosisTime = new Date(modelTime.getTimeInMillis());
	  
	  return prognosisTime;
	}
	
	
	
//	public static void main(String[] args)
//	{
//	  String modelName = "ECM-REJS_GG";
//	  int timeStep = 3;
//	  int maxTimeStepsToFetch = 30;
//	  String[] fieldsArr = {"10MWu", "10MWv", "2MT"};
//	  List<String> gribParNames = Arrays.asList(fieldsArr);
//	  
//	  
//	  GetGribFieldsForPeriod getGribFieldsForPeriod = new GetGribFieldsForPeriod();
//	  
//	  try
//	  {
//	    DateTime aTime  = new DateTime("2016-09-24 00", "yyyy-MM-dd HH");
//	    DateTime mTime1 = new DateTime("2016-09-24 00", "yyyy-MM-dd HH");
//	    DateTime mTime2 = new DateTime("2016-10-01 03", "yyyy-MM-dd HH");
//	    
//	    // Covert DateTime to Date
//	    Date anaTime = new Date(aTime.getTimeInMillis());
//	    Date mTimeFrom = new Date(mTime1.getTimeInMillis());
//	    Date mTimeTo   = new Date(mTime2.getTimeInMillis());
//	    
//	    System.out.println();
//	    System.out.println("Model: " + modelName);
//	    System.out.println("anaTime: " + aTime.getDateTimeString("yyyy-MM-dd HH"));
//	    System.out.println("mTime1:  " + mTime1.getDateTimeString("yyyy-MM-dd HH"));
//	    System.out.println("mTime2:  " + mTime2.getDateTimeString("yyyy-MM-dd HH"));
//	    System.out.println("timeStep:            " + timeStep);	    
//	    System.out.println("maxTimeStepsToFetch: " + maxTimeStepsToFetch);
//	    // Print field names
//	    System.out.print("fields:");
//	    for (String field : gribParNames)
//	      System.out.printf(" %s", field);
//	    System.out.println();
//	    System.out.println();
//	    
//	    Map<Date, Map<String, GribField>> gfForPeriod = getGribFieldsForPeriod.execute(modelName, anaTime, mTimeFrom, mTimeTo, gribParNames, timeStep, maxTimeStepsToFetch);
//	    
//	    //
//	    // Has all requested fields in the nodelrun been received
//	    //
//	    if (gfForPeriod == null)
//	    {
//	      System.out.println();
//	      System.out.println("Fatal error");
//	      System.out.println("Requested fields for the modelrun has not been fetched");
//	      System.out.println("Returning");
//	      return;
//	    }
//	    
//	    
//	    
//	    //
//	    // Show what is in the Map
//	    //
//	    System.out.println("\ngfForPeriod.size = " + gfForPeriod.size());
//	    
//	    
//	    for (Date mDate : gfForPeriod.keySet())
//	    {
//	      DateTime mT = (new DateTime(mDate));
//	      String mTs = mT.getDateTimeString("yyyy-MM-dd HH");
//	      int length = getLength(anaTime, mDate);
//	      Map<String, GribField> gfForTimeStep = gfForPeriod.get(mDate);
//	      for(String field: gfForTimeStep.keySet())
//	      {
//		GribField gf = gfForTimeStep.get(field);
//		int parTableVer = gf.getParTableVersion();
//		int parId = gf.getParUnitIndicator();
//		System.out.printf("\nmTs: %s  length:%3d field:%7s TabelId:%2d ParId:%2d", mTs, length, field, parTableVer, parId);
//	      }
//	    }
//	    
//	    
//	  }
//	    catch (Exception e)
//	  {
//	      System.out.println("Exception:");
//	      e.printStackTrace();
//	  }
//	}
	
	public static void main(String[] args) throws Exception {
		GetGribFieldsForPeriod getGribFieldsForPeriod = new GetGribFieldsForPeriod();
		
		Date date = DateUtils.parseStringToDate("2016-11-17 00:00", DateUtils.DATE_FORMAT_DEFAULT);
		
		List<String> gribParameterNames = new ArrayList<String>();
		gribParameterNames.add("SEAHGT");
		
		getGribFieldsForPeriod.execute("ECM-SEA3", date, date, date, gribParameterNames, 3, 12);
		
	}
}
