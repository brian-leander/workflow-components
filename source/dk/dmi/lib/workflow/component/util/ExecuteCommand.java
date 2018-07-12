package dk.dmi.lib.workflow.component.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import dk.dmi.lib.common.ExternalExecution;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Execute External Commands", 
		category = "Util",
		description = "Execute a serie of external commands, e.g. A bash command or another program. Commands and input are seperated by newline, the first line is a command and the following lines are inputs (stdin). To execute several commands, seperate them by double newline.",
        version = 1)
public class ExecuteCommand extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getOutputChoices(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String[] outputChoices = new String[]{ExternalExecution.FILTER_STD_OUT, ExternalExecution.FILTER_STD_ERR, ExternalExecution.FILTER_STD_BOTH};
		return outputChoices;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_AREA_SMALL, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Commands to execute (note: for scripts, depending on OS, start first line with bash, csh, cmd, etc...)", "Outout filter"}, 
			returnDescription = "List of execution output and errors")
    public List<String> execute(String commands, String outputFilter) throws Exception {
		commands = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), commands);
		
		ExternalExecution externalExecution = new ExternalExecution();
		return externalExecution.executeMultibleCommandLines(commands, outputFilter);
    }

}
