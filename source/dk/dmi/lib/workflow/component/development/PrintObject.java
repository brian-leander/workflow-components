package dk.dmi.lib.workflow.component.development;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Print", 
		category = BaseComponent.CATEGORY_DEVELOPMENT,
		description = "Prints out object toString to console",
        version = 1)
public class PrintObject extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Object to print"})
    public void execute(Object object) {
		System.out.println(object);
    }
	
}
