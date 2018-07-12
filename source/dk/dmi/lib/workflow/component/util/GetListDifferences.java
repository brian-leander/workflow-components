package dk.dmi.lib.workflow.component.util;

import java.io.IOException;
import java.util.List;
import dk.dmi.lib.common.FileUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get list differences", 
		category = "Util",
		description = "If two lists holds the same content and in the same order then null is returned, else a text with the differences is returned.",
        version = 1)
public class GetListDifferences extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"First list", "Second list"}, 
			returnDescription = "A text with the lines that are diffrent or null if the lists are identical")
    public String execute(List<String> list1, List<String> list2) throws IOException {
		String listDifferences = FileUtils.getListDifferences(list1, list2);
        return listDifferences;
    }
	
}
