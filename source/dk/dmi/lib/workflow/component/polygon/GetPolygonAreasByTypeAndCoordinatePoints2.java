package dk.dmi.lib.workflow.component.polygon;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dk.dmi.lib.persistence.database.climadb.polygon.controller.GridCellController;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonAreaController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridCell;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonArea;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonType;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Deprecated // Use GetPolygonAreasByTypeAndCoordinatePointsOrAreaNames instead
@Component(
		name = "Get polygon areas by type and coordinate points", 
		category = "Polygon",
		description = "Get the polygon areas for a given polygon type and assosiated point list, that covers the specified list of eastings and northings coordinate points where x = eastings and y = northings. "
				+ "The grid cell covering the coordinates is used to find the polygon areas, all areas that use the same grid cell are returned. If no coordinate points are proveded, all polygon areas are returned. "
				+ "If there are coordinate points provided, a map with key = polygon area id and value = point list, will be injected into context with the name POLYGON_AREA_COODINATE_MAP; format: (Long, List<Point2D>>). The map will not be persisted.",
        version = 2)
public class GetPolygonAreasByTypeAndCoordinatePoints2 extends BaseComponent {
	static final String POLYGON_AREA_COODINATE_MAP_KEY = "POLYGON_AREA_COODINATE_MAP";
	static final double MAX_GRID_CELL_RADIUS = 8000.0;
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDescriptions = {"The polygon type to filter by", "List of Point2D.Double repesenting eastings northings coordinates"},
			returnDescription = "A list of polygon areas, covering the coordinates")
    public List<PolygonArea> execute(PolygonType polygonType, List<Point2D> eastingsNorthingsPoints) {
		List<PolygonArea> polygonAreaList = null;
		
		if(eastingsNorthingsPoints != null) {
			if(eastingsNorthingsPoints.size() < 20) {
				polygonAreaList = getPolygonAreaMapByTypeAndCoordinatePoints(polygonType, eastingsNorthingsPoints);
			} else {
				polygonAreaList = getPolygonAreaMapByTypeAndCoordinatePointsManyPoints(polygonType, eastingsNorthingsPoints);
			}
			
			tryAddMissingPoints(polygonType, polygonAreaList, eastingsNorthingsPoints);
		} else {
			polygonAreaList = getPolygonAreaMapByType(polygonType);
		}
		
		return polygonAreaList;
    }
	
	// ##################################################################################################################################################################
	
	List<PolygonArea> getPolygonAreaMapByType(PolygonType polygonType) {
		PolygonAreaController polygonAreaController = PolygonAreaController.getInstance();
		List<PolygonArea> polygonAreaList = polygonAreaController.getPolygonAreasByType(polygonType);
		workflowContextController.addObjectToContext(POLYGON_AREA_COODINATE_MAP_KEY, new HashMap<Long, List<Point2D>>(), false);
		return polygonAreaList;
	}
	
	// ##################################################################################################################################################################
	
	List<PolygonArea> getPolygonAreaMapByTypeAndCoordinatePoints(PolygonType polygonType, List<Point2D> eastingsNorthingsPoints) {
		List<PolygonArea> polygonAreaList = new ArrayList<PolygonArea>();
		Map<Long, List<Point2D>> polygonAreaCoordinateMap = new HashMap<Long, List<Point2D>>();
		PolygonAreaController polygonAreaController = PolygonAreaController.getInstance();
		
		for (Point2D eastingsNorthingsPoint : eastingsNorthingsPoints) {
			double eastings = eastingsNorthingsPoint.getX();
			double northings = eastingsNorthingsPoint.getY();
			
			List<PolygonArea> currentPolygonAreaList = polygonAreaController.getPolygonAreaByTypeAndCoordinates(polygonType, eastings, northings);
			
			for (PolygonArea currentPolygonArea : currentPolygonAreaList) {
				addPolygonAreaAndPoint(currentPolygonArea, polygonAreaList, eastingsNorthingsPoint, polygonAreaCoordinateMap);
			}
		}
		
		workflowContextController.addObjectToContext(POLYGON_AREA_COODINATE_MAP_KEY, polygonAreaCoordinateMap, false);
		return polygonAreaList;
	}
	
	void addPolygonAreaAndPoint(PolygonArea currentPolygonArea, List<PolygonArea> polygonAreaList, Point2D eastingsNorthingsPoint, Map<Long, List<Point2D>> polygonAreaCoordinateMap) {
		List<Point2D> currentPointList = polygonAreaCoordinateMap.get(currentPolygonArea.getId());
		
		if(currentPointList == null) {
			polygonAreaList.add(currentPolygonArea);
			currentPointList = new ArrayList<Point2D>();
			polygonAreaCoordinateMap.put(currentPolygonArea.getId(), currentPointList);
		}
		
		currentPointList.add(eastingsNorthingsPoint);
	}
	
	// ##################################################################################################################################################################
	
	List<PolygonArea> getPolygonAreaMapByTypeAndCoordinatePointsManyPoints(PolygonType polygonType, List<Point2D> eastingsNorthingsPoints) {
		Map<Long, GridCellPoint> gridCellPointMap = createGridCellPointMap(polygonType, eastingsNorthingsPoints);
		List<GridCell> gridCellFilteredList = createGridCellFilteredList(gridCellPointMap);
		
		PolygonAreaController polygonAreaController = PolygonAreaController.getInstance();
		List<Object[]> polygonAreaGridAndCellList = polygonAreaController.getPolygonAreaAndGridCellListsByTypeAndGridCells(polygonType, gridCellFilteredList);

		List<PolygonArea> polygonAreaList = new ArrayList<PolygonArea>();
		Map<Long, List<Point2D>> polygonAreaCoordinateMap = new HashMap<Long, List<Point2D>>();
		
		for (Object[] polygonAreaGridAndCell : polygonAreaGridAndCellList) {
			PolygonArea currentPolygonArea = (PolygonArea) polygonAreaGridAndCell[0];
			GridCell currentGridCell = (GridCell) polygonAreaGridAndCell[1];
			List<Point2D> currentPointList = polygonAreaCoordinateMap.get(currentPolygonArea.getId());
			
			if(currentPointList == null) {
				polygonAreaList.add(currentPolygonArea);
				currentPointList = new ArrayList<Point2D>();
				polygonAreaCoordinateMap.put(currentPolygonArea.getId(), currentPointList);
			}
			
			currentPointList.addAll(gridCellPointMap.get(currentGridCell.getId()).getPointList());
		}
		
		workflowContextController.addObjectToContext(POLYGON_AREA_COODINATE_MAP_KEY, polygonAreaCoordinateMap, false);
		return polygonAreaList;
	}

	Map<Long, GridCellPoint> createGridCellPointMap(PolygonType polygonType, List<Point2D> eastingsNorthingsPoints) {
		GridCellController gridCellController = GridCellController.getInstance();
		List<GridCell> gridCellList = gridCellController.getGridCellsByPolygonType(polygonType);
		Map<Long, GridCellPoint> gridCellPointMap = new HashMap<Long, GridCellPoint>();
		
		for (Point2D point : eastingsNorthingsPoints) {
			double pointEastings = point.getX();
			double pointNorthings = point.getY();
			
			double minEastings = pointEastings - 500.0;
			double maxEastings = pointEastings + 500.0;
			double minNothings = pointNorthings - 500.0;
			double maxNothings = pointNorthings + 500.0;
			
			for (GridCell gridCell : gridCellList) {
				Integer gridCellEastings = gridCell.getEastings();
				Integer gridCellNorthings = gridCell.getNorthings();
				
				if(gridCellEastings >= minEastings && gridCellEastings < maxEastings && gridCellNorthings >= minNothings && gridCellNorthings < maxNothings) {
					GridCellPoint currentGridCellPoint = gridCellPointMap.get(gridCell.getId());
					
					if(currentGridCellPoint == null) {
						currentGridCellPoint = new GridCellPoint(gridCell);
						gridCellPointMap.put(gridCell.getId(), currentGridCellPoint);
					}
					
					currentGridCellPoint.addPoint(point);
				}
			}
		}
		
		return gridCellPointMap;
	}
	
	List<GridCell> createGridCellFilteredList(Map<Long, GridCellPoint> gridCellPointMap) {
		List<GridCell> gridCellList = new ArrayList<GridCell>();
		List<GridCellPoint> gridCellPointList = new ArrayList<GridCellPoint>(gridCellPointMap.values());
		
		for (GridCellPoint gridCellPoint : gridCellPointList) {
			gridCellList.add(gridCellPoint.getGridCell());
		}
		
		return gridCellList;
	}
	
	private class GridCellPoint {
		private GridCell gridCell = null;
		private List<Point2D> pointList = null;
		
		public GridCellPoint(GridCell gridCell) {
			this.gridCell = gridCell;
			this.pointList = new ArrayList<Point2D>();
		}
		
		public GridCell getGridCell() {
			return gridCell;
		}
		
		public List<Point2D> getPointList() {
			return pointList;
		}
		
		public void addPoint(Point2D point) {
			pointList.add(point);
		}
	}
	
	// ##################################################################################################################################################################
	
	void tryAddMissingPoints(PolygonType polygonType, List<PolygonArea> polygonAreaList, List<Point2D> eastingsNorthingsPoints) {
		@SuppressWarnings("unchecked")
		Map<Long, List<Point2D>> polygonAreaCoordinateMap = (Map<Long, List<Point2D>>) workflowContextController.getObjectForKey(POLYGON_AREA_COODINATE_MAP_KEY);
		
		for (Point2D point : eastingsNorthingsPoints) {
			boolean isPointMissing = isPointMissingFromPointMap(point, polygonAreaCoordinateMap);
			
			if(isPointMissing) {
				PolygonAreaController polygonAreaController = PolygonAreaController.getInstance();
				PolygonArea nearestPolygonArea = polygonAreaController.getNearestPolygonAreaByTypeAndCoordinatesAndMaxRadius(polygonType, point.getX(), point.getY(), MAX_GRID_CELL_RADIUS);
				
				if(nearestPolygonArea != null) {
					addPolygonAreaAndPoint(nearestPolygonArea, polygonAreaList, point, polygonAreaCoordinateMap);
				}
			}
		}
	}
	
	boolean isPointMissingFromPointMap(Point2D point, Map<Long, List<Point2D>> polygonAreaCoordinateMap) {
		Iterator<Entry<Long, List<Point2D>>> polygonAreaCoordinateMapIterator = polygonAreaCoordinateMap.entrySet().iterator();
		
		while (polygonAreaCoordinateMapIterator.hasNext()) {
			Map.Entry<Long, List<Point2D>> polygonAreaCoordinatePairs = (Map.Entry<Long, List<Point2D>>) polygonAreaCoordinateMapIterator.next();
			List<Point2D> coordinateList = polygonAreaCoordinatePairs.getValue();
			
			for (Point2D coordinate : coordinateList) {
				
				if(coordinate.getX() == point.getX() && coordinate.getY() == point.getY()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	// ##################################################################################################################################################################

}
