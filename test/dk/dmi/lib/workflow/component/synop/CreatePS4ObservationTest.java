package dk.dmi.lib.workflow.component.synop;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import dk.dmi.lib.workflow.common.MockLogger;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectLogger;
import dk.dmi.lib.workflow.common.WorkflowComponentLogger;
import junit.framework.Assert;

public class CreatePS4ObservationTest {

	final static String PS4_TIMESTAMP = "1505471807"; 
	final static String PS4_STATION_NO = "12234";
	
	final static String XML_VALID = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+	
	  "<data>"+
		  "<supplier>KDI</supplier>"+
		  "<productiontime format=\"unixtimestamp UTC\">1506417534</productiontime>"+		  
		  "<observations>"+
		    "<observationtime format=\"unixtimestamp UTC\">1497438600</observationtime>"+
		    "<observation>"+
		      "<pnt>NEE_ANALOG4</pnt>"+
		      "<time format=\"unixtimestamp UTC\">1507722000</time>"+
		      "<value unit=\"cm\">37</value>"+
		    "</observation>"+
		    "<observation>"+
		      "<pnt>ASS_FLYDER1_NIV</pnt>"+
		      "<time format=\"unixtimestamp UTC\">1507722000</time>"+
		      "<value unit=\"cm\">24</value>"+
		    "</observation>"+
		    "<observation>"+
		      "<pnt>FÅB_FLYDER1_NIV</pnt>"+
		      "<time format=\"unixtimestamp UTC\">1507722000</time>"+
		      "<value unit=\"cm\">23</value>"+
		    "</observation>"+
		 "</observations>"+
	 "</data>";	
	    
	final static String XML_INVALID = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
			"<data>"+
			  "<supplier>KDI</supplier>"+
			  "<productiontime format=\"unixtimestamp UTC\">1506417534</productiontime>"+
			  "<station>"+
			    "<id>RØM</id>"+			    			  
			    "<observationtime format=\"unixtimestamp UTC\">1497439800</observationtime>"+
			    "<observations>"+
					  "<observation>"+
						  "<ident>5110</ident>"+
						  "<vst unit=\"cm\">-20.0</vst>"+
					  "</observation>"+
					  "<observation>"+
						  "<ident>5101</ident>"+
						  "<vst unit=\"cm\">-18.8</vst>"+
						  "<tw unit=\"celcius\">10.9</tw>"+
					  "</observation>"+
				  "</observations>"+
			  "</station>"+
			"</data>";	
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreatePS4Observation_throwsExceptionWhenXMLisInvalid() throws Exception {			
		CreatePS4Observation createPS4Observation = new CreatePS4Observation() {
			protected String getFileContent(String fileName) throws IOException {				
				return XML_INVALID;
			}
			@InjectLogger
			public void injectLogger(WorkflowComponentLogger logger) {
				LOGGER = logger;
			}
		};		
		createPS4Observation.injectLogger(new MockLogger());
		
		InputStream stream = new ByteArrayInputStream(XML_INVALID.getBytes(StandardCharsets.UTF_8.name()));				
		createPS4Observation.createPS4Observation("VANDSTAND", stream);
	}
	
	@Test
	public void testCreatePS4Observation_XMLisValid() throws Exception {			
		CreatePS4Observation createPS4Observation = new CreatePS4Observation() {
			protected String getFileContent(String fileName) throws IOException {				
				return XML_VALID;
			}
			@InjectLogger
			public void injectLogger(WorkflowComponentLogger logger) {
				LOGGER = logger;
			}
		};		
		createPS4Observation.injectLogger(new MockLogger());
		
		InputStream stream = new ByteArrayInputStream(XML_VALID.getBytes(StandardCharsets.UTF_8.name()));				
		PS416Observation obs = createPS4Observation.createPS4Observation("VANDSTAND", stream);
		
		Assert.assertEquals(new Long(1497438600l * 1000), obs.getObservationTime());
		Assert.assertEquals(3, obs.getWaterlevel().size());
		Assert.assertEquals("NEE_ANALOG4", obs.getWaterlevel().get(0).getSensorId());		
		Assert.assertEquals(37, obs.getWaterlevel().get(0).getValue());		
		Assert.assertEquals("cm", obs.getWaterlevel().get(0).getUnit() );		
		Assert.assertEquals("ASS_FLYDER1_NIV", obs.getWaterlevel().get(1).getSensorId());		
		Assert.assertEquals(24, obs.getWaterlevel().get(1).getValue());
		Assert.assertEquals("FÅB_FLYDER1_NIV", obs.getWaterlevel().get(2).getSensorId());		
		Assert.assertEquals(23, obs.getWaterlevel().get(2).getValue());		
	}
}
