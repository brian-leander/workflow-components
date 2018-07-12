package dk.dmi.lib.workflow.component.file;

import java.io.File;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get File", 
		category = "File",
		description = "Read file from specified path.",
        version = 1)
public class GetFile extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Path and name to the file"}, 
			returnDescription = "File that was read, null if the file is missing")
    public File execute(String filePath) {
		File file = new File(filePath);
		
		if(file.exists() && !file.isDirectory()) {
			return file;
		} else {
			return null;
		}
    }
	
}
