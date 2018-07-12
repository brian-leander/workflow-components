package dk.dmi.lib.workflow.component.polygon;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Filter parameter list", 
		category = "Polygon",
		description = "Filter list of parameters by id, name or element number white-lists",
        version = 1)
public class FilterParameterList extends BaseComponent {
	
	public static final String FILTER_BY_ID = "Id";
	public static final String FILTER_BY_NAME = "Name";
	public static final String FILTER_BY_ELEM_NO = "Elem No";
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfDatabases(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String[] filterOptions = {FILTER_BY_ID, FILTER_BY_NAME, FILTER_BY_ELEM_NO};
		return filterOptions;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"List of parameters to be filtered", "Options to filter by", "Filter white-list"}, 
			returnDescription = "New filtered list of parameters")
    public Object execute(List<Parameter> parameterList, String filterBy, List<String> filterWhiteList) {
		List<Parameter> parameterListFiltered = filterParametersByWhiteList(parameterList, filterBy, filterWhiteList);
        return parameterListFiltered;
    }

	List<Parameter> filterParametersByWhiteList(List<Parameter> parameterList,
			String filterBy, List<String> filterWhiteList) {
		List<Parameter> parameterListFiltered = new ArrayList<Parameter>();
		
		for (Parameter parameter : parameterList) {
			String parameterFilterValue = getParameterFilterValue(filterBy, parameter);
			boolean parameterInWhiteList = filterWhiteList.contains(parameterFilterValue);
			
			if(parameterInWhiteList) {
				parameterListFiltered.add(parameter);
			}
		}
		return parameterListFiltered;
	}
	
	String getParameterFilterValue(String filterBy, Parameter parameter) {
		String parameterFilterValue = null;
		
		switch (filterBy) {
		case FILTER_BY_ID:
			parameterFilterValue = parameter.getId()+"";
			break;
			
		case FILTER_BY_NAME:
			parameterFilterValue = parameter.getName();
			break;
			
		case FILTER_BY_ELEM_NO:
			parameterFilterValue = parameter.getElementNumber()+"";
			break;
		}
		
		return parameterFilterValue;
	}
	
}
