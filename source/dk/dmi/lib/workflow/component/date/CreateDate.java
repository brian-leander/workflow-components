package dk.dmi.lib.workflow.component.date;

import java.util.Date;
import java.util.GregorianCalendar;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Deprecated
@Component(
		name = "Create Date", 
		category = "Date",
		description = "Creates a date, based on an excisting or new date. Eash of the specified fields will be set on the date, use -1 to ignore a field",
        version = 1)
public class CreateDate extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Initial date (Use null for current date)", "Year (Min value 0 or -1 to ignore)", "Month (Min value 1 or -1 to ignore)", "Day of month (Min value 1 or -1 to ignore)", "Hour of day (Min value 0 or -1 to ignore)", "Minute (Min value 0 or -1 to ignore)", "Second (Min value 0 or -1 to ignore)" }, 
			returnDescription = "Newly created date")
    public Date execute(Date date, int year, int month, int day, int hour, int minute, int second) {
		if(date == null) {
			date = new Date();
		}
		
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(date);
		
		if(year >= 0) {
			gregorianCalendar.set(GregorianCalendar.YEAR, year);
		}
		if(month >= 0) {
			gregorianCalendar.set(GregorianCalendar.MONTH, month-1);
		}
		if(day >= 0) {
			gregorianCalendar.set(GregorianCalendar.DAY_OF_MONTH, day);
		}
		if(hour >= 0) {
			gregorianCalendar.set(GregorianCalendar.HOUR_OF_DAY, hour);
		}
		if(minute >= 0) {
			gregorianCalendar.set(GregorianCalendar.MINUTE, minute);
		}
		if(second >= 0) {
			gregorianCalendar.set(GregorianCalendar.SECOND, second);
		}
		
		return gregorianCalendar.getTime();
    }
	
}
