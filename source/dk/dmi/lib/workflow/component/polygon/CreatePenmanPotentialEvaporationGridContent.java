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
		name = "Create Penman Potential Evaporation Grid Content",
		category = "Polygon",
		description = "Creates grid file containing Penman Potential Evaporation, specified by date, temperature, relative humidity, windSpeed, and radiation grid lists.",
		version = 1)
public class CreatePenmanPotentialEvaporationGridContent extends BaseComponent {
	
    @ExecuteMethod(
	    argumentDescriptions = {"Timestamp for date to be calculated", "ListList of temperature grid data", "ListList of humidity grid data", "ListList of radiation grid data", "ListList of wind speed grid data"},
	    returnDescription = "List of grid cell values containing potential evaporation data")
    public List<String> execute(Date timeStamp, List<List<String>> temperatureListList, List<List<String>> humidityListList, List<List<String>> radiationListList, List<List<String>> windSpeedListList) throws ClassNotFoundException {
    	List<String> evapurationGridPointsTextList = createEvapurationGridPointsTextList(timeStamp, temperatureListList, humidityListList, radiationListList, windSpeedListList);
		return evapurationGridPointsTextList;
	}
    
	List<String> createEvapurationGridPointsTextList(Date timeStamp, List<List<String>> temperatureListList, List<List<String>> humidityListList, List<List<String>> radiationListList, List<List<String>> windSpeedListList) {
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
    		List<String> humidityGridCell = humidityListList.get(i);
    		double humidityValue = Double.parseDouble(humidityGridCell.get(3).trim());
    		
    		// get radiation value
    		List<String> radiationGridCell = radiationListList.get(i);
    		double radiationValue = Double.parseDouble(radiationGridCell.get(3).trim());
    		
    		// get radiation value
    		List<String> windSpeedGridCell = windSpeedListList.get(i);
    		double windSpeedValue = Double.parseDouble(windSpeedGridCell.get(3).trim());
    		
    		// calculate evaporation value
    		double evaporationValue = createEvaporationValue(timeStamp, temperatureValue, humidityValue, radiationValue, windSpeedValue);
    		
    		// add evaporation grid points text
    		addEvaporationGridPointsText(evaporationGridPointsTextList, id, eastings, northings, evaporationValue);
		}
    	
		return evaporationGridPointsTextList;
	}


	double createEvaporationValue(Date timeStamp, double temperatureValue, double humidityValue, double radiationValue, double windSpeedValue) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(timeStamp);
		
		double evaporationValue = Evaporation.getPotentialEvaporationPenman(gregorianCalendar, temperatureValue, humidityValue, radiationValue, windSpeedValue);
		return evaporationValue;
	}
    
	void addEvaporationGridPointsText(List<String> evaporationGridPointsTextList, int id, int eastings, int northings, double evaporationValue) {
		GridPointSimple gridPointSimple = new GridPointSimple(id, eastings, northings, 0, evaporationValue);
		evaporationGridPointsTextList.add(gridPointSimple.toString());
	}
	
}
