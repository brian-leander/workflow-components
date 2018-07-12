package dk.dmi.lib.workflow.component.date;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Set date time zone dynamic", 
		category = "Date",
		description = "Change date time zone by specifying the time zone name.",
        version = 1)
public class SetDateTimeZoneDynamic extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"The date to set time zone for", "Time zone name, e.g. UTC or Europe/Copenhagen"},
			returnDescription = "Date converted to new time zone")
    public Date execute(Date date, String timeZoneName) throws ParseException {
		TimeZone newTimeZone = TimeZone.getTimeZone(timeZoneName);
		Date newDate = DateUtils.convertDateToTimeZones(date, newTimeZone);
		return newDate;
    }
	
}
