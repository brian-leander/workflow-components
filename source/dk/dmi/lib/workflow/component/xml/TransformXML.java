package dk.dmi.lib.workflow.component.xml;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import dk.dmi.lib.common.XmlUtils;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Transform XML", 
		category = "Xml",
		description = "Transform XML into another text using XSLT stylesheet.",
		version = 1)
public class TransformXML extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_XML_AREA_MEDIUM},
			argumentDescriptions = {"XML text to transform", "Format xml to a readable structure", "XSLT stylesheet"}, 
			returnDescription = "Transformed text")
	public String execute(String xmlText, boolean formatXml, String xsltText) throws Exception {
		xsltText = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), xsltText);
		String result = transformContent(xmlText, xsltText, formatXml);
		return result;
	}
	
	String transformContent(String xmlText, String xsltText, boolean formatXml) throws TransformerException{
		Source xmlSource = createSourceFromText(xmlText);
		Source xsltSource = createSourceFromText(xsltText);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(outputStream);
		Transformer transformer = transformerFactory.newTransformer(xsltSource);
		
		if(formatXml) {
			XmlUtils.setFormatIndent(transformer);
		}
		
		transformer.transform(xmlSource, streamResult);
		byte[] resultByteArray = ((ByteArrayOutputStream) streamResult.getOutputStream()).toByteArray();  
		String transformedContent = new String(resultByteArray);
		
		return transformedContent;
	}

	Source createSourceFromText(String text) {
		Source xmlSource = new StreamSource(new java.io.StringReader(text));
		return xmlSource;
	}
	
}
