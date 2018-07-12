package dk.dmi.lib.workflow.component.date;

import java.util.Date;
import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Zero Pad Date", 
		category = "Date",
		description = "Alters date by zero padding below specified calander field. E.g. Padding date by MONTH: (2014-11-12 15:23:34) becomes (2014-11-01 00:00:00).",
        version = 1)
public class ZeroPadDate extends BaseComponent {
		
	@ExecuteMethod(
			argumentDescriptions = {"Date to zero pad (use null to create new date)", "Date field to zero pad below: YEAR, MONTH, DAY, HOUR, MINUTE, SECOND"}, 
			returnDescription = "Zero padded date")
    public Date execute(Date date, String zeroPadFrom) {
		if(date == null) {
			date = new Date();
		}
		
		return DateUtils.zeroPadDate(date, zeroPadFrom);
    }
	
}
