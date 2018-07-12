package dk.dmi.lib.workflow.component.date;

import java.util.Date;

import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Alter Date To Nearest", 
		category = "Date",
		description = "Alters a date to nearest date field value, based on an excisting or new date.",
        version = 1)
public class AlterDateToNearest extends BaseComponent {
	
	@ArgumentListGetMethod(
			argumentIndex = "2")
	public String[] getListOfDatabases(String ignore) {
		String[] upDown = {DateUtils.DIRECTION_UP, DateUtils.DIRECTION_DOWN};
		return upDown;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Initial date (Use null for current date)", "The date field to alter, i.e. YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND", "Direction to move","Date field value" }, 
			returnDescription = "Altered date")
    public Date execute(Date date, String dateField, String direction, int dateFieldValue) throws Exception {
		if(date == null) {
			date = new Date();
		}
		
		Date newDate = DateUtils.moveDateToFieldValue(date, dateField, direction, dateFieldValue);
		return newDate;
    }
	
}
