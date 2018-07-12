package dk.dmi.lib.workflow.component.synop;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.component.synop.PS416Observation.PS4ObservationBuilder;

public class CreateSynopCodeTest {
	CreateSynopCode createSynopCode;
	List<Integer> ps4StationData;
	String expectedResult;
	PS4ObservationBuilder ps4ObservationBuilder;
	PS416Observation ps4Observation;
	WorkflowContextController workflowContextController;
	Calendar cal; 
	final static String PS4_TIMESTAMP = "1505471807"; 
	final static String PS4_STATION_NO = "12234", PS4_STATION_NO_2 = "23680";
	final static String PS4_VST_METER = "12", PS4_VST_NEGATIVE_METER = "-13" , PS4_VST_UNIT = "cm"; 
	final static String PS4_TEMPERATURE_CELCIUS = "120", PS4_TEMPERATURE_NEGATIVE_CELCIUS = "-20", PS4_TEMPERATURE_UNIT = "celcius";
	
	@Before
	public void setup() throws Exception {
		createSynopCode = new CreateSynopCode();		
		cal = Calendar.getInstance();
		cal.setTimeInMillis(Long.parseLong(PS4_TIMESTAMP) * 1000);		
	}
	
	@Test
	public void testCreateSynopCode_withPositiveValues() throws Exception {				
		List<WaterLevelObservation> waterLevelObservations = new ArrayList<>();
		WaterLevelObservation obs1 = new WaterLevelObservation(Integer.parseInt(PS4_STATION_NO), "", Long.parseLong(PS4_TIMESTAMP) * 1000, Integer.parseInt(PS4_VST_METER), Integer.parseInt(PS4_TEMPERATURE_CELCIUS), WaterLevelObservationStatus.NEW);
		waterLevelObservations.add(obs1);
		String result = createSynopCode.execute(waterLevelObservations);
		Assert.assertEquals(getExpectedResultWithPositiveValues(Integer.parseInt(PS4_VST_METER), Integer.parseInt(PS4_TEMPERATURE_CELCIUS)), result);
	}
	
	@Test
	public void testCreateSynopCode_withNegativeValues() throws Exception {				
		List<WaterLevelObservation> waterLevelObservations = new ArrayList<>();
		WaterLevelObservation obs1 = new WaterLevelObservation(Integer.parseInt(PS4_STATION_NO), "", Long.parseLong(PS4_TIMESTAMP) * 1000, Integer.parseInt(PS4_VST_NEGATIVE_METER), Integer.parseInt(PS4_TEMPERATURE_NEGATIVE_CELCIUS), WaterLevelObservationStatus.NEW);
		waterLevelObservations.add(obs1);
		
		String result = createSynopCode.execute(waterLevelObservations);
		Assert.assertEquals(getExpectedResultWithPositiveValues(Integer.parseInt(PS4_VST_NEGATIVE_METER), Integer.parseInt(PS4_TEMPERATURE_NEGATIVE_CELCIUS)), result);
	}
	
	@Test
	public void testCreateSynopCode_withSeveralValues() throws Exception {				
		List<WaterLevelObservation> waterLevelObservations = new ArrayList<>();
		WaterLevelObservation obs1 = new WaterLevelObservation(Integer.parseInt(PS4_STATION_NO), "", Long.parseLong(PS4_TIMESTAMP) * 1000, Integer.parseInt(PS4_VST_NEGATIVE_METER), Integer.parseInt(PS4_TEMPERATURE_NEGATIVE_CELCIUS), WaterLevelObservationStatus.NEW);
		WaterLevelObservation obs2 = new WaterLevelObservation(Integer.parseInt(PS4_STATION_NO_2), "", Long.parseLong(PS4_TIMESTAMP) * 1000, Integer.parseInt(PS4_VST_METER), Integer.parseInt(PS4_TEMPERATURE_CELCIUS), WaterLevelObservationStatus.NEW);
		waterLevelObservations.add(obs1);
		waterLevelObservations.add(obs2);
		
		String result = createSynopCode.execute(waterLevelObservations);
		Assert.assertEquals(getExpectedResultWithSeveralValues(Integer.parseInt(PS4_VST_NEGATIVE_METER), Integer.parseInt(PS4_TEMPERATURE_NEGATIVE_CELCIUS), Integer.parseInt(PS4_VST_METER), Integer.parseInt(PS4_TEMPERATURE_CELCIUS)), result);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSynopCode_ThrowsExceptionWhenParameterIsNull() throws Exception {		
		createSynopCode.execute(Collections.emptyList());
	}
	
	private String getExpectedResultWithPositiveValues(int ps4Vst, int ps4Temperature) {
		String year = new DecimalFormat("0000").format(cal.get(Calendar.YEAR));
		String month = new DecimalFormat("00").format(cal.get(Calendar.MONTH) + 1);
		String day = new DecimalFormat("00").format(cal.get(Calendar.DAY_OF_MONTH));
		String hour = new DecimalFormat("00").format(cal.get(Calendar.HOUR_OF_DAY));
		String minute = new DecimalFormat("00").format(cal.get(Calendar.MINUTE));
		String vst = String.format("%1$" + 4 + "s", ps4Vst); //String.format("%" + 1 + "d", ps4Vst);
		String temperature = String.format("%4d", ps4Temperature);
//		
		return "\u0001" +  System.lineSeparator() + 
				CreateSynopCode.SXDN + CreateSynopCode.BULLETIN_NO + CreateSynopCode.EKMI + day + hour + minute + System.lineSeparator() +
				PS4_STATION_NO + year + month + day + hour + minute + vst + temperature + CreateSynopCode.SALT + "=" + System.lineSeparator() + 
				System.lineSeparator() + "\u0003";
	}	
	
	private String getExpectedResultWithSeveralValues(int ps4Vst, int ps4Temperature, int ps4Vst2, int ps4Temperature2) {
		String year = new DecimalFormat("0000").format(cal.get(Calendar.YEAR));
		String month = new DecimalFormat("00").format(cal.get(Calendar.MONTH) + 1);
		String day = new DecimalFormat("00").format(cal.get(Calendar.DAY_OF_MONTH));
		String hour = new DecimalFormat("00").format(cal.get(Calendar.HOUR_OF_DAY));
		String minute = new DecimalFormat("00").format(cal.get(Calendar.MINUTE));
		String vst = String.format("%" + 4 + "d", ps4Vst); //String.format("%" + 1 + "d", ps4Vst);
		String temperature = String.format("%" + 4 + "d", ps4Temperature);
		
		String vst2 = String.format("%" + 4 + "d", ps4Vst2); //String.format("%" + 1 + "d", ps4Vst);
		String temperature2 = String.format("%" + 4 + "d", ps4Temperature2);
		
		return "\u0001" +  System.lineSeparator() + 
				CreateSynopCode.SXDN + CreateSynopCode.BULLETIN_NO + CreateSynopCode.EKMI + day + hour + minute + System.lineSeparator() +
				PS4_STATION_NO + year + month + day + hour + minute + vst + temperature + CreateSynopCode.SALT + System.lineSeparator() + 
				PS4_STATION_NO_2 + year + month + day + hour + minute + vst2 + temperature2 + CreateSynopCode.SALT + System.lineSeparator() +
				System.lineSeparator() + "\u0003";
	}	
}
