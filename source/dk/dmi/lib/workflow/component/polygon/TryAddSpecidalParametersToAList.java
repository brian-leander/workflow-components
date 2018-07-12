package dk.dmi.lib.workflow.component.polygon;

import java.util.List;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import java.util.ArrayList;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.ParameterController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.TimeResolution;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Try add special parameters to a list",
		category = "Polygon",
		description = "Tries to add all special parameters to the list specified by listName. "
				+ "If the parameter is a special parameter, it is added to the list. If the parameters grid is ground for a special parameter, "
				+ "that special parameters is added to the list. The same special parameter can only be added once.",
        version = 1)
public class TryAddSpecidalParametersToAList extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDescriptions = {"Name of the special parameter list", "Parameter to check", "Should list be persisted"}, 
			returnDescription = "True if the given parameter was a special parameter.")
	public boolean execute(String listName, Parameter parameter, boolean persistData) throws Exception {
		List<Parameter> prameterList = getParameterList(listName, persistData);
		boolean isParameterSpecial = addSpecialParameterToListBasedOnParameter(prameterList, parameter);
		return isParameterSpecial;
    }
	
	@SuppressWarnings("unchecked")
	List<Parameter> getParameterList(String listName, boolean persistData) {
		List<Parameter> parameterList = null;
		
		if(listName != null && !listName.trim().equals("")) {
			parameterList = (ArrayList<Parameter>) workflowContextController.getObjectForKey(listName);
		}
		
		if(parameterList == null) {
			parameterList = new ArrayList<Parameter>();
			workflowContextController.addObjectToContext(listName, parameterList, persistData);
		}
		
		return parameterList;
	}
	
	boolean addSpecialParameterToListBasedOnParameter(List<Parameter> prameterList, Parameter parameter) throws Exception {
		boolean isParameterSpecial = false;
		Parameter resultParameter = null;
		int elementNumber = parameter.getElementNumber();
		
		if(elementNumber == 216 || elementNumber == 219 || elementNumber == 3021 || elementNumber == 1121 || elementNumber == 6331 || elementNumber == 212) {
			resultParameter = parameter;
			isParameterSpecial = true;
		}
		
		if(elementNumber == 101 || elementNumber == 550) {
			resultParameter = getSpecialParameter(parameter.getTimeResolution(), 216);
		}
		
		if(elementNumber == 101 || elementNumber == 201 || elementNumber == 301 || elementNumber == 550) {
			resultParameter = getSpecialParameter(parameter.getTimeResolution(), 219);
		}
		
		if(elementNumber == 302) {
			resultParameter = getSpecialParameter(parameter.getTimeResolution(), 3021);
		}
		
		if(elementNumber == 112) {
			resultParameter = getSpecialParameter(parameter.getTimeResolution(), 1121);
		}
		
		if(elementNumber == 633) {
			resultParameter = getSpecialParameter(parameter.getTimeResolution(), 6331);
		}
		
		if(elementNumber == 101 || elementNumber == 201 || elementNumber == 301 || elementNumber == 550 || elementNumber == 601) {
			resultParameter = getSpecialParameter(parameter.getTimeResolution(), 212);
		}
		
		if(resultParameter != null) {
			tryAddParameterToUniqueList(prameterList, resultParameter);
		}
		
		return isParameterSpecial;
	}
	
	Parameter getSpecialParameter(TimeResolution timeResolution, int elementNumber) throws Exception {
		Parameter specialParameter = null;
		ParameterController parameterController= ParameterController.getInstance();
		List<Parameter> parameterResultList = parameterController.getParametersByTimeResolutionAndElementNumber(timeResolution, elementNumber);
		
		if(parameterResultList.size() == 1) {
			specialParameter = parameterResultList.get(0);
		} else if(parameterResultList.size() > 1) {
			throw new Exception("Component: "+this.getClass()+", there is no support for special parameters with multible calculation methods!");
		}
		
		return specialParameter;
	}
	
	void tryAddParameterToUniqueList(List<Parameter> parameterList, Parameter parameter) {
		if(!parameterListContainsParameter(parameterList, parameter)) {
			if(parameter.getId() == 212) {
				parameterList.add(parameter);
			} else {
				parameterList.add(0, parameter);
			}
		}
	}
	
	boolean parameterListContainsParameter(List<Parameter> parameterList, Parameter parameter) {
		for (Parameter currParameter : parameterList) {
			if(currParameter.getId() == parameter.getId()) {
				return true;
			}
		}
		
		return false;
	}
	
}
