package dk.dmi.lib.workflow.component.synop.synopcode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import dk.dmi.lib.synop.SynopBuilders;
import dk.dmi.lib.synop.SynopListBuilder;
import dk.dmi.lib.synop.observation.Observation;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create synop codes for observations.", 
		category = "Synop",
		description = "Creates a list of synop code strings.",
        version = 1)
public class CreateSynop extends BaseComponent {

	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfSynopBuilders(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return SynopBuilders.getSynopBuilderName();
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Choose project name for specific synop builder", "List of observations of type dk.dmi.lib.synop.Observation"}, 
			returnDescription = "The newly created list of synop codes.")
	public List<String> createSynops(String project, Collection<Observation> observations) {
		SynopListBuilder synopListBuilder = new SynopListBuilder();
		synopListBuilder.addAll(observations);		
		synopListBuilder.createSynop(project);
		
		return synopListBuilder.getSynops();		
	}
}
