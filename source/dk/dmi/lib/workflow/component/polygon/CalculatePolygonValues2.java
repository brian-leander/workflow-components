package dk.dmi.lib.workflow.component.polygon;

import java.util.Date;
import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonType;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonTypeParameter;
import dk.dmi.lib.polygon.logic.PolygonValueCalculator;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Calculate Polygon Values", 
		category = "Polygon",
		description = "Calculates polygon data values for specified polygon type, parameter and datetime interval.",
        version = 2)
public class CalculatePolygonValues2 extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Location of the interpolation folder for the parameter grid value files, e.g. (Y:\\\\) or (\\\\vortex.dmi.dk\\tkdata\\)", "Polygon type to calculate values for", 
					"Parameter to calculate values for. If parameter does exist for polygon type, an exception is thrown. If parameter is null, all parameters for that polygon type is calculated.", 
					"Start date of calculation interval", "End date of calculation interval", "Commit interval between grid files (use null for never)", "Email to send progress noptifications to (use null to ignore)", "Should an exception be thrown if the grid file is missing or empty"})
    public void execute(String location, PolygonType polygonType, Parameter parameter, Date beginDateTime, Date endDateTime, Long commitInterval, String notificationEmail, boolean throwExceptionWhenMissingGridFile) throws Exception {
		PolygonValueCalculator polygonValueCalculator = new PolygonValueCalculator();
		List<PolygonTypeParameter> polygonTypeParameters = polygonType.getPolygonTypeParameters();
		
		if(parameter == null) {
			calculatePolygonValuesForAllPolygonTypeParameters(location, polygonType, beginDateTime, endDateTime, polygonValueCalculator, polygonTypeParameters, commitInterval, notificationEmail, throwExceptionWhenMissingGridFile);
		} else if(polygonTypeParametersContainParameter(polygonTypeParameters, parameter)) {
			polygonValueCalculator.calculatePolygonValuesForParameterInDateTimeInterval(location, polygonType, parameter, beginDateTime, endDateTime, commitInterval, notificationEmail, throwExceptionWhenMissingGridFile);
		} else {
			throw new Exception("Wrong parameter is passed in component CalculatePolygonValues");
		}
    }

	void calculatePolygonValuesForAllPolygonTypeParameters(String drive, PolygonType polygonType, Date beginDateTime, Date endDateTime, PolygonValueCalculator polygonValueCalculator, List<PolygonTypeParameter> polygonTypeParameters, Long commitInterval, String notificationEmail, boolean throwExceptionWhenMissingGridFile) throws Exception {
		for (PolygonTypeParameter polygonTypeParameter : polygonTypeParameters) {
			Parameter currentParameter = polygonTypeParameter.getParameter();
			polygonValueCalculator.calculatePolygonValuesForParameterInDateTimeInterval(drive, polygonType, currentParameter, beginDateTime, endDateTime, commitInterval, notificationEmail, throwExceptionWhenMissingGridFile);
		}
	}
	
	boolean polygonTypeParametersContainParameter(List<PolygonTypeParameter> polygonTypeParameters, Parameter parameter) {
		for (PolygonTypeParameter polygonTypeParameter : polygonTypeParameters) {
			Parameter currentParameter = polygonTypeParameter.getParameter();
			
			if(currentParameter.getId() == parameter.getId()) {
				return true;
			}
		}
		
		return false;
	}
	
}
