package dk.dmi.lib.workflow.component.polygon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonValueController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonArea;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonValue;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get polygon area/values map by areas and parameter", 
		category = "Polygon",
		description = "Get a map with key=polygonAreaId and value=List<PolygonValue>, defined by a list of polygon areas, parameter and time observation interval.",
        version = 1)
public class GetPolygonAreaAndValueMapByAreasAndParameter extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Polygon area list to filter by", "Parameter to filter by", "From date to filter by", "To date to filter by"}, 
			returnDescription = "Map of polygon area id's and List of polygon values")
	public Map<Long, List<PolygonValue>> execute(List<PolygonArea> polygonAreas, Parameter parameter, Date from, Date to) {
		PolygonValueController polygonValueController = PolygonValueController.getInstance();
		List<Object[]> polygonAreaAndValueList = polygonValueController.getPolygonAreaIdAndValuesByPolygonAreaAndParameterAndTimeObsInterval(polygonAreas, parameter, from, to);
		Map<Long, List<PolygonValue>> polygonAreaIdAndValueMap = createPolygonAreaValueMap(polygonAreaAndValueList);
		return polygonAreaIdAndValueMap;
	}

	Map<Long, List<PolygonValue>> createPolygonAreaValueMap(List<Object[]> polygonAreaAndValueList) {
		Map<Long, List<PolygonValue>> polygonAreaIdAndValueMap = new HashMap<Long, List<PolygonValue>>();
		
		for (Object[] objects : polygonAreaAndValueList) {
			PolygonArea polygonArea = (PolygonArea) objects[0];
			PolygonValue polygonValue = (PolygonValue) objects[1];
			List<PolygonValue> polygonValues = polygonAreaIdAndValueMap.get(polygonArea.getId());
			
			if(polygonValues == null) {
				polygonValues = new ArrayList<PolygonValue>();
				polygonAreaIdAndValueMap.put(polygonArea.getId(), polygonValues);
			}
			
			polygonValues.add(polygonValue);
		}
		
		return polygonAreaIdAndValueMap;
	}
	
}
