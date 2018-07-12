package dk.dmi.lib.workflow.component.email;

import java.io.IOException;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;
import dk.dmi.lib.workflow.common.WorkflowMailUtils;

@Deprecated
@Component(
		name = "Send Workflow Notification Email", 
		category = "Email",
		description = "Sends an information, warning or error email from workflow@dmi.dk, defined by: to email, subject, text and type",
        version = 1)
public class SendWorkflowEmail  extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfEntityNames(String ignore) throws IOException {
		return new String[]{WorkflowMailUtils.MAIL_TYPE_INFO, WorkflowMailUtils.MAIL_TYPE_WARNING, WorkflowMailUtils.MAIL_TYPE_ERROR};
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT ,WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_AREA_SMALL},
			argumentDescriptions = {"Notification type", "Email destination", "Subject text", "Message text"})
    public void execute(String type, String toEmail, String subjectText, String messageText) throws Exception {
		messageText = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), messageText);
		WorkflowMailUtils.sendWorkflowMail(type, toEmail, subjectText, messageText, null);
    }
	
}
