package dk.dmi.lib.workflow.component.check;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Text white list", 
		category = "Check",
		description = "Checks if a text is part of a text list specified by seperated.",
        version = 1)
public class TextWhiteList extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Text to check", "Comma seperated text list to check aginst", "Text list seperator"},
			returnDescription = "Returns true if the text is part of the white list")
    public boolean execute(String text, String textList, String seperator) {
		boolean textInWhiteList = false;
		
		text = text.trim();
		String[] whiteList = textList.trim().split(seperator);
		
		for (String whiteText : whiteList) {
			if(whiteText.trim().equals(text)) {
				textInWhiteList = true;
				break;
			}
		}
		
		return textInWhiteList;
    }
	
}
