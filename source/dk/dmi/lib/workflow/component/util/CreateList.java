package dk.dmi.lib.workflow.component.util;

import java.util.ArrayList;
import java.util.List;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Deprecated
@Component(
		name = "Create new list", 
		category = "Util",
		description = "Creates a new text list, based on a comma seperated text",
        version = 1)
public class CreateList extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Comma seperated text"}, 
			returnDescription = "The newly created list")
    public Object execute(String text) {
		String[] strArray = text.split(",");
		List<String> list = new ArrayList<String>();
		
		for (String string : strArray) {
			list.add(string.trim());
		}
		
        return list;
    }
	
}
