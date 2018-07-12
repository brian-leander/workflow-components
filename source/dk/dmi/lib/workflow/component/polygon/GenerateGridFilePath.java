/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.dmi.lib.workflow.component.polygon;

import dk.dmi.lib.persistence.database.climadb.polygon.controller.ParameterController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Kenan
 */
@Deprecated // new version available
@Component(
	name = "Generate Grid File Path",
	category = "Polygon",
	description = "Creates a path to interpolated file.",
	version = 1)
public class GenerateGridFilePath extends BaseComponent {

    @WorkflowAnnotations.ExecuteMethod(
	    argumentDescriptions = {"Parameter", "Timestamp for the interpolated file"},
	    returnDescription = "Generated path without root location")
    public String execute(Parameter parameter, Date timeStamp) {
    	SimpleDateFormat dateFormat = getDateFormat(parameter);
    	String filePathName = createFilePath(parameter, timeStamp, dateFormat);
		return filePathName;
    }

	SimpleDateFormat getDateFormat(Parameter parameter) {
		ParameterController parameterController = ParameterController.getInstance();
		SimpleDateFormat dateFormat = null;
		
		if(parameterController.isHourlyParameter(parameter)) {
			dateFormat = new SimpleDateFormat("yyyy/MM/dd/yyyyMMdd_HH00");
    	} else if(parameterController.isDailyParameter(parameter)) {
    		dateFormat = new SimpleDateFormat("yyyy/MM/yyyyMMdd");
    	} else if(parameterController.isMonthlyParameter(parameter)) {
			dateFormat = new SimpleDateFormat("yyyy/yyyyMM");
		} else if(parameterController.isYearlyParameter(parameter)) {
			dateFormat = new SimpleDateFormat("yyyy");
		}
		
		return dateFormat;
	}
	
	String createFilePath(Parameter parameter, Date timeStamp, SimpleDateFormat dateFormat) {
		int resolutionInMeters = 1000;
    	String filePathName = null;
		
		if(dateFormat != null) {
			filePathName = "grid_files/" + parameter.getTimeResolution().getName().toLowerCase() + "/"+ parameter.getElementNumber() + "/interpolated_" + resolutionInMeters / 1000 + "/" + dateFormat.format(timeStamp.getTime()) + ".txt.gz";
		}
		
		return filePathName;
	}
	
}
