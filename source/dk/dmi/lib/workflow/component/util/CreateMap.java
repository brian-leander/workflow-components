package dk.dmi.lib.workflow.component.util;

import java.util.HashMap;
import java.util.Map;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Deprecated // new version available
@Component(
		name = "Create new map", 
		category = "Util",
		description = "Creates a new map of key value string pairs, defined by a text string.",
        version = 1)
public class CreateMap extends BaseComponent {
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_AREA_MEDIUM},
			argumentDescriptions = {"Text to generate map from - use ; to split each key/value pairs, user , to split key and value"}, 
			returnDescription = "The newly created map")
    public Map<String, String> execute(String text) {		
		String[] strArray = text.split(";");
		
		Map<String, String> map = new HashMap<>(strArray.length);
		for (String line : strArray) {
			String[] strKeyValue = line.split(",");
			String value;
			if (strKeyValue.length < 2)
				value = "";
			else
				value = strKeyValue[1];
			map.put(strKeyValue[0], value);
		}
		
		return map;
    }
}
