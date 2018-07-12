package dk.dmi.lib.workflow.component.util;

import java.util.TimeZone;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Set time zone", 
		category = "Util",
		description = "Change default time zone of the jvm, for the remainder of the process life cycle or until a new time zone is set.",
        version = 1)
public class SetTimeZone extends BaseComponent {
	
	@ArgumentListGetMethod(
			argumentIndex = "0")
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
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Time zone name"},
			returnDescription = "Date converted to new time zone")
    public void execute(String timeZoneName) {
		TimeZone newTimeZone = TimeZone.getTimeZone(timeZoneName);
		TimeZone.setDefault(newTimeZone);
    }
	
}
