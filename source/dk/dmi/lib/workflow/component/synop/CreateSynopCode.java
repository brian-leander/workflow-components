package dk.dmi.lib.workflow.component.synop;

import java.util.Calendar;
import java.util.List;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create VST synop code", 
		category = "VST",
		description = "Create synop code string.",
        version = 1)
public class CreateSynopCode extends BaseComponent {

	private static final String lineSeparator = System.lineSeparator();
	
	static final int BULLETIN_NO = 46;
	static final int SALT = 9999;
	static final String START_OF_HEADER = "\u0001";
	static final String END_OF_TEXT = "\u0003";	
	static final String EKMI = " EKMI ";
	static final String SXDN = "SXDN";
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"List of WaterLevelObservation"}, 
			returnDescription = "Formatted Synop Code")
    public String execute(List<WaterLevelObservation> waterLevelObservations) throws IllegalArgumentException {
		// SXDN46 EKMI 240910 20047201708240910 -1999999999= 
		// SXDN46 EKMI 240910 20043201708240910 -19 1779999= 
		
		if (waterLevelObservations.size() == 0) {
			throw new IllegalArgumentException("No observations to put in synop file.");
		}
		
		final Calendar observationTime = Calendar.getInstance();
		observationTime.setTimeInMillis(waterLevelObservations.get(0).getObservationTime());
		
		String wmoHeader = createHeader(SXDN, BULLETIN_NO, 
				observationTime.get(Calendar.DAY_OF_MONTH), observationTime.get(Calendar.HOUR_OF_DAY), observationTime.get(Calendar.MINUTE));				
		String vstBulletin = createText(waterLevelObservations);

		return vstBulletin.equals("") ? null : (START_OF_HEADER + lineSeparator + wmoHeader + lineSeparator + vstBulletin + lineSeparator + END_OF_TEXT);
	}
	
	private String createHeader(String code, int bulletinNo, int day, int hour, int minute) {
		return code + leftpadZero(bulletinNo, 2) + EKMI + 
			   leftpadZero(day, 2) + leftpadZero(hour, 2) + leftpadZero(minute, 2);
	}
	
	private String createText(List<WaterLevelObservation> waterLevelObservations) {
			
		StringBuilder builder = new StringBuilder();
		for (WaterLevelObservation waterLevelObservation : waterLevelObservations) {
			if (waterLevelObservation.getStatus().equals(WaterLevelObservationStatus.IGNORED)) {
				continue;
			}
			
			Calendar obsTime = Calendar.getInstance();
			obsTime.setTimeInMillis(waterLevelObservation.getObservationTime());
			
			builder.append(leftpadZero(waterLevelObservation.getDmiCode(), 5)).
				append(leftpadZero(obsTime.get(Calendar.YEAR), 4)).
				append(leftpadZero(obsTime.get(Calendar.MONTH) + 1, 2)).
				append(leftpadZero(obsTime.get(Calendar.DAY_OF_MONTH), 2)).
				append(leftpadZero(obsTime.get(Calendar.HOUR_OF_DAY), 2)).
				append(leftpadZero(obsTime.get(Calendar.MINUTE), 2)).
				append(leftpadSpace(waterLevelObservation.getWaterlevel(), 4)).append(leftpadSpace(waterLevelObservation.getTemperature(), 4)).
				append(SALT);
			if (waterLevelObservations.size() == 1) {
				builder.append("=");
			}
			builder.append(lineSeparator);			
		}
		
		return builder.toString();		
    }
	
	private String leftpadSpace(int number, int length) {
		return String.format("%" + length + "d", number);	
	}

	private String leftpadZero(int number, int length) {
		return String.format("%0" + length + "d", number);	
	}
		
}

