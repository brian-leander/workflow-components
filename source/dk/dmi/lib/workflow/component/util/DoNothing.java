package dk.dmi.lib.workflow.component.util;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Do Nothing", 
		category = "Util",
		description = "Does nothing, empty component",
        version = 1)
public class DoNothing extends BaseComponent {
	
	@ExecuteMethod()
    public void execute() {
    }
	
}
