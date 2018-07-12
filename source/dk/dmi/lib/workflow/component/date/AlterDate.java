package dk.dmi.lib.workflow.component.date;

import java.util.Date;
import java.util.GregorianCalendar;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Alter Date", 
		category = "Date",
		description = "Alters a date, based on an excisting or new date. Eash of the specified field values will be added or subtracted from the date, use 0 to ignore a field",
        version = 1)
public class AlterDate extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Initial date (Use null for current date)", "Years (+/- value, 0 to ignore)", "Months (+/- value, 0 to ignore)", "Days (+/- value, 0 to ignore)", "Hours (+/- value, 0 to ignore)", "Minutes (+/- value, 0 to ignore)", "Seconds (+/- value, 0 to ignore)" }, 
			returnDescription = "Altered date")
    public Date execute(Date date, int year, int month, int day, int hour, int minute, int second) {
		if(date == null) {
			date = new Date();
		}
		
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(date);
		
		gregorianCalendar.add(GregorianCalendar.YEAR, year);
		gregorianCalendar.add(GregorianCalendar.MONTH, month);
		gregorianCalendar.add(GregorianCalendar.DAY_OF_MONTH, day);
		gregorianCalendar.add(GregorianCalendar.HOUR_OF_DAY, hour);
		gregorianCalendar.add(GregorianCalendar.MINUTE, minute);
		gregorianCalendar.add(GregorianCalendar.SECOND, second);
		gregorianCalendar.add(GregorianCalendar.MILLISECOND, 0);
		
		return gregorianCalendar.getTime();
    }
	
}
