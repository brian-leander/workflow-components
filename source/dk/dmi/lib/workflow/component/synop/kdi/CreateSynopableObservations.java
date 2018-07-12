package dk.dmi.lib.workflow.component.synop.kdi;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import dk.dmi.lib.synop.SynopBuilders;
import dk.dmi.lib.synop.builder.SynopBuilder;
import dk.dmi.lib.synop.builder.SynopBuilderFactory;
import dk.dmi.lib.synop.observation.Observation;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.component.synop.WaterLevelObservation;

@Component(
		name = "Create synop codes for kdi observations.", 
		category = "Synop",
		description = "Creates a list of synop code strings.",
        version = 1)
public class CreateSynopableObservations {
	
	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfSynopBuilders(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return SynopBuilders.getSynopBuilderName();
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Choose project name for specific synop builder", "List of observations of type dk.dmi.lib.synop.Observation"}, 
			returnDescription = "The newly created list of synop codes.")
	public String getObservationType(String project, List<WaterLevelObservation> waterLevelObservations) {		
		List<Observation> observations = new ArrayList<>();
		
		for (WaterLevelObservation waterLevelObservation : waterLevelObservations) {
			Observation waterLevel = Observation.forCurrentWaterLevel(
					String.valueOf(waterLevelObservation.getDmiCode()), 
					waterLevelObservation.getObservationTime(), 
					String.valueOf(waterLevelObservation.getWaterlevel())); 
					
			Observation waterTemperature = Observation.forCurrentWaterTemperature(
					String.valueOf(waterLevelObservation.getDmiCode()), 
					waterLevelObservation.getObservationTime(), 
					String.valueOf(waterLevelObservation.getTemperature())); 
			
			observations.add(waterLevel);
			observations.add(waterTemperature);
		}
		
//		SynopListBuilder synopListBuilder = new SynopListBuilder();
//		synopListBuilder.addAll(observations);		
//		synopListBuilder.createSynop(project);
		
		SynopBuilder synopBuilder = SynopBuilderFactory.getSynopBuilder(project);
		String synop = synopBuilder.createSynopForObservations(observations, observations.get(0).getStationNo(), observations.get(0).getTimestamp());			
				
		return synop;
	}
}
