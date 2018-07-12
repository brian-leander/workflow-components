package dk.dmi.lib.workflow.component.development;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Print List", 
		category = BaseComponent.CATEGORY_DEVELOPMENT,
		description = "Prints out a list of objects toString, or defined method, to console",
        version = 1)
public class PrintObjectList extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Object list to print", "Print method to invoke on object, without specifying \"()\" (if null toString is invoked)"})
	public void execute(List<Object> objects, String objectDataMethod) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		for (Object object : objects) {
			Object dataToPrint = object;
			
			if(objectDataMethod != null && !objectDataMethod.trim().equals("")) {
				Method getDataMethod = object.getClass().getMethod(objectDataMethod);
				dataToPrint = getDataMethod.invoke(object, new Object[0]);
			}
			
			System.out.println(dataToPrint.toString());
		}
	}
	
}
