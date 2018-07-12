package dk.dmi.lib.workflow.component.polygon;

import java.util.Date;
import java.util.List;
import java.util.Map;

import dk.dmi.lib.common.GeneralUtils;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.ParameterController;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonTypeController;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonValueController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonArea;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonType;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonValue;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonValuePayload;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Create Polygon Classification Table Storm", 
		category = "Polygon",
		description = "Generate storm classification html tables for the given parameter, day and polygon type. Classification intervals are configured as a map of comma seperated key values and pairs seperated by simicolon.",
        version = 1)
public class CreatePolygonClassificationTableStorm extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "2")
	public String[] getListOfEntityNames(String ignore) {
		PolygonTypeController polygonTypeController = PolygonTypeController.getInstance();
		List<PolygonType> polygonTypes = polygonTypeController.getAllPolygonTypes();
		String[] polygonTypeNames = new String[polygonTypes.size()];
		int outerIndexCounter = 3;
		
		for (int i = 0; i < polygonTypes.size(); i++) {
			PolygonType polygonType = polygonTypes.get(i);
			
			if(polygonType.getId() == PolygonTypeController.DENMARK) {
				polygonTypeNames[0] = polygonType.getName();
			} else if(polygonType.getId() == PolygonTypeController.REGION) {
				polygonTypeNames[1] = polygonType.getName();
			} else if(polygonType.getId() == PolygonTypeController.MUNICIPALITY) {
				polygonTypeNames[2] = polygonType.getName();
			} else {
				polygonTypeNames[outerIndexCounter++] = polygonType.getName();
			}
		}
		
		return polygonTypeNames;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_MAP_CHECK_BOX, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_AREA_MEDIUM},
			argumentDescriptions = {"Parameter instance to used for generating table", "Time observation", "Generate tables for the following polygons", "Classification values (highest threshold first), i.e. value1, >= threshold1 ; value2, >= threshold2 ; value3, >= threshold3"}, 
			returnDescription = "Html text of table data")
	public String execute(Parameter parameter, Date timeObs, String polygonNames, String classificationIntervals) {
		classificationIntervals = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), classificationIntervals);
		
		Map<String, Boolean> polygonNameMap = GeneralUtils.createBooleanMapFromString(polygonNames, ";", ",");
		Map<String, Integer> classificationIntervalMap = GeneralUtils.createIntegerMapFromString(classificationIntervals, ";", ",");
		
		String htmlTable = createHtmlData(polygonNameMap,  parameter, timeObs, classificationIntervalMap);
		
		return htmlTable;
		
	}
	
	String createHtmlData(Map<String, Boolean> polygonNameMap, Parameter parameter, Date timeObs, Map<String, Integer> classificationIntervalMap) {
		String htmlData = "";
		htmlData += createPolygonTables(polygonNameMap, parameter, timeObs, classificationIntervalMap);
		return htmlData;
	}
	
	String createPolygonTables(Map<String, Boolean> polygonNameMap, Parameter parameter, Date timeObs, Map<String, Integer> classificationIntervalMap) {
		String htmlData = "";
		
		for (Map.Entry<String, Boolean> entry : polygonNameMap.entrySet()) {
			if(entry.getValue().booleanValue()) {
				String polygonName = entry.getKey();
				htmlData += "<h3>"+polygonName+"</h3>";
				htmlData += "<table class=\"fixed\" id=\""+polygonName+"\" border=\"1\"><col width=\"50%\" /><tbody>";
				htmlData += createTableRow("Område,Stormklasse,Landareal storm, Landareal stærk storm, Landareal orkan, Højeste 10-Min vind, Højeste vindstød", "#B0B0B0", polygonName);
				htmlData += createPolygonRows(polygonName, parameter, timeObs, classificationIntervalMap);
				htmlData += "</tbody></table>";
				htmlData += "<br/>";
			}
		}
		
		return htmlData;
	}
	
	String createPolygonRows(String polygonName, Parameter parameter, Date timeObs, Map<String, Integer> classificationIntervalMap) {
		String htmlData = "";
		
		PolygonTypeController polygonTypeController = PolygonTypeController.getInstance();
		PolygonType polygonType = polygonTypeController.getPolygonTypeByName(polygonName);
		
		PolygonValueController polygonValueController = PolygonValueController.getInstance();
		List<PolygonValue> polygonValues = polygonValueController.getPolygonValuesByPolygonTypeAndParameterAndTimeObsInterval(polygonType, parameter, timeObs, timeObs);
		
		for (PolygonValue polygonValue : polygonValues) {
			// get polygon area name
			String areaName = polygonValue.getPolygonArea().getName();
			
			// get three land percent coverage by storm
			List<PolygonValuePayload> polygonValuePayloads = polygonValue.getPolygonValuePayloads();
			float landProcentHitByStorm = 0;
			float landProcentHitByStrongStorm = 0;
			float landProcentHitByHurricane = 0;
			
			for (PolygonValuePayload polygonValuePayload : polygonValuePayloads) {
				int polygonValuePayName = (int) Double.parseDouble(polygonValuePayload.getName());
				
				if(polygonValuePayName > 0) {
					landProcentHitByStorm += Math.round(polygonValuePayload.getValue());
					
					if(polygonValuePayName > 1) {
						landProcentHitByStrongStorm += Math.round(polygonValuePayload.getValue());
						
						if(polygonValuePayName > 2) {
							landProcentHitByHurricane += Math.round(polygonValuePayload.getValue());
						}
					}
				}
			}
			
			// get polygon area name
			String classification = getClassification(landProcentHitByStorm, landProcentHitByStrongStorm, landProcentHitByHurricane, classificationIntervalMap);
						
			// get wind parameters
			String highest10MinWind = getWindParameterForDay(polygonValue.getPolygonArea(), 20, timeObs);
			String HighestWindSpeed = getWindParameterForDay(polygonValue.getPolygonArea(), 21, timeObs);
			
			htmlData += createTableRow(areaName+","+classification+","+((int) landProcentHitByStorm)+" %,"+((int) landProcentHitByStrongStorm)+" %,"+((int) landProcentHitByHurricane)+" %,"+highest10MinWind+", "+HighestWindSpeed, null, null);
		}
		
		
		return htmlData;
	}
	
	String getWindParameterForDay(PolygonArea polygonArea, long parameterId, Date timeObs) {
		String highest10MinWindForDay = "";
		
		ParameterController parameterController = ParameterController.getInstance();
		Parameter parameter = parameterController.getParameterById(parameterId);
		
		PolygonValueController polygonValueController = PolygonValueController.getInstance();
		List<PolygonValue> PolygonValues = polygonValueController.getPolygonValuesByPolygonAreaAndParameterAndTimeObsInterval(polygonArea, parameter, timeObs, timeObs);
		
		if(PolygonValues.size() > 0) {
			highest10MinWindForDay = PolygonValues.get(0).getValue()+" m/s";
		}
		
		return highest10MinWindForDay;
	}
	
	String getClassification(float landProcentHitByStorm, float landProcentHitByStrongStorm, float landProcentHitByHurricane, Map<String, Integer> classificationIntervalMap) {
		String classification = "---";
		String letter = null;
		
		if(landProcentHitByHurricane > 0) {
			letter = "A";
		} else if(landProcentHitByStrongStorm > 0) {
			letter = "B";
		} else if(landProcentHitByStorm > 0) {
			letter = "C";
		}
		
		if(letter != null) {
			int stormClass = 0;
			
			for (Map.Entry<String, Integer> classificationInterval : classificationIntervalMap.entrySet()) {
				int classificationIntervalKey = Integer.parseInt(classificationInterval.getKey());
				int classificationIntervalValue = classificationInterval.getValue().intValue();
				
				if(landProcentHitByStorm > classificationIntervalValue && classificationIntervalKey > stormClass) {
					stormClass = classificationIntervalKey;
				}
			}
			
			classification = letter+stormClass;
		}
		
		return classification;
	}
	
	String createTableRow(String rowData, String backgroundColor, String tableId) {
		String rowStyle = "";
		
		if(backgroundColor != null) {
			rowStyle = "style=\"background-color: "+backgroundColor+"\"";
		}
		
		String htmlRow = "<tr "+rowStyle+">";
		String[] cellValues = rowData.split(",");
		
		for (int i = 0; i < cellValues.length; i++) {
			String cellValue = cellValues[i];
			
			if(backgroundColor != null) {
				htmlRow += "<th onclick=\"sortTable("+i+", '"+tableId+"')\">"
						+ cellValue
						+ "</th>";
			} else {
				htmlRow += "<td>"
						+ cellValue
						+ "</td>";
			}
		}
		
		htmlRow += "</tr>";
		
		return htmlRow;
	}
	
}
