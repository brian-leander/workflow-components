package dk.dmi.lib.workflow.component.development;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Stopwatch", 
		category = BaseComponent.CATEGORY_DEVELOPMENT,
		description = "Initialize a timer and print start, lab and stop times.",
        version = 1)
public class StopWatch extends BaseComponent {
	public static final String STOPWATCH_INITIAL_TIME_MILLISEC = "_STOPWATCH_INITIAL_TIME_MILLISEC";
	public static final String ACTION_START = "Start";
	public static final String ACTION_STOP = "Stop";
	public static final String ACTION_LAB = "Lab";
	
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfEntityNames(String ignore) throws IOException {
		return new String[]{ACTION_START, ACTION_LAB, ACTION_STOP};
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Action to perform"},
			returnDescription = "Time corresponding to the specified action")
    public String execute(String action) throws Exception {
		String timeStr = "";
		
		switch(action) {
			case ACTION_START :
				timeStr = handleStartAction();
			break;
			
			case ACTION_STOP :
				timeStr = handleStopAction();
			break;
			
			case ACTION_LAB :
				timeStr = handleLabAction();
			break;
			
			default :
				throw new Exception("Error: Wrong action in component StopWatch!");
		}
		
		return timeStr;
	}
	
	String handleStartAction() {
		long initialTimeMilliSec = System.currentTimeMillis();
		workflowContextController.addObjectToContext(STOPWATCH_INITIAL_TIME_MILLISEC, initialTimeMilliSec, false);
		String timeStr = formatMilliSecToTimeLabString(0);
		LOGGER.debug("Stopwatch start: "+timeStr);
		return timeStr;
	}
	
	String handleLabAction() throws Exception {
		String timeLab = getTimeLabString();
		LOGGER.debug("Stopwatch lab: "+timeLab);
		return timeLab;
	}
	
	String handleStopAction() throws Exception {
		String timeLab = getTimeLabString();
		LOGGER.debug("Stopwatch stop: "+timeLab);
		workflowContextController.removeObjectFromContext(STOPWATCH_INITIAL_TIME_MILLISEC);
		return timeLab;
	}
	
	String getTimeLabString() throws Exception {
		long initialTimeMilliSec = getInitialTimeMilliSec();
		long timeLabMilliSec = System.currentTimeMillis() - initialTimeMilliSec;
		
		String timeStr = formatMilliSecToTimeLabString(timeLabMilliSec);
		return timeStr;
	}

	String formatMilliSecToTimeLabString(long timeLabMilliSec) {
		String timeLabString = getFormattedDays(timeLabMilliSec);
		timeLabString += getFormattedTime(timeLabMilliSec);
		return timeLabString;
	}

	String getFormattedDays(long timeLabMilliSec) {
		String dateFormatted = "";
		long days = timeLabMilliSec / DateUtils.MILLISEC_IN_DAY;
		
		if(days > 0) {
			dateFormatted += days + "days + ";
		}
		
		return dateFormatted;
	}

	String getFormattedTime(long timeLabMilliSec) {
		long rest = timeLabMilliSec % DateUtils.MILLISEC_IN_DAY;
		Date date = new Date(rest);
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss .SSS");
		String timeFormatted = formatter.format(date);
		return timeFormatted;
	}
	
	long getInitialTimeMilliSec() throws Exception {
		long initialTimeMilliSec = -1;
		Object initialTimeMilliSecObject = workflowContextController.getObjectForKey(STOPWATCH_INITIAL_TIME_MILLISEC);
		
		if(initialTimeMilliSecObject != null) {
			initialTimeMilliSec = ((Long) initialTimeMilliSecObject).longValue();
		} else {
			throw new Exception("Error: Missing stop watch initial time context object!");
		}
		
		return initialTimeMilliSec;
	}
	
}
