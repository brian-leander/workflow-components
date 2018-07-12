package dk.dmi.lib.workflow.component.synop.foulum;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.synop.observation.Observation;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;

import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Date;

@WorkflowAnnotations.Component(
        name = "Load Foulum observations.",
        category = "Foulum",
        description = "",
        version = 1)
public class LoadNewFoulumObservations extends BaseComponent {
    private WorkflowContextController workflowContextController;
    @WorkflowAnnotations.InjectContextControllerMethod
    public void injectContext(WorkflowContextController workflowContextController) {
        this.workflowContextController = workflowContextController;
    }

    @WorkflowAnnotations.ExecuteMethod(
            argumentDisplayTypes = {},
            argumentDescriptions = {},
            returnDescription = "Map of Observation id and Foulum observations.")
    public Map<Integer, Observation> execute() {

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            final Connection connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
            final Statement statement = connection.createStatement();

            Map<Integer, Observation> obsMap = new HashMap<>();
            Map<Integer, Observation> ignoreMap = new HashMap<>();
            Map<String, Set<Long>> summedAccumulatedObservations = new HashMap<>();
            Map<String, Set<Long>> averageAccumulatedObservations = new HashMap<>();

            String sqlLoadNewObservations =
                    "select da.id, da.observation_type, da.observation_interval, da.value, da.observation_time, sa.dmi_station_number, sa.foulum_station_number from data_foulum da "
                            + "join station_foulum sa on sa.foulum_station_number=da.foulum_station_id "
                            + "where da.sent_to_production=0 and da.value is not null order by da.observation_time";
            final ResultSet observations = statement.executeQuery(sqlLoadNewObservations);
            while (observations.next()) {
                int idIndex = observations.findColumn("id");
                int id = observations.getInt(idIndex);

                int dmiStationIdIndex = observations.findColumn("dmi_station_number");
                String dmiStationId = observations.getString(dmiStationIdIndex);

                int observationTypeIndex = observations.findColumn("observation_type");
                String observationType = observations.getString(observationTypeIndex);

                int observationIntervalIndex = observations.findColumn("observation_interval");
                int observationInterval = observations.getInt(observationIntervalIndex);

                int valueIndex = observations.findColumn("value");
                String value = observations.getString(valueIndex);

                int observationTimeIndex = observations.findColumn("observation_time");
                long observationTime = observations.getTimestamp(observationTimeIndex).getTime();


                ZonedDateTime startTime = Instant.ofEpochMilli(observationTime).atZone(ZoneId.of("UTC") );

                if (startTime.getMinute() % 10 == 0) {
                    if (FoulumParser.ObservationType.USE_CURRENT_VALUES.contains(FoulumParser.ObservationType.getByLabel(observationType))) {
                        Observation observation = getObservationTypeForCurrentValues(observationType, observationInterval, dmiStationId, observationTime, value);
                        if (observation != null) {
                            obsMap.put(id, observation);
                            continue;
                        }
                    }

                    int foulumStationNumberIndex = observations.findColumn("foulum_station_number");
                    String foulumStationNumber = observations.getString(foulumStationNumberIndex);

                    if (FoulumParser.ObservationType.USE_ACCUMULATED_SUM_VALUES.contains(FoulumParser.ObservationType.getByLabel(observationType))) {
                        addToAccumulationMap(summedAccumulatedObservations, observationTime, foulumStationNumber);
                    }

                    if (FoulumParser.ObservationType.USE_ACCUMULATED_AVERAGE_VALUES.contains(FoulumParser.ObservationType.getByLabel(observationType))) {
                        addToAccumulationMap(averageAccumulatedObservations, observationTime, foulumStationNumber);
                    }
                } else {
                    Observation observation = getObservationTypeForCurrentValues(observationType, observationInterval, dmiStationId, observationTime, value);
                    if (observation != null) {
                        ignoreMap.put(id, observation);
                    }
                }
            }

            int id = 0;
            if (!summedAccumulatedObservations.isEmpty()) {
                for (FoulumParser.ObservationType type : FoulumParser.ObservationType.USE_ACCUMULATED_SUM_VALUES) {
                    // loop accumau map
                    String sqlLoadAccumulatedPrecipitationObservations = createAccumulationSql(summedAccumulatedObservations, type, "SUM(t.value)");

                    final ResultSet precipitationObservations = statement.executeQuery(sqlLoadAccumulatedPrecipitationObservations);
                    while (precipitationObservations.next()) {
                        int valueIndex = precipitationObservations.findColumn("value");
                        String value = precipitationObservations.getString(valueIndex);
                        if (Float.parseFloat(value) == 0f) continue;

                        int dmiStationIdIndex = precipitationObservations.findColumn("dmi_station_number");
                        String dmiStationId = precipitationObservations.getString(dmiStationIdIndex);

                        int observationTypeIndex = precipitationObservations.findColumn("observation_type");
                        String observationType = precipitationObservations.getString(observationTypeIndex);

                        int observationIntervalIndex = precipitationObservations.findColumn("observation_interval");
                        int observationInterval = precipitationObservations.getInt(observationIntervalIndex);

                        int observationTimeIndex = precipitationObservations.findColumn("observation_time");
                        long observationTime = precipitationObservations.getTimestamp(observationTimeIndex).getTime();

                        int cntIndex = precipitationObservations.findColumn("cnt");
                        int cnt = precipitationObservations.getInt(cntIndex);

                        if (cnt < 10) {
                            LOGGER.warn("Only "+cnt+" Foulum data records in past 10 minutes. Cannot accumulate sums for : " + observationType + " / " + new Date(observationTime) );
                            continue;
                        }

                        Observation observation = getObservationTypeForAccumulated(observationType, observationInterval, dmiStationId, observationTime, value);
                        if (observation != null) {
                            obsMap.put(--id, observation);
                        }
                    }
                }
            }

            if (!averageAccumulatedObservations.isEmpty()) {
                for (FoulumParser.ObservationType type : FoulumParser.ObservationType.USE_ACCUMULATED_AVERAGE_VALUES) {
                    String sqlLoadAccumulatedObservations = createAccumulationSql(averageAccumulatedObservations, type, "ROUND(AVG(t.value), 2)");

                    final ResultSet accumulatedObservations = statement.executeQuery(sqlLoadAccumulatedObservations);
                    while (accumulatedObservations.next()) {
                        int valueIndex = accumulatedObservations.findColumn("value");
                        String value = accumulatedObservations.getString(valueIndex);
                        if (Float.parseFloat(value) == 0f) continue;

                        int dmiStationIdIndex = accumulatedObservations.findColumn("dmi_station_number");
                        String dmiStationId = accumulatedObservations.getString(dmiStationIdIndex);

                        int observationTypeIndex = accumulatedObservations.findColumn("observation_type");
                        String observationType = accumulatedObservations.getString(observationTypeIndex);

                        int observationIntervalIndex = accumulatedObservations.findColumn("observation_interval");
                        int observationInterval = accumulatedObservations.getInt(observationIntervalIndex);

                        int observationTimeIndex = accumulatedObservations.findColumn("observation_time");
                        long observationTime = accumulatedObservations.getTimestamp(observationTimeIndex).getTime();

                        int cntIndex = accumulatedObservations.findColumn("cnt");
                        int cnt = accumulatedObservations.getInt(cntIndex);

                        if (cnt < 10) {
                            LOGGER.warn("Only "+cnt+" Foulum data records in past 10 minutes. Cannot accumulate averages for : " + observationType + " / " + new Date(observationTime) );
                            continue;
                        }

                        Observation observation = getObservationTypeForAverage(observationType, observationInterval, dmiStationId, observationTime, value);
                        if (observation != null) {
                            obsMap.put(--id, observation);
                        }
                    }
                }
            }

            statement.close();
            connection.close();

            return obsMap;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            LOGGER.error("Error fetching Foulum observations." + System.lineSeparator() + "Error: " + e.getMessage(), e);
            workflowContextController.addObjectToContext("FETCH_OBSERVATIONS_ERROR", "Error executing LoadNewFoulumObservations. -- exception message: " + e.getMessage(), false);
        }

        return Collections.emptyMap();
    }

    private void addToAccumulationMap(Map<String, Set<Long>> accumulation, long observationTime, String foulumStationNumber) {
        if (accumulation.containsKey(foulumStationNumber)) {
            Set<Long> times = accumulation.get(foulumStationNumber);
            times.add(observationTime);
        } else {
            Set<Long> times = new HashSet<>();
            times.add(observationTime);

            accumulation.put(foulumStationNumber, times);
        }
    }

    private String createAccumulationSql(Map<String, Set<Long>> accumulation, FoulumParser.ObservationType type, String select) {
        String sqlLoadAccumulatedObservations = "";
        for (String stationNo : accumulation.keySet()) {
            Set<Long> obsTimes = accumulation.get(stationNo);

            for (long observationTime : obsTimes) {

                int interval = 9;

                if (sqlLoadAccumulatedObservations.length() > 0) {
                    sqlLoadAccumulatedObservations += " union " + System.lineSeparator();
                }

                observationTime /= 1000;
                sqlLoadAccumulatedObservations +=
                        getLoadAccumulatedObservationsSql(type, stationNo, observationTime, interval, select);
            }
        }

        return sqlLoadAccumulatedObservations + ";";
    }

    private String getLoadAccumulatedObservationsSql(FoulumParser.ObservationType type, String stationNo, long observationTime, int interval, String select) {
        return "SELECT "+select+" as value, t.observation_type, "+interval+" as observation_interval , FROM_UNIXTIME("+observationTime+") as observation_time, sa.dmi_station_number, count(*) as cnt"+
                " FROM data_foulum t"+
                " join station_foulum sa on sa.foulum_station_number=t.foulum_station_id"+
                " WHERE t.observation_type = '"+ type.getLabel() +"'"+
                " and t.observation_time BETWEEN DATE_ADD(FROM_UNIXTIME("+observationTime+"),INTERVAL -"+(interval)+" MINUTE) AND FROM_UNIXTIME("+observationTime+")"+
                " and sa.foulum_station_number='"+stationNo+"'" +
                " group by t.foulum_station_id";
    }

    private Observation getObservationTypeForCurrentValues(String parameterName, int observationInterval, String stationNo, long timestamp, String value) {
        FoulumParser.ObservationType observationType = FoulumParser.ObservationType.getByLabel(parameterName);

        if (observationType.equals(FoulumParser.ObservationType.TEMPERATURE)) {
            return Observation.forCurrentTemperature(stationNo, timestamp, value);
        }
        if (observationType.equals(FoulumParser.ObservationType.RELATIVE_HUMIDITY)) {
            return Observation.forHumidity10Minutes(stationNo, timestamp, value);
        }
        if (observationType.equals(FoulumParser.ObservationType.AIR_PRESSURE)) {
            return Observation.forAirPressureHour(stationNo, timestamp, value); // TO-DO: Skal lufttryk midles over 10 min eller bruges blot den nuværende ?
        }
        if (observationType.equals(FoulumParser.ObservationType.GRASS_TEMPERATURE)) {
            return Observation.forCurrentEarthTemperature_GrassLevel(stationNo, timestamp, value);
        }
        if (observationType.equals(FoulumParser.ObservationType.EARTH_TEMPERATURE_10)) {
            return Observation.forCurrentEarthTemperature_10cm(stationNo, timestamp, value);
        }
        if (observationType.equals(FoulumParser.ObservationType.EARTH_TEMPERATURE_30)) {
            return Observation.forCurrentEarthTemperature_30cm(stationNo, timestamp, value);
        }

        return null;
    }

    private Observation getObservationTypeForAverage(String parameterName, int observationInterval, String stationNo, long timestamp, String value) {
        FoulumParser.ObservationType observationType = FoulumParser.ObservationType.getByLabel(parameterName);

        /* Akku AVG */
        if (observationType.equals(FoulumParser.ObservationType.WIND_DIRECTION)) {   // AVG
            return Observation.forWindDirection10Minutes(stationNo, timestamp, value);
        }
        if (observationType.equals(FoulumParser.ObservationType.WIND_SPEED)) {       // AVG
            return Observation.forWindSpeed10Minutes(stationNo, timestamp, value);
        }
        if (observationType.equals(FoulumParser.ObservationType.RADIATION)) {        // AVG
            return Observation.forShortwaveRadiation10Minutes(stationNo, timestamp, value);
        }

        return null;
    }

    private Observation getObservationTypeForAccumulated(String parameterName, int observationInterval, String stationNo, long timestamp, String value) {
        FoulumParser.ObservationType observationType = FoulumParser.ObservationType.getByLabel(parameterName);
        /* Akku SUM */
        if (observationType.equals(FoulumParser.ObservationType.ACCUMULATED_PRECIPITATION)) {    // SUM
            return Observation.forPrecipitation10Minutes(stationNo, timestamp, value);
        }
        if (observationType.equals(FoulumParser.ObservationType.LEAVE_HUMIDITY)) {   // SUM
            return Observation.forLeaveHumidity10Minutes(stationNo, timestamp, value);
        }


        /*
        Skal vi have min/max/middel temperatur eller bliver det beregnet i obsdb ?
         */

        return null;
    }
}
