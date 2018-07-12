package dk.dmi.lib.workflow.component.util;

import java.util.ArrayList;
import java.util.List;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create list", 
		category = "Util",
		description = "Creates a new generic list.",
        version = 1)
public class CreateGenericList extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {}, 
			returnDescription = "The newly created list")
    public <T> List<T> execute() {
		List<T> list = new ArrayList<>();
				
        return list;
    }
	
}
