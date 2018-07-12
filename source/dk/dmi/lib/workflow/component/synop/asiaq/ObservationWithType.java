package dk.dmi.lib.workflow.component.synop.asiaq;

import java.util.List;
import java.util.function.Predicate;

public class ObservationWithType {

	private long id;
	private String stationNo; 
	private String observationType;
	private String unit;
	private int observationInterval;
	private long utcUnixTimeStamp; 
	private String value;
	
//	public ObservationWithType(Observation observation, String observationType, int observationInterval, String unit) {
//		this.observation = observation;
//		this.observationType = observationType;
//		this.observationInterval = observationInterval;
//		this.unit = unit;
//	}

	public ObservationWithType(String stationNo, String observationType, int observationInterval, long utcUnixTimeStamp, String value, String unit) {		
		this.stationNo = stationNo;
		this.observationType = observationType;
		this.observationInterval = observationInterval;
		this.utcUnixTimeStamp = utcUnixTimeStamp;
		this.value = value;
		this.unit = unit;
		// TODO Auto-generated constructor stub
	}

//	public Observation getObservation() {
//		return observation;
//	}
	
	public String getUnit() {
		return unit;
	}

	public String getObservationType() {
		return observationType;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public int getObservationInterval() {
		return observationInterval;
	}

	public String getStationNo() {
		return stationNo;
	}

	public long getUtcUnixTimeStamp() {
		return utcUnixTimeStamp;
	}

	public String getValue() {
		return value;
	}
	
	public static long getMaxTime(List<ObservationWithType> observationsWithType) {
		return observationsWithType.stream().mapToLong(i -> i.getUtcUnixTimeStamp()).max().getAsLong();		
	}

	public static long getMinTime(List<ObservationWithType> observationsWithType) {
		return observationsWithType.stream().mapToLong(i -> i.getUtcUnixTimeStamp()).min().getAsLong();
	}
	
	public static void removeDuplicates(List<ObservationWithType> observationsWithType, int observationInterval, long observationTime, String observationType) {
		Predicate<ObservationWithType> observationPredicate = o -> ( o.getObservationInterval() == observationInterval 
				&& (o.getUtcUnixTimeStamp() * 1000) == observationTime
				&& o.getObservationType().equals(observationType));
		
		observationsWithType.removeIf(observationPredicate);
	}
}
