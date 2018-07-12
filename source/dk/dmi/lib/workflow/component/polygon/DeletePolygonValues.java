package dk.dmi.lib.workflow.component.polygon;

import java.util.Date;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonValueController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonType;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Delete Polygon Values", 
		category = "Polygon",
		description = "Delete polygon data values for specified polygon type, parameter and datetime interval.",
        version = 1)
public class DeletePolygonValues extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Polygon type to delete values for", "Parameter to delete values for", "Start date for deletion", "Stop date for deletion"})
    public void execute(PolygonType polygonType, Parameter parameter, Date beginDateTime, Date endDateTime) throws Exception {
		PolygonValueController polygonValueController = PolygonValueController.getInstance();
		polygonValueController.deletePolygonValues(polygonType, parameter, beginDateTime, endDateTime);
    }

}
