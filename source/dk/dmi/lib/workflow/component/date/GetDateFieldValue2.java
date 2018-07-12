package dk.dmi.lib.workflow.component.date;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get date field value", 
		category = "Date",
		description = "Get calendar field value for specified date and time zone.",
        version = 2)
public class GetDateFieldValue2 extends BaseComponent {
	
	private static final Map<String, Integer> CALENDER_FIELD_MAP = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 4699656312851513120L;
		{
			put("YEAR", GregorianCalendar.YEAR);
			put("MONTH", GregorianCalendar.MONTH);
			put("DAY_OF_MONTH", GregorianCalendar.DAY_OF_MONTH);
			put("DAY_OF_WEEK", GregorianCalendar.DAY_OF_WEEK);
			put("HOUR_OF_DAY", GregorianCalendar.HOUR_OF_DAY);
			put("MINUTE", GregorianCalendar.MINUTE);
			put("SECOND", GregorianCalendar.SECOND);
			put("MILLISECOND", GregorianCalendar.MILLISECOND);
		}
	};
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfCalendarFieldNames(String ignore) {
		Set<String> calendarFieldSet = CALENDER_FIELD_MAP.keySet();
		String[] calendarFieldList = calendarFieldSet.toArray(new String[calendarFieldSet.size()]);
		return calendarFieldList;
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "2")
	public String[] getListOfCalendarTimeZones(String ignore) {
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
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Date (Use null for current date)", "Field to filter by. (Day of week start monday with value 1)" }, 
			returnDescription = "Date field value")
    public int execute(Date date, String field, String timeZone) {
		if(date == null) {
			date = new Date();
		}
		
		int fieldValue = getDateValueForField(date, field, timeZone);
		return fieldValue;
    }
	
	int getDateValueForField(Date date, String field, String timeZone) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone(timeZone));
		gregorianCalendar.setTime(date);
		
		int fieldKey = CALENDER_FIELD_MAP.get(field);
		correctDayOfWeek(field, gregorianCalendar);
		int fieldValue = gregorianCalendar.get(fieldKey);
		fieldValue = correctMonth(field, fieldValue);
		
		return fieldValue;
	}

	int correctMonth(String field, int fieldValue) {
		if(field.equals("MONTH")) {
			fieldValue += 1;
		}
		return fieldValue;
	}

	void correctDayOfWeek(String field, GregorianCalendar gregorianCalendar) {
		if(field.equals("DAY_OF_WEEK")) {
			gregorianCalendar.add(GregorianCalendar.DAY_OF_WEEK, -1);
		}
	}
	
}
