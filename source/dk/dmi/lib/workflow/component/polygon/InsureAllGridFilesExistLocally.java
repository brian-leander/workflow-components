package dk.dmi.lib.workflow.component.polygon;

import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFile;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Insure all grid files exists locally", 
		category = "Polygon",
		description = "Checks that all grid files exists locally. If not, tries to copy them from remote location based on information stored in polygon configuration, i.e. copy from POLYGON_FILES_REMOTE_SERVER + POLYGON_FILES_REMOTE_LOCATION to POLYGON_FILES_LOCAL_LOCATION.",
        version = 1)
public class InsureAllGridFilesExistLocally extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"List of grid file entities to check"}, 
			returnDescription = "True if all files are available locally, or successfully copied from remote server, else false if not all files are available")
	public boolean execute(List<GridFile> gridFiles) throws Exception {
		boolean allGridFilesExists = true;
		
		InsureGridFileExistLocally insureGridFileExistLocally = new InsureGridFileExistLocally();
		
		for (GridFile gridFile : gridFiles) {
			allGridFilesExists = allGridFilesExists && insureGridFileExistLocally.execute(gridFile);
		}
		
		return allGridFilesExists;
	}

}
