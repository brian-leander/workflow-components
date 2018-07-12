package dk.dmi.lib.workflow.component.development;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

//@Deprecated - Never change component structure, instead use deprecated annotation and create a new component (Deprecated components are not accessible from workflow admin components menu)
@Component(
		name = "Component template", 
		category = BaseComponent.CATEGORY_DEVELOPMENT,
		description = "A description of how the component should be used",
        version = 1)
public class ComponentTemplate extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Description of argument arg1", "Description of argument arg2"}, 
			returnDescription = "Description of return value")
    public String execute(String arg1, String arg2) {
		LOGGER.debug("arg1 = " + arg1);
		LOGGER.debug("arg2 = " + arg2);
        
        return "You sent: " + arg1 + " and " + arg2;
    }
	
}
