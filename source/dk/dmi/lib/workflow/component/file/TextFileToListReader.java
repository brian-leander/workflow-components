package dk.dmi.lib.workflow.component.file;

import java.io.IOException;
import java.util.List;
import dk.dmi.lib.common.FileUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Text File To List Reader", 
		category = "File",
		description = "Read from text file and split lines into an array list.",
        version = 1)
public class TextFileToListReader extends BaseComponent {
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfFileCompressionFormats(String ignore) throws IOException {
		String[] compressionList = FileUtils.getListOfFileCompressionFormats();
		return compressionList;
	}

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Path and name to the file", "File encoding if compressed", "Number of header lines that should be ignored"}, 
			returnDescription = "List of strings, and null if the file doesn't exist")
    public List<String> execute(String filePath, String compression, int ignoreLines) throws IOException {
		List<String> list = FileUtils.textFileToListReader(filePath, compression, ignoreLines);
		return list;
    }
	
}
