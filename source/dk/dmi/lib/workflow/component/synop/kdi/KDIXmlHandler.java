package dk.dmi.lib.workflow.component.synop.kdi;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dk.dmi.lib.workflow.component.synop.PS416Data;

public class KDIXmlHandler extends DefaultHandler {
	private List<PS416Data> observationDataVOs = new ArrayList<>();
	private Stack<String> elementStack = new Stack<>();
	private Stack<Object> objectStack  = new Stack<>();
    private Stack<Attributes> attributeStack  = new Stack<>();

    public List<PS416Data> getObservationDataVOs() {
		return observationDataVOs;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		this.elementStack.push(qName);
        this.attributeStack.push(attributes);
        
        if ("data".equals(qName)) {                	
        	PS416Data observationDataVO = new PS416Data();
        	this.observationDataVOs.add(observationDataVO);
        	this.objectStack.push(observationDataVO);  
        }
        
        if ("observation".equals(qName)) {
        	PS416Data observationDataVO = (PS416Data) this.objectStack.peek();
        	observationDataVO.createObservationVO();        	
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

    	this.elementStack.pop();
        this.attributeStack.pop();

        if("data".equals(qName)){
            this.objectStack.pop();            
        }
    }
    
    public void characters(char ch[], int start, int length) throws SAXException {
    	String value = new String(ch, start, length).trim();
    	if (value.length() == 0) {
    		return; // ignore white space
    	}

    	PS416Data observationDataVO = (PS416Data) this.objectStack.peek();
    	if ("supplier".equals(currentElement()) && "data".equals(currentElementParent())) {
    		observationDataVO.supplierName = value;
    	} else if ("productiontime".equals(currentElement()) && "data".equals(currentElementParent())) {
    		observationDataVO.productiontime = value;
    	} else if ("observationtime".equals(currentElement()) && "observations".equals(currentElementParent())) {                
    		observationDataVO.observationtime = value;
    	} else if ("pnt".equals(currentElement())) {    		
    		observationDataVO.currentObservationVO.sensorId = value;    		
    	} else if ("value".equals(currentElement())) {    	
			observationDataVO.currentObservationVO.value = value;
			observationDataVO.currentObservationVO.valueObservationUnit = this.attributeStack.peek().getValue("unit");
    	} else if ("time".equals(currentElement()) && true) {
			observationDataVO.currentObservationVO.valueObservationTime = value;    			
    	}
    }
    
    private String currentElement() {
        return this.elementStack.peek();
    }

    private String currentElementParent() {
        if(this.elementStack.size() < 2) {
        	return null;
        }
        return this.elementStack.get(this.elementStack.size()-2);
    }
	
}