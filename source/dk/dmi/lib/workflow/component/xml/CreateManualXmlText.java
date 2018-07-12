package dk.dmi.lib.workflow.component.xml;

import org.w3c.dom.Document;
import dk.dmi.lib.common.XmlUtils;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Create Manual XML Text", 
		category = "Xml",
		description = "Creates a new xml text, based on input xml text. Any context keys between tags will be evaluated.",
        version = 1)
public class CreateManualXmlText extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_XML_AREA_MEDIUM},
			argumentDescriptions = {"Format xml to a readable structure", "Append xml header declaration", "An xml text that can include context keys."}, 
			returnDescription = "Xml content")
	public String execute(boolean formatXml, boolean appendXmlDeclaration, String xmlText) throws Exception {
		xmlText = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), xmlText);
		xmlText = XmlUtils.removeXmlDeclarations(xmlText);
		xmlText = XmlUtils.removeXmlFormat(xmlText);
		Document document = XmlUtils.createDocumentFromString(xmlText);
		
		xmlText = XmlUtils.convertDocumentToString(document, formatXml, appendXmlDeclaration);
		return xmlText;
	}
	
}
