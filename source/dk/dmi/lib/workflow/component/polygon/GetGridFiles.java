package dk.dmi.lib.workflow.component.polygon;

import java.util.Date;
import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.GridFileController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFile;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get Grid File Entities", 
		category = "Polygon",
		description = "Gets a list of grid file entities, specified by parameter and time obs interval",
        version = 1)
public class GetGridFiles extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Parameter instance to filter by", "Time obs from filter", "Time obs to filter"}, 
			returnDescription = "List of grid file entities matching criteria")
	public List<GridFile> execute(Parameter parameter, Date dateFrom, Date dateTo) {
		GridFileController gridFileController = GridFileController.getInstance();
	
		List<GridFile> gridFiles = gridFileController.getGridFileForParameterAndTimeObs(parameter, dateFrom, dateTo);
		return gridFiles;
	}
	
}
