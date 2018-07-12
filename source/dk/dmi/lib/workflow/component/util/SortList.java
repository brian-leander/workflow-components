package dk.dmi.lib.workflow.component.util;

import java.util.Collections;
import java.util.List;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Sort list", 
		category = "Util",
		description = "Sorts a list by ascending or descending order.",
        version = 1)
public class SortList extends BaseComponent {
	
	public static final String SORT_ORDER_ASC = "Ascending";
	public static final String SORT_ORDER_DESC = "Descending";
	public static final String SORT_ORDER_REV = "Reverse";
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfDatabases(String ignore) {
		return new String[]{SORT_ORDER_ASC, SORT_ORDER_DESC, SORT_ORDER_REV};
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"List to be sorted", "String order"})
    public void execute(List list, String order) {
		if(SORT_ORDER_ASC.equals(order)) {
			Collections.sort(list);
		} else if(SORT_ORDER_DESC.equals(order)) {
			Collections.sort(list, Collections.reverseOrder());
		} else if(SORT_ORDER_REV.equals(order)) {
			Collections.reverse(list);
		}
    }
	
}
