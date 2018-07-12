package dk.dmi.lib.workflow.component.date;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Convert date between time zones dynamic", 
		category = "Date",
		description = "Convert date time zone by specifying two time zone name.",
        version = 1)
public class ConvertDateTimeZoneDynamic extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"The date to convert time zone for", "Time zone from name, e.g. UTC or Europe/Copenhagen", "Time zone to name, e.g. UTC or Europe/Copenhagen"},
			returnDescription = "Date converted to new time zone")
    public Date execute(Date date, String timeZoneFromName, String timeZoneToName) throws ParseException {
		TimeZone newTimeZoneFrom = TimeZone.getTimeZone(timeZoneFromName);
		TimeZone newTimeZoneTo = TimeZone.getTimeZone(timeZoneToName);
		Date newDate = DateUtils.convertDateBetweenTimeZones(date, newTimeZoneFrom, newTimeZoneTo);
		return newDate;
    }
	
}
