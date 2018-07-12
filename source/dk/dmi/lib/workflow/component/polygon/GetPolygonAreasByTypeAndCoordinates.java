package dk.dmi.lib.workflow.component.polygon;

import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonAreaController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonArea;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonType;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get polygon areas by type and coordinates", 
		category = "Polygon",
		description = "Get the polygon areas, for a given polygon type, that covers the specified eastings and northings coordinates. "
				+ "The grid cell covering the coordinates is used to find the polygon areas, all areas that use the same grid cell are returned.",
        version = 1)
public class GetPolygonAreasByTypeAndCoordinates extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"The polygon type to filter by", "Eastings coodinates", "Northings coodinates"},
			returnDescription = "A list of polygon areas covering the coodrdinates")
    public List<PolygonArea> execute(PolygonType polygonType, Double eastings, Double northings) {
		PolygonAreaController polygonAreaController = PolygonAreaController.getInstance();
		List<PolygonArea> polygonAreas = polygonAreaController.getPolygonAreaByTypeAndCoordinates(polygonType, eastings, northings);
		return polygonAreas;
    }

}
