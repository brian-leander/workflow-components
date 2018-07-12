package dk.dmi.lib.workflow.component.polygon;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import dk.dmi.lib.polygon.data.Evaporation;
import dk.dmi.lib.polygon.gridproduction.locations.GridPointSimple;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create Makkink Potential Evaporation Grid Content",
		category = "Polygon",
		description = "Creates grid file containing Makkink Potential Evaporation, specified by date, temperature grid list and radiation grid list.",
		version = 1)
public class CreateMakkinkPotentialEvaporationGridContent extends BaseComponent {
	
    @ExecuteMethod(
	    argumentDescriptions = {"Timestamp for date to be calculated", "ListList of temperature grid data", "ListList of radiation grid data"},
	    returnDescription = "List of grid cell values containing potential evaporation data")
    public List<String> execute(Date timeStamp, List<List<String>> temperatureListList, List<List<String>> radiationListList) throws ClassNotFoundException {
    	List<String> evapurationGridPointsTextList = createEvapurationGridPointsTextList(timeStamp, temperatureListList, radiationListList);
		return evapurationGridPointsTextList;
	}

	List<String> createEvapurationGridPointsTextList(Date timeStamp, List<List<String>> temperatureListList, List<List<String>> radiationListList) {
		List<String> evaporationGridPointsTextList = new ArrayList<String>(temperatureListList.size());
		
    	for (int i = 0; i < temperatureListList.size(); i++) {
    		// get id, eastings, northings
    		List<String> temperatureGridCell = temperatureListList.get(i);
    		int id = Integer.parseInt(temperatureGridCell.get(0).trim());
    		int eastings = Integer.parseInt(temperatureGridCell.get(1).trim());
    		int northings = Integer.parseInt(temperatureGridCell.get(2).trim());
    		
    		// get temperature value
    		double temperatureValue = Double.parseDouble(temperatureGridCell.get(3).trim());
    		
    		// get radiation value
    		List<String> radiationGridCell = radiationListList.get(i);
    		double radiationValue = Double.parseDouble(radiationGridCell.get(3).trim());
    		
    		// calculate evaporation value
    		double evaporationValue = createEvaporationValue(timeStamp, temperatureValue, radiationValue);
    		
    		// add evaporation grid points text
    		addEvaporationGridPointsText(evaporationGridPointsTextList, id, eastings, northings, evaporationValue);
		}
    	
		return evaporationGridPointsTextList;
	}


	double createEvaporationValue(Date timeStamp, double temperatureValue, double radiationValue) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(timeStamp);
		
		double evaporationValue = Evaporation.getPotentialEvaporationMakkink(gregorianCalendar, temperatureValue, radiationValue);
		return evaporationValue;
	}
    
	void addEvaporationGridPointsText(List<String> evaporationGridPointsTextList, int id, int eastings, int northings, double evaporationValue) {
		GridPointSimple gridPointSimple = new GridPointSimple(id, eastings, northings, 0, evaporationValue);
		evaporationGridPointsTextList.add(gridPointSimple.toString());
	}
	
}
