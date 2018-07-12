package dk.dmi.lib.workflow.component.database;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
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
	name = "Select Data", 
	category = "Database",
	description = "Execute select query, by specifying database, entity, where clause and max count.",
    version = 1)
public class SelectData {
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
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_SQL_AREA_SMALL, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Choose database", "Choose entity", "SQL where clause (without specifying WHERE)", "Maximum number of results to return (use null to ignore)"}, 
			returnDescription = "List of persistence objects specified by entity type")
	public List<?> execute(String databaseName, String entityName, String whereClause, Integer maxResults) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		EntityUtils.tryInstantiateEntityUtilsClass(databaseName);
		EntityUtils entityUtils = EntityUtils.getEntityUtilsForName(databaseName);
		EntityManager entityManager = entityUtils.getEntityManager();
		
		int maxResultsInt = Integer.MAX_VALUE;
		
		if(maxResults != null) {
			maxResultsInt = maxResults.intValue();
		}
		
		List<?> persistenceObjectList = CommonDataUtils.selectEntityListByWhereClause(entityManager, entityUtils.getDatabaseSchema(), entityUtils.getEntityPackagePath()+entityName, true, whereClause, maxResultsInt, workflowContextController.getWorkflowContextMap());
		
		entityUtils.closeEntityManager();
		return persistenceObjectList;
	}
	
}
