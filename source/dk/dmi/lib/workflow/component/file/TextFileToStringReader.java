package dk.dmi.lib.workflow.component.file;

import java.io.IOException;
import dk.dmi.lib.common.FileUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Text File To String Reader", 
		category = "File",
		description = "Read from text file into a string.",
        version = 1)
public class TextFileToStringReader extends BaseComponent {
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfFileCompressionFormats(String ignore) throws IOException {
		String[] compressionList = FileUtils.getListOfFileCompressionFormats();
		return compressionList;
	}

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Path and name to the file", "File encoding if compressed", "Number of header lines that should be ignored"}, 
			returnDescription = "Concatenated strings or empty string if the file doesn't exist")
    public String execute(String filePath, String compression, int ignoreLines) throws IOException {
		String str = FileUtils.textFileToStringReader(filePath, compression, ignoreLines);
		return str;
    }
	
}
