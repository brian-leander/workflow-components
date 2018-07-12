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
@Component(
	name = "Generate Grid Content Path",
	category = "Polygon",
	description = "Creates a path to grid generated content",
	version = 2)
public class GenerateGridFilePath2 extends BaseComponent {

    @WorkflowAnnotations.ExecuteMethod(
	    argumentDescriptions = {"Parameter", "Root directory name", "Timestamp for the interpolated file", "Extension of the file without . infront"},
	    returnDescription = "Generated path without root location")
    public String execute(Parameter parameter, String rootDir, Date timeStamp, String fileExtension) {
    	SimpleDateFormat dateFormat = getDateFormat(parameter);
    	String filePathName = createFilePath(parameter, rootDir, timeStamp, dateFormat, fileExtension);
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
	
	String createFilePath(Parameter parameter, String rootDir, Date timeStamp, SimpleDateFormat dateFormat, String fileExtension) {
		int resolutionInMeters = 1000;
    	String filePathName = null;
		
		if(dateFormat != null) {
			filePathName = rootDir + "/" + parameter.getTimeResolution().getName().toLowerCase() + "/"+ parameter.getElementNumber() + "/interpolated_" + resolutionInMeters / 1000 + "/" + dateFormat.format(timeStamp.getTime()) + "."+fileExtension;
		}
		
		return filePathName;
	}
	
}
