package dk.dmi.lib.workflow.component.check;

import java.util.Map;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Deprecated // newer version available
@Component(
		name = "Context Object Exist", 
		category = "Context",
		description = "Checks if a spesified context object exist.",
        version = 1)
public class ContextObjectExist extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDescriptions = {"The key to verify exists."}, 
			returnDescription = "Returns true if context object exists, returns false if none can be found by that name.")
    public boolean execute(String key) throws Exception {
		Map<String, Object> contextMap = workflowContextController.getWorkflowContextMap();
		return contextMap.containsKey(key);
    }
	
}
