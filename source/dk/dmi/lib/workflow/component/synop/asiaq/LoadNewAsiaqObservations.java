package dk.dmi.lib.workflow.component.synop.asiaq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.synop.observation.Observation;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Load Asiaq observations.", 
		category = "Asiaq",
		description = "",
        version = 1)
public class LoadNewAsiaqObservations extends BaseComponent {

	private WorkflowContextController workflowContextController;
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {},
			argumentDescriptions = {},
			returnDescription = "Map of Observation id and Asiaq observations.")
	public Map<Integer, Observation> execute() {
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			final Connection connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
			final Statement statement = connection.createStatement();
						
			Map<Integer, Observation> obsMap = new HashMap<>();
			Map<String, Set<Long>> accumulation = new HashMap<>();
			Map<String, Set<Long>> minMax = new HashMap<>();
	 				
			String sqlLoadNewObservations = 
					"select da.id, da.observation_type, da.observation_interval, da.value, da.observation_time, sa.dmi_station_number, sa.asiaq_station_number from data_asiaq_history da "
							+ "join station_asiaq sa on sa.asiaq_station_number=da.asiaq_station_id "
							+ "where da.sent_to_production=0 order by da.observation_time ";
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
				
				Observation observation = getObservationType(observationType, observationInterval, dmiStationId, observationTime, value);
				if (observation != null) {
					obsMap.put(id, observation);
				}				
				
				if (getAccumulation(observationTime) != null) {
					int asiaqStationNumberIndex = observations.findColumn("asiaq_station_number");
					String asiaqStationNumber = observations.getString(asiaqStationNumberIndex);
					
					addObservationTimeToPrecipitationAccumulation(accumulation, observationTime, asiaqStationNumber);
				}
				
				ZonedDateTime startTime = Instant.ofEpochMilli(observationTime).atZone(ZoneId.of("UTC") );							
				if (startTime.getMinute() == 0 && (startTime.getHour() == 6 || startTime.getHour() == 18)) {				
					int asiaqStationNumberIndex = observations.findColumn("asiaq_station_number");
					String asiaqStationNumber = observations.getString(asiaqStationNumberIndex);
					
					if (minMax.containsKey(asiaqStationNumber)) {
						Set<Long> times = minMax.get(asiaqStationNumber);
						times.add(observationTime);							
					} else {
						Set<Long> times = new HashSet<>();
						times.add(observationTime);
						
						minMax.put(asiaqStationNumber, times);
					}
				}
			}
			
			if (obsMap.isEmpty()) {
				statement.close();
				connection.close();
				return obsMap;
			}
			
			int id = 0;
			if (!accumulation.isEmpty()) {
				// loop accumau map
				String sqlLoadAccumulatedPrecipitationObservations = createPrecipitationAccumulationSql(accumulation);			
				
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
					
					Observation observation = getObservationType(observationType, observationInterval, dmiStationId, observationTime, value);
					if (observation != null) {					
						obsMap.put(--id, observation);
					}
				}
			}
			
			if (minMax.isEmpty()) {
				statement.close();
				connection.close();
				return obsMap;
			}
			
			String sqlLoadMinMaxTemperatureObservations = "";
			for (String stationNo : minMax.keySet()) {
				Set<Long> obsTimes = minMax.get(stationNo);
				
				for (long observationTime : obsTimes) {
					if (sqlLoadMinMaxTemperatureObservations.length() > 0) {
						sqlLoadMinMaxTemperatureObservations += " union " + System.lineSeparator();
					}
					
					observationTime /= 1000;
					int interval = 12;
					
					// loop max min temp
					sqlLoadMinMaxTemperatureObservations += "SELECT max(cast(value AS decimal(5,1))) as mxValue, min(cast(value AS decimal(5,1))) as minValue, t.id, t.asiaq_station_id, t.observation_type, "+interval+" as observation_interval, FROM_UNIXTIME("+observationTime+") as observation_time, sa.dmi_station_number FROM data_asiaq_history t"+
							" join station_asiaq sa on sa.asiaq_station_number=t.asiaq_station_id"+ 
							" WHERE (t.observation_type like 'ATM%' or t.observation_type like 'ATN%') "+
							" and t.observation_time BETWEEN DATE_ADD(FROM_UNIXTIME("+observationTime+"),INTERVAL -"+interval+" HOUR) AND FROM_UNIXTIME("+observationTime+")"+
							" and t.asiaq_station_id='"+stationNo+"'"+
							" group by t.asiaq_station_id, t.observation_type";					
				}
			}
			
			sqlLoadMinMaxTemperatureObservations += ";";
			
			final ResultSet minMaxTemperatureObservations = statement.executeQuery(sqlLoadMinMaxTemperatureObservations);			
			while (minMaxTemperatureObservations.next()) {
				int dmiStationIdIndex = minMaxTemperatureObservations.findColumn("dmi_station_number");			
				String dmiStationId = minMaxTemperatureObservations.getString(dmiStationIdIndex);	
				
				int observationTypeIndex = minMaxTemperatureObservations.findColumn("observation_type");
				String observationType = minMaxTemperatureObservations.getString(observationTypeIndex);

				int valueIndex = observationType.startsWith("ATM") ? minMaxTemperatureObservations.findColumn("mxValue") : minMaxTemperatureObservations.findColumn("minValue");
				String value = minMaxTemperatureObservations.getString(valueIndex);				
				
				int observationIntervalIndex = minMaxTemperatureObservations.findColumn("observation_interval");
				int observationInterval = minMaxTemperatureObservations.getInt(observationIntervalIndex);
				
				int observationTimeIndex = minMaxTemperatureObservations.findColumn("observation_time");
				long observationTime = minMaxTemperatureObservations.getTimestamp(observationTimeIndex).getTime();
				
				Observation observation = getObservationType(observationType, observationInterval, dmiStationId, observationTime, value);
				if (observation != null) {					
					obsMap.put(--id, observation);
				}
			}
			
			statement.close();
			connection.close();
			
			return obsMap;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			LOGGER.error("Error fetching Asiaq observations." + System.lineSeparator() + "Error: " + e.getMessage(), e);			
			workflowContextController.addObjectToContext("FETCH_OBSERVATIONS_ERROR", "Error executing LoadNewAsiaqObservations. -- exception message: " + e.getMessage(), false);
		}
		
		return Collections.emptyMap();
	}

	private String createPrecipitationAccumulationSql(Map<String, Set<Long>> accumulation) {
		String sqlLoadAccumulatedPrecipitationObservations = "";
		for (String stationNo : accumulation.keySet()) {
			Set<Long> obsTimes = accumulation.get(stationNo);
			
			for (long observationTime : obsTimes) {
				
				int interval = getAccumulation(observationTime).getInterval();
				
				if (sqlLoadAccumulatedPrecipitationObservations.length() > 0) {
					sqlLoadAccumulatedPrecipitationObservations += " union " + System.lineSeparator();
				}
				
				observationTime /= 1000;
				sqlLoadAccumulatedPrecipitationObservations += 
						"SELECT sum(t.value) as value, t.observation_type, "+interval+" as observation_interval , FROM_UNIXTIME("+observationTime+") as observation_time, sa.dmi_station_number"+
							" FROM data_asiaq_history t"+
							" join station_asiaq sa on sa.asiaq_station_number=t.asiaq_station_id"+
							" WHERE t.observation_type like 'PRE%'"+ // 'PRE%' changed from 'PRE2016'
							" and t.observation_time BETWEEN DATE_ADD(FROM_UNIXTIME("+observationTime+"),INTERVAL -"+interval+" HOUR) AND FROM_UNIXTIME("+observationTime+")"+
							" and sa.asiaq_station_number='"+stationNo+"'" +
							" group by t.asiaq_station_id";
			}
		}
		
		return sqlLoadAccumulatedPrecipitationObservations + ";";
	}

	private void addObservationTimeToPrecipitationAccumulation(Map<String, Set<Long>> accumulation, long observationTime, String asiaqStationNumber) {
		if (accumulation.containsKey(asiaqStationNumber)) {
			Set<Long> times = accumulation.get(asiaqStationNumber);
			times.add(observationTime);	
			return;
		}
		
		Set<Long> times = new HashSet<>();
		times.add(observationTime);
		
		accumulation.put(asiaqStationNumber, times);							
	}
	
	private PrecipitationAccumulationHours getAccumulation(long observationTime) {
		ZonedDateTime startTime = Instant.ofEpochMilli(observationTime).atZone(ZoneId.of("UTC") );
		for (PrecipitationAccumulationHours precip : PrecipitationAccumulationHours.values()) {					
			if (startTime.getMinute() == 0 && startTime.getHour() == precip.getHour()) {
				return precip;
			}
		}
		
		return null;
	}
	
	private Observation getObservationType(String parameterName, int observationInterval, String stationNo, long timestamp, String value) {
		if (parameterName.startsWith("ATA") && observationInterval == 60) {
			return Observation.forAverageTemperatureHour(stationNo, timestamp, value);			
		}
		if (parameterName.startsWith("ATM") && observationInterval == 60) {
			return Observation.forMaximumTemperatureHour(stationNo, timestamp, value);			
		}
		if (parameterName.startsWith("ATM") && observationInterval == 12) {
			return Observation.forMaximumTemperature_12Hour(stationNo, timestamp, value);			
		}		
		if (parameterName.startsWith("ATN") && observationInterval == 60) {
			return Observation.forMinimumTemperatureHour(stationNo, timestamp, value);			
		}
		if (parameterName.startsWith("ATN") && observationInterval == 12) {
			return Observation.forMinimumTemperature_12Hour(stationNo, timestamp, value);			
		}
		if (parameterName.startsWith("AT") && observationInterval == 60) {
			return Observation.forCurrentTemperature(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("RH") && observationInterval == 60) { // temp inserted for historic data import
			return Observation.forHumidityHour(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("RH") && observationInterval == 10) {
			return Observation.forHumidity10Minutes(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("WD") && observationInterval == 60) {
			return Observation.forWindDirectionHour(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("WD") && observationInterval == 10) {
			return Observation.forWindDirection10Minutes(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("WSM") && observationInterval == 60) {
			return Observation.forWindSpeedMaximumHour(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("WSM") && observationInterval == 10) {
			return Observation.forWindSpeedMaximum10Minutes(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("WS") && observationInterval == 60) {
			return Observation.forWindSpeedHour(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("WS") && observationInterval == 10) {
			return Observation.forWindSpeed10Minutes(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("SRI") && observationInterval == 60) {
			return Observation.forShortwaveRadiationHour(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("SRI") && observationInterval == 10) {
			return Observation.forShortwaveRadiation10Minutes(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("PRE") && observationInterval == 60) {
			return Observation.forPrecipitationHour(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("PRE") && (observationInterval == 6 || observationInterval == 12)) {
			return Observation.forPrecipitationAccumulatedHours(stationNo, timestamp, value, observationInterval);
		}
		if (parameterName.startsWith("QFE") && observationInterval == 60) {
			return Observation.forAirPressureHour(stationNo, timestamp, value);
		}
		if (parameterName.startsWith("UVB") && observationInterval == 60) {
			return Observation.forAverageUVBHour(stationNo, timestamp, value);
		}		
		
		return null;
	}	
}

enum PrecipitationAccumulationHours {
	MIDNIGHT(0, 6),
	MORNING(6, 12),
	MIDDAY(12, 6),
	EVENING(18, 12);
	
	private int hour; 
	private int interval;
	
	PrecipitationAccumulationHours(int hour, int interval) {
		this.hour = hour;
		this.interval = interval;
	}
	
	public int getHour() {
		return hour;
	}
	
	public int getInterval() {
		return interval;
	}
}
