package dk.dmi.lib.workflow.component.polygon;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonArea;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonValue;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;
import dk.dmi.lib.workflow.component.convert.Point2DListToText;
import dk.dmi.lib.workflow.component.convert.TransformCoordinatePointList;
import dk.dmi.lib.workflow.component.xml.AppendEntityListToXml;

@Component(
		name = "Generate polygon area values xml", 
		category = "Polygon",
		description = "Generate xml for all specified polygon areas, based on a hard coded xml data structure. This is a combination of components, as part of an optimazation, to carry out a spesific task.",
        version = 1)
public class GeneratePolygonAreaValuesXml extends BaseComponent {
	
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(// PARAMETER, PARAMETER_AREA_ENTRY.getValue(), POLYGON_AREA_COODINATE_MAP, COORDINATE_SYSTEM, COORDINATE_NUMBER_FORMAT
			argumentDescriptions = {"Current parameter", "Map of: key = Polygon area, value = list of polygon values", "Map of: key = polygon area id, value = list of coordinate points", "Coordinate system to use (e.g. WGS84 UTM Zone 32N)", "The number format to use for coordinate points"}, 
			returnDescription = "Xml text for all polygon areas for a given parameter")
	public String execute(Parameter parameter, Map<PolygonArea, List<PolygonValue>> polygonAreaMap, Map<Long, List<Point2D>> polygonAreaCoordinateMap, String coordinateSystem, String coordinateNumberFormat) throws Exception {
		StringBuffer polygonAreaListXmlBuffer = new StringBuffer();

		for (Map.Entry<PolygonArea, List<PolygonValue>> polygonAreaEntry : polygonAreaMap.entrySet()) {
			PolygonArea polygonArea = polygonAreaEntry.getKey();
			String polygonAreaCoodinateText = "[]";
			List<Point2D> polygonAreaCoodinatePoints = polygonAreaCoordinateMap.get(polygonArea.getId());
			
			if(polygonAreaCoodinatePoints != null) {
				if(coordinateSystem.equals("GEO")) {
					polygonAreaCoodinatePoints = new TransformCoordinatePointList().execute(polygonAreaCoodinatePoints, "WGS84 UTM Zone 32N", "ETRS89 Geo");
				}
				
				polygonAreaCoodinateText = "[" + (new Point2DListToText().execute(polygonAreaCoodinatePoints, "][", ",", coordinateNumberFormat)) + "]";
			}
			
			List<PolygonValue> polygonValueList = polygonAreaEntry.getValue();
			List<PolygonValue> polygonValueCloneList = new ArrayList<PolygonValue>();
			
			if(parameter.getId() == 11) {
				for (PolygonValue polygonValue : polygonValueList) {
					PolygonValue polygonValueClone = new PolygonValue();
					polygonValueClone.setValue((float)((polygonValue.getValue()*3600.0)/1000000.0));
					polygonValueClone.setTimeObs(polygonValue.getTimeObs());
					polygonValueCloneList.add(polygonValueClone);
				}
				
				polygonValueList = polygonValueCloneList;
			}
			
			String polygonAreaName = polygonArea.getName();
			String customRootElement = "<polygonArea name=\""+polygonAreaName+"\" coordinates=\""+polygonAreaCoodinateText+"\">";
			String xmlDataStructure = "<?xml-multiple polygonValue?>\n\r<polygonValue>\n\r<value/>\n\r<timeObs/>\n\r</polygonValue>";
			
			AppendEntityListToXml appendEntityListToXml = new AppendEntityListToXml();
			appendEntityListToXml.injectContext(workflowContextController);
			polygonAreaListXmlBuffer.append(appendEntityListToXml.execute(polygonValueList, true, false, true, customRootElement, xmlDataStructure));
		}
		
		return polygonAreaListXmlBuffer.toString();
	}
	
}
