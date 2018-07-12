package dk.dmi.lib.workflow.component.file;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import dk.dmi.lib.common.FileUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Map List To Text File Writer", 
		category = "File",
		description = "Write a Map of lists to a text file of columns and rows. Specify column seperator and file compression. For every object, toString is written.",
		version = 1)
public class MapListToTextFileWriter extends BaseComponent {
	
	@ArgumentListGetMethod(
			argumentIndex = "3")
	public String[] getListOfFileCompressionFormats(String ignore) throws IOException {
		String[] compressionList = FileUtils.getListOfFileCompressionFormats();
		return compressionList;
	}

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Map of lists to write in file", "Path and name of the file, if path don't exist its created", "String of character to seperate columns by", "File encoding if compressed"})
    public void execute(Map<Object, List<Object>> mapList, String filePath, String columnSeperator, String compression) throws IOException {
		FileUtils.mapListToTextFileWriter(mapList, filePath, columnSeperator, compression);
    }
	
}
