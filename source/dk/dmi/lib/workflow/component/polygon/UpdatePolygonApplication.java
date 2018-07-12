package dk.dmi.lib.workflow.component.polygon;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.ApplicationController;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.GridFileCalculationController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Application;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFileCalculation;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Update Application", 
		category = "Polygon",
		description = "Update polygon applications last grid file calculation, automatic or manual.",
	    version = 1)
public class UpdatePolygonApplication {

	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getGridFileCalculationTypes(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return new String[]{GridFileCalculationController.GRID_FILE_TYPE_AUTO_TEXT, GridFileCalculationController.GRID_FILE_TYPE_MANUAL_TEXT};
	}

	@ExecuteMethod(
			argumentDescriptions = {"Application to update", "Type of grid file calculations (i.e. AUTO or MANUAL)", "Grid file calculation to set as latest calculated"})
	public void execute(Application application, String gridFileCalculationtType, GridFileCalculation latestGridFileCalculation) throws Exception {
		ApplicationController applicationController = ApplicationController.getInstance();
		applicationController.updateApplicationLatestGridFileCalculation(application, gridFileCalculationtType, latestGridFileCalculation);
	}
	
}
