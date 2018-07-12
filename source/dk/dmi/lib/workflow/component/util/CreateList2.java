package dk.dmi.lib.workflow.component.util;

import java.util.ArrayList;
import java.util.List;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create new list", 
		category = "Util",
		description = "Creates a new list of text strings, defined by a text and a seperator.",
        version = 2)
public class CreateList2 extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Text to generate list from (use null to create empty list)", "Seperator to split the list by"}, 
			returnDescription = "The newly created list")
    public List<String> execute(String text, String seperator) {
		List<String> list = new ArrayList<String>();
		
		if(text != null) {
			populateList(text, seperator, list);
		}
		
        return list;
    }

	void populateList(String text, String seperator, List<String> list) {
		String[] strArray = text.split(seperator);
		
		for (String string : strArray) {
			list.add(string.trim());
		}
	}
	
}
