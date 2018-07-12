package dk.dmi.lib.workflow.component.check;

import java.util.Map;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Context Objects Exist", 
		category = "Context",
		description = "Checks if all spesified context object exist, and if selected, does it differ from null.",
        version = 2)
public class ContextObjectExist2 extends BaseComponent {
	static final String SPLIT_CHAR = ";";
	
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX},
			argumentDescriptions = {"The key to verify exists, use ; to seperate context keys", "Should also check if context object != null"}, 
			returnDescription = "Returns true if context object exists. Returns false if context object don't exists or, if selected, does exists but is null.")
    public boolean execute(String key, boolean checkForNull) throws Exception {
		boolean keysExists = false;
		Map<String, Object> contextMap = workflowContextController.getWorkflowContextMap();
		String[] keyValues = key.split(SPLIT_CHAR);
		
		for (String keyValue : keyValues) {
			keyValue = keyValue.trim();
			keysExists = contextMap.containsKey(keyValue);
			
			if(checkForNull && keysExists) {
				Object obj = contextMap.get(keyValue);
				keysExists = obj != null;
			}
			
			if(!keysExists) {
				break;
			}
		}
		
		return keysExists;
    }
	
}
