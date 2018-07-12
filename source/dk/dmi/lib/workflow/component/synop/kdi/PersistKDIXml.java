package dk.dmi.lib.workflow.component.synop.kdi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;
import dk.dmi.lib.workflow.component.synop.PS416DataType;
import dk.dmi.lib.workflow.component.synop.PS416Observation;
import dk.dmi.lib.workflow.component.synop.PS416Observation.ObservationVO;
import dk.dmi.lib.workflow.component.synop.WaterLevelObservationStatus;

@Component(
		name = "Persist KDI observation data", 
		category = "VST",
		description = "",
		version = 1)
public class PersistKDIXml extends BaseComponent {

	private WorkflowContextController workflowContextController;
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"KDI observation data."})
	public boolean execute(PS416Observation observation) {
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
			connection.setAutoCommit(false);
			
			final Statement statementFiles = connection.createStatement();
						
			LOGGER.debug("Saving file info for " + observation.getFileName() + " to obs1_2.data_kdi_file");
			
			statementFiles.executeUpdate("INSERT INTO obs1_2.data_kdi_file (created, file_name, xml, supplier, data_type, production_time, observation_time) VALUES(" +
					"FROM_UNIXTIME(" + System.currentTimeMillis() / 1000 + ")," +
					"'" + observation.getFileName() + "'," +					
					"'" + observation.getXml() + "'," +
					"'" + observation.getSupplier() + "'," +
					"'" + observation.getDataType().name() + "'," +
					"FROM_UNIXTIME(" + observation.getProductionTime() / 1000 + ")," +
					"FROM_UNIXTIME(" + observation.getObservationTime() / 1000 + ") )");
			
			LOGGER.debug("Saving observations from file " + observation.getFileName() + " to obs1_2.data_kdi");			
			
			String sql = "";
			if (PS416DataType.VANDSTAND.equals(observation.getDataType())) {
				sql = "INSERT INTO obs1_2.data_kdi (created, observation_time, kdi_sensor_id, " + 
							"waterlevel, waterlevel_obs_time, waterlevel_unit, sent_to_production, temperature) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
			}
			if (PS416DataType.TEMPERATURE.equals(observation.getDataType())) {
				sql = "INSERT INTO obs1_2.data_kdi (created, observation_time, kdi_sensor_id, temperature, temperature_obs_time, temperature_unit, " + 
							"sent_to_production) VALUES(?, ?, ?, ?, ?, ?, ?)";
			}
			PreparedStatement statementData = connection.prepareStatement(sql);
			
			for (ObservationVO observationData : observation.getWaterlevel()) {				
				statementData.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
				statementData.setTimestamp(2, new Timestamp(observation.getObservationTime()));
				statementData.setString(3, observationData.getSensorId());				
				statementData.setFloat(4, observationData.getValue());
				statementData.setTimestamp(5, new Timestamp(observationData.getTimestamp()));
				statementData.setString(6, observationData.getUnit());				
				statementData.setInt(7, WaterLevelObservationStatus.NEW.getStatus());
				if (PS416DataType.VANDSTAND.equals(observation.getDataType())) {
					statementData.setFloat(8, 9999);
				}
				statementData.addBatch();				
			}
			statementData.executeBatch();
			connection.commit();
			statementData.close();
			statementFiles.close();
			connection.close();
			
			return true;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			LOGGER.error("Error in PersistKDIXml. ", e);
			workflowContextController.addObjectToContext("PS4_CREATE_ERROR", "Error persisting observation data. -- " + e.getMessage(), false);
			if (connection != null) {
	            try {
	                LOGGER.warn("Transaction is being rolled back");
	                connection.rollback();
	            } catch(SQLException excep) {
	            	LOGGER.error("Error in PersistKDIXml. Cannot roll back transaction.", excep);
	            }
	        }
		}

		return false;
	}
	
}
