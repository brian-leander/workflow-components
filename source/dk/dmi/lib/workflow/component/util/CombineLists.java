package dk.dmi.lib.workflow.component.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Combine lists", 
		category = "Util",
		description = "Combine two lists by UNION or INTERSECTION, by a value defined by both types of object, i.e. same class getter method must be available for both object types for comparison. When objects have identical compare values, list1 object is added to the combined result list.",
        version = 1)
public class CombineLists extends BaseComponent {
	
	public static final String UNION_KEY = "UNION";
	public static final String INTERSECTION_KEY = "INTERSECTION";
	
	@ArgumentListGetMethod(
			argumentIndex = "2")
	public String[] getCombineList(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String[] filterOptions = {UNION_KEY, INTERSECTION_KEY};
		return filterOptions;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"First list to combine", "First class getter compare method name (String without parentheses)", "Options to combine by", "Second list to combine", "Second class getter compare method name (String without parentheses)"}, 
			returnDescription = "A new list combined by list1 and list2")
    public List<Object> execute(List<?> list1, String class1Method, String combineBy, List<?> list2, String class2Method) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		List<Object> combinedList = new ArrayList<Object>();
		
		if(list1 != null && list1.size() > 0 && list2 != null && list2.size() > 0) {
			Map<Object, Object> map1 = new HashMap<Object, Object>();
			for (Object list1Object : list1) {
				Object list1ObjectValue = invokeMethodOnObject(class1Method, list1Object);
				map1.put(list1ObjectValue, list1Object);
			}
			
			Map<Object, Object> map2 = new HashMap<Object, Object>();
			for (Object list2Object : list2) {
				Object list2ObjectValue = invokeMethodOnObject(class2Method, list2Object);
				map2.put(list2ObjectValue, list2Object);
			}
			combinedList = tryAddObjectsToCombinedMap(combineBy, map1, map2);
		} else if(UNION_KEY.equals(combineBy) && list1 != null && list1.size() > 0) {
			for (Object list1Object : list1) {
				combinedList.add(list1Object);
			}
		} else if(UNION_KEY.equals(combineBy) && list2 != null && list2.size() > 0) {
			for (Object list2Object : list2) {
				combinedList.add(list2Object);
			}
		}
		
        return combinedList;
    }
	
	Object invokeMethodOnObject(String classMethod, Object object) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method = object.getClass().getMethod(classMethod);
		Object value = method.invoke(object, new Object[0]);
		return value;
	}
	
	List<Object> tryAddObjectsToCombinedMap(String combineBy, Map<Object, Object> map1, Map<Object, Object> map2) {
		List<Object> combinedList = new ArrayList<Object>();
		
		switch(combineBy) {
		case UNION_KEY :
			for (Map.Entry<Object, Object> entry1 : map1.entrySet()) {
				map2.put(entry1.getKey(), entry1.getValue());
			}
			combinedList = new ArrayList<Object>(map2.values());
			break;
			
		case INTERSECTION_KEY :
			for (Map.Entry<Object, Object> entry1 : map1.entrySet()) {
				if(map2.get(entry1.getKey()) != null) {
					combinedList.add(entry1.getValue());
				}
			}
			break;
		}
		
		return combinedList;
	}
	
}
