package dk.dmi.lib.workflow.component.synop.asiaq;

import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;

@Component(
		name = "Parse ZRX file", 
		category = "Asiaq",
		description = "Parse file.",
        version = 1)
public class ParseZrxData extends BaseComponent {

	private static final String HEADER_PREFIX = "#";
	private static final String DATE_FORMAT = "yyyyMMddHHmmss";
	private static final String KEY_STATION_NUMBER = "SANR";
	private static final String KEY_PARAMETER_NAME = "CNAME";
	private static final String KEY_OBSERVATIONS_PER_DAY = "CMW";
	private static final String KEY_UNIT = "CUNIT";
	private static final String INVALID_DATA = "RINVAL";
	private static final String TIME_ZONE = "TZ";	
	
	private WorkflowContextController workflowContextController;
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDescriptions = {"String containing observations in ZRX format."}, 
			returnDescription = "List of type (dk.dmi.lib.synop.asiaq.ObservationWithType, parameter type).")
	public List<ObservationWithType> parseZrxFile(String zrxObservation) {
		return parseZrxFormat(zrxObservation);
	}
		
	public List<ObservationWithType> parseZrxFormat(String zrxObservation) {		
		List<ObservationWithType> observations = new ArrayList<>();

		String[] lines = zrxObservation.split(System.lineSeparator());
		int lineNo = 0;
		while (lineNo < lines.length) {
		
			// Read header lines
			StringBuilder header = new StringBuilder(); 
			while (lineNo < lines.length && lines[lineNo].startsWith(HEADER_PREFIX)) {
				header.append(lines[lineNo].replace(HEADER_PREFIX, "").trim());
				lineNo++;
			}
			
			String[] headerRecords = header.toString().split("\\|\\*\\|");
			
			try {
				String stationNo = extractValueFromHeader(KEY_STATION_NUMBER, headerRecords);						
				if ("".equals(stationNo)) {
					throw new MissingFormatArgumentException("Error in zrx file header. File should contain "+KEY_STATION_NUMBER+" keyword followed by value.");
				}
				
				String dmiStationNo = getDmiStationNo(stationNo);				
				if ("".equals(dmiStationNo)) {
					throw new MissingFormatArgumentException("Error in zrx file header. Asiaq station no :"+stationNo+" is unknown DMI (WMO) station number.");
				}
				
				String parameterName = extractValueFromHeader(KEY_PARAMETER_NAME, headerRecords);						
				if ("".equals(parameterName)) {
					throw new MissingFormatArgumentException("Error in zrx file header. File should contain "+KEY_PARAMETER_NAME+" keyword followed by value.");
				}			
				
				String unit = extractValueFromHeader(KEY_UNIT, headerRecords);						
				if ("".equals(unit)) {
					throw new MissingFormatArgumentException("Error in zrx file header. File should contain "+KEY_UNIT+" keyword followed by value.");
				}
				
				String invalidValue = extractValueFromHeader(INVALID_DATA, headerRecords);						
				if ("".equals(invalidValue)) {
					throw new MissingFormatArgumentException("Error in zrx file header. File should contain "+INVALID_DATA+" keyword followed by value.");
				}
				
				String timeZone = extractValueFromHeader(TIME_ZONE, headerRecords);						
				if ("".equals(timeZone)) {
					throw new MissingFormatArgumentException("Error in zrx file header. File should contain "+TIME_ZONE+" keyword followed by value.");
				}
							
				String observationsPerDay = extractValueFromHeader(KEY_OBSERVATIONS_PER_DAY, headerRecords);
				if ("".equals(observationsPerDay)) {
					throw new MissingFormatArgumentException("Error in zrx file header. File should contain "+KEY_OBSERVATIONS_PER_DAY+" keyword followed by value.");
				}								
				int observationInterval = 24*60 / Integer.parseInt(observationsPerDay);
				
				// Read observation lines
				LOGGER.debug("Reading Asiaq observation data for station "+stationNo+" and parameter " + parameterName + " (" + observationInterval + ")");
				while (lineNo < lines.length && !lines[lineNo].startsWith(HEADER_PREFIX)) {
					
					String[] observationRecords = lines[lineNo].trim().split(" ");
					
					long utcUnixTimeStamp = DateUtils.getUtcDateTime(observationRecords[0], timeZone, DATE_FORMAT).toEpochSecond();
					
					String value = observationRecords[1];
					if (!value.equals(invalidValue)) {				
						observations.add(new ObservationWithType(stationNo, parameterName, observationInterval, utcUnixTimeStamp, value, unit));
					}
					
					lineNo++;				
				}
			} catch (NumberFormatException | MissingFormatArgumentException e) {
				LOGGER.error("Error in ParseZrxData.", e);
				workflowContextController.addObjectToContext("ERROR", e.getMessage(), false);
				return null;
			}
		}
		
		return observations;
	}
	
	private String getDmiStationNo(String stationNo) {		
		Object contextObject = workflowContextController.getObjectForKey("ASIAQ2DMI");
		
		try {
			Map<String, String> a2d = (Map<String, String>) contextObject;
			if (a2d.get(stationNo) != null) {
				return a2d.get(stationNo);
			}
			
			LOGGER.warn("ASIAQ station " + stationNo + " was not found in DMI station list.");
		} catch (ClassCastException e) {
			LOGGER.error("ASIAQ DMI station map was not found.", e);
		}
		
		return "";
			
//		04204 / 671
//		04206 / 526
//		04210 / 536
//		04212 / 556
//		04217 / 527
//		04218 / 523
//		04222 / 545
//		04225 / 529
//		04233 / 531
//		04239 / 528
//		04248 / 532
//		04249 / 522
//		04255 / 537
//		04259 / 525
//		04280 / 502_2
//		04283 / 501_2
	}

	private String extractValueFromHeader(String key, String[] headerRecords) {
		for (String headerRecord : headerRecords) {
			if (headerRecord.startsWith(key)) {
				return headerRecord.replace(key, "");
			}
		}
		
		return "";
	}

}