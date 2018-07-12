package dk.dmi.lib.workflow.component.ftp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "SFtp Put Files", 
		category = "Ftp",
		description = "Puts files to a sftp server.",
		version = 1)
public class SftpPutFiles extends BaseComponent {
	private static final int FTP_PORT = 22;
	
	WorkflowContextController workflowContextController;	

	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDescriptions = {"Map containing follwing key/value pairs : 'ftpServer': URL to FTP server, 'username': User name for the ftp account, 'password': Password " + 
					"'filesPath': Path to the source files on the local server, 'filesProcessedPath': Path to move the uploaded files on the local server, " +
					"'filesDuplicatePath': Path to move any duplicated source files on the local server, 'ftpServerOutputPath': Path to put the files on the SFTP server, 'fileType': File type to upload (XML, CSV ...)"},
			returnDescription="True if files are successfully uploaded. Otherwise false")
	public boolean putFiles(Map<String, String> serverInfo) {
		final JSch client = new JSch();        
		Session session = null;	
		boolean success = false;
		
		try {
			String username = serverInfo.get("username");   
			String ftpServer = serverInfo.get("ftpServer"); 
			String password = serverInfo.get("password"); 
			String filesPath = serverInfo.get("filesPath"); 
			String filesProcessedPath = serverInfo.get("filesProcessedPath");
			Optional<String> fileType = Optional.ofNullable(serverInfo.get("fileType"));
			String ftpServerOutputPath = serverInfo.get("ftpServerOutputPath");
			
			session = client.getSession(username, ftpServer, FTP_PORT);           
			UserInfo ui = new SftpUserInfo(password); 	
			session.setUserInfo(ui);
			session.connect();

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp channelSftp = (ChannelSftp) channel;
			
			channelSftp.cd(ftpServerOutputPath);

			LOGGER.info("### Begin - folder : " + filesPath );
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(filesPath))) {
				for (Path fileEntry: stream) {
			        Path fileName = fileEntry.getFileName();
			    			        
			        if (fileType.isPresent()) {
			        	if (!fileName.toString().endsWith(fileType.get())) {
			        		continue;
			        	}
			        }
			        
			        if (Files.isDirectory(fileEntry)) {
			        	continue;
			        }
		            
			        try (FileInputStream fileInputStream = new FileInputStream(fileEntry.toFile())) {
						channelSftp.put(fileInputStream, fileName.toString());									
					} catch (SftpException | FileNotFoundException e) {
						LOGGER.error("Error in SftpPutFiles. Can not upload file: " + fileName, e);
						continue;
					}
			        
			        if (filesProcessedPath != null && !"".equals(filesProcessedPath)) {
			        	Files.move(fileEntry, Paths.get(filesProcessedPath).resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
			        }
				}
		    } catch (IOException io) {
				LOGGER.error("Error in SftpPutFiles. Cannot read files in directory " + filesPath,  io);
				workflowContextController.addObjectToContext("FTP_ERROR", io.getMessage(), false);
		    }
			
			channel.disconnect();
			success = true;			
		} catch (JSchException | SftpException e) {
			workflowContextController.addObjectToContext("FTP_ERROR", e.getMessage(), false);
			LOGGER.error("Error in SftpPutFiles. ", e);
		} finally {
			session.disconnect();
		}

		return success;
	}
	
	private class SftpUserInfo implements UserInfo {
		private String passWord;			

		private SftpUserInfo(String passWord) {			
			this.passWord = passWord;
		}

		public String getPassphrase() {
			return null;
		}

		public String getPassword() {
			return passWord;
		}

		public boolean promptPassphrase(String arg0) {
			return true;
		}

		public boolean promptPassword(String arg0) {
			return true;
		}

		public boolean promptYesNo(String arg0) {
			return true;
		}

		public void showMessage(String arg0) {
		}

	}
}
