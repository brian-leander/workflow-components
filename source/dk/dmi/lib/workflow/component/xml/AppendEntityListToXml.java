package dk.dmi.lib.workflow.component.xml;

import java.util.List;
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
		name = "Append Entity List To XML", 
		category = "Xml",
		description = "Appends a list of entity objects to a new xml text, based on the specified xml data structure. Every element must match an entity name or entity variable. If the structure variable is another entity object, variables for that entity are defined as child elements. If the variable is a list of entity objects, all entities are defined as child elements. Attribute tagName can be used specify a custom tag name, othervise the table/column name Will be used as default.",
        version = 1)
public class AppendEntityListToXml extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_XML_AREA_MEDIUM},
			argumentDescriptions = {"List of entity objects to append", "Combine entity and name elements if parent element only has one child element", "Format xml to a readable structure", "Append xml header declaration", "Custom root element, e.g. <tagName attr=\"value\">. Use null to add default dataType_list tag", "An xml data structure that defines the entity data to append, and how it should be arranged, in the xml result"}, 
			returnDescription = "Xml content")
	public String execute(List<? extends PersistenceObject> persistenceObjectList, boolean combineElements, boolean formatXml, boolean appendXmlDeclaration, String customRootElement, String xmlDataStructure) throws Exception {
		xmlDataStructure = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), xmlDataStructure);
		xmlDataStructure = XmlUtils.removeXmlFormat(xmlDataStructure);
		Document dataStructureDocument = XmlUtils.createDocumentFromString(xmlDataStructure);
		CommonXmlComponentUtils commonXmlComponentUtils = new CommonXmlComponentUtils(combineElements);
		
		String xmlResult = handleEntityList(persistenceObjectList, customRootElement, dataStructureDocument, commonXmlComponentUtils, formatXml, appendXmlDeclaration);
		return xmlResult;
	}

	String handleEntityList(List<? extends PersistenceObject> persistenceObjectList, String customRootElement, Document dataStructureDocument, CommonXmlComponentUtils commonXmlComponentUtils, boolean formatXml, boolean appendXmlDeclaration) throws TransformerFactoryConfigurationError, Exception {
		Element inputRootElement = dataStructureDocument.getDocumentElement();
		Element outputRootElement = null;
		
		if(customRootElement != null) {
			outputRootElement = appendCustomRootElement(customRootElement, commonXmlComponentUtils);
		} else {
			outputRootElement = commonXmlComponentUtils.appendRootElement(inputRootElement.getNodeName()+"_list");
		}
		
		commonXmlComponentUtils.appendRootProcessingInstructions(dataStructureDocument.getChildNodes(), outputRootElement);
		
		for (PersistenceObject persistenceObject : persistenceObjectList) {
			commonXmlComponentUtils.appendEntityToElementEqualToNodeName(inputRootElement, outputRootElement, persistenceObject, "");
		}
		
		Document document = commonXmlComponentUtils.getDocument();
		String xmlResult = XmlUtils.convertDocumentToString(document, formatXml, appendXmlDeclaration);
		
		return xmlResult;
	}

	Element appendCustomRootElement(String customRootElement, CommonXmlComponentUtils commonXmlComponentUtils) {
		Element outputRootElement = null;
		customRootElement = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), customRootElement);
		customRootElement = customRootElement.replaceAll("[<>]", "").trim();
		
		String customElementName = customRootElement.split(" ")[0].trim();
		outputRootElement = commonXmlComponentUtils.appendRootElement(customElementName);
		
		customRootElement = customRootElement.substring(customElementName.length());
		String[] customAttributes = customRootElement.split("\"");
		
		if(customAttributes.length > 1) {
			for (int i = 0; i < customAttributes.length; i++) {
				String attributeName = customAttributes[i].replace("=", "").trim();
				String attributeValue = customAttributes[++i].trim();
				
				outputRootElement.setAttribute(attributeName, attributeValue);
			}
		}
		
		return outputRootElement;
	}
	
}
