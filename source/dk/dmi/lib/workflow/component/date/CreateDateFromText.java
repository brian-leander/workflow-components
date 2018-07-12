package dk.dmi.lib.workflow.component.date;

import java.text.ParseException;
import java.util.Date;
import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create date from text", 
		category = "Date", 
		description = "Create a date from text based on the specified date format.", 
		version = 1)
public class CreateDateFromText extends BaseComponent {

	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfDateFormats(String ignore) {
		String[] formats = {DateUtils.DATE_FORMAT_DEFAULT, DateUtils.DATE_FORMAT_1, DateUtils.DATE_FORMAT_2, DateUtils.DATE_FORMAT_3, DateUtils.DATE_FORMAT_4, DateUtils.DATE_FORMAT_5, DateUtils.DATE_FORMAT_6, DateUtils.DATE_FORMAT_7};
		return formats;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = { "Text representation of date", "Date format to use for parsing" }, 
			returnDescription = "Date instance of text")
	public Date execute(String dateText, String dateFormat) throws ParseException {
		Date parsedDate = DateUtils.parseStringToDate(dateText, dateFormat);
		return parsedDate;
	}
	
}
