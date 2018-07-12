package dk.dmi.lib.workflow.component.check;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Is new text signature", 
		category = "Check",
		description = "Adds a text to current execution and checks if it has been added before. Optionally, the date can also be used to check if the signature is newer than the exsisting one. Signatures are not persisted, i.e. will only be checked against current in-process.",
        version = 1)
public class FirstTimeTextSignature extends BaseComponent {
	static final String TEXT_SIGNATURE_MAP_KEY = "_TEXT_SIGNATURE_MAP";
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDescriptions = {"Text to check and add", "Date (Optional, use null to ignore)"}, 
			returnDescription = "Returns true if text signature is added for the first time (or if the date is higher than the existing same signature), returns false if the signature already exists (with a higher date).")
    public boolean execute(String text, Date date) {
		Map<String, Date> textSignatureMap = getTextSignatureMap();
		
		if(textSignatureMap.containsKey(text)) {
			Date storedDate = textSignatureMap.get(text);
			
			if(date == null || storedDate == null || !date.after(storedDate)) {
				return false;
			}
		}
		
		textSignatureMap.put(text, date);
		return true;
    }
	
	@SuppressWarnings("unchecked")
	Map<String, Date> getTextSignatureMap() {
		Map<String, Date> textSignatureMap;
		Object textSignatureMapObject = workflowContextController.getObjectForKey(TEXT_SIGNATURE_MAP_KEY);
		
		if(textSignatureMapObject != null) {
			textSignatureMap = (Map<String, Date>) textSignatureMapObject;
		} else {
			textSignatureMap = new HashMap<String, Date>();
			workflowContextController.addObjectToContext(TEXT_SIGNATURE_MAP_KEY, textSignatureMap, false);
		}
		
		return textSignatureMap;
	}
	
}
