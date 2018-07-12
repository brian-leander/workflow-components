package dk.dmi.lib.workflow.component.development;

import java.text.DecimalFormat;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Print heap memory", 
		category = BaseComponent.CATEGORY_DEVELOPMENT,
		description = "Prints out heap memory stats in megabytes, and returns the value.",
        version = 1)
public class PrintMemory extends BaseComponent {
	static final String TYPE_USED = "Used";
	static final String TYPE_FREE = "Free";
	static final String TYPE_TOTAL = "Total";
	static final String TYPE_MAX = "Max";
	static final double MEGA_BYTE_MULTIPLIER = 1048576.0; // 1024*1204
	
	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfDatabases(String ignore) {
		return new String[]{TYPE_USED, TYPE_FREE, TYPE_TOTAL, TYPE_MAX};
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Type of memory stat to print"},
			returnDescription = "Memory result")
    public double execute(String type) {
		double resultMegaBytes = 0.0;
		DecimalFormat decimalFormat = new DecimalFormat("###.###");
		
		switch(type) {
		case TYPE_USED :
			resultMegaBytes = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEGA_BYTE_MULTIPLIER;
			LOGGER.debug("Used memory:" + decimalFormat.format(resultMegaBytes) + " MB");
			break;
			
		case TYPE_FREE :
			resultMegaBytes = Runtime.getRuntime().freeMemory() / MEGA_BYTE_MULTIPLIER;
			LOGGER.debug("Free memory:" + decimalFormat.format(resultMegaBytes) + " MB");
			break;
			
		case TYPE_TOTAL :
			resultMegaBytes = (Runtime.getRuntime().totalMemory()) / MEGA_BYTE_MULTIPLIER;
			LOGGER.debug("Total memory:" + decimalFormat.format(resultMegaBytes) + " MB");
			break;
			
		case TYPE_MAX :
			resultMegaBytes = (Runtime.getRuntime().maxMemory()) / MEGA_BYTE_MULTIPLIER;
			LOGGER.debug("Max memory:" + decimalFormat.format(resultMegaBytes) + " MB");
			break;
		}
        
		return resultMegaBytes;
    }
	
}
