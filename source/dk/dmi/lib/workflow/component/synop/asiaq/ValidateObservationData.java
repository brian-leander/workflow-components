package dk.dmi.lib.workflow.component.synop.asiaq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Validate Asiaq Observation Data", 
		category = "Asiaq",
		description = "Validate",
		version = 1)
public class ValidateObservationData extends BaseComponent {

	@ExecuteMethod(
			argumentDescriptions = {"List of Asiaq observation data."},
			returnDescription = "false or true")
	public boolean validate(List<ObservationWithType> observationsWithType) {

		long maxTimestamp = ObservationWithType.getMaxTime(observationsWithType); 
		long minTimestamp = ObservationWithType.getMinTime(observationsWithType); 
		LOGGER.debug("### Max time : " + maxTimestamp);
		LOGGER.debug("### Min time : " + minTimestamp);
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			final Connection connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
			final Statement statement = connection.createStatement();
			
			String stationId = observationsWithType.get(0).getStationNo();
	 				
			String sqlLoadNewObservations = 
					"select observation_type, observation_interval, observation_time from data_asiaq"							
							+ " where asiaq_station_id = '"+stationId+"'"
							+ " and observation_time between FROM_UNIXTIME("+minTimestamp+") and FROM_UNIXTIME("+maxTimestamp+");";		
			final ResultSet observations = statement.executeQuery(sqlLoadNewObservations);			
			while (observations.next()) {				
				
				int observationTypeIndex = observations.findColumn("observation_type");
				String observationType = observations.getString(observationTypeIndex);
				
				int observationIntervalIndex = observations.findColumn("observation_interval");
				int observationInterval = observations.getInt(observationIntervalIndex);

				int observationTimeIndex = observations.findColumn("observation_time");
				long observationTime = observations.getTimestamp(observationTimeIndex).getTime();
				
				ObservationWithType.removeDuplicates(observationsWithType, observationInterval, observationTime, observationType);
				if (observationsWithType.size() == 0) 
					break;

//				for (ObservationWithType observationWithType : observationsWithType) {
//					if (observationWithType.getObservationInterval() == observationInterval 
//							&& (observationWithType.getUtcUnixTimeStamp() * 1000) == observationTime
//							&& observationWithType.getObservationType().equals(observationType) ) {
//						observationsWithType.remove(observationWithType);
//						break;
//					}
//				}
			}
			
			statement.close();
			connection.close();
			
			return observationsWithType.size() > 0;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			LOGGER.error("Error fetching Asiaq observations." + System.lineSeparator() + "Error: " + e.getMessage(), e);			
			// workflowContextController.addObjectToContext("FETCH_OBSERVATIONS_ERROR", "Error executing LoadNewAsiaqObservations. -- exception message: " + e.getMessage(), false);
		}
		
		return false;
	}	
}
