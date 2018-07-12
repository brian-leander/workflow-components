package dk.dmi.lib.workflow.component.polygon;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import dk.dmi.lib.persistence.database.climadb.polygon.controller.ApplicationController;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.GridFileCalculationController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Application;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFile;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFileCalculation;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Deprecated // new version available
@Component(
		name = "Get New Grid File Calculations", 
		category = "Polygon",
		description = "Get all grid file calculations with a higher date than last calculated grid file for the given application. If choosen to exclude old calculation, only the newest calculation will be returend when there are several calculations with the same timeobs. Last calculated grid file calculation, will automaticlly be updated. Newest grid files calculations are returned first.",
	    version = 1)
public class GetNewGridFileCalculations {

	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfGridFileCalculations(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		ApplicationController applicationController = ApplicationController.getInstance();
		List<Application> applicationList = applicationController.getAllApplications();
		
		String[] applicationArray = new String[applicationList.size()+1];
		applicationArray[0] = "";
		
		for(int i = 0; i < applicationList.size(); i++) {
			Application application = applicationList.get(i);
			applicationArray[i+1] = application.getName();
		}
		
		return applicationArray;
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getTrueFalse(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return new String[]{"true", "false"};
	}

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Application to receive new grid file calculations for", "Exclude old calculations with the same datetime", "List of parameters to filter by (use null to ignore)", "Maximum number of results to fetch (use null to ignore)"}, 
			returnDescription = "List of persistence objects specified by entity type")
	public List<GridFileCalculation> execute(String applicationName, String excludeOldCalculations, List<Parameter> parameterList, Integer maxResults) throws Exception {
		Application application = getApplication(applicationName);
		Date applicationLastGridFileCalculationDate = getApplicationLastGridFileCalculationDate(application);
		List<GridFileCalculation> gridFileCalculationList = getGridFileCalculationList(applicationLastGridFileCalculationDate, parameterList, maxResults);
		
		updateApplicationsLastGridFileCalculation(application, gridFileCalculationList);
		gridFileCalculationList = filterOutDublicateGridFileCalculation(excludeOldCalculations, gridFileCalculationList);
		
		return gridFileCalculationList;
	}

	void updateApplicationsLastGridFileCalculation(Application application, List<GridFileCalculation> gridFileCalculationList) {
		GridFileCalculation nextLastGridFileCalculation = null;
		GridFileCalculation lastGridFileCalculation = application.getLastGridFileCalculation();
		
		if(lastGridFileCalculation == null) {
			GridFileCalculationController gridFileCalculationController = GridFileCalculationController.getInstance();
			nextLastGridFileCalculation = gridFileCalculationController.getCompletedLatestGridFileCalculation();
		} else if(gridFileCalculationList.size() > 0) {
			nextLastGridFileCalculation = gridFileCalculationList.get(0);
		}
		
		if(nextLastGridFileCalculation != null) {
			application.setLastGridFileCalculation(nextLastGridFileCalculation);
		}
	}

	Application getApplication(String applicationName) throws Exception {
		ApplicationController applicationController = ApplicationController.getInstance();
		Application application = applicationController.getApplicationForName(applicationName);
		
		if(application == null) {
			throw new Exception("Application does not exist!");
		}
		return application;
	}

	private Date getApplicationLastGridFileCalculationDate(Application application) {
		GridFileCalculation lastGridFileCalculation = application.getLastGridFileCalculation();
		Date applicationLastGridFileCalculationDate = new Date();
		
		if(lastGridFileCalculation != null) {
			applicationLastGridFileCalculationDate = lastGridFileCalculation.getTimeCalculated();
		}
		
		return applicationLastGridFileCalculationDate;
	}
	
	List<GridFileCalculation> getGridFileCalculationList(Date applicationLastGridFileCalculationDate, List<Parameter> parameterList, Integer maxResults) {
		GridFileCalculationController gridFileCalculationController = GridFileCalculationController.getInstance();
		int maxResultsInt = getMaxResultInt(maxResults);
		List<GridFileCalculation> gridFileCalculationList = null;
		
		if(parameterList == null) {
			gridFileCalculationList = gridFileCalculationController.getCompletedGridFileCalculationsGreaterThanTimeCalculatedAsc(applicationLastGridFileCalculationDate, GridFileCalculationController.GRID_FILE_TYPE_AUTO, maxResultsInt);
		} else {
			gridFileCalculationList = gridFileCalculationController.getCompletedGridFileCalculationsGreaterThanTimeCalculatedByParametersAsc(applicationLastGridFileCalculationDate, GridFileCalculationController.GRID_FILE_TYPE_AUTO, parameterList, maxResultsInt);
		}
		
		Collections.reverse(gridFileCalculationList);
		return gridFileCalculationList;
	}


	List<GridFileCalculation> filterOutDublicateGridFileCalculation(String excludeOldCalculations, List<GridFileCalculation> gridFileCalculationList) {
		boolean excludeOldCalculationsBool = Boolean.parseBoolean(excludeOldCalculations);
		
		if(excludeOldCalculationsBool && gridFileCalculationList.size() > 0) {
			List<GridFileCalculation> gridFileCalculationListFiltered = new ArrayList<GridFileCalculation>();
			
			for (GridFileCalculation gridFileCalculation : gridFileCalculationList) {
				tryAddReplaceGridFileCalculationToResultList(gridFileCalculationListFiltered, gridFileCalculation);
			}
			
			gridFileCalculationList = gridFileCalculationListFiltered;
		}
		
		return gridFileCalculationList;
	}

	void tryAddReplaceGridFileCalculationToResultList(List<GridFileCalculation> gridFileCalculationListFiltered, GridFileCalculation gridFileCalculation) {
		GridFileCalculation existingGridFileCalculation = getGridFileCalculationWithSameGridFileRef(gridFileCalculationListFiltered, gridFileCalculation);
		
		if(existingGridFileCalculation == null) {
			gridFileCalculationListFiltered.add(gridFileCalculation);
		} else {
			Date existingGridFileCalculationDate = existingGridFileCalculation.getTimeCalculated();
			Date gridFileCalculationDate = gridFileCalculation.getTimeCalculated();
			
			if(gridFileCalculationDate.after(existingGridFileCalculationDate)) {
				gridFileCalculationListFiltered.remove(existingGridFileCalculation);
				gridFileCalculationListFiltered.add(gridFileCalculation);
			}
		}
	}
	
	GridFileCalculation getGridFileCalculationWithSameGridFileRef(List<GridFileCalculation> gridFileCalculationList, GridFileCalculation checkGridFileCalculation) {
		GridFile checkGridFile = checkGridFileCalculation.getGridFile();
		Date checkGridFileTimeObs = checkGridFile.getTimeObs();
		long checkGridFileParameterId = checkGridFile.getParameter().getId().longValue();
		
		for (GridFileCalculation gridFileCalculation : gridFileCalculationList) {
			GridFile gridFile = gridFileCalculation.getGridFile();
			Date gridFileTimeObs = gridFile.getTimeObs();
			long gridFileParameterId = gridFile.getParameter().getId().longValue();
			
			if(gridFileTimeObs.equals(checkGridFileTimeObs) && gridFileParameterId == checkGridFileParameterId) {
				return gridFileCalculation;
			}
		}
		
		return null;
	}

	int getMaxResultInt(Integer maxResults) {
		int maxResultsInt = Integer.MAX_VALUE;
		
		if(maxResults != null) {
			maxResultsInt = maxResults.intValue();
		}
		
		return maxResultsInt;
	}
	
}
