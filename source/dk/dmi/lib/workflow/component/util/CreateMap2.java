package dk.dmi.lib.workflow.component.util;

import java.util.HashMap;
import java.util.Map;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Create new map", 
		category = "Util",
		description = "Creates a new map of key value string pairs, defined by a text string and split expressions.",
        version = 2)
public class CreateMap2 extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}	
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_AREA_MEDIUM, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Text to generate map from", "Text to split each key/value pairs with", "Text to split each key and value by"}, 
			returnDescription = "The newly created map")
    public Map<String, String> execute(String text, String elementSplit, String keyValueSplit) {
		text = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), text);
		String[] strArray = text.split(elementSplit);
		Map<String, String> map = new HashMap<>(strArray.length);
		
		for (String line : strArray) {
			String[] strKeyValue = line.split(keyValueSplit);
			String value;
			
			if (strKeyValue.length < 2) {
				value = "";
			} else {
				value = strKeyValue[1];
			}
			
			map.put(strKeyValue[0], value);
		}
		
		return map;
    }
	
}
