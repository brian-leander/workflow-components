package dk.dmi.lib.workflow.component.ftp;

import java.util.Map;
import java.util.Vector;

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
		name = "SFtp Get Files", 
		category = "Ftp",
		description = "Gets files from an sftp server.",
		version = 1)
public class SftpGetFiles extends BaseComponent {	
	private static final int FTP_PORT = 22;

	WorkflowContextController workflowContextController;	

	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}

	@ExecuteMethod(
			argumentDescriptions = {"Map containing follwing key/value pairs : 'ftpServer': URL to FTP server, 'username': User name for the ftp account, 'password': Password " + 
					"'ftpServerIncomingPath': Path to the source files on the FTP server, 'ftpServerProcessedPath': Path to move the downloaded files on the FTP server, " +
					"'ftpServerDuplicatePath': Path to move any duplicated source files on the FTP server, 'localOutputPath': Path to put the files locally, 'fileType': File type to download (XML, CSV ...)"},
			returnDescription="True if files are successfully downloadeded. Otherwise false")
	public boolean getFiles(Map<String, String> serverInfo) {		
		
		final JSch client = new JSch();        
		Session session = null;		
		boolean success = false;
		
		try {
			String username = serverInfo.get("username");   
			String ftpServer = serverInfo.get("ftpServer"); 
			String password = serverInfo.get("password"); 
			String ftpServerIncomingPathsStr = serverInfo.get("ftpServerIncomingPath"); 
			String ftpServerProcessedPath = serverInfo.get("ftpServerProcessedPath");
			String ftpServerDuplicatePath = serverInfo.get("ftpServerDuplicatePath");
			String fileType = serverInfo.get("fileType");
			String localOutputPath = serverInfo.get("localOutputPath");
			
			session = client.getSession(username, ftpServer, FTP_PORT);           
			UserInfo ui = new SftpUserInfo(password); 	
			session.setUserInfo(ui);
			session.connect();

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp channelSftp = (ChannelSftp) channel;

			String[] ftpServerIncomingPaths = ftpServerIncomingPathsStr.split("#");
			for (String ftpServerIncomingPath : ftpServerIncomingPaths) {
				channelSftp.cd(ftpServerIncomingPath);

				LOGGER.info("### Begin - folder : " + ftpServerIncomingPath );				
				Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*." + fileType);        	
				for (ChannelSftp.LsEntry entry : list) {
					LOGGER.info("### entry:" + entry.getFilename());
					channelSftp.get(entry.getFilename(), localOutputPath + entry.getFilename());
					try {
						channelSftp.rename(entry.getFilename(), ftpServerProcessedPath + "/" + entry.getFilename());
					} catch (SftpException e) {
						LOGGER.error("Error in SftpGetFiles. Can not move file: " + entry.getFilename(), e);
						String file = entry.getFilename();
						channelSftp.rename(file, ftpServerDuplicatePath + file.substring(0, file.length() - 4) + "_" + System.currentTimeMillis() + "." + fileType);
					}					
				}
				LOGGER.info("### Done");			
			}
			
			channel.disconnect();

			success = true;
		} catch (JSchException | SftpException e) {
			workflowContextController.addObjectToContext("FTP_ERROR", e.getMessage(), false);
			LOGGER.error("Error in SftpGetFiles. ", e);
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
