package dk.dmi.lib.workflow.component.polygon;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonAreaController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonArea;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonType;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Deprecated // Its not possible to use PolygonArea as key, as the object might not always be the same instance (version 2 will return a list of polygon areas and create a map with polygon area id and coordinate points in context, if coordinate list is provided)
@Component(
		name = "Get polygon area map by type and coordinate points", 
		category = "Polygon",
		description = "Get the polygon areas and assosiated points (Map<PolygonArea, List<Point2D>>), for a given polygon type, that covers the specified list of eastings and northings coordinate points where x = eastings and y = northings. "
				+ "The grid cell covering the coordinates is used to find the polygon areas, all areas that use the same grid cell are returned.",
        version = 1)
public class GetPolygonAreasByTypeAndCoordinatePoints extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"The polygon type to filter by", "List of Point2D.Double repesenting eastings northings coordinates"},
			returnDescription = "A map of polygon areas and assosiated points, covering the coordinates")
    public Map<PolygonArea, List<Point2D>> execute(PolygonType polygonType, List<Point2D> eastingsNorthingsPoints) {
		Map<PolygonArea, List<Point2D>> polygonAreaMap = null;
		
		if(eastingsNorthingsPoints != null) {
			polygonAreaMap = getPolygonAreaMapByTypeAndCoordinatePoints(polygonType, eastingsNorthingsPoints);
		} else {
			polygonAreaMap = getPolygonAreaMapByType(polygonType);
		}
		
		return polygonAreaMap;
    }

	Map<PolygonArea, List<Point2D>> getPolygonAreaMapByTypeAndCoordinatePoints(PolygonType polygonType, List<Point2D> eastingsNorthingsPoints) {
		Map<PolygonArea, List<Point2D>> polygonAreaMap = new HashMap<PolygonArea, List<Point2D>>();
		PolygonAreaController polygonAreaController = PolygonAreaController.getInstance();
		
		for (Point2D eastingsNorthingsPoint : eastingsNorthingsPoints) {
			double eastings = eastingsNorthingsPoint.getX();
			double northings = eastingsNorthingsPoint.getY();
			
			List<PolygonArea> currentPolygonAreaList = polygonAreaController.getPolygonAreaByTypeAndCoordinates(polygonType, eastings, northings);
			
			for (PolygonArea currentPolygonArea : currentPolygonAreaList) {
				List<Point2D> currentPointList = polygonAreaMap.get(currentPolygonArea);
				
				if(currentPointList == null) {
					currentPointList = new ArrayList<Point2D>();
					polygonAreaMap.put(currentPolygonArea, currentPointList);
				}
				
				currentPointList.add(eastingsNorthingsPoint);
			}
		}
		
		return polygonAreaMap;
	}
	
	Map<PolygonArea, List<Point2D>> getPolygonAreaMapByType(PolygonType polygonType) {
		Map<PolygonArea, List<Point2D>> polygonAreaMap = new HashMap<PolygonArea, List<Point2D>>();
		PolygonAreaController polygonAreaController = PolygonAreaController.getInstance();
		List<PolygonArea> polygonAreaList = polygonAreaController.getPolygonAreasByType(polygonType);
		
		for (PolygonArea currentPolygonArea : polygonAreaList) {
			polygonAreaMap.put(currentPolygonArea, new ArrayList<Point2D>());
		}
		
		return polygonAreaMap;
	}
	
}
