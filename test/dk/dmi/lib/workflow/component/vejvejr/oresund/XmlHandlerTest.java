package dk.dmi.lib.workflow.component.vejvejr.oresund;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Assert;
import org.junit.Test;

import dk.dmi.lib.workflow.component.vejvejr.oresund.OresundObservation;

public class XmlHandlerTest {

	private static final double SURFACE_TEMP = 11.4;
	private static final double AIR_TEMP = 8.8;
	private static final String STATION_ID = "54WES0001";
	String XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
			"<weather xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"+
			  "<updated>2017-10-30T13:54:00Z</updated>"+
			  "<sensors>"+
				  	"<sensor id=\"54WES0000.AirTemp\">"+
					  "<trv />"+
					  "<location>General (Calculated value based on all weather stations)</location>"+
					  "<description>AIR TEMPERATURE</description>"+
					  "<value>7.68</value>"+
					"</sensor>"+
					"<sensor id=\"54WES0000.WindDir10\">"+
					  "<trv />"+
					  "<location>General (Calculated value based on all weather stations)</location>"+
					  "<description>AVERAGE WIND DIRECTION 10 MIN</description>"+
					  "<value>310</value>"+
					"</sensor>"+
					"<sensor id=\"54WES0000.WindSpeed\">"+
					  "<trv />"+
					  "<location>General (Calculated value based on all weather stations)</location>"+
					  "<description>CURRENT WIND SPEED</description>"+
					  "<value>11.48</value>"+
					"</sensor>"+
					"<sensor id=\"54WES0000.Visibility\">"+
					  "<trv />"+
					  "<location>General (Calculated value based on all weather stations)</location>"+
					  "<description>VISIBILITY</description>"+
					  "<value>640</value>"+
					"</sensor>"+
					"<sensor id=\""+STATION_ID+".AirDewPoint\">"+
					  "<trv>1231</trv>"+
					  "<location>Kastrup</location>"+
					  "<description>AIR DEW POINT</description>"+
					  "<value>-0.2</value>"+
					"</sensor>"+
					"<sensor id=\""+STATION_ID+".AirTemp\">"+
					  "<trv>1231</trv>"+
					  "<location>Kastrup</location>"+
					  "<description>AIR TEMPERATURE</description>"+
					  "<value>"+AIR_TEMP+"</value>"+
					"</sensor>"+
					"<sensor id=\""+STATION_ID+".DirtLevel\">"+
					  "<trv>1231</trv>"+
					  "<location>Kastrup</location>"+
					  "<description>DIRT LEVEL</description>"+
					  "<value>11</value>"+
					"</sensor>"+
					"<sensor id=\""+STATION_ID+".FreezePointTemp\">"+
					  "<trv>1231</trv>"+
					  "<location>Kastrup</location>"+
					  "<description>FREEZING POINT TEMPERATURE</description>"+
					  "<value />"+
					"</sensor>"+
					"<sensor id=\""+STATION_ID+".Humidity\">"+
					  "<trv>1231</trv>"+
					  "<location>Kastrup</location>"+
					  "<description>AIR RELATIVE HUMIDITY</description>"+
					  "<value>50</value>"+
					"</sensor>"+
					"<sensor id=\""+STATION_ID+".Precipitation\">"+
					  "<trv>1231</trv>"+
					  "<location>Kastrup</location>"+
					  "<description>PRECIPITATION</description>"+
					  "<value>0</value>"+
					"</sensor>"+
					"<sensor id=\""+STATION_ID+".RemSurfaceTemp\">"+
					  "<trv>1231</trv>"+
					  "<location>Kastrup</location>"+
					  "<description>REMOTE SURFACE TEMPERATURE</description>"+
					  "<value />"+
					"</sensor>"+
					"<sensor id=\""+STATION_ID+".SlipperyRoad\">"+
					  "<trv>1231</trv>"+
					  "<location>Kastrup</location>"+
					  "<description>SLIPPERY ROAD</description>"+
					  "<value>0</value>"+
					"</sensor>"+
					"<sensor id=\""+STATION_ID+".SurfaceTemp\">"+
					  "<trv>1231</trv>"+
					  "<location>Kastrup</location>"+
					  "<description>SURFACE TEMPERATURE</description>"+
					  "<value>"+SURFACE_TEMP+"</value>"+
					"</sensor>"+
					"<sensor id=\""+STATION_ID+".TypeOfPrecipitation\">"+
					  "<trv>1231</trv>"+
					  "<location>Kastrup</location>"+
					  "<description>TYPE OF PRECIPITATION</description>"+
					  "<value>1</value>"+
					"</sensor>"+
			"</sensors>"+
			"</weather>";
	
	@Test
	public void testXmlParser() throws Exception {
		InputStream stream = new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8.name()));
		
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser saxParser = factory.newSAXParser();
		XmlHandler handler = new XmlHandler();
		saxParser.parse(stream, handler);			
		
		Map<String, OresundObservation> observation = handler.getObservationVOs();

		int itemAirTemp = ObservationItem.AirTemp.getItem();
		int itemSurfaceTemp = ObservationItem.SurfaceTemp.getItem();
		
		Assert.assertEquals(AIR_TEMP, observation.get(STATION_ID).getObservations().get(itemAirTemp));
		Assert.assertEquals(SURFACE_TEMP, observation.get(STATION_ID).getObservations().get(itemSurfaceTemp));
		Assert.assertEquals("2017-10-30T13:54:00Z", observation.get(STATION_ID).getTime());
	}
}
