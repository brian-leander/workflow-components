package dk.dmi.lib.workflow.component.monitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Alarm file Writer", 
		category = "Monitor",
		description = "Write a text to a alarm file.",
		version = 1)
public class SignalAlert extends BaseComponent {
	
  private String alarmFilePath = "alert/";

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Alarm file name", "Alarm text"})
	public void execute(String alarmFileName, String alarmText) throws IOException
	{	  
	    //Create path and make sure the path exists
	    Path targetPath = Paths.get(alarmFilePath);
	    targetPath = Files.createDirectories(targetPath);
	    
	    // Write file to (eventually created) path
	    Path filePath = targetPath.resolve(alarmFileName);    
	    Files.write(filePath, alarmText.getBytes(),StandardOpenOption.CREATE);	  
	}	
}
