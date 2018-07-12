package dk.dmi.lib.workflow.component.synop;

import java.util.Calendar;
import java.util.TimeZone;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create Result File Name", 
		category = "VST",
		description = "Create Result File Name.",
        version = 2)
public class CreateResultFileName extends BaseComponent {

	@ExecuteMethod(returnDescription = "Formatted File Name")
	public String execute() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
				
		return getFileName(calendar);
	}

	String getFileName(Calendar calendar) {
		return calendar.get(Calendar.YEAR) + 
				leftpadZero((calendar.get(Calendar.MONTH) + 1), 2) + 
				leftpadZero(calendar.get(Calendar.DAY_OF_MONTH), 2) + 
				leftpadZero(calendar.get(Calendar.HOUR_OF_DAY), 2) + 
				leftpadZero(calendar.get(Calendar.MINUTE), 2) + 
				leftpadZero(calendar.get(Calendar.SECOND), 2) + 
				"." +
				leftpadZero((calendar.get(Calendar.MILLISECOND)), 7);		
	}
	
	private String leftpadZero(int number, int length) {
		return String.format("%0" + length + "d", number);	
	}
}