package dk.dmi.lib.workflow.component.polygon;

import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.PolygonConfigurationController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.PolygonConfiguration;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get polygon configuration value", 
		category = "Polygon",
		description = "Get a specific polygon configuration value based on a tag name.",
        version = 1)
public class GetPolygonConfigurationValue extends BaseComponent {
	
	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfPolygonConfigurationTags(String ignore) {
		PolygonConfigurationController polygonConfigurationController = PolygonConfigurationController.getInstance();
		List<PolygonConfiguration> polygonConfigurationList = polygonConfigurationController.getAllPolygonConfiguration();
		String[] polygonConfigurationTags = new String[polygonConfigurationList.size()];
		
		for (int i = 0; i < polygonConfigurationList.size(); i++) {
			PolygonConfiguration polygonConfiguration = polygonConfigurationList.get(i);
			polygonConfigurationTags[i] = polygonConfiguration.getTag();
		}
		
		return polygonConfigurationTags;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Polygon configuration tag"}, 
			returnDescription = "Polygon configuration value")
	public String execute(String tag) {
		PolygonConfigurationController polygonConfigurationController = PolygonConfigurationController.getInstance();
		PolygonConfiguration polygonConfiguration = polygonConfigurationController.getPolygonConfigurationByTag(tag);
		String polygonConfigurationValue = polygonConfiguration.getValue();
		return polygonConfigurationValue;
	}
	
}
