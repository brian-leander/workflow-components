package dk.dmi.lib.workflow.component.polygon;

import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.ParameterController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonType;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.TimeResolution;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get Polygon Parameters", 
		category = "Polygon",
		description = "Get a list of parameters, specified by polygon type, time resolution and element number. Only one argument can be null",
        version = 1)
public class GetPolygonParameters extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Polygon type (Use null to ignore)", "Time resolution (Use null to ignore)", "Element number (Use null to ignore)"}, 
			returnDescription = "List of parameters matching criteria")
	public List<Parameter> execute(PolygonType polygonType, TimeResolution timeResolution, Integer elementNumber) throws ClassNotFoundException {
		ParameterController parameterController = ParameterController.getInstance();
		List<Parameter> parameters = null;
		
		if(polygonType == null) {
			parameters = parameterController.getParametersByTimeResolutionAndElementNumber(timeResolution, elementNumber.intValue());
		} else if(timeResolution == null) {
			parameters = parameterController.getParametersByPolygonTypeAndElementNumber(polygonType, elementNumber.intValue());
		} else if(elementNumber == null) {
			parameters = parameterController.getParametersByPolygonTypeAndTimeResolution(polygonType, timeResolution);
		} else {
			parameters = parameterController.getParametersByPolygonTypeAndTimeResolutionAndElementNumber(polygonType, timeResolution, elementNumber.intValue());
		}
		
		return parameters;
	}
}
