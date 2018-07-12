package dk.dmi.lib.workflow.component.util;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Echo Text", 
		category = "Util",
		description = "Returns text reseived",
        version = 1)
public class EchoText extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_AREA_MEDIUM},
			argumentDescriptions = {"Text to return"}, 
			returnDescription = "Text received")
    public String execute(String text) {
		text = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), text);
        return text;
    }
	
}
