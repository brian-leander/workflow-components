package dk.dmi.lib.workflow.component.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "MapList To ListList", 
		category = "Convert",
		description = "Converts a map of lists to an arraylist of lists",
        version = 1)
public class MapListToListList extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Map to convert"}, 
			returnDescription = "List created from the map")
    public List<List<Object>> execute(Map<Object, List<Object>> mapList) {
		List<List<Object>> listList = new ArrayList<List<Object>>(mapList.values());
        return listList;
    }
	
}
