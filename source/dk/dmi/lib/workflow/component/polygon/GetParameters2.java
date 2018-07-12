package dk.dmi.lib.workflow.component.polygon;

import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.ParameterController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get Parameters", 
		category = "Polygon",
		description = "Get a list of parameters, specified by time resolutions and element numbers. List is returned ordered by element number.",
        version = 2)
public class GetParameters2 extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Comma seperated list of time resolutions (e.g. HOUR,DAY,MONTH,YEAR) (Use null to fetch all)", "Comma seperated list of element numbers. (Use null to fetch all)"}, 
			returnDescription = "List of parameters matching criteria")
	public List<Parameter> execute(String timeResolutions, String elementNumbers) {
		ParameterController parameterController = ParameterController.getInstance();
		List<Parameter> parameters = parameterController.getParametersByTimeResolutionsAndElementNumbers(timeResolutions, elementNumbers);
		return parameters;
	}
	
}
