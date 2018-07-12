package dk.dmi.lib.workflow.component.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Parse Date/Time", 
		category = "Date",
		description = "Parses Date/Time string using a given pattern.",
        version = 1)
public class ParseTime extends BaseComponent {

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Time", "Pattern"},
			returnDescription="Calendar")
	public Calendar createObservations(String time, String expectedPattern) {		
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	    try {
	    	SimpleDateFormat formatter = new SimpleDateFormat(expectedPattern);
	    	formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	    	calendar.setTime(formatter.parse(time));	      
	    } catch (ParseException e) {
	    	LOGGER.error("CreateCalendarForCurrentObservation ", e);	      
	    }
	    
	    return calendar;
	}
}
