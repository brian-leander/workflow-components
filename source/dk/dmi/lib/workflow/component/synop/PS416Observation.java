package dk.dmi.lib.workflow.component.synop;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import dk.dmi.lib.workflow.component.synop.PS416Data.PS416ObservationDataVO;

public class PS416Observation {

	private final Long observationTime, productionTime;
	private final List<ObservationVO> waterlevel;
	private final String supplier, fileName, xml;
	private final PS416DataType dataType;
	 
	public static class PS4ObservationBuilder {
		private String productionTime, observationTime, supplier, fileName, xml;
		private PS416DataType dataType;
		private List<PS416ObservationDataVO> observations = new ArrayList<>();
		
		public PS4ObservationBuilder(String timestamp, PS416DataType dataType) {			
			this.observationTime = timestamp;			
			this.dataType = dataType;			
		}
				
		public PS4ObservationBuilder supplier(String supplier) {
			this.supplier = supplier;
			return this;
		}
		
		public PS4ObservationBuilder productionTime(String productionTime) {
			this.productionTime = productionTime;
			return this;
		}
		
		public PS4ObservationBuilder fileName(String fileName) {
			this.fileName = fileName;
			return this;
		}
		
		public PS4ObservationBuilder xml(String xml) {
			this.xml = xml;
			return this;
		}
		
		public PS4ObservationBuilder observation(List<PS416ObservationDataVO> observationVOs) {
			this.observations.addAll(observationVOs);
			return this;
		}
		
		public PS416Observation build() {			
			return new PS416Observation(this);			
		}
	}
	
	public final class ObservationVO {
		private int value;
		private long timestamp;
		private String unit;
		private String sensorId;
		
		public ObservationVO(String sensorId, int value, long timestamp, String unit) {			
			this.sensorId = sensorId;
			this.value = value;
			this.timestamp = timestamp;
			this.unit = unit;
		}

		public String getSensorId() {
			return sensorId;
		}
		
		public int getValue() {
			return value;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public String getUnit() {
			return unit;
		}
		
	}
		
	private PS416Observation(PS4ObservationBuilder builder) {
		if (builder.observationTime == null || builder.observationTime.equals("")) {
			throw new IllegalArgumentException("Error: timestamp must exist.");
		}
		
		this.observationTime = validateTimeStamp(builder.observationTime);
		
		if (builder.dataType == null || builder.dataType.equals("")) {
			throw new IllegalArgumentException("Error: dataType id is unknown.");
		}
		dataType = builder.dataType;		
		
		if (builder.observations == null || builder.observations.size() == 0) {
			waterlevel = Collections.emptyList();
		} else {
			waterlevel = new ArrayList<>();			
			for (PS416ObservationDataVO waterlevelObs : builder.observations) {
				if (waterlevelObs.getSensorId() == null || waterlevelObs.getSensorId().equals("")) {
					throw new IllegalArgumentException("Error: KDI sensor id is unknown.");
				}
					
				try {
					if (PS416DataType.VANDSTAND.equals(dataType)) {
						ObservationVO waterLevelObservationVO = 
								new ObservationVO(
										waterlevelObs.getSensorId(),
										Math.round(Float.valueOf(waterlevelObs.getValue())), 
										validateTimeStamp(waterlevelObs.getValueObservationTime()), 
										waterlevelObs.getValueObservationUnit());
						waterlevel.add(waterLevelObservationVO);
					}
					
					if (PS416DataType.TEMPERATURE.equals(dataType)) {
						Float fl = Float.valueOf(waterlevelObs.getValue()) * new Float(10);					
						
						ObservationVO waterLevelObservationVO = 
								new ObservationVO(
										waterlevelObs.getSensorId(),
										Math.round(fl), 
										validateTimeStamp(waterlevelObs.getValueObservationTime()), 
										waterlevelObs.getValueObservationUnit());
						waterlevel.add(waterLevelObservationVO);
					}
				} catch (NumberFormatException nfe) {
					throw new IllegalArgumentException("Error: Waterlevel/Temperature must be a decimal number. (Was: " + waterlevelObs.getValue() + ")");
				}									
			}
		}	
		
		// TO-DO : wind speed and direction			
		
		supplier = builder.supplier;
		productionTime = builder.productionTime != null ? validateTimeStamp(builder.productionTime) : null;
		fileName = builder.fileName;
		xml = builder.xml;
	}

	private long validateTimeStamp(String timestamp) {
		long utcTime;
		try {
			utcTime = Long.parseLong(timestamp) * 1000;
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Error: timestamp must be valid unix time stamp.");
		}
		
		if (utcTime > System.currentTimeMillis()) { // Is observation time in the future ?
			throw new IllegalArgumentException("Validation Error: Observation time cannot be in the future.");
		}
		
		return utcTime;
	}	

	public Date getTimeStamp() {
		final Calendar time = Calendar.getInstance();
		time.setTimeInMillis(observationTime);
		return time.getTime();
	}

	public PS416DataType getDataType() {
		return dataType;
	}

	public Long getObservationTime() {
		return observationTime;
	}

	public Long getProductionTime() {
		return productionTime;
	}

	public List<ObservationVO> getWaterlevel() {
		return waterlevel;
	}

	public String getSupplier() {
		return supplier;
	}

	public String getFileName() {
		return fileName;
	}

	public String getXml() {
		return xml;
	}
}