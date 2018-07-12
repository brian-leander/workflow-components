package dk.dmi.lib.workflow.component.util;

import org.apache.commons.io.FilenameUtils;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Path To System Path", 
		category = "Util", 
		description = "Converts a path to current executing OS file system format", 
		version = 1)
public class PathToSystemPath extends BaseComponent {

	@ExecuteMethod(
			argumentDescriptions = { "Path to convert" }, 
			returnDescription = "Converted path")
	public String execute(String path) {
		String systemPath = FilenameUtils.separatorsToSystem(path);
		return systemPath;
	}
	
}
