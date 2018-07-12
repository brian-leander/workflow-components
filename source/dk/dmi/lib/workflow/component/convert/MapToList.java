package dk.dmi.lib.workflow.component.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Deprecated // New version available
@Component(
		name = "Map To List", 
		category = "Convert",
		description = "Converts a map to an arraylist",
        version = 1)
public class MapToList extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Map to convert"}, 
			returnDescription = "List created from the map")
    public List<Object> execute(Map<Object, Object> map) {
		List<Object> list = new ArrayList<Object>(map.values());
        return list;
    }
	
}
