package dk.dmi.lib.workflow.component.database;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import dk.dmi.lib.persistence.common.EntityUtils;
import dk.dmi.lib.persistence.common.PersistenceObject;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
	name = "Save Entity Instance", 
	category = "Database",
	description = "Save the specified persistence object",
    version = 1)
public class SaveEntity {
	
	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfDatabases(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return EntityUtils.instantiateDatabasesAndGetEntityUtilsNames();
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Choose database", "Entity instance to save"})
	public void execute(String databaseName, PersistenceObject persistenceObject) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		EntityUtils.tryInstantiateEntityUtilsClass(databaseName);
		EntityUtils entityUtils = EntityUtils.getEntityUtilsForName(databaseName);
		entityUtils.save(persistenceObject);
		entityUtils.closeEntityManager();
	}
	
}
