package dk.dmi.lib.workflow.component.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Evaluate expression", 
		category = "Util",
		description = "Matches a text by regular expression and returns true if there was a match.",
        version = 1)
public class EvaluateExpression extends BaseComponent {

	@ExecuteMethod(
			argumentDescriptions = {"String to evaluate", "Regular expression"}, 
			returnDescription = "boolean true for match, false for no match")
	public boolean evaluateExpression(String stringToEvaluate, String expression) {		
		Matcher m = Pattern.compile(expression).matcher(stringToEvaluate);
		return m.matches();
	}
}
