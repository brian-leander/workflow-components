package dk.dmi.lib.workflow.component.polygon;

import java.util.List;
import dk.dmi.lib.common.GeneralUtils;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonAreaController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonArea;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonType;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get polygon areas by type and names", 
		category = "Polygon",
		description = "Get a list of polygon areas defined by polygon type and a comma seperated list of area names.",
        version = 1)
public class GetPolygonAreasByTypeAndNames extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Polygon type to filter by", "Comma seperated text of polygon area names to filter by (use null to ignore filter)"}, 
			returnDescription = "List of polygon types")
	public List<PolygonArea> execute(PolygonType polygonType, String names) throws ClassNotFoundException {
		PolygonAreaController polygonAreaController = PolygonAreaController.getInstance();
		List<PolygonArea> polygonAreaList = null;
		
		if(names != null && !names.trim().equals("")) {
			List<String> nameList = GeneralUtils.splitStringIntoStringList(names, ",");
			polygonAreaList = polygonAreaController.getPolygonAreaByTypeAndNames(polygonType, nameList);
		} else {
			polygonAreaList = polygonAreaController.getPolygonAreasByType(polygonType);
		}
		
		return polygonAreaList;
	}
	
}
