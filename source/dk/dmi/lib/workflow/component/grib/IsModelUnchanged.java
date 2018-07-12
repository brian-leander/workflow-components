package dk.dmi.lib.workflow.component.grib;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.grib.GribField;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Is model unchanged", 
		category = "Grib",
		description = "Check if a models grid field has changed over the last 3 days.",
        version = 1)
public class IsModelUnchanged extends BaseComponent {
	
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
  
	@ExecuteMethod(
			argumentDescriptions = {"Model name", "Time of the model run", "Parameter name known by grib server"},
			returnDescription = "False if the models grib fields has changed, otherwise true")
	public boolean execute(String modelName, Date analyseDateTime, String gribParameterName) throws Exception {
		boolean isSameGrid = true;
		
		ArrayList<String> gribParameterNames = new ArrayList<String>();
		gribParameterNames.add(gribParameterName);
		Date oldAnalyseDateTime = DateUtils.alterDate(analyseDateTime, DateUtils.DATE_FIELD_TEXT_DAY, -3);
		
		GetGribFieldsForPeriod getGribFieldsForPeriod = new GetGribFieldsForPeriod();
		getGribFieldsForPeriod.injectContext(workflowContextController);
		getGribFieldsForPeriod.injectLogger(LOGGER);
		GribField newGribField = getGribField(modelName, analyseDateTime, gribParameterName, gribParameterNames, getGribFieldsForPeriod);
		
		if(newGribField != null) {
			GribField oldGribField = getGribField(modelName, oldAnalyseDateTime, gribParameterName, gribParameterNames, getGribFieldsForPeriod);
			
			if(oldGribField != null) {
				HasSameGrid hasSameGrid = new HasSameGrid();
				isSameGrid = hasSameGrid.execute(newGribField, oldGribField);
			}
		}
		
		return isSameGrid;
	}

	GribField getGribField(String modelName, Date analyseTime, String gribParameterName, ArrayList<String> gribParameterNames, GetGribFieldsForPeriod getGribFieldsForPeriod) throws Exception {
		Map<Date, Map<String, GribField>> newGribFieldMapMap = getGribFieldsForPeriod.execute(modelName, analyseTime, analyseTime, analyseTime, gribParameterNames, 1, 1);
		GribField newGribField = null;
		
		if(newGribFieldMapMap != null) {
			Map<String, GribField> newGribFieldMap = newGribFieldMapMap.get(analyseTime);
			
			if(newGribFieldMap != null) {
				newGribField = newGribFieldMap.get(gribParameterName);
			}
		}
		
		return newGribField;
	}
	
}
