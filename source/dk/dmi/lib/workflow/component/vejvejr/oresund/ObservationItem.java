package dk.dmi.lib.workflow.component.vejvejr.oresund;

enum ObservationItem {
	AirTemp(1, 1),
	Humidity(2, 2),
	AirDewPoint(3, 1),
	WindSpeed10(5, 1),
	WindDir10(6, 2),
	Precipitation(9, 1),
	Visibility(11, 2),
	SurfaceTemp(30, 1);	
	
	private int itemNumber;		// VSOP file item number of observation type 
	private int itemValueType;	// Value type of observation type (1 = double, 2 = int)
	ObservationItem(int itemNumber, int itemValueType) {
		this.itemNumber = itemNumber;
		this.itemValueType = itemValueType;
	}
	
	public int getItem() {
		return itemNumber;
	}
	
	public int getItemValueType() {
		return itemValueType;
	}
}