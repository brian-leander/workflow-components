package dk.dmi.lib.workflow.component.synop;

public class WaterLevelObservation {		
	private int dmiCode;
	private String kdiSensorId;
	private long observationTime;
	private float waterlevel;
	private float temperature;	
	private WaterLevelObservationStatus status;	
	
	public WaterLevelObservation(int dmiCode, String kdiSensorId, long observationTime, float waterlevel, float temperature, WaterLevelObservationStatus status) {
		super();
		this.dmiCode = dmiCode;
		this.kdiSensorId = kdiSensorId;
		this.observationTime = observationTime;
		this.waterlevel = waterlevel;
		this.temperature = temperature;
		this.status = status;
	}
	
	public int getDmiCode() {
		return dmiCode;
	}
	
	public String getKdiSensorId() {
		return kdiSensorId;
	}

	public long getObservationTime() {
		return observationTime;
	}
	
	public int getWaterlevel() {
		return (int) waterlevel;
	}
	
	public int getTemperature() {
		return (int) temperature;		
	}
	
	public WaterLevelObservationStatus getStatus() {
		return status;
	}
}
