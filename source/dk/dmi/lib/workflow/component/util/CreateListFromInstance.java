package dk.dmi.lib.workflow.component.util;

import java.util.ArrayList;
import java.util.List;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Deprecated // use component AddObjectToList
@Component(
		name = "Create list from instance", 
		category = "Util",
		description = "Creates a new generic list, based on the argument instance type. The argument object will be added to the list.",
        version = 1)
public class CreateListFromInstance extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Instance to create the list from"}, 
			returnDescription = "The newly created list")
    public List<?> execute(Object object) {
		List<Object> list = new ArrayList<Object>();
		list.add(object);
        return list;
    }
	
}
