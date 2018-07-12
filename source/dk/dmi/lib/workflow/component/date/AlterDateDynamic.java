package dk.dmi.lib.workflow.component.date;

import java.util.Date;
import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Alter Date Dynamic", 
		category = "Date",
		description = "Alters a date dynamically, based on an excisting or new date. The specified date field will be added or subtracted from the date.",
        version = 1)
public class AlterDateDynamic extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Initial date (Use null for current date)", "The date field to alter, i.e. YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND", "Amount (+/- value)" }, 
			returnDescription = "Altered date")
    public Date execute(Date date, String dateField, int amount) throws Exception {
		if(date == null) {
			date = new Date();
		}
		
		Date newDate = DateUtils.alterDate(date, dateField, amount);
		return newDate;
    }
	
}
