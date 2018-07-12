package dk.dmi.lib.workflow.component.synop.asiaq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.MockLogger;

public class ZrxReaderTest {
			
	String zrxObservation = "#ZRXPVERSION2300.100|*|ZRXPCREATORKiIOSystem.ZRXPV2R2_E|*|"+System.lineSeparator()+
			"#SANR545|*|SNAMEIlulissat Lufthavn Climate St.|*|SWATER---|*|CNRATA1203|*|CNAMEATA1203|*|"+System.lineSeparator()+
			"#CMW24|*|"+System.lineSeparator()+
			"#TSPATH/Ilulissat/545/ATA1203/60m.Cmd.P|*|"+System.lineSeparator()+
			"#TZUTC-3|*|RINVAL-777|*|"+System.lineSeparator()+
			"#CUNIT°C|*|"+System.lineSeparator()+
			"#LAYOUT(timestamp,value)|*|"+System.lineSeparator()+
			"20171115040000 -9.0"+System.lineSeparator()+
			"20171115050000 -8.5"+System.lineSeparator()+
			"#ZRXPVERSION2300.100|*|ZRXPCREATORKiIOSystem.ZRXPV2R2_E|*|"+System.lineSeparator()+
			"#SANR545|*|SNAMEIlulissat Lufthavn Climate St.|*|SWATER---|*|CNRAT1203|*|CNAMEAT1203|*|"+System.lineSeparator()+
			"#CMW24|*|"+System.lineSeparator()+
			"#TSPATH/Ilulissat/545/AT1203/60m.Cmd.P|*|"+System.lineSeparator()+
			"#TZUTC-3|*|RINVAL-777|*|"+System.lineSeparator()+
			"#CUNIT°C|*|"+System.lineSeparator()+
			"#LAYOUT(timestamp,value)|*|"+System.lineSeparator()+
			"20171115040000 -8.9"+System.lineSeparator()+
			"20171115050000 -8.4"+System.lineSeparator()+
			"#ZRXPVERSION2300.100|*|ZRXPCREATORKiIOSystem.ZRXPV2R2_E|*|"+System.lineSeparator()+
			"#SANR545|*|SNAMEQasigiannguit Climate St.|*|SWATER---|*|CNRWS1600|*|CNAMEWS1600|*|"+System.lineSeparator()+
			"#CMW24|*|"+System.lineSeparator()+
			"#TSPATH/Qasigiannguit/527/WS1600/60m.Cmd.P|*|"+System.lineSeparator()+
			"#TZUTC-3|*|RINVAL-777|*|"+System.lineSeparator()+
			"#CUNITm/s|*|"+System.lineSeparator()+
			"#LAYOUT(timestamp,value)|*|"+System.lineSeparator()+
			"20171115040000 1.39"+System.lineSeparator()+
			"20171115050000 1.55"+System.lineSeparator()+							
			"#ZRXPVERSION2300.100|*|ZRXPCREATORKiIOSystem.ZRXPV2R2_E|*|"+System.lineSeparator()+
			"#SANR523|*|SNAMEQeqertarsuaq Climate St.|*|SWATER---|*|CNRRH1203|*|CNAMERH1203|*|"+System.lineSeparator()+
			"#CMW24|*|"+System.lineSeparator()+
			"#TSPATH/Qeqertarsuaq/523/RH1203/60m.Cmd.P|*|"+System.lineSeparator()+
			"#TZUTC-3|*|RINVAL-777|*|"+System.lineSeparator()+
			"#CUNIT%|*|"+System.lineSeparator()+
			"#LAYOUT(timestamp,value)|*|"+System.lineSeparator()+
			"20171117070000 78.12"+System.lineSeparator()+
			"20171117080000 77.91"+System.lineSeparator()+
			"#ZRXPVERSION2300.100|*|ZRXPCREATORKiIOSystem.ZRXPV2R2_E|*|"+System.lineSeparator()+
			"#SANR523|*|SNAMEQeqertarsuaq Climate St.|*|SWATER---|*|CNRAT1203|*|CNAMEAT1203|*|"+System.lineSeparator()+
			"#SOURCESYSTEMWISKI|*|SOURCEID82b5cff5-b0cc-493a-aa87-0cee1c62ac7e|*|"+System.lineSeparator()+
			"#CMW144|*|"+System.lineSeparator()+
			"#TSPATH/Qeqertarsuaq/523/AT1203/10m.Cmd.P|*|"+System.lineSeparator()+
			"#TZUTC-3|*|RINVAL-777|*|"+System.lineSeparator()+
			"#CUNIT°C|*|"+System.lineSeparator()+
			"#LAYOUT(timestamp,value)|*|"+System.lineSeparator()+
			"20171117061000 -8.5"+System.lineSeparator()+
			"20171117062000 -8.6"+System.lineSeparator()+
			"20171117063000 -8.5"+System.lineSeparator() + 
			"#ZRXPVERSION2300.100|*|ZRXPCREATORKiIOSystem.ZRXPV2R2_E|*|"+System.lineSeparator() +
			"#SANR545|*|SNAMENuuk Climate St.|*|SWATER---|*|CNRWD1500|*|CNAMEWD1500|*|"+System.lineSeparator() +
			"#SOURCESYSTEMWISKI|*|SOURCEID711bf86f-3d38-4f47-81e6-bc5df81a38ff|*|"+System.lineSeparator() +
			"#CMW24|*|"+System.lineSeparator() +
			"#TSPATH/Nuuk/545/WD1500/h.Cmd|*|"+System.lineSeparator() +
			"#TZUTC-3|*|RINVAL-777|*|"+System.lineSeparator() +
			"#CUNIT°|*|"+System.lineSeparator() +
			"#LAYOUT(timestamp,value)|*|"+System.lineSeparator() +
			"20171115040000 47.39"+System.lineSeparator()+
			"20171115050000 42.54"+System.lineSeparator()+
			"#ZRXPVERSION2300.100|*|ZRXPCREATORKiIOSystem.ZRXPV2R2_E|*|					"+System.lineSeparator()+
			"#SANR522|*|SNAMENuuk Climate St.|*|SWATER---|*|CNRSRI1200|*|CNAMESRI1200|*| "+System.lineSeparator()+
			"#SOURCESYSTEMWISKI|*|SOURCEIDdf6bb314-207a-47e9-b045-917d8b5ccbde|*|        "+System.lineSeparator()+
			"#CMW144|*|                                                                  "+System.lineSeparator()+
			"#TSPATH/Nuuk/522/SRI1200/10m.Mean|*|                                        "+System.lineSeparator()+
			"#TZUTC-3|*|RINVAL-777|*|                                                    "+System.lineSeparator()+
			"#CUNITW/m²|*|                                                               "+System.lineSeparator()+
			"#LAYOUT(timestamp,value)|*|                                                 "+System.lineSeparator()+
			"20171117061000 -2.24                                                        "+System.lineSeparator()+
			"20171117062000 -2.18                                                        "+System.lineSeparator()+
			"20171117063000 -2.28                                                        "+System.lineSeparator()+
			"20171117064000 -2.37                                                        "+System.lineSeparator()+
			"20171117065000 -2.40                                                        "+System.lineSeparator()+
			"20171117070000 -2.16                                                        "+System.lineSeparator()+
			"20171117071000 -1.48                                                        "+System.lineSeparator()+
			"20171117072000 -1.45                                                        "+System.lineSeparator()+
			"20171117073000 -1.72                                                        "+System.lineSeparator()+
			"20171117074000 -1.02                                                        "+System.lineSeparator()+
			"20171117075000 -1.26                                                        "+System.lineSeparator()+
			"20171117080000 -1.19                                                        "+System.lineSeparator()+
			"#ZRXPVERSION2300.100|*|ZRXPCREATORKiIOSystem.ZRXPV2R2_E|*|                  "+System.lineSeparator()+
			"#SANR522|*|SNAMENuuk Climate St.|*|SWATER---|*|CNRSRI1200|*|CNAMESRI1200|*| "+System.lineSeparator()+
			"#SOURCESYSTEMWISKI|*|SOURCEIDe2df3345-bafc-413b-be3e-e8e7ce0ddc20|*|        "+System.lineSeparator()+
			"#CMW24|*|                                                                   "+System.lineSeparator()+
			"#TSPATH/Nuuk/522/SRI1200/h.Mean|*|                                          "+System.lineSeparator()+
			"#TZUTC-3|*|RINVAL-777|*|                                                    "+System.lineSeparator()+
			"#CUNITW/m²|*|                                                               "+System.lineSeparator()+
			"#LAYOUT(timestamp,value)|*|                                                 "+System.lineSeparator()+
			"20171117070000 -2.27                                                        "+System.lineSeparator()+
			"20171117080000 -1.35                                                        "+System.lineSeparator()+
			"#ZRXPVERSION2300.100|*|ZRXPCREATORKiIOSystem.ZRXPV2R2_E|*|"+System.lineSeparator()+
			"#SANR545|*|SNAMENuuk Climate St.|*|SWATER---|*|CNRWSM1500|*|CNAMEWSM1500|*|"+System.lineSeparator()+
			"#CMW144|*|"+System.lineSeparator()+
			"#TSPATH/Nuuk/522/WSM1500/10m.Cmd.P|*|"+System.lineSeparator()+
			"#TZUTC-3|*|RINVAL-777|*|"+System.lineSeparator()+
			"#CUNITm/s|*|"+System.lineSeparator()+
			"#LAYOUT(timestamp,value)|*|"+System.lineSeparator()+
			"20171115041000 8.39"+System.lineSeparator()+
			"20171115042000 8.38"+System.lineSeparator()+
			"20171115043000 6.17"+System.lineSeparator()+
			"20171115044000 5.16"+System.lineSeparator()+
			"20171115045000 5.52"+System.lineSeparator()+
			"20171115050000 6.70"+System.lineSeparator()+
			"20171115051000 6.87"+System.lineSeparator()+
			"20171115052000 7.14"+System.lineSeparator()+
			"20171115053000 8.34"+System.lineSeparator()+
			"20171115054000 7.74"+System.lineSeparator()+
			"20171115055000 7.62"+System.lineSeparator()+
			"20171115060000 9.77"+System.lineSeparator()+
			"#ZRXPVERSION2300.100|*|ZRXPCREATORKiIOSystem.ZRXPV2R2_E|*|"+System.lineSeparator()+
			"#SANR545|*|SNAMENuuk Climate St.|*|SWATER---|*|CNRWSM1500|*|CNAMEWSM1500|*|"+System.lineSeparator()+
			"#CMW24|*|"+System.lineSeparator()+
			"#TSPATH/Nuuk/522/WSM1500/60m.Cmd.P|*|"+System.lineSeparator()+
			"#TZUTC-3|*|RINVAL-777|*|"+System.lineSeparator()+
			"#CUNITm/s|*|"+System.lineSeparator()+
			"#LAYOUT(timestamp,value)|*|"+System.lineSeparator()+
			"20171115040000 8.39"+System.lineSeparator()+
			"20171115050000 9.77";
	
	Map<String, String> contextMap;
	ParseZrxData parseZrxData;
	WorkflowContextController workflowContextController;
	@Before
	public void setup() throws Exception {		
		contextMap = new HashMap<>();
		workflowContextController = new WorkflowContextController();			
		workflowContextController.addObjectToContext("ASIAQ2DMI", contextMap, false);
		
		parseZrxData = new ParseZrxData();
		parseZrxData.injectContext(workflowContextController);
		parseZrxData.injectLogger(new MockLogger());
	}
	
	@Test
	public void testParser() {
		contextMap.put("522", "04249");
		contextMap.put("523", "04218");
		contextMap.put("545", "04222");
		List<ObservationWithType> observations = parseZrxData.parseZrxFormat(zrxObservation);
		
		Assert.assertEquals(41, observations.size());
	}
	
}