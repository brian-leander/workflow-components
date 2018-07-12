package dk.dmi.lib.workflow.component.synop.asiaq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.dmi.lib.persistence.common.UniqueIdentifierGenerator;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Persist Asiaq observation data", 
		category = "Asiaq",
		description = "",
		version = 1)
public class PersistAsiaqData extends BaseComponent {

	private WorkflowContextController workflowContextController;
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"File name", "File Content", "Asiaq observation data."})
	public boolean execute(String fileName, String zrxObservation, List<ObservationWithType> observations) {
		
		long utcUnixTime = getProductionTime(fileName, zrxObservation);
		
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
			connection.setAutoCommit(false);

			final Statement statementFiles = connection.createStatement();

			LOGGER.debug("Saving file info for " + fileName + " to obs1_2.data_asiaq_file");

			statementFiles.executeUpdate("INSERT INTO obs1_2.data_asiaq_file (created, file_name, data, supplier, production_time, observation_time) VALUES(" +
					"FROM_UNIXTIME(" + System.currentTimeMillis() / 1000 + ")," +
					"'" + fileName + "'," +					
					"'" + zrxObservation + "'," +
					"'ASIAQ'," +
					"FROM_UNIXTIME(" + utcUnixTime + ")," +
					"FROM_UNIXTIME(" + observations.get(0).getUtcUnixTimeStamp() + ")"+
					" )");

			LOGGER.debug("Saving observations from file " + fileName + " to obs1_2.data_asiaq");			

			String sql = "INSERT INTO obs1_2.data_asiaq (created, asiaq_station_id, " + 
					"observation_type, observation_interval, value, observation_time, unit, id) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
			
			PreparedStatement statementData = connection.prepareStatement(sql);
			
			for (ObservationWithType observationWithTypeData : observations) {
				long id = UniqueIdentifierGenerator.generateNewId(true);
				observationWithTypeData.setId(id);
				
				statementData.setTimestamp(1, new Timestamp(System.currentTimeMillis()));				
				statementData.setString(2, observationWithTypeData.getStationNo());				
				statementData.setString(3, observationWithTypeData.getObservationType());				
				statementData.setInt(4, observationWithTypeData.getObservationInterval());				
				statementData.setString(5, observationWithTypeData.getValue());
				statementData.setTimestamp(6, new Timestamp(observationWithTypeData.getUtcUnixTimeStamp() * 1000));
				statementData.setString(7, observationWithTypeData.getUnit());				
				statementData.setLong(8, id);							
				statementData.addBatch();				
			}
			
			statementData.executeBatch();
			connection.commit();
			
			statementData.close();
			statementFiles.close();
			connection.close();
			
			return true;
		} catch (Exception e) {
			LOGGER.error("Error in PersistAsiaqData. ", e);
			workflowContextController.addObjectToContext("ERROR", "Error persisting observation data. -- " + e.getMessage(), false);
			if (connection != null) {
	            try {
	                LOGGER.warn("Transaction is being rolled back");
	                connection.rollback();
	            } catch(SQLException excep) {
	            	LOGGER.error("Error in PersistAsiaqData. Cannot roll back transaction.", excep);
	            }
	        }
		}

		return false;
	}
	
	private long getProductionTime(String fileName, String zrxObservation) {		
		Matcher m = Pattern.compile("(\\d{8}-\\d{2}-\\d{2}-\\d{2})").matcher(fileName);
		
		String productionDate ;
		if (m.find()) {        	
			productionDate = m.group(1);
        } else {
        	LOGGER.error("Error in PersistAsiaqData. Unexpected file name. Could not parse production date.");
        	return 0;
        }
        
		String timeZone;
		m = Pattern.compile("TZ[\\w-]*").matcher(zrxObservation);
		if (m.find()) {        	
			timeZone = m.group(0).substring(2);
        } else {
        	LOGGER.error("Error in PersistAsiaqData. No time zone found in file name.");
        	return 0;
        }
		
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyMMdd-HH-mm-ss");
		LocalDateTime date = LocalDateTime.parse(productionDate, formatter);			
		ZonedDateTime zonedDateTime = ZonedDateTime.of(
				date.getYear(), 
				date.getMonthValue(), 
				date.getDayOfMonth(), 
				date.getHour(), 
				date.getMinute(), 
				date.getSecond(), 
				0, ZoneId.of(timeZone));
		
		return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toEpochSecond();
	}
	
}