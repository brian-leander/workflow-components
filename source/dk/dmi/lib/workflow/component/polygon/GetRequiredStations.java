package dk.dmi.lib.workflow.component.polygon;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.dmi.lib.common.GeoTransform;
import dk.dmi.lib.polygon.gridproduction.locations.Station;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.StationRequiredController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.StationRequired;
import dk.dmi.lib.persistence.database.obsdb.publicc.controller.StationsController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get required stations", 
		category = "Polygon",
		description = "Gets a list of stations required for basic data",
        version = 1)
public class GetRequiredStations extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Element number for parameter to get requred stations", "Date time of the stations"}, 
			returnDescription = "List of grid file entities matching criteria")
	public List<Station> execute(int elementNumber, Date date) throws Exception {
		StationRequiredController stationRequiredController = StationRequiredController.getInstance();
		StationsController stationsController= new StationsController();
		
		List<StationRequired> stationRequiredList = stationRequiredController.getStationRequiredByElementNumerAndDate(elementNumber, date);
		List<Station> resultStationList = new ArrayList<Station>(stationRequiredList.size());
		
		for (StationRequired stationRequired : stationRequiredList) {
			Object[] obsStation = stationsController.getSingleStationPositionByStatIdAndDateInterval(stationRequired.getStatid(), date, date);
			
			if(obsStation != null) {
				Point2D stationPoint = new Point2D.Double(((Double) obsStation[1]).doubleValue(), ((Double) obsStation[2]).doubleValue());
				Point2D resulStationPoint = GeoTransform.genericTransform(stationPoint, "EPSG:4258", "EPSG:32632");
				
				Station station = new Station(stationRequired.getStatid(), (int) resulStationPoint.getX(), (int) resulStationPoint.getY());
				station.setToBeExcluded(true);
				resultStationList.add(station);
			}
		}
		
		return resultStationList;
	}
	
}
