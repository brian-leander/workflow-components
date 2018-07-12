package dk.dmi.lib.workflow.component.util;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Echo", 
		category = "Util",
		description = "Returns object reseived",
        version = 1)
public class Echo extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Object to return"}, 
			returnDescription = "Object received")
    public Object execute(Object object) {
        return object;
    }
	
}
