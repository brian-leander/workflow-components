package dk.dmi.lib.workflow.component.synop.kdi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;
import dk.dmi.lib.workflow.component.synop.PS416DataType;
import dk.dmi.lib.workflow.component.synop.WaterLevelObservationStatus;
import dk.dmi.lib.workflow.component.synop.WaterLevelObservation;

@Component(
		name = "Load observations from obs6 DB.", 
		category = "VST",
		description = "Get PS416 observations from obs6 DB.",
        version = 1)
public class GetKDIObservations extends BaseComponent {

	private WorkflowContextController workflowContextController;
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {},
			argumentDescriptions = {},
			returnDescription = "Map of KDI observations.")
	public Map<Long, List<WaterLevelObservation>> execute() {	
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			final Connection connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
			final Statement statement = connection.createStatement();
			
			String sqlObservationTimes = "select dkf.observation_time from data_kdi_file dkf " + 
							 "join data_kdi dk on dk.observation_time=dkf.observation_time " +
							 "where dk.sent_to_production="+WaterLevelObservationStatus.NEW.getStatus()+" and dkf.data_type='" + PS416DataType.VANDSTAND.name() + "' group by dkf.observation_time";		
			final ResultSet observationTimes = statement.executeQuery(sqlObservationTimes);

			List<Date> observationTimesResultSet = new ArrayList<>();
			while (observationTimes.next()) {				
				int observationTimeIndex = observationTimes.findColumn("observation_time");			
				Date observationTime = new Date(observationTimes.getTimestamp(observationTimeIndex).getTime());
				observationTimesResultSet.add(observationTime);
			}

			Map<Long, List<WaterLevelObservation>> obsMap = new HashMap<>();
			for (Date observationTime : observationTimesResultSet) {				
				String sql = "select distinct(skd.dmi_code), dk.kdi_sensor_id, dk.waterlevel, dk.temperature from data_kdi_file dkf " +
							 "inner join data_kdi dk on dk.observation_time=dkf.observation_time " +
							 "left join station_kdi_dmi skd on skd.kdi_sensor_id_name=dk.kdi_sensor_id " + 
							 "where dk.sent_to_production="+WaterLevelObservationStatus.NEW.getStatus()+" and dkf.data_type='" + PS416DataType.VANDSTAND.name() + "' " +
							 "and dkf.observation_time = FROM_UNIXTIME("+observationTime.getTime()/1000+");";
				final ResultSet result = statement.executeQuery(sql);
	
				List<WaterLevelObservation> waterLevelObservations = new ArrayList<>(); 				
				while (result.next()) {
					int dmiCodeIndex = result.findColumn("dmi_code");			
					int dmiCode = result.getInt(dmiCodeIndex);
					
					int kdiSensorIdIndex = result.findColumn("kdi_sensor_id");			
					String kdiSensorId = result.getString(kdiSensorIdIndex);														
					
					int waterlevelIndex = result.findColumn("waterlevel");
					float waterlevel = result.getFloat(waterlevelIndex);
					
					int temperatureIndex = result.findColumn("temperature");
					float temperature = result.getFloat(temperatureIndex);
										
					WaterLevelObservationStatus status = getStatus(dmiCode, kdiSensorId);
					WaterLevelObservation waterLevelObservation = new WaterLevelObservation(dmiCode, kdiSensorId, observationTime.getTime(), waterlevel, temperature, status);
					waterLevelObservations.add(waterLevelObservation);
				}
				
				obsMap.put(observationTime.getTime(), waterLevelObservations);
			}
			
			statement.close();
			connection.close();
			
			return obsMap;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			LOGGER.error("Error fetching KDI observations." + System.lineSeparator() + "Error: " + e.getMessage(), e);			
			workflowContextController.addObjectToContext("FETCH_OBSERVATIONS_ERROR", "Error executing GetKDIObservations. -- exception message: " + e.getMessage(), false);
		}
		
		return Collections.emptyMap();
	}

	private WaterLevelObservationStatus getStatus(int dmiCode, String kdiSensorId) {
		if (dmiCode == 0) {
			LOGGER.warn("Observation for KDI sensor " + kdiSensorId + " does not have equivalent DMI code. Observation will be ignored.");
			return WaterLevelObservationStatus.IGNORED;
		}
		
		return WaterLevelObservationStatus.NEW;
	}
	
}
