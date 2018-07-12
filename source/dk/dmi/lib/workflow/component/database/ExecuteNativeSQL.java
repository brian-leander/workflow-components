package dk.dmi.lib.workflow.component.database;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import dk.dmi.lib.persistence.common.EntityUtils;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
	name = "Execute Native SQL", 
	category = "Database",
	description = "Execute native sql query on specified database. Use execute read for select statements, returns a list of object arrays List<Object[]>. Use write for insert, update and delete statements, returns null.",
    version = 1)
public class ExecuteNativeSQL {
	public static final String TYPE_READ = "Read";
	public static final String TYPE_WRITE = "Write";
	
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
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getStatementTypeList(String ignore) {
		String[] statementTypes = {TYPE_READ, TYPE_WRITE};
		return statementTypes;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_SQL_AREA_MEDIUM},
			argumentDescriptions = {"Choose database", "Statement type, read for selects and write for insert, update and delete", "Native SQL statement"},
			returnDescription = "If read statement a list of object arrays, else null")
	public List<Object[]> execute(String databaseName, String statementType, String sqlStatement) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		EntityUtils.tryInstantiateEntityUtilsClass(databaseName);
		EntityUtils entityUtils = EntityUtils.getEntityUtilsForName(databaseName);
		EntityManager entityManager = entityUtils.getEntityManager();
		
		List<Object[]> executeResult = executeNativeSqlStatement(statementType, sqlStatement, entityManager);
		
		entityUtils.closeEntityManager();
		return executeResult;
	}

	List<Object[]> executeNativeSqlStatement(String statementType, String sqlStatement, EntityManager entityManager) {
		List<Object[]> executeResult = null;
		Map<String, Object> context = workflowContextController.getWorkflowContextMap();
		sqlStatement = ContextHelper.evaluateAllContextKeysInText(context, sqlStatement);
		Query nativeQuery = entityManager.createNativeQuery(sqlStatement);
		
		switch(statementType) {
		case TYPE_READ :
			@SuppressWarnings("unchecked")
			List<Object[]> nativeResultList = nativeQuery.getResultList();
			executeResult = nativeResultList;
			break;
			
		case TYPE_WRITE :
			nativeQuery.executeUpdate();
			break;
		}
		
		return executeResult;
	}
	
}
