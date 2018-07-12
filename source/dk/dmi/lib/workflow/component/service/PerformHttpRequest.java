package dk.dmi.lib.workflow.component.service;

import dk.dmi.lib.workflow.client.WorkflowRestServiceClient;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Perform HTTP request ", 
		category = "Service",
		description = "Execute a HTTP request, based on url and payload parameters.",
		version = 1)
public class PerformHttpRequest extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Complete url including protocol, server, port, path and url parameters", "Payload parameters. If null a GET request will be performed, otherwise a POST with parameters in the body."},
			returnDescription = "HTTP response text")
    public String execute(String url, String payloadParameters) throws Exception {
		WorkflowRestServiceClient workflowRestServiceClient = new WorkflowRestServiceClient();
		String responseText = workflowRestServiceClient.executeHttpRequest(url, payloadParameters);
		return responseText;
    }

}
