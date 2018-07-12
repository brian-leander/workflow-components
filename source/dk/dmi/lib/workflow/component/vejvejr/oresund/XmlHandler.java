package dk.dmi.lib.workflow.component.vejvejr.oresund;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler {
	private Map<String, OresundObservation> observationVOs = new HashMap<>();
	private String observationTime;
	private Stack<String> elementStack = new Stack<>();
	private Stack<Object> objectStack  = new Stack<>();
    private Stack<Attributes> attributeStack  = new Stack<>();
    
    
	public Map<String, OresundObservation> getObservationVOs() {
		return observationVOs;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {	
		this.elementStack.push(qName);
        this.attributeStack.push(attributes);
        
        if ("sensor".equals(qName)) {                	
        	String sensorId = currentAttribute().getValue("id");
        	
        	OresundObservation observation = getOresundObservation(sensorId);
        	this.objectStack.push(observation);  
        }
	}
	
	private OresundObservation getOresundObservation(String sensor) {
		String[] sensorIdArray = sensor.split("\\.");
		String sensorId = sensorIdArray[0];
		String observationType = sensorIdArray[1];
		
		OresundObservation observation;
		if (observationVOs.containsKey(sensorId)) {
			observation = observationVOs.get(sensorId);
		} else {		
			observation = new OresundObservation(observationTime);
			observationVOs.put(sensorId, observation);	
		}
		observation.setCurrentObservationElement(observationType);
		
		return observation;
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		this.elementStack.pop();
        this.attributeStack.pop();

        if("sensor".equals(qName)){
            this.objectStack.pop();            
        }
	}
	
	public void characters(char ch[], int start, int length) throws SAXException {
		String value = new String(ch, start, length).trim();
    	if (value.length() == 0) {
    		return; // ignore white space
    	}
    	
    	if ("updated".equals(currentElement())) {
        	observationTime = value; 
        	return;
    	}
    	
    	OresundObservation observation = (OresundObservation) this.objectStack.peek(); 
    	if ("value".equals(currentElement())) {
    		observation.setCurrentObservationValue(value);
    	}
	}
	
	private String currentElement() {
        return this.elementStack.peek();
    }
	
	private Attributes currentAttribute() {
        return this.attributeStack.peek();
    }	
}
