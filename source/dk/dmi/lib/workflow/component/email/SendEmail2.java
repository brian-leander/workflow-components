package dk.dmi.lib.workflow.component.email;

import dk.dmi.lib.common.EmailUtils;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Send Email", 
		category = "Email",
		description = "Sends an email defined by: to email, from email, subject, text and text type",
        version = 2)
public class SendEmail2 extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}	
	
	@ArgumentListGetMethod(
			argumentIndex = "4")
	public String[] getListOfMessageTypes(String ignore) {
		String[] messageTypeList = {EmailUtils.MESSAGE_TYPE_CLEAR_TEXT, EmailUtils.MESSAGE_TYPE_TEXT_HTML};
		return messageTypeList;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_AREA_SMALL, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Email destination (Seperate email addresses by simicolon)", "Email sender", "Subject text", "Message text", "Message type"})
    public void execute(String toEmail, String fromEmail, String subjectText, String messageText, String messageType) throws Exception {
		if(messageType.equals(EmailUtils.MESSAGE_TYPE_CLEAR_TEXT)) {
			messageType = null;
		}
		
		messageText = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), messageText);
		EmailUtils.sendEmail(toEmail, fromEmail, subjectText, messageText, messageType);
		
		LOGGER.debug("Email with subject : '"+subjectText+"' was sent to : "+toEmail);		
    }
	
}
