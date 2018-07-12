package dk.dmi.lib.workflow.component.date;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Months;
import org.joda.time.Years;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get Time Interval Dynamic", 
		category = "Date",
		description = "Counts the date time between two dates, based on the time resolution.",
        version = 1)
public class GetDateIntervalDynamic extends BaseComponent {
	public static final String RESOLUTION_HOURS = "HOUR";
	public static final String RESOLUTION_DAYS = "DAY";
	public static final String RESOLUTION_MONTHS = "MONTH";
	public static final String RESOLUTION_YEARS = "YEAR";
	
	@ExecuteMethod(
			argumentDescriptions = {"Date to start from", "Date to stop at", "Time resolution name to count (HOUR, DAY, MONTH, YEAR)"}, 
			returnDescription = "Newly created date")
    public int execute(Date dateStart, Date dateEnd, String resolution) throws Exception {
		int interval;
		
		switch(resolution) {
		case RESOLUTION_HOURS :
			interval = Hours.hoursBetween(new DateTime(dateStart), new DateTime(dateEnd)).getHours();
			break;
			
		case RESOLUTION_DAYS :
			interval = Days.daysBetween(new DateTime(dateStart), new DateTime(dateEnd)).getDays();
			break;
			
		case RESOLUTION_MONTHS :
			interval = Months.monthsBetween(new DateTime(dateStart), new DateTime(dateEnd)).getMonths();
			break;
			
		case RESOLUTION_YEARS :
			interval = Years.yearsBetween(new DateTime(dateStart), new DateTime(dateEnd)).getYears();
			break;
			
		default:
			throw new Exception("GetDateInterval: Wrong resolution");
		}
		
		return interval;
    }
	
}
