package dk.dmi.lib.workflow.component.synop.foulum;

import dk.dmi.lib.synop.observation.Observation;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

import java.sql.*;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Component(
		name = "Mark Foulum observations as sent to GTS-OUT.",
		category = "Foulum",
		description = "",
        version = 1)
public class MarkFoulumObservationAsSent extends BaseComponent {
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"List of Observation"}, 
			returnDescription = "")
	public void execute(Map<Integer, Observation> observations) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
			connection.setAutoCommit(false);

			final Comparator<Observation> comp = Comparator.comparingLong(Observation::getTimestamp);

			long oldest = observations.entrySet().stream().filter(map -> map.getKey() > 0).map(map -> map.getValue()).max(comp).get().getTimestamp();
			long youngest = observations.entrySet().stream().filter(map -> map.getKey() > 0).map(map -> map.getValue()).min(comp).get().getTimestamp();

			LOGGER.debug("Mark as sent - all current observation in interval : " + String.format("%tc", new Date(youngest)) + " - " + String.format("%tc", new Date(oldest)));

			// Update current values
			String sql = "UPDATE data_foulum SET sent_to_production=1 " +
					"WHERE observation_time<=FROM_UNIXTIME(" + oldest/1000 + ") and observation_time>=FROM_UNIXTIME(" + youngest/1000 +") and sent_to_production=0 " +
					"and observation_type in ("+ getUseCurrentValueLabels()+")";

			final Statement statementFiles = connection.createStatement();
			statementFiles.executeUpdate(sql);


			// Update summed values
			long oldestSum = observations.entrySet().stream().filter(map -> map.getKey() <= 0).map(map -> map.getValue()).max(comp).get().getTimestamp();
			long youngestSum = observations.entrySet().stream().filter(map -> map.getKey() <= 0).map(map -> map.getValue()).min(comp).get().getTimestamp();

			LOGGER.debug("Mark as sent - all summed observation in interval : " + String.format("%tc", new Date(youngestSum)) + " - " + String.format("%tc", new Date(oldestSum)));
			String sumSql = "UPDATE data_foulum SET sent_to_production=1 " +
					"WHERE observation_time BETWEEN DATE_ADD(FROM_UNIXTIME("+youngestSum/1000+"),INTERVAL -9 MINUTE) AND FROM_UNIXTIME(" + (oldestSum/1000) +") "+
					"and sent_to_production=0 " +
					"and (observation_type in ("+ getUseSummedValueLabels()+") or observation_type in ("+ getUseAveragedValueLabels() +") )";

			final Statement statementSumFiles = connection.createStatement();
			statementSumFiles.executeUpdate(sumSql);

			connection.commit();
			statementFiles.close();
			statementSumFiles.close();

			connection.close();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			LOGGER.error("Can not update obs1_2.data_foulum records. Should update sent_to_production column. Error message:" + e.getMessage(), e);
		}
	}

	private String getUseCurrentValueLabels() {
		return FoulumParser.ObservationType.USE_CURRENT_VALUES.stream()
				.map(label -> label.getLabel())
				.collect( Collectors.joining( "','", "'", "'" ) );
	}

	private String getUseSummedValueLabels() {
		return FoulumParser.ObservationType.USE_ACCUMULATED_SUM_VALUES.stream()
				.map(label -> label.getLabel())
				.collect( Collectors.joining( "','", "'", "'" ) );
	}

	private String getUseAveragedValueLabels() {
		return FoulumParser.ObservationType.USE_ACCUMULATED_AVERAGE_VALUES.stream()
				.map(label -> label.getLabel())
				.collect( Collectors.joining( "','", "'", "'" ) );
	}
}
