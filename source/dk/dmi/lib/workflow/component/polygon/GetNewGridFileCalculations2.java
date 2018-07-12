package dk.dmi.lib.workflow.component.polygon;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.GridFileCalculationController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Application;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFile;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFileCalculation;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get New Grid File Calculations", 
		category = "Polygon",
		description = "Get all grid file calculations with a higher date than last calculated grid file for the given application. Only the newest calculation will be returend when there are several calculations with the same timeobs. Last calculated grid file calculation, must manually be updated. Oldest grid file calculations are returned first.",
	    version = 2)
public class GetNewGridFileCalculations2 {

	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getGridFileCalculationTypes(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return new String[]{GridFileCalculationController.GRID_FILE_TYPE_AUTO_TEXT, GridFileCalculationController.GRID_FILE_TYPE_MANUAL_TEXT};
	}

	@ExecuteMethod(
			argumentDescriptions = {"Application to receive new grid file calculations for", "Type of grid file calculations (i.e. AUTO or MANUAL)", "List of parameters to filter by (use null to ignore)", "Maximum number of results to fetch (use null to ignore)"}, 
			returnDescription = "List of persistence objects specified by entity type")
	public List<GridFileCalculation> execute(Application application, String gridFileCalculationtType, List<Parameter> parameterList, Integer maxResults) throws Exception {
		int type = GridFileCalculationController.gridFileTypeTextToInt(gridFileCalculationtType);
		Date applicationLastGridFileCalculationDate = getApplicationLastGridFileCalculationDate(application, type);
		
		List<GridFileCalculation> gridFileCalculationList = getGridFileCalculationList(applicationLastGridFileCalculationDate, type, parameterList, maxResults);
		gridFileCalculationList = filterOutDublicateGridFileCalculation(gridFileCalculationList);
		
		return gridFileCalculationList;
	}

	private Date getApplicationLastGridFileCalculationDate(Application application, int type) {
		Date applicationLastGridFileCalculationDate = new Date();
		GridFileCalculation lastGridFileCalculation = null;
		
		if(type == GridFileCalculationController.GRID_FILE_TYPE_AUTO) {
			lastGridFileCalculation = application.getLastGridFileCalculation();
		} else if(type == GridFileCalculationController.GRID_FILE_TYPE_MANUAL) {
			lastGridFileCalculation = application.getLastGridFileCalculationManual();
		}
		
		if(lastGridFileCalculation != null) {
			applicationLastGridFileCalculationDate = lastGridFileCalculation.getTimeCalculated();
		}
		
		return applicationLastGridFileCalculationDate;
	}
	
	List<GridFileCalculation> getGridFileCalculationList(Date applicationLastGridFileCalculationDate, int type, List<Parameter> parameterList, Integer maxResults) {
		GridFileCalculationController gridFileCalculationController = GridFileCalculationController.getInstance();
		int maxResultsInt = getMaxResultInt(maxResults);
		List<GridFileCalculation> gridFileCalculationList = null;
		
		if(parameterList == null) {
			gridFileCalculationList = gridFileCalculationController.getCompletedGridFileCalculationsGreaterThanTimeCalculatedAsc(applicationLastGridFileCalculationDate, type, maxResultsInt);
		} else {
			gridFileCalculationList = gridFileCalculationController.getCompletedGridFileCalculationsGreaterThanTimeCalculatedByParametersAsc(applicationLastGridFileCalculationDate, type, parameterList, maxResultsInt);
		}
		
//		Collections.reverse(gridFileCalculationList);
		return gridFileCalculationList;
	}


	List<GridFileCalculation> filterOutDublicateGridFileCalculation(List<GridFileCalculation> gridFileCalculationList) {
		if(gridFileCalculationList.size() > 0) {
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
