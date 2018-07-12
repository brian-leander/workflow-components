package dk.dmi.lib.workflow.component.file;

import java.io.IOException;
import dk.dmi.lib.common.FileUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "String To Text File Writer", 
		category = "File",
		description = "Write a string to a text file.",
		version = 1)
public class StringToTextFileWriter extends BaseComponent {
	
	@ArgumentListGetMethod(
			argumentIndex = "2")
	public String[] getListOfFileCompressionFormats(String ignore) throws IOException {
		String[] compressionList = FileUtils.getListOfFileCompressionFormats();
		return compressionList;
	}

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"String to write in file", "Path and name of the file, if path don't exist its created", "File encoding if compressed"})
    public void execute(String str, String filePath, String compression) throws IOException {
		FileUtils.stringToTextFileWriter(str, filePath, compression);
    }
	
}
