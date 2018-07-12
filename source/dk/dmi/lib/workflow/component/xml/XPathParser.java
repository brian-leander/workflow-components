package dk.dmi.lib.workflow.component.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import dk.dmi.lib.common.NamespaceContextMap;
import dk.dmi.lib.common.XmlUtils;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "XPath Parser", 
		category = "Xml",
		description = "An XPath query parser that allows for selecting data, nodes and nodesets from an xml text. Nodes are of type org.w3c.dom.Node and cannot be persisted in context.",
        version = 1)
public class XPathParser {
	
	static final String XPATH_NODESET = "NODESET";
	static final String XPATH_NODE = "NODE";
	static final String XPATH_STRING = "STRING";
	static final String XPATH_NUMBER = "NUMBER";
	static final String XPATH_BOOLEAN = "BOOLEAN";
	
	@ArgumentListGetMethod(
			argumentIndex = "2")
	public String[] getListOfXPathConstants(String ignore) {
		String[] xPathConstants = {XPATH_NODESET, XPATH_NODE, XPATH_STRING, XPATH_NUMBER, XPATH_BOOLEAN};
		return xPathConstants;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_AREA_SMALL},
			argumentDescriptions = {"Xml text to parse", "An XPath expression (e.g. //some_tag/../@some_attribute)", "Result type of the expression, NODESET returns a list of nodes, and NUMBER returns a Double. <a href=\"http://docs.oracle.com/javase/8/docs/api/org/w3c/dom/Node.html\">See Node documentation</a>", "A list of namespaces, one per line. Each namespace should be written: prefix=url"}, 
			returnDescription = "Query result, which will be of class type matching the choosen result type.")
	public Object execute(String xmlText, String xPathExpression, String resultType, String namespaces) throws Exception {
		Document document = XmlUtils.createDocumentFromString(xmlText);
		XPath xPath = createXPathObject();
		
		NamespaceContextMap namespaceContextMap = createNamespaceContextMap(namespaces);
		xPath.setNamespaceContext(namespaceContextMap);
		
		Object result = evaluateExpression(xPath, document, xPathExpression, resultType);
        return result;
    }
	
	XPath createXPathObject() {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		return xpath;
	}
	
	NamespaceContextMap createNamespaceContextMap(String namespaces) {
		String[] namespaceList = namespaces.split("\n\r");
		Map<String, String> namespaceMap = new HashMap<String, String>();
		
		for (String namespace : namespaceList) {
			String[] prefixUrl = namespace.split("=");
			String prefix = prefixUrl[0].trim();
			String url = prefixUrl[1].trim();
			namespaceMap.put(prefix, url);
		}
		
		NamespaceContextMap namespaceContextMap = new NamespaceContextMap(namespaceMap);
		return namespaceContextMap;
	}
	
	Object evaluateExpression(XPath xpath, Document document, String xPathExpresion, String resultType) throws XPathExpressionException {
		XPathExpression xPathExpression = xpath.compile(xPathExpresion);
		Object result = null;
		
		switch(resultType) {
		case XPATH_NODESET :
			result = xPathExpression.evaluate(document, XPathConstants.NODESET);
			result = nodeListToArrayList((NodeList) result);
			break;
			
		case XPATH_NODE :
			result = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
			break;
			
		case XPATH_STRING :
			result = (String) xPathExpression.evaluate(document, XPathConstants.STRING);
			break;
			
		case XPATH_NUMBER :
			result = (Double) xPathExpression.evaluate(document, XPathConstants.NUMBER);
			break;
			
		case XPATH_BOOLEAN :
			result = (Boolean) xPathExpression.evaluate(document, XPathConstants.BOOLEAN);
			break;
		}
		
		return result;
	}
	
	List<Node> nodeListToArrayList(NodeList nodeList) {
		List<Node> nodeArrayList = new ArrayList<Node>();
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			nodeArrayList.add(nodeList.item(i));
		}
		
		return nodeArrayList;
	}
	
}
