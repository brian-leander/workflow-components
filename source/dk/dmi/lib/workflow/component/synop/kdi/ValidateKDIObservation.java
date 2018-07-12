package dk.dmi.lib.workflow.component.synop.kdi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;
import dk.dmi.lib.workflow.component.synop.PS416Observation;

@Component(
		name = "Validate PS4 Observation", 
		category = "VST",
		description = "Validate PS4 Observation is new",
        version = 1)
public class ValidateKDIObservation extends BaseComponent {

	private WorkflowContextController workflowContextController;
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"PS4 Observation"},
			returnDescription="returns false if observation is already in DB, else true")
    public boolean validate(PS416Observation ps4Observation) {
		boolean validation = true;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();		
			final Connection connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
			final Statement stmt = connection.createStatement();
						
			final ResultSet result = stmt.executeQuery("select * from obs1_2.data_kdi_file where file_name='" + ps4Observation.getFileName() + "' and observation_time=FROM_UNIXTIME(" + ps4Observation.getObservationTime() / 1000 + ")");
			
			// if record exists this is duplicate ... or replacement ????
			if (result.next()) {
				Calendar observationTime = Calendar.getInstance();
				observationTime.setTimeInMillis(ps4Observation.getObservationTime());
				
				LOGGER.warn("Observations in file " + ps4Observation.getFileName() + " with observations at " + DateUtils.formatDate(observationTime.getTime(), DateUtils.DATE_FORMAT_1) + " already exists.");

				workflowContextController.addObjectToContext("PS4_CREATE_ERROR", "Observations in file " + ps4Observation.getFileName() + " with observations at " + DateUtils.formatDate(observationTime.getTime(), DateUtils.DATE_FORMAT_1) + " already exists.", false);				
				validation = false;
			}			
			
			stmt.close();
			connection.close();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			LOGGER.error("Error in ValidateKDIObservation.", e);
			workflowContextController.addObjectToContext("PS4_CREATE_ERROR", e.getMessage(), false);
			validation = false;
		}
		
		return validation;
	}
}
