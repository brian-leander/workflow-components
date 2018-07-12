package dk.dmi.lib.workflow.component.check;

import dk.dmi.lib.persistence.common.DatabasePersistenceConfiguration;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Is Test Database", 
		category = "Check",
		description = "Check if persistance layer is connected to test or production database.",
        version = 1)
public class IsTestDatabase extends BaseComponent {
	
	@ExecuteMethod(
			returnDescription = "Returns true if connected to test database, returns false for production database.")
    public boolean execute() throws Exception {
		
		if(DatabasePersistenceConfiguration.getCurrentConnectedDatabaseId() == DatabasePersistenceConfiguration.TEST_DATABASE_ID) {
			return true;
		} else if(DatabasePersistenceConfiguration.getCurrentConnectedDatabaseId() == DatabasePersistenceConfiguration.PROD_DATABASE_ID) {
			return false;
		} else {
			throw new Exception("Error: Wrong database id, current id set: "+DatabasePersistenceConfiguration.getCurrentConnectedDatabaseId());
		}
    }
	
}
