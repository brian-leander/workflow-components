package dk.dmi.lib.workflow.component.polygon;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import dk.dmi.lib.persistence.database.climadb.polygon.common.ClimadbPolygonPersistenceUtils;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.ApplicationController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Application;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get application", 
		category = "Polygon",
		description = "Get polygon application for the given name.",
	    version = 1)
public class GetPolygonApplication {

	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfGridFileCalculations(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		ApplicationController applicationController = ApplicationController.getInstance();
		List<Application> applicationList = applicationController.getAllApplications();
		
		String[] applicationArray = new String[applicationList.size()+1];
		applicationArray[0] = "";
		
		for(int i = 0; i < applicationList.size(); i++) {
			Application application = applicationList.get(i);
			applicationArray[i+1] = application.getName();
		}
		
		return applicationArray;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Application to receive new grid file calculations for"}, 
			returnDescription = "Polygon application with the given name")
	public Application execute(String applicationName) throws Exception {
		Application application = getApplication(applicationName);
		return application;
	}

	Application getApplication(String applicationName) throws Exception {
		ClimadbPolygonPersistenceUtils climadbPolygonPersistenceUtils = ClimadbPolygonPersistenceUtils.getInstance();
		climadbPolygonPersistenceUtils.refreshClassCache(Application.class);
		
		ApplicationController applicationController = ApplicationController.getInstance();
		Application application = applicationController.getApplicationForName(applicationName);
		
		if(application == null) {
			throw new Exception("Application does not exist!");
		}
		
		return application;
	}

}
