package dk.dmi.lib.workflow.component.synop.asiaq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Mark Asiaq observations as sent to GTS-OUT.", 
		category = "Asiaq",
		description = "",
        version = 1)
public class MarkAsiaqObservationAsSent extends BaseComponent {	
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"List of Observation"}, 
			returnDescription = "")
	public void execute(Set<Integer> observationIds) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
			connection.setAutoCommit(false);

			String sqlUpdate = "UPDATE data_asiaq SET sent_to_production=? WHERE id=?";
			PreparedStatement statementData = connection.prepareStatement(sqlUpdate);
			
			for (Integer id : observationIds) {				
				statementData.setInt(1, 1);				
				statementData.setLong(2, id);
				
				statementData.addBatch();
			}
			
			statementData.executeBatch();			
			
			final Statement statementFiles = connection.createStatement();
			statementFiles.executeUpdate("UPDATE data_asiaq SET sent_to_production=2 WHERE sent_to_production=0;");

			connection.commit();
			statementData.close();
			statementFiles.close();			
			connection.close();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			LOGGER.error("Can not update obs1_2.data_asiaq records. Should update sent_to_production column. Error message:" + e.getMessage(), e);
		}		
	}
}
