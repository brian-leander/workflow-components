package dk.dmi.lib.workflow.component.polygon;

import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonTypeController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonType;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get Polygon Types", 
		category = "Polygon",
		description = "Get a list of polygon types linked with the specified by parameter.",
        version = 1)
public class GetPolygonTypes extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Parameter to filter by"}, 
			returnDescription = "List of polygon types")
	public List<PolygonType> execute(Parameter parameter) throws ClassNotFoundException {
		PolygonTypeController polygonTypeController = PolygonTypeController.getInstance();
		List<PolygonType> polygonTypeList = polygonTypeController.getPolygonTypeByParameter(parameter);
		return polygonTypeList;
	}
	
}
