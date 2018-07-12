package dk.dmi.lib.workflow.component.vejvejr.oresund;

import java.text.DecimalFormat;
import java.util.Calendar;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create VSOP file name", 
		category = "VejVejr",
		description = "",
        version = 1)
public class CreateVsopFileName extends BaseComponent {

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Station number", "Observation time (Calendar)"},
			returnDescription="VSOP File name (Type M14)")
	public String createFileName(int stationNumber, Calendar calendar) {
		
		String MMMM = new DecimalFormat("0000").format(stationNumber);
        String YMD = getYMD(calendar);
        String HMS = getHMS(calendar);

        String filename = new String(MMMM + "-" + YMD + "T" + HMS + ".M14");
        return filename;
    }	
	
	private String getYMD(Calendar calendar) {
		return new DecimalFormat("0000").format(calendar.get(Calendar.YEAR)) +
                new DecimalFormat("00").format(calendar.get(Calendar.MONTH) + 1) +
                new DecimalFormat("00").format(calendar.get(Calendar.DAY_OF_MONTH));
	}
	
	private String getHMS(Calendar calendar) {
		return new DecimalFormat("00").format(calendar.get(Calendar.HOUR_OF_DAY)) +
                new DecimalFormat("00").format(calendar.get(Calendar.MINUTE)) + "00";

	}
}