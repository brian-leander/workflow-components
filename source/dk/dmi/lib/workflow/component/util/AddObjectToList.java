package dk.dmi.lib.workflow.component.util;

import java.util.ArrayList;
import java.util.List;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Add object to list", 
		category = "Util",
		description = "Adds an object to the list with the specified name. If the list name is null or doesn't exist, a new list is created. If value object is null, an empty list is returned.",
        version = 1)
public class AddObjectToList extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@SuppressWarnings("unchecked")
	@ExecuteMethod(
			argumentDescriptions = {"Name of the list to add object (use null to create new)", "Object to add to list (use null if nothing should be added)"}, 
			returnDescription = "The existing or newly created list, with the added object")
    public List<?> execute(String listName, Object object) {
		List<Object> list = null;
		
		if(listName != null && !listName.trim().equals("")) {
			Object listObject = workflowContextController.getObjectForKey(listName);
			
			if(listObject instanceof List) {
				list = (ArrayList<Object>) listObject;
			}
		}
		
		if(list == null) {
			list = new ArrayList<Object>();
		}
		
		if(object != null) {
			list.add(object);
		}
		
        return list;
    }
	
}
