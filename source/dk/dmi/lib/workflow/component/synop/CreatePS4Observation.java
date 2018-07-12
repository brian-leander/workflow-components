package dk.dmi.lib.workflow.component.synop;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
import dk.dmi.lib.workflow.component.synop.PS416Observation.PS4ObservationBuilder;
import dk.dmi.lib.workflow.component.synop.kdi.KDIXmlHandler;

@Component(
		name = "Create PS4 Observations", 
		category = "VST",
		description = "Create and validate PS4 Observations",
        version = 1)
public class CreatePS4Observation extends BaseComponent {

	private WorkflowContextController workflowContextController;
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"XML file"},
			returnDescription="Ps4Observation")
    public PS416Observation create(String fileName) {
		if (getDataTypeFromFileName(fileName) == null) {
			workflowContextController.addObjectToContext("PS4_CREATE_ERROR", "Unknown data type. Data type should be in file name.", false);
			return null;
		}
				
		try (InputStream xmlInput = new FileInputStream(fileName)) {			
			return createPS4Observation(fileName, xmlInput);			
		} catch (IOException | SAXException | ParserConfigurationException | IllegalStateException | IllegalArgumentException e) {
			LOGGER.error("Error in CreatePS4Observation.", e);
			workflowContextController.addObjectToContext("PS4_CREATE_ERROR", e.getMessage(), false);
			return null;
		}		
	}

	protected PS416Observation createPS4Observation(String fileName, InputStream xmlInput) throws IOException, ParserConfigurationException, SAXException {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser saxParser = factory.newSAXParser();
		final KDIXmlHandler handler = new KDIXmlHandler();
		saxParser.parse(xmlInput, handler);	
		
		LOGGER.debug("File : " + fileName + " is parsed.");
		
		final PS416Data observationDataVO = handler.getObservationDataVOs().get(0);	// Expecting only one <data> tag block in each xml file		
		
		String kdiFileName = getShortFileName(fileName);
		PS416DataType dataType = getDataTypeFromFileName(fileName);
		String xml = getFileContent(fileName);

		final PS416Observation ps4Observation = new PS4ObservationBuilder(observationDataVO.getObservationtime(), dataType).
				observation(observationDataVO.getObservationVOs()).
				supplier(observationDataVO.getSupplierName()).
				productionTime(observationDataVO.getProductiontime()).
				fileName(kdiFileName).
				xml(xml).
				build();
		
		LOGGER.debug(observationDataVO.getObservationVOs().size() + " observations found in file.");
		
		return ps4Observation;
	}

	private PS416DataType getDataTypeFromFileName(String fileName) {
		if (fileName.contains(PS416DataType.VANDSTAND.name())) {
			return PS416DataType.VANDSTAND;
		}
		
		if (fileName.contains(PS416DataType.TEMPERATURE.name())) {
			return PS416DataType.TEMPERATURE;
		}
		
		return null;
	}

	private String getShortFileName(String fileName) {
		int lastIndex = fileName.lastIndexOf("/");
		if (lastIndex <= 0)
			return fileName;
		
		return fileName.substring(lastIndex + 1);
	}

	protected String getFileContent(String fileName) throws IOException {
		String xml = "";
		final List<String> xmlFile = Files.readAllLines(Paths.get(fileName));
		for (String line : xmlFile) {
			xml += line + System.lineSeparator();
		}
		return xml;
	}
}