package dk.dmi.lib.workflow.component.util;

import java.util.HashMap;
import java.util.Map;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Add entry to map", 
		category = "Util",
		description = "Adds an entry to the map with the specified name. If the map name is null or doesn't exist, a new map is created. If map key is null, an empty map is returned.",
        version = 1)
public class AddObjectToMap extends BaseComponent {
	
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@SuppressWarnings("unchecked")
	@ExecuteMethod(
			argumentDescriptions = {"Name of the map to add object (use null to create new)", "Key for the entry (use null if nothing should be added)", "Object for the entry"}, 
			returnDescription = "The existing or newly created map, with the added entry")
    public Map<?, ?> execute(String mapName, Object key, Object value) {
		Map<Object, Object> map = null;
		
		if(mapName != null && !mapName.trim().equals("")) {
			Object mapObject = workflowContextController.getObjectForKey(mapName);
			
			if(mapObject instanceof Map) {
				map = (Map<Object, Object>) mapObject;
			}
		}
		
		if(map == null) {
			map = new HashMap<Object, Object>();
		}
		
		if(key != null) {
			map.put(key, value);
		}
		
        return map;
    }
	
}
