package dk.dmi.lib.workflow.component.service;

import java.util.Map;
import dk.dmi.lib.workflow.client.WorkflowRestServiceClient;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Call workflow REST service", 
		category = "Service",
		description = "Workflow rest client to perform workflow rest service request, based on service, login, hookpoint and parameteres. Each parameter will be converted to context object on the server side.",
		version = 1)
public class CallWorkflowRestService extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Server (e.g. http://servername:port)", "Rest service endpoint (e.g. workflow_rest_service)", "Username", "Password", "Workflow hookpoint", "Map of request url parameters", "Map of request body parameters"},
			returnDescription = "Response text")
    public String execute(String server, String endpoint, String username, String password, String hookpoint, Map<String, String> urlParameters, Map<String, String> payloadParameters) throws Exception {
		WorkflowRestServiceClient workflowRestServiceClient = new WorkflowRestServiceClient(server, endpoint, username, password);
		String responseText = workflowRestServiceClient.executeWorkflowRestService(hookpoint, urlParameters, payloadParameters);
		return responseText;
    }

}
