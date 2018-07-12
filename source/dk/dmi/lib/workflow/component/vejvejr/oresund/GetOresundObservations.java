package dk.dmi.lib.workflow.component.vejvejr.oresund;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Create Oresund Observations", 
		category = "VejVejr",
		description = "Create Observations",
        version = 1)
public class GetOresundObservations extends BaseComponent {

	private WorkflowContextController workflowContextController;
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"URL"},
			returnDescription="Observation Map")
	public Map<String, OresundObservation> getObservations(String url) {
		XmlHandler handler;
		try {
			URL oresundsbron = new URL(url); //"http://data.oresundsbron.com/pcms/weatherfeed.xml");	
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			final SAXParser saxParser = factory.newSAXParser();
			handler = new XmlHandler();
			saxParser.parse(oresundsbron.openStream(), handler);
		} catch (IOException | SAXException | ParserConfigurationException | IllegalStateException | IllegalArgumentException e) {
			LOGGER.error("Error in CreateOresundObservations.", e);
			workflowContextController.addObjectToContext("ERROR", e.getMessage(), false);
			return null;
		}		
		
		return handler.getObservationVOs();
	}
}
