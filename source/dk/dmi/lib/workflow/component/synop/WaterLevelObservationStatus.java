package dk.dmi.lib.workflow.component.synop;

public enum WaterLevelObservationStatus {
	NEW(0),
	SENT_TO_PRODUCTION(1),
	IGNORED(2);
	
	private int status;
	WaterLevelObservationStatus(int status) {
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}
}
