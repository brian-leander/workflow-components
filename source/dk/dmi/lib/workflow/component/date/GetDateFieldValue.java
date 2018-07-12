package dk.dmi.lib.workflow.component.date;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Deprecated
@Component(
		name = "Get date field value", 
		category = "Date",
		description = "Get calender field value for specified date.",
        version = 1)
public class GetDateFieldValue extends BaseComponent {
	
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
	public String[] getListOfCalenderFieldNames(String ignore) {
		Set<String> calenderFieldSet = CALENDER_FIELD_MAP.keySet();
		String[] calenderFieldList = calenderFieldSet.toArray(new String[calenderFieldSet.size()]);
		return calenderFieldList;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Date (Use null for current date)", "Field to filter by. (Day of week start monday with value 1)" }, 
			returnDescription = "Date field value")
    public int execute(Date date, String field) {
		if(date == null) {
			date = new Date();
		}
		
		int fieldValue = getDateValueForField(date, field);
		return fieldValue;
    }

	int getDateValueForField(Date date, String field) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
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
