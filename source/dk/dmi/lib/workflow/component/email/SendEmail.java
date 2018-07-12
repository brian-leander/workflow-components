package dk.dmi.lib.workflow.component.email;

import dk.dmi.lib.common.EmailUtils;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Deprecated
@Component(
		name = "Send Email", 
		category = "Email",
		description = "Sends an email defined by: to email, from email, subject and text",
        version = 1)
public class SendEmail extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_AREA_SMALL},
			argumentDescriptions = {"Email destination", "Email sender", "Subject text", "Message text"})
    public void execute(String toEmail, String fromEmail, String subjectText, String messageText) throws Exception {
		messageText = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), messageText);
		
		EmailUtils.sendEmail(toEmail, fromEmail, subjectText, messageText, null);
    }

}
