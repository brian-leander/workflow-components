package dk.dmi.lib.workflow.component.synop.kdi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.component.synop.WaterLevelObservation;
import dk.dmi.lib.workflow.component.synop.WaterLevelObservationStatus;

@Component(
		name = "Mark KDI observations as sent to GTS-OUT.", 
		category = "VST",
		description = "",
        version = 1)
public class MarkObservationAsSent extends BaseComponent {	
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"List of WaterLevelObservation"}, 
			returnDescription = "")
	public void execute(List<WaterLevelObservation> waterLevelObservations) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
			connection.setAutoCommit(false);

			String sqlUpdate = "UPDATE data_kdi SET sent_to_production=? WHERE observation_time=? and kdi_sensor_id=?";
			PreparedStatement statementData = connection.prepareStatement(sqlUpdate);
			
			for (WaterLevelObservation waterLevelObservation : waterLevelObservations) {				
				statementData.setInt(1, getStatus(waterLevelObservation.getStatus()).getStatus());
				statementData.setTimestamp(2, new Timestamp(waterLevelObservation.getObservationTime()));
				statementData.setString(3, waterLevelObservation.getKdiSensorId());
				
				statementData.addBatch();
			}
			
			statementData.executeBatch();
			connection.commit();
			statementData.close();
			connection.close();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			LOGGER.error("Can not update obs1_2.data_kdi records. Should update sent_to_production column for records with observation time :  " + waterLevelObservations.get(0).getObservationTime() + 
					System.lineSeparator() + "Error message:" + e.getMessage(), e);
		}
		
	}
	
	private WaterLevelObservationStatus getStatus(WaterLevelObservationStatus waterLevelObservationStatus) {
		return waterLevelObservationStatus.equals(WaterLevelObservationStatus.NEW) ? WaterLevelObservationStatus.SENT_TO_PRODUCTION : WaterLevelObservationStatus.IGNORED;				
	}
}
