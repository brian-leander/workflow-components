package dk.dmi.lib.workflow.component.file;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get Filenames", 
		category = "File",
		description = "Get all filenames in specified path.",
		version = 1)
public class GetFileNames extends BaseComponent {

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Path to files"},
			returnDescription="List of all files in given folder")
	public List<String> execute(String path) {		
		return Arrays.asList(new File(path).list());		
	}
}