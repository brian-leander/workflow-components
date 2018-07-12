package dk.dmi.lib.workflow.component.util;

import java.io.IOException;
import java.util.List;
import dk.dmi.lib.common.FileUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Is identical lists", 
		category = "Util",
		description = "Checks if two lists holds the same content and in the same order.",
        version = 1)
public class isIdenticalLists extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"First list", "Second list"}, 
			returnDescription = "True if the lists are identical, otherwise false")
    public boolean execute(List<String> list1, List<String> list2) throws IOException {
		boolean identicalLists = FileUtils.isIdenticalList(list1, list2);
        return identicalLists;
    }
	
}
