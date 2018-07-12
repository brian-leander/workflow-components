package dk.dmi.lib.workflow.component.polygon;

import java.io.IOException;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.GridFileCalculationController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFile;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFileCalculation;

@Component(
		name = "Get Newest Grid File Calculation", 
		category = "Polygon",
		description = "Get newest grid file calculation by grid file",
        version = 1)
public class GetNewestGridFileCalculation extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"grid file entity"},
			returnDescription = "Latest grid file calculation")
	public GridFileCalculation execute(GridFile gridFile) throws IOException {
		GridFileCalculationController gridFileCalculationController = GridFileCalculationController.getInstance();
		GridFileCalculation gridFileCalculation = gridFileCalculationController.getCompletedLatestGridFileCalculationByGridFile(gridFile);
		return gridFileCalculation;
	}
	
}
