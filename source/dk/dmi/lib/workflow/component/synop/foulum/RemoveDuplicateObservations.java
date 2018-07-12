package dk.dmi.lib.workflow.component.synop.foulum;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.component.synop.asiaq.ObservationWithType;

import java.sql.*;
import java.util.List;

@WorkflowAnnotations.Component(
        name = "Remove Duplicate Observation Data",
        category = "Foulum",
        description = "Remove Duplicate Observation Data",
        version = 1)
public class RemoveDuplicateObservations extends BaseComponent {
    @WorkflowAnnotations.ExecuteMethod(
            argumentDescriptions = {"List of observation data (Of type ObservationWithType)."},
            returnDescription = "false or true")
    public boolean removeDuplicates(List<ObservationWithType> observationsWithType) {

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
                    "select observation_type, observation_interval, observation_time from data_foulum"
                            + " where foulum_station_id = '"+stationId+"'"
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
            }

            statement.close();
            connection.close();

            return observationsWithType.size() > 0;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            LOGGER.error("Error fetching Foulum observations." + System.lineSeparator() + "Error: " + e.getMessage(), e);
        }

        return false;
    }
}
