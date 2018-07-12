package dk.dmi.lib.workflow.component.database;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.EntityManager;

import dk.dmi.lib.persistence.common.EntityUtils;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.CommonDataUtils;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.LinkArgumentMethod;

@Deprecated // new version available
@Component(
	name = "Delete Data", 
	category = "Database",
	description = "Execute delete query, by specifying database, entity and where clause.",
    version = 1)
public class DeleteData {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfDatabases(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return EntityUtils.instantiateDatabasesAndGetEntityUtilsNames();
	}
	
	@LinkArgumentMethod(
			argumentIndex = "0")
	public int getArgumentIndexToLinkWith() throws IOException {
		return 1;
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfEntityNames(String databaseName) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return CommonDataUtils.getEntityNamesForDatabase(databaseName);
	}

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_SQL_AREA_SMALL},
			argumentDescriptions = {"Choose database", "Choose entity", "SQL where clause (without specifying WHERE)"})
	public void execute(String databaseName, String entityName, String whereClause) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		EntityUtils.tryInstantiateEntityUtilsClass(databaseName);
		EntityUtils entityUtils = EntityUtils.getEntityUtilsForName(databaseName);
		EntityManager entityManager = entityUtils.getEntityManager();
		
		CommonDataUtils.deleteByWhereClause(entityManager, entityUtils.getDatabaseSchema(), entityUtils.getEntityPackagePath()+entityName, true, whereClause, workflowContextController.getWorkflowContextMap());
		
		entityUtils.closeEntityManager();
	}
	
}
