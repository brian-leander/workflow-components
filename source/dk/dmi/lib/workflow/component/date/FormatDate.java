package dk.dmi.lib.workflow.component.date;

import java.util.Date;

import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Format Date", 
		category = "Date",
		description = "Returns a text representation of a date with the given format",
        version = 1)
public class FormatDate extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Date to format (use null to create new date)", "The format to use, e.x. yyyy-MM-dd HH:mm:ss SSS"}, 
			returnDescription = "Formatted ")
    public String execute(Date date, String format) {
		if(date == null) {
			date = new Date();
		}
		
		String formattedDate = DateUtils.formatDate(date, format);
        return formattedDate;
    }
	
}
