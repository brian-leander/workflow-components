package dk.dmi.lib.workflow.component.polygon;

import java.util.Date;
import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonValueController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonArea;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonValue;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get polygon values by area and parameter", 
		category = "Polygon",
		description = "Get a list of polygon values defined by polygon area, parameter and time observation interval.",
        version = 1)
public class GetPolygonValuesByAreaAndParameter extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Polygon area to filter by", "Parameter to filter by", "From date to filter by", "To date to filter by"}, 
			returnDescription = "List of polygon types")
	public List<PolygonValue> execute(PolygonArea polygonArea, Parameter parameter, Date from, Date to) throws ClassNotFoundException {
		PolygonValueController polygonValueController = PolygonValueController.getInstance();
		List<PolygonValue> polygonValueList = polygonValueController.getPolygonValuesByPolygonAreaAndParameterAndTimeObsInterval(polygonArea, parameter, from, to);
		return polygonValueList;
	}
	
}
