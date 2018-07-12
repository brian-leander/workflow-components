package dk.dmi.lib.workflow.component.file;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import dk.dmi.lib.common.ExternalExecution;
import dk.dmi.lib.common.OSValidator;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Copy", 
		category = "File",
		description = "Copy files and folder between machines using scp, on windows cp command will be used (works only for local copy). The user dataman is used for accessign the remote machines. Throws an exception if stderr contains information.",
        version = 2)
public class Copy2 extends BaseComponent {
	
	public static final String REMOTE_USER_NAME = "dataman";
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX},
			argumentDescriptions = {"Source machine (use null for localhost)", "Source file to copy (e.g. /data/my_file.txt)", "Destination machine (use null for localhost)", "Destination file (e.g. /data/my_new_file.txt)", "Should source file be deleted after copy"},
			returnDescription = "True if copy was successful, otherwise false")
    public boolean execute(String sourceMachine, String sourceFilePath, String destinationMachine, String destinationFilePath, boolean deleteSourceFile) throws Exception {
		String sourceNameAndMachine = generateNameAndMachine(sourceMachine);
		File sourceFile = createFileFromPath(sourceFilePath);
		
		String destinationNameAndMachine = generateNameAndMachine(destinationMachine);
		File destinationFile = createFileFromPath(destinationFilePath);
		
		List<String> copyErrorResultList = copyFile(sourceNameAndMachine, sourceFile, destinationNameAndMachine, destinationFile);
		
		if(copyErrorResultList.size() > 0) {
			return false;
		} else {
			return tryCleanUpSourceFile(deleteSourceFile, sourceNameAndMachine, sourceFile);
		}
    }

	String generateNameAndMachine(String machine) throws UnknownHostException {
		if(machine == null && OSValidator.isWindows()) {
			return "";
		} else {
			if(machine == null) {
				machine = InetAddress.getLocalHost().getHostName();
			}
			return REMOTE_USER_NAME + "@" + machine + ":";
		}
	}

	List<String> copyFile(String sourceNameAndMachine, File sourceFile, String destinationNameAndMachine, File destinationFile) throws Exception {
		String commands = null;
		
		if (OSValidator.isWindows()) {
			commands = createWinCopyCommands(sourceFile, destinationFile);
		} else {
			commands = createUnixCopyCommands(sourceNameAndMachine, sourceFile, destinationNameAndMachine, destinationFile);
		}
		
		ExternalExecution externalExecution = new ExternalExecution();
		List<String> copyErrorResultList = externalExecution.executeMultibleCommandLines(commands, ExternalExecution.FILTER_STD_ERR);
		return copyErrorResultList;
	}
	
	File createFileFromPath(String path) {
		path = FilenameUtils.separatorsToSystem(path);
		File file = new File(path);
		
		if(file.getParentFile() == null && !path.substring(0, 1).equals(".")) {
			file = createFileFromPath("./"+path);
		}
		
		return file;
	}
	
	String createWinCopyCommands(File sourceFile, File destinationFile) {
		String commands = "";
		commands += "cmd" + System.lineSeparator();
		commands += "if not exist " + destinationFile.getParentFile().getAbsolutePath() + " mkdir " + destinationFile.getParentFile().getAbsolutePath() + System.lineSeparator();
		commands += "copy " + sourceFile.getAbsolutePath() + " " + destinationFile.getAbsolutePath();
		return commands;
	}
	
	String createUnixCopyCommands(String sourceNameAndMachine, File sourceFile, String destinationNameAndMachine, File destinationFile) {
		String commands = "";
		commands += "ssh " + destinationNameAndMachine.replace(":", "") + " mkdir -p " + destinationFile.getParentFile().getAbsolutePath() + System.lineSeparator();
		commands += System.lineSeparator();
		commands += "scp " + sourceNameAndMachine + sourceFile.getAbsolutePath() + " " + destinationNameAndMachine+ destinationFile.getAbsolutePath();
		return commands;
	}
	
	boolean tryCleanUpSourceFile(boolean deleteSourceFile, String sourceNameAndMachine, File sourceFile) throws Exception {
		if(deleteSourceFile) {
			List<String> deleteErrorResultList = deleteFile(sourceNameAndMachine, sourceFile);
			
			if(deleteErrorResultList.size() > 0) {
				return false;
			}
		}
		
		return true;
	}
	
	List<String> deleteFile(String machine, File file) throws Exception {
		String commands = null;
		
		if (OSValidator.isWindows()) {
			commands = createWinDeleteCommands(file);
		} else {
			commands = createUnixDeleteCommands(machine, file);
		}
		
		ExternalExecution externalExecution = new ExternalExecution();
		List<String> deleteErrorResultList = externalExecution.executeMultibleCommandLines(commands, ExternalExecution.FILTER_STD_ERR);
		return deleteErrorResultList;
	}

	private String createWinDeleteCommands(File file) {
		String commands = "";
		commands += "cmd" + System.lineSeparator();
		commands += "del " + file.getAbsolutePath();
		return commands;
	}
	
	private String createUnixDeleteCommands(String nameAndMachine, File file) {
		String commands = "";
		commands += "ssh " + nameAndMachine.replace(":", "") + " rm " + file.getAbsolutePath();
		return commands;
	}
	
}
