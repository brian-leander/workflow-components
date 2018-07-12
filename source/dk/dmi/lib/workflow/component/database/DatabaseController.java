package dk.dmi.lib.workflow.component.database;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import dk.dmi.lib.persistence.common.DatabasePersistenceException;
import dk.dmi.lib.persistence.common.EntityUtils;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
	name = "Database controller", 
	category = "Database",
	description = "Allows for manual control of database entity utils. Note: When manually committing a transaction, the following inserts will be cached offline and committed when the workflow completes, unless a new transaction is started manually. "
					+ "It is not posible to execute update queries outside transactions. "
					+ "The command '"+DatabaseController.COMMIT_AND_BEGIN_TRANSACTION+"' commits the transaction and begins a new one. "
					+ "Commit includes commiting the transaction (if one exists), persist all entities insertet/updated outside a transaction and closes the entity manager. "
					+ "The command '"+DatabaseController.CLEAR_CACHE+"' evicts all entity objects.",
    version = 1)
public class DatabaseController {
	
	static final String COMMIT_AND_BEGIN_TRANSACTION = "Commit and begin transaction";
	static final String BEGIN_TRANSACTION = "Begin transaction";
	static final String COMMIT_TRANSACTION = "Commit transaction";
	static final String ROLLBACK_TRANSACTION = "Rollback transaction";
	static final String CLEAR_CACHE = "Clear cache";
	
	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfDatabases(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return EntityUtils.instantiateDatabasesAndGetEntityUtilsNames();
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfCommands(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String[] commands = {COMMIT_AND_BEGIN_TRANSACTION, BEGIN_TRANSACTION, COMMIT_TRANSACTION, ROLLBACK_TRANSACTION, CLEAR_CACHE};
		return commands;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Choose database", "Choose command to execute"})
	public void execute(String databaseName, String command) throws DatabasePersistenceException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		EntityUtils.tryInstantiateEntityUtilsClass(databaseName);
		EntityUtils entityUtils = EntityUtils.getEntityUtilsForName(databaseName);
		
		switch (command) {
		case COMMIT_AND_BEGIN_TRANSACTION :
			entityUtils.commitTransaction();
			entityUtils.persistAndCommitOfflineData();
			entityUtils.closeEntityManager();
			entityUtils.beginTransaction();
			break;
			
		case BEGIN_TRANSACTION :
			entityUtils.beginTransaction();
			break;
			
		case COMMIT_TRANSACTION :
			entityUtils.commitTransaction();
			entityUtils.persistAndCommitOfflineData();
			entityUtils.closeEntityManager();
			break;
			
		case ROLLBACK_TRANSACTION :
			entityUtils.rollbackTransaction();
			break;
			
		case CLEAR_CACHE :
			entityUtils.refreshCache();
			break;
			
		}
	}
	
}
