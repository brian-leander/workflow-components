package dk.dmi.lib.workflow.component.polygon;

import java.util.ArrayList;
import java.util.List;
import dk.dmi.lib.common.GeneralUtils;
import dk.dmi.lib.polygon.gridproduction.locations.GridPointSimple;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create Max Temp Three Days Avg Grid Content",
		category = "Polygon",
		description = "Creates grid content for max temperature average over three days.",
		version = 1)
public class CreateMaxTempThreeDaysAvgGridContent extends BaseComponent {
	
    @ExecuteMethod(
	    argumentDescriptions = {"ListList of max temperature day 1 grid data", "ListList of max temperature day 2 grid data", "ListList of max temperature day 3 grid data"},
	    returnDescription = "List List of grid content containing max avg temperature over the three days")
    public List<List<String>> execute(List<List<String>> maxTemperatureDay1ListList, List<List<String>> maxTemperatureDay2ListList, List<List<String>> maxTemperatureDay3ListList) {
    	List<List<String>> createMaxAvgTemperatureGridPointsContentListList = createMaxAvgTemperatureGridPointsContentListList(maxTemperatureDay1ListList, maxTemperatureDay2ListList, maxTemperatureDay3ListList);
		return createMaxAvgTemperatureGridPointsContentListList;
	}
    
    List<List<String>> createMaxAvgTemperatureGridPointsContentListList(List<List<String>> maxTemperatureDay1ListList, List<List<String>> maxTemperatureDay2ListList, List<List<String>> maxTemperatureDay3ListList) {
		List<List<String>> maxAvgTemperatureGridPointListList = new ArrayList<List<String>>(maxTemperatureDay1ListList.size());
		
    	for (int i = 0; i < maxTemperatureDay1ListList.size(); i++) {
    		// get grid values
    		List<String> maxTemperatureDay1GridCell = maxTemperatureDay1ListList.get(i);
    		int id = Integer.parseInt(maxTemperatureDay1GridCell.get(0).trim());
    		int eastings = Integer.parseInt(maxTemperatureDay1GridCell.get(1).trim());
    		int northings = Integer.parseInt(maxTemperatureDay1GridCell.get(2).trim());
    		double maxTemperatureValueDay1 = Double.parseDouble(maxTemperatureDay1GridCell.get(3).trim());
    		
    		List<String> maxTemperatureDay2GridCell = maxTemperatureDay2ListList.get(i);
    		double maxTemperatureValueDay2 = Double.parseDouble(maxTemperatureDay2GridCell.get(3).trim());
    		
    		List<String> maxTemperatureDay3GridCell = maxTemperatureDay3ListList.get(i);
    		double maxTemperatureValueDay3 = Double.parseDouble(maxTemperatureDay3GridCell.get(3).trim());
    		
    		// calculate avg value
    		double maxAvgTemperatureValue = (maxTemperatureValueDay1 + maxTemperatureValueDay2 + maxTemperatureValueDay3) / 3.0;
    		
    		// add grid point line
    		addMaxAvgTemperatureGridPointsLine(maxAvgTemperatureGridPointListList, id, eastings, northings, maxAvgTemperatureValue);
		}
    	
		return maxAvgTemperatureGridPointListList;
	}

	void addMaxAvgTemperatureGridPointsLine(List<List<String>> maxAvgTemperatureGridPointListList, int id, int eastings, int northings, double maxAvgTemperatureValue) {
		GridPointSimple gridPointSimple = new GridPointSimple(id, eastings, northings, 0, maxAvgTemperatureValue);
		List<String> gridPointSimpleListData = GeneralUtils.splitStringIntoStringList(gridPointSimple.toString(), " ");
		maxAvgTemperatureGridPointListList.add(gridPointSimpleListData);
	}
	
}
