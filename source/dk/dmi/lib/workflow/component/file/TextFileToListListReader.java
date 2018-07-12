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
		name = "Text File To List List Reader", 
		category = "File",
		description = "Read from text file and split text into a list of lists, based on document lines and each line split by regular expression",
		version = 1)
public class TextFileToListListReader extends BaseComponent {
	
	@ArgumentListGetMethod(
			argumentIndex = "2")
	public String[] getListOfFileCompressionFormats(String ignore) throws IOException {
		String[] compressionList = FileUtils.getListOfFileCompressionFormats();
		return compressionList;
	}

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Path and name to the file", "Regular expression to split the line", "File encoding if compressed", "Number of header lines that should be ignored"}, 
			returnDescription = "Double array list of strings, and null if the file doesn't exist")
    public List<List<String>> execute(String filePath, String regex, String compression, int ignoreLines) throws IOException {
		List<List<String>> lineList = FileUtils.textFileToListListReader(filePath, regex, compression, ignoreLines);
		return lineList;
    }
	
}
