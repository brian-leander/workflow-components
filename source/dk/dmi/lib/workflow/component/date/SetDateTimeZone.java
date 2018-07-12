package dk.dmi.lib.workflow.component.date;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Set date time zone", 
		category = "Date",
		description = "Change date time zone by specifying the time zone name.",
        version = 1)
public class SetDateTimeZone extends BaseComponent {
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getTimeZoneOptions(String ignore) {
		String[] calendarTimeZones = TimeZone.getAvailableIDs();
		
		for (int i = 0; i < calendarTimeZones.length; i++) {
			String timeZone = calendarTimeZones[i];
			
			if("UTC".equals(timeZone)) {
				calendarTimeZones[i] = calendarTimeZones[0];
				calendarTimeZones[0] = "UTC";
				break;
			}
		}
		
		return calendarTimeZones;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"The date to set time zone for", "Time zone name"},
			returnDescription = "Date converted to new time zone")
    public Date execute(Date date, String timeZoneName) throws ParseException {
		TimeZone newTimeZone = TimeZone.getTimeZone(timeZoneName);
		Date newDate = DateUtils.convertDateToTimeZones(date, newTimeZone);
		return newDate;
    }
	
}
