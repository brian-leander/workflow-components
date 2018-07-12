package dk.dmi.lib.workflow.component.ftp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import dk.dmi.lib.common.ExternalExecution;
import dk.dmi.lib.persistence.common.DatabasePersistenceConfiguration;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Dmi Ftp Writer", 
		category = "Ftp",
		description = "Write a file to ftpserver.dmi.dk, based on username and file.",
        version = 1)
public class DmiFtpWriter extends BaseComponent {
	public static final String PUT_DATA_LOCATION = "PUT_DATA_LOCATION";
	public static final String SOURCE_DEFINITION = "SOURCE_DEFINITION";
	public static final String USERNAME = "USERNAME";
	public static final String PASSWORD = "PASSWORD";
	public static final String INPUT_FILE = "INPUT_FILE";
	public static final String OUTPUT_FILE = "OUTPUT_FILE";
	
	@ExecuteMethod(
			argumentDescriptions = {"Username to the ftp account", "Path and name to the source file", "Name of the destination file"},
			returnDescription="True if successfully sent file to ftp, else false")
    public boolean execute(String username, String inputFile, String outputFile) throws Exception {
		List<String> commandLinesList = new ArrayList<String>();
		commandLinesList.add("/bin/csh");
		String executionServer = null;
		String putDataLocation = null;
		String sourceDefinition = null;
		
		if(DatabasePersistenceConfiguration.getCurrentConnectedDatabaseId() == DatabasePersistenceConfiguration.TEST_DATABASE_ID) {
			executionServer = "varulven";
			putDataLocation = "/opt/GetData";
			sourceDefinition = "dolly";
			
			File inFile = new File(inputFile);
			String newInputFile = "/tmp/" + inFile.getName();
			commandLinesList.add("scp " + inputFile + " " + executionServer + ":" + newInputFile);
			commandLinesList.add("/bin/csh");
			inputFile = newInputFile;
		} else {
			putDataLocation = "/opt/DMI/bin";
			sourceDefinition = "$FTPSERV";
			commandLinesList.add("source ~dataman/DATA_GROUPS");
		}
		
		String ftpCommand = createFtpCommand(executionServer, putDataLocation, sourceDefinition, username, inputFile, outputFile);
		commandLinesList.add(ftpCommand);
		
		if(DatabasePersistenceConfiguration.getCurrentConnectedDatabaseId() == DatabasePersistenceConfiguration.TEST_DATABASE_ID) {
			commandLinesList.add("exit");
		}
		
		ExternalExecution externalExecution = new ExternalExecution();
		List<String> processResultErrorList = externalExecution.executeCommandList(commandLinesList, ExternalExecution.FILTER_STD_ERR);
		
		if(processResultErrorList.size() == 0) {
			return true;
		} else {
			LOGGER.warn("################################ FTP COMMAND/RESPONSE ####################################");
			
			LOGGER.warn("Command request:");
			for (String commandString : commandLinesList) {
				LOGGER.warn(commandString);
			}
			
			LOGGER.warn("Error response:");
			for (String errorString : processResultErrorList) {
				LOGGER.warn(errorString);
			}
			
			LOGGER.warn("###################################### END FTP ###########################################");
			return false;
		}
    }

	String createFtpCommand(String executionServer, String putDataLocation, String sourceDefinition, String username, String inputFile, String outputFile) {
		String ftpCommand = "";
		
		if(executionServer != null) {
			ftpCommand += "ssh " + executionServer + " '"; 
		}
		
		ftpCommand += "PUT_DATA_LOCATION/PutData -D \"SOURCE_DEFINITION\" -F /data/putdata-spooling/USERNAME@OUTPUT_FILE -M /data/putdata-spooling/USERNAME@OUTPUT_FILE.signal < INPUT_FILE";
		
		if(executionServer != null) {
			ftpCommand += "'"; 
		}
		
		ftpCommand = ftpCommand.replace(PUT_DATA_LOCATION, putDataLocation);
		ftpCommand = ftpCommand.replace(SOURCE_DEFINITION, sourceDefinition);
		ftpCommand = ftpCommand.replace(USERNAME, username);
		ftpCommand = ftpCommand.replace(INPUT_FILE, inputFile);
		ftpCommand = ftpCommand.replace(OUTPUT_FILE, outputFile);
		return ftpCommand;
	}

}
