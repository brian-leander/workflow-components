package dk.dmi.lib.workflow.component.vejvejr.oresund;

import java.util.HashMap;
import java.util.Map;

public class OresundObservation {
	private String time;
	private String currentObservationElement;

	private Map<Integer, Number> types = new HashMap<>();
	
	public OresundObservation(String time) {
		this.time = time;
	}
	
	void setCurrentObservationElement(String type) {
		currentObservationElement = type;
	}
	
	void setCurrentObservationValue(String value) {
		for (ObservationItem item : ObservationItem.values()) {
	    	String itemKey = item.name();
	    	if (currentObservationElement.equals(itemKey)) {
	    		Number nValue = getValueAsNumber(value, item);
	    		types.put(item.getItem(), nValue);
	    		break;
	    	}
		}
	}

	private Number getValueAsNumber(String value, ObservationItem item) {
		Number nValue;
		
		switch (item.getItemValueType()) {
			case 2:	
				nValue = new Integer(value);
				break;
			default:
				nValue = new Double(value);				
		}
		return nValue;
	}
	
	public String getTime() {
		return time;
	}
	
	public Map<Integer, Number> getObservations() {
		return types;
	}
	
	public String toString() {
		return "Time: "+ time + " obs: " + types.toString();
	}
}