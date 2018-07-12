package dk.dmi.lib.workflow.component.xml;

import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.dmi.lib.common.XmlUtils;
import dk.dmi.lib.persistence.common.PersistenceObject;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.CommonXmlComponentUtils;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Append Entity To XML", 
		category = "Xml",
		description = "Appends an entity objects to a new xml text, based on the spesified xml data structure. Every element must match the entity name or entity variable. If the structure variable is another entity object, variables for that entity are defined as child elements. If the variable is a list of entity objects, all entities are defined as child elements.",
        version = 1)
public class AppendEntityToXml extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_XML_AREA_MEDIUM},
			argumentDescriptions = {"Entity objects to append", "Combine entity and name elements if parent element only has one child element", "Format xml to a readable structure", "Append xml header declaration", "An xml data structure that defines the entity data to append, and how it should be arranged, in the xml result"}, 
			returnDescription = "Xml content")
	public String execute(PersistenceObject persistenceObject, boolean combineElements, boolean formatXml, boolean appendXmlDeclaration, String xmlDataStructure) throws Exception {
		xmlDataStructure = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), xmlDataStructure);
		xmlDataStructure = XmlUtils.removeXmlFormat(xmlDataStructure);
		Document dataStructureDocument = XmlUtils.createDocumentFromString(xmlDataStructure);
		CommonXmlComponentUtils commonXmlComponentUtils = new CommonXmlComponentUtils(combineElements);
		
		String xmlResult = handleEntityList(persistenceObject, dataStructureDocument, commonXmlComponentUtils, formatXml, appendXmlDeclaration);
		return xmlResult;
	}

	String handleEntityList(PersistenceObject persistenceObject, Document dataStructureDocument, CommonXmlComponentUtils commonXmlComponentUtils, boolean formatXml, boolean appendXmlDeclaration) throws TransformerFactoryConfigurationError, Exception {
		Element inputRootElement = dataStructureDocument.getDocumentElement();
		
		Document document = commonXmlComponentUtils.getDocument();
		commonXmlComponentUtils.appendRootProcessingInstructions(dataStructureDocument.getChildNodes(), document);
		
		commonXmlComponentUtils.appendEntityToElementEqualToNodeName(inputRootElement, null, persistenceObject, "");
		String xmlResult = XmlUtils.convertDocumentToString(document, formatXml, appendXmlDeclaration);
		
		return xmlResult;
	}

}
