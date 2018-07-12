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
		name = "Get polygon parameter area values nested map", 
		category = "Polygon",
		description = "Get a nested map of parameters -> polygon areas -> polygon values, defined by parameters, polygon areas, and time observation interval.",
		version = 1)
public class GetPolygonParameterAreaValueMapByAreasAndParameters extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Polygon area list to filter by", "Parameter to filter by", "From date to filter by", "To date to filter by"}, 
			returnDescription = "Nested map of parameters, polygon areas and List of polygon values")
	public Map<Parameter, HashMap<PolygonArea, List<PolygonValue>>> execute(List<Parameter> parameters, List<PolygonArea> polygonAreas, Date from, Date to) {
		PolygonValueController polygonValueController = PolygonValueController.getInstance();
		
		List<PolygonValue> polygonValueList = polygonValueController.getValuesByParametersAndAreasAndTimeObsInterval(parameters, polygonAreas, from, to);
		Map<Parameter, HashMap<PolygonArea, List<PolygonValue>>> parameterAreaValuesMap = createParameterAreaValueNestedList(polygonValueList);
		
		return parameterAreaValuesMap;
	}
	
	Map<Parameter, HashMap<PolygonArea, List<PolygonValue>>> createParameterAreaValueNestedList(List<PolygonValue> polygonValueList) {
		Map<Parameter, HashMap<PolygonArea, List<PolygonValue>>> parameterAreaValuesMap = new HashMap<Parameter, HashMap<PolygonArea, List<PolygonValue>>>();
		
		for (PolygonValue polygonValue : polygonValueList) {
			Parameter parameter =  polygonValue.getParameter();
			PolygonArea polygonArea = polygonValue.getPolygonArea();
			
			HashMap<PolygonArea, List<PolygonValue>> areaValueMap = parameterAreaValuesMap.get(parameter);
			
			if(areaValueMap == null) {
				areaValueMap = new HashMap<PolygonArea, List<PolygonValue>>();
				parameterAreaValuesMap.put(parameter, areaValueMap);
			}
			
			List<PolygonValue> polygonValues = areaValueMap.get(polygonArea);
			
			if(polygonValues == null) {
				polygonValues = new ArrayList<PolygonValue>();
				areaValueMap.put(polygonArea, polygonValues);
			}
			
			polygonValues.add(polygonValue);
		}
		
		return parameterAreaValuesMap;
	}
	
}
