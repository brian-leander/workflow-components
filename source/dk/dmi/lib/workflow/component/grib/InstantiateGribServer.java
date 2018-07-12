package dk.dmi.lib.workflow.component.grib;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import dk.dmi.lib.grib.GribServer;
import dk.dmi.lib.persistence.common.DatabasePersistenceConfiguration;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.persistence.database.processdb.publicc.entity.WorkflowActivity;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Deprecated // new version available
@Component(
		name = "Instantiate grib server", 
		category = "Grib",
		description = "Create an instance of grib server, to use when getting grib fields. Host = gdbopr, port = 5000.",
        version = 1)
public class InstantiateGribServer extends BaseComponent {
  
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDescriptions = {"Project name used on grib server for logging purposes"},
			returnDescription = "Grib server instance")
	public GribServer execute(String projectName) throws IOException {
		GribServer gribServer = null;
		
		ResourceBundle gribServerResourceBundle = null;
		
		if(DatabasePersistenceConfiguration.getCurrentConnectedDatabaseId() == DatabasePersistenceConfiguration.TEST_DATABASE_ID) {
			File configFile = new File(DatabasePersistenceConfiguration.getTestDatabaseConfigurationFilePath()[0]+"/..");
			URL[] configUrls = new URL[]{configFile.toURI().toURL()};
			ClassLoader configClassLoader = new URLClassLoader(configUrls);
			gribServerResourceBundle = ResourceBundle.getBundle("GribServerTest", Locale.getDefault(), configClassLoader);
		} else if(DatabasePersistenceConfiguration.getCurrentConnectedDatabaseId() == DatabasePersistenceConfiguration.PROD_DATABASE_ID) {
			File configFile = new File(DatabasePersistenceConfiguration.getProdDatabaseConfigurationFilePath()[0]+"/..");
			URL[] configUrls = new URL[]{configFile.toURI().toURL()};
			ClassLoader configClassLoader = new URLClassLoader(configUrls);
			gribServerResourceBundle = ResourceBundle.getBundle("GribServerProd", Locale.getDefault(), configClassLoader);
		}
		
		if(gribServerResourceBundle != null) {
			Object workflowActivityObject = workflowContextController.getObjectForKey("_ACTIVITY");
			String workflowDefinitionName = "Undefined workflow";
			
			if(workflowActivityObject != null && workflowActivityObject instanceof WorkflowActivity) {
				WorkflowActivity workflowActivity = (WorkflowActivity) workflowActivityObject;
				workflowDefinitionName = workflowActivity.getWorkflow().getWorkflowDefinition().getName();
			}
			
			gribServer = new GribServer(gribServerResourceBundle, workflowDefinitionName);
			
		}
		
		return gribServer;
	}
	
}
