package dk.dmi.lib.workflow.component.vejvejr.oresund;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create VSOP (M14) File Content", 
		category = "VejVejr",
		description = "Create VSOP file in M14 format",
        version = 1)
public class CreateVsopFileContent extends BaseComponent {

	private static final int ENTRY_LENGTH = 7;
	private static final String VSOP_FORMAT = "M14";

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT,WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT,WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Calendar","Key","Map of observations"},
			returnDescription="M14 file content")
	public String createFileContent(Calendar calendar, String sensorId, Map<Integer, Number> observations) {
		final StringBuilder output = new StringBuilder();
		output.append(getHeaderDate(calendar)).append(",01," + VSOP_FORMAT + ",").append(sensorId);		
	    
		for (Integer key : observations.keySet()) {
			String linie = getVsopLine(key, observations.get(key));
    		output.append(System.lineSeparator()).append(linie);
		}
	    output.append(System.lineSeparator()).append("=");
	    
	    return output.toString();
	}
	
	private String getHeaderDate(Calendar calendar) {        
        String date = new DecimalFormat("0000").format(calendar.get(Calendar.YEAR)) + "-" +
                new DecimalFormat("00").format(calendar.get(Calendar.MONTH) + 1) + "-" +
                new DecimalFormat("00").format(calendar.get(Calendar.DAY_OF_MONTH)) + " " +
                new DecimalFormat("00").format(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                new DecimalFormat("00").format(calendar.get(Calendar.MINUTE));        

        return date;
    }
	
	private String getVsopLine(int itemNumber, Number value) {
		Locale.setDefault(Locale.US);
		
		String formattedItemNo = new DecimalFormat("00").format(itemNumber);		
		if (value == null) {
			return String.format("%s" + "%" + ENTRY_LENGTH + "s", formattedItemNo, "///;");
        }
		
        String format = value instanceof Integer ? "0" : "0.0";                        
        String formattedValue = new DecimalFormat(format).format(value);        
        return String.format("%s" + "%" + ENTRY_LENGTH + "s" + ";", formattedItemNo, formattedValue);        
    }
	
}