package dk.dmi.lib.workflow.component.synop.asiaq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Validate Asiaq observation file", 
		category = "Asiaq",
		description = "Validate Asiaq observation file",
        version = 1)
public class ValidateAsiaqDataFile extends BaseComponent {

	private WorkflowContextController workflowContextController;
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Asiaq data file name"},
			returnDescription="returns false if file name is already in DB, else true")
    public boolean validate(String fileName) {
		boolean validation = true;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();		
			final Connection connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
			final Statement stmt = connection.createStatement();
						
			final ResultSet result = stmt.executeQuery("select * from obs1_2.data_asiaq_file_history where file_name='" + fileName + "'");
			
			// if record exists this is duplicate ... or replacement ????
			if (result.next()) {				
				LOGGER.warn("Asiaq observations in file " + fileName + " already exists.");

				workflowContextController.addObjectToContext("ERROR", "Asiaq observations in file " + fileName + " already exists.", false);				
				validation = false;
			}			
			
			stmt.close();
			connection.close();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			LOGGER.error("Error in ValidateAsiaqData.", e);
			workflowContextController.addObjectToContext("ERROR", e.getMessage(), false);
			validation = false;
		}
		
		return validation;
	}
}
