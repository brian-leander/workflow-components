package dk.dmi.lib.workflow.component.xml;

import org.json.JSONObject;
import org.json.XML;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Deprecated
@Component(
		name = "XML/JSON converter", 
		category = "Xml",
		description = "Convert text between XML and JSON formats.",
		version = 1)
public class XmlJsonConverter extends BaseComponent {
	public static int JSON_INDENT_FACTOR = 4;
	public static String XML_TO_JSON = "XML to JSON";
	public static String JSON_TO_XML = "JSON to XML";
	
	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfFormats(String ignore) {
		String[] formats = {XML_TO_JSON, JSON_TO_XML};
		return formats;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Format to convert from and to", "Text to cnvert"}, 
			returnDescription = "Formated text")
	public String execute(String formatToFormat, String text) throws Exception {
		String resultText = null;
		
		if(XML_TO_JSON.equals(formatToFormat)) {
			resultText = convertXmlToJson(text);
		} else if(JSON_TO_XML.equals(formatToFormat)) {
			resultText = convertJsonToXml(text);
		} else {
			throw new Exception("Component XmlJsonConverter, wrong format configuration!");
		}
		
		return resultText;
	}
	
	String convertJsonToXml(String text) {
		String resultText;
		JSONObject jsonObject = new JSONObject(text);
		resultText = XML.toString(jsonObject);
		return resultText;
	}
	
	String convertXmlToJson(String text) {
		String resultText;
		JSONObject jsonObject = XML.toJSONObject(text);
		resultText = jsonObject.toString(JSON_INDENT_FACTOR);
		return resultText;
	}
	
}
