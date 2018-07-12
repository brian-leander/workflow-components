package dk.dmi.lib.workflow.component.polygon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import dk.dmi.lib.common.GeneralUtils;
import dk.dmi.lib.polygon.gridproduction.locations.GridPointSimple;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create classification grid content", 
		category = "Polygon",
		description = "Classification of grid content. Input grid content will be transformed to clasification values",
        version = 1)
public class CreateClassificationGridContent extends BaseComponent {
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Grid input content", "Classification values (highest threshold first), i.e. value1, >= threshold1 ; value2, >= threshold2 ; value3, >= threshold3"}, 
			returnDescription = "Transformed grid content")
	public List<String> execute(List<List<String>> inputListList, String classification) throws Exception {
		Map<String, String> classificationMap = GeneralUtils.createMapFromString(classification, ";", ",");
		List<String> classificationGridPointsTextList = createClassificationGridPointsTextList(inputListList, classificationMap);
		return classificationGridPointsTextList;
	}
	
	List<String> createClassificationGridPointsTextList(List<List<String>> inputListList, Map<String, String> classificationMap) {
		List<String> classificationGridPointsTextList = new ArrayList<String>(inputListList.size());
		
    	for (int i = 0; i < inputListList.size(); i++) {
    		// get grid id, eastings, northings, value
    		List<String> inputListListGridCell = inputListList.get(i);
    		int id = Integer.parseInt(inputListListGridCell.get(0).trim());
    		int eastings = Integer.parseInt(inputListListGridCell.get(1).trim());
    		int northings = Integer.parseInt(inputListListGridCell.get(2).trim());
    		double gridValue = Double.parseDouble(inputListListGridCell.get(3).trim());
    		
    		// calculate classification value
    		double classificationValue = createClassificationValue(classificationMap, gridValue);
    		
    		// add classification grid points text
    		addClassificationGridPointsText(classificationGridPointsTextList, id, eastings, northings, classificationValue);
		}
    	
		return classificationGridPointsTextList;
	}

	double createClassificationValue(Map<String, String> classificationMap, double gridValue) {
		double classificationValue = Double.MIN_VALUE;
		Iterator<Entry<String, String>> classificationMapIterator = classificationMap.entrySet().iterator();
		
		while (classificationMapIterator.hasNext()) {
	        Map.Entry<String, String> classificationMapPair = classificationMapIterator.next();
	        double classificationThreshold = Double.parseDouble(classificationMapPair.getValue());
	        
	        if(gridValue >= classificationThreshold) {
	        	classificationValue = Double.parseDouble(classificationMapPair.getKey());
	        }
		}
		
		return classificationValue;
	}
	
	void addClassificationGridPointsText(List<String> classificationGridPointsTextList, int id, int eastings, int northings, double classificationValue) {
		GridPointSimple gridPointSimple = new GridPointSimple(id, eastings, northings, 0, classificationValue);
		classificationGridPointsTextList.add(gridPointSimple.toString());
	}
	
}
