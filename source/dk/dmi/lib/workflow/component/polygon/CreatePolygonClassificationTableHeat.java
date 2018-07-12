package dk.dmi.lib.workflow.component.polygon;

import java.util.Date;
import java.util.List;
import java.util.Map;
import dk.dmi.lib.common.GeneralUtils;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonTypeController;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonValueController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonType;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonValue;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonValuePayload;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Create Polygon Classification Table Heat", 
		category = "Polygon",
		description = "Generate heat classification html tables for the given parameter, day and polygon type.",
        version = 1)
public class CreatePolygonClassificationTableHeat extends BaseComponent {
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
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_MAP_CHECK_BOX, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Parameter instance to used for generating table", "Time observation", "Generate tables for the following polygons", "Procent % polygon area that must be covered for warm wave", "Procent % polygon area that must be covered for heat wave"}, 
			returnDescription = "Html text of table data")
	public String execute(Parameter parameter, Date timeObs, String polygonNames, int warmWaveAreaProcent, int heatWaveAreaProcent) {
		Map<String, Boolean> polygonNameMap = GeneralUtils.createBooleanMapFromString(polygonNames, ";", ",");
		String htmlTable = createHtmlData(polygonNameMap,  parameter, timeObs, warmWaveAreaProcent, heatWaveAreaProcent);
		return htmlTable;
	}
	
	String createHtmlData(Map<String, Boolean> polygonNameMap, Parameter parameter, Date timeObs, int warmWaveAreaProcent, int heatWaveAreaProcent) {
		String htmlData = "";
		htmlData += createPolygonTables(polygonNameMap, parameter, timeObs, warmWaveAreaProcent, heatWaveAreaProcent);
		return htmlData;
	}
	
	String createPolygonTables(Map<String, Boolean> polygonNameMap, Parameter parameter, Date timeObs, int warmWaveAreaProcent, int heatWaveAreaProcent) {
		String htmlData = "";
		
		for (Map.Entry<String, Boolean> entry : polygonNameMap.entrySet()) {
			if(entry.getValue().booleanValue()) {
				String polygonName = entry.getKey();
				htmlData += "<h3>"+polygonName+"</h3>";
				htmlData += "<table class=\"fixed\" id=\""+polygonName+"\" border=\"1\"><col width=\"50%\" /><tbody>";
				htmlData += createTableRow("Område,Varmebølge,Hedebølge", "#B0B0B0", polygonName);
				htmlData += createPolygonRows(polygonName, parameter, timeObs, warmWaveAreaProcent, heatWaveAreaProcent);
				htmlData += "</tbody></table>";
				htmlData += "<br/>";
			}
		}
		
		return htmlData;
	}
	
	String createPolygonRows(String polygonName, Parameter parameter, Date timeObs, int warmWaveAreaProcent, int heatWaveAreaProcent) {
		String htmlData = "";
		
		PolygonTypeController polygonTypeController = PolygonTypeController.getInstance();
		PolygonType polygonType = polygonTypeController.getPolygonTypeByName(polygonName);
		
		PolygonValueController polygonValueController = PolygonValueController.getInstance();
		List<PolygonValue> polygonValues = polygonValueController.getPolygonValuesByPolygonTypeAndParameterAndTimeObsInterval(polygonType, parameter, timeObs, timeObs);
		
		for (PolygonValue polygonValue : polygonValues) {
			// get polygon area name
			String areaName = polygonValue.getPolygonArea().getName();
			
			// get two land percent coverage by heat
			List<PolygonValuePayload> polygonValuePayloads = polygonValue.getPolygonValuePayloads();
			float landProcentHitByWarmWave = 0;
			float landProcentHitByHeatWave = 0;
			
			for (PolygonValuePayload polygonValuePayload : polygonValuePayloads) {
				int polygonValuePayName = (int) Double.parseDouble(polygonValuePayload.getName());
				
				if(polygonValuePayName > 0) {
					landProcentHitByWarmWave += Math.round(polygonValuePayload.getValue());
					
					if(polygonValuePayName > 1) {
						landProcentHitByHeatWave += Math.round(polygonValuePayload.getValue());
					}
				}
			}
			
			htmlData += createTableRow(areaName+","+((int) landProcentHitByWarmWave)+" %,"+((int) landProcentHitByHeatWave)+" %", null, null);
		}
		
		
		return htmlData;
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
