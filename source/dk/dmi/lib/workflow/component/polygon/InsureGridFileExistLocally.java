package dk.dmi.lib.workflow.component.polygon;

import java.util.Arrays;
import java.util.List;
import dk.dmi.lib.common.ExternalExecution;
import dk.dmi.lib.common.FileUtils;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonConfigurationController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFile;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonConfiguration;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Insure grid file exists locally", 
		category = "Polygon",
		description = "Checks grid file exists locally. If not, tries to copy the file from remote location based on information stored in polygon configuration, i.e. copy from POLYGON_FILES_REMOTE_SERVER + POLYGON_FILES_REMOTE_LOCATION to POLYGON_FILES_LOCAL_LOCATION.",
        version = 1)
public class InsureGridFileExistLocally extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Grid file entity to insure file for"}, 
			returnDescription = "True if file available locally, or successfully copied from remote server, else false if file still not available")
	public boolean execute(GridFile gridFile) throws Exception {
		PolygonConfigurationController polygonConfigurationController = PolygonConfigurationController.getInstance();
		
		PolygonConfiguration polygonConfigurationLocalLocation = polygonConfigurationController.getPolygonConfigurationByTag(PolygonConfigurationController.POLYGON_FILES_LOCAL_LOCATION);
		String gridFileLocalLocation = polygonConfigurationLocalLocation.getValue();
		
		String gridFilePath = gridFile.getFileUri();
		String gridFileLocalURI = gridFileLocalLocation + gridFilePath;
		
		boolean fileExists = FileUtils.fileExists(gridFileLocalURI);
		
		if(!fileExists) {
			ExternalExecution externalExecution = new ExternalExecution();
			List<String> insureGridFileLocalFolderResultList = insureGridFileLocalFolder(gridFileLocalURI, externalExecution);
			
			if(insureGridFileLocalFolderResultList == null || insureGridFileLocalFolderResultList.size() == 0) {
				PolygonConfiguration polygonConfigurationRemoteServer = polygonConfigurationController.getPolygonConfigurationByTag(PolygonConfigurationController.POLYGON_FILES_REMOTE_SERVER);
				String gridFileRemoteServer = polygonConfigurationRemoteServer.getValue();
				
				PolygonConfiguration polygonConfigurationRemoteLocation = polygonConfigurationController.getPolygonConfigurationByTag(PolygonConfigurationController.POLYGON_FILES_REMOTE_LOCATION);
				String gridFileRemoteLocation = polygonConfigurationRemoteLocation.getValue();
				
				List<String> copyGridFileToLocalMachineResultList = copyGridFileToLocalMachine(gridFilePath, gridFileLocalURI, externalExecution, gridFileRemoteServer, gridFileRemoteLocation);
				fileExists = copyGridFileToLocalMachineResultList == null || copyGridFileToLocalMachineResultList.size() == 0;
			}
		}
		
		return fileExists;
	}

	List<String> insureGridFileLocalFolder(String gridFileLocalURI, ExternalExecution externalExecution) throws Exception {
		String gridFileLocalFolderURI = gridFileLocalURI.substring(0, gridFileLocalURI.lastIndexOf(java.io.File.separator));
		String[] createGridFileFolderCommandArray = {"mkdir -p " + gridFileLocalFolderURI};
		List<String> createGridFileFolderCommandList = Arrays.asList(createGridFileFolderCommandArray);
		List<String> insureGridFileLocalFolderResultList = externalExecution.executeCommandList(createGridFileFolderCommandList, ExternalExecution.FILTER_STD_ERR);
		return insureGridFileLocalFolderResultList;
	}
	
	List<String> copyGridFileToLocalMachine(String gridFilePath, String gridFileLocalURI, ExternalExecution externalExecution, String gridFileRemoteServer, String gridFileRemoteLocation) throws Exception {
		String gridFileRemoteURI = gridFileRemoteServer+":"+gridFileRemoteLocation+gridFilePath;
		String[] copyGridFileToLocalMachineCommandArray = {"scp dataman@"+gridFileRemoteURI+" "+gridFileLocalURI};
		List<String> copyGridFileToLocalMachineCommandList = Arrays.asList(copyGridFileToLocalMachineCommandArray);
		List<String> copyGridFileToLocalMachineResultList = externalExecution.executeCommandList(copyGridFileToLocalMachineCommandList, ExternalExecution.FILTER_STD_ERR);
		return copyGridFileToLocalMachineResultList;
	}
	
}
