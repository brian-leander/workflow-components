package dk.dmi.lib.workflow.component.util;

import java.util.ArrayList;
import java.util.List;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Extract From Text", 
		category = "Util",
		description = "Splits a text by regular expression and returns the list. The list can be filtered by specifying a text each substring must contain.",
        version = 1)
public class ExtractFromString extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"String to split", "Regular expression", "Text to filter results (use null to ignore)"}, 
			returnDescription = "Filtered list for split text")
    public List<String> execute(String text, String regEx, String filter) {
		List<String> resultList = new ArrayList<String>();
		String[] splitText = text.split(regEx);
		
		for (String subText : splitText) {
			if(filter == null || subText.contains(filter)) {
				resultList.add(subText);
			}
		}
		
        return resultList;
    }
	
}
