package dk.dmi.lib.workflow.component.database;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import dk.dmi.lib.persistence.common.EntityUtils;
import dk.dmi.lib.persistence.common.PersistenceLayerUtils;
import dk.dmi.lib.persistence.common.PersistenceObject;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.CommonDataUtils;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.LinkArgumentMethod;

@Component(
		name = "Insert Data", 
		category = "Database",
		description = "Execute insert query, by specifying database, entity and values. If record with the same primary key already exists, the record is replaced.",
        version = 1)
public class InsertData extends BaseComponent {
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
	public int getArgumentIndexToLinkWith0() throws IOException {
		return 1;
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfEntityNames(String databaseName) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return CommonDataUtils.getEntityNamesForDatabase(databaseName);
	}
	
	@LinkArgumentMethod(
			argumentIndex = "1")
	public int getArgumentIndexToLinkWith1() throws IOException {
		return 2;
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "2")
	public String[] getListOfEntityColumns(String databaseNameAndEntityName) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String[] databaseNameAndEntityNameList = databaseNameAndEntityName.split(CommonDataUtils.STRING_LIST_SEPERATOR);
		
		if(databaseNameAndEntityNameList.length == 2) {
			String databaseName = databaseNameAndEntityNameList[0].trim();
			String entityName = databaseNameAndEntityNameList[1].trim();
			EntityUtils entityUtils = EntityUtils.getEntityUtilsForName(databaseName);
			return PersistenceLayerUtils.getListOfEntityColumnVariablesAndType(entityUtils.getEntityPackagePath(), entityName);
		} else {
			return new String[0];
		}
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_MAP},
			argumentDescriptions = {"Choose database", "Choose entity", "Map of the insert values. Use AUTO_ID for new Long id."}, 
			returnDescription = "Newly created persistence object")
	public PersistenceObject execute(String databaseName, String entityName, String values) throws Exception {
		EntityUtils.tryInstantiateEntityUtilsClass(databaseName);
		EntityUtils entityUtils = EntityUtils.getEntityUtilsForName(databaseName);
		PersistenceObject persistenceObject = CommonDataUtils.executeInsertQuery(entityUtils, entityUtils.getEntityPackagePath()+entityName, values, workflowContextController.getWorkflowContextMap());
		entityUtils.closeEntityManager();
		return persistenceObject;
	}
	
}
