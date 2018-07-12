package dk.dmi.lib.workflow.component.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create attribute list from object list", 
		category = "Util",
		description = "Creates a list object attribute values based on a list of objects and getter method name.",
        version = 1)
public class CreateAttributeListFromObjectList extends BaseComponent {
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX},
			argumentDescriptions = {"List of objects to get attributes from", "Get attribute method", "Should list only contain unique values"}, 
			returnDescription = "List of attribute objects")
    public List<Object> execute(List<?> objectList, String methodName, boolean distinct) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		List<Object> attributeList = new ArrayList<Object>(); 
		
		for (Object object : objectList) {
			Method objectMethodName = object.getClass().getMethod(methodName);
			Object objectGetResult = (Object) objectMethodName.invoke(object, new Object[0]);
			
			if(!distinct || !attributeList.contains(objectGetResult)) {
				attributeList.add(objectGetResult);
			}
		}
		
        return attributeList;
    }
	
}
