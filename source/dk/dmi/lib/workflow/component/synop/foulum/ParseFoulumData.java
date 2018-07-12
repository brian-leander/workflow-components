package dk.dmi.lib.workflow.component.synop.foulum;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;
import dk.dmi.lib.workflow.component.synop.asiaq.ObservationWithType;

import java.util.List;
import java.util.MissingFormatArgumentException;

@Component(
        name = "Parse Foulum observations.",
        category = "Foulum",
        description = "",
        version = 1)
public class ParseFoulumData extends BaseComponent {

    private WorkflowContextController workflowContextController;
    @InjectContextControllerMethod
    public void injectContext(WorkflowContextController workflowContextController) {
        this.workflowContextController = workflowContextController;
    }

    @ExecuteMethod(
            argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
            argumentDescriptions = {"String containing observations in Foulum format."},
            returnDescription = "List of type (dk.dmi.lib.synop.asiaq.ObservationWithType, parameter type).")
    public List<ObservationWithType> parseFoulumData(List<String> foulumObservations) {
        if (foulumObservations == null) {
            LOGGER.warn("ParseFoulumData.parseFoulumData : Empty List argument. No observations to parse.");
            throw new IllegalArgumentException("Observation list cannot be null.");
        }

        try {
            return FoulumParser.getObservationWithTypes(foulumObservations);
        } catch (NumberFormatException | MissingFormatArgumentException e) {
            LOGGER.error("Error in ParseFoulumData.", e);
            workflowContextController.addObjectToContext("ERROR", e.getMessage(), false);
            return null;
        }
    }
}
