package dk.dmi.lib.workflow.component.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Map To List", 
		category = "Convert",
		description = "Converts a map to an arraylist, eighter by keyset or values.",
        version = 2)
public class MapToList2 extends BaseComponent {
	
	static final String KEYSET = "Keyset";
	static final String VALUES = "Values";
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfDatabases(String ignore) {
		String[] keysetOrValueList = {KEYSET, VALUES};
		return keysetOrValueList;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Map to convert", "What the list should be create from"}, 
			returnDescription = "List created from the map")
    public List<Object> execute(Map<Object, Object> map, String keysetOrValues) throws Exception {
		List<Object> list = null;
		
		if(KEYSET.equals(keysetOrValues)) {
			list = new ArrayList<Object>(map.keySet());
		} else if (VALUES.equals(keysetOrValues)) {
			list = new ArrayList<Object>(map.values());
		} else {
			throw new Exception("Impossible keysetOrValue chooice!");
		}
		
        return list;
    }
	
}
