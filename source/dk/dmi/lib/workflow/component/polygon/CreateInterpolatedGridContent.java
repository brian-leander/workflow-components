package dk.dmi.lib.workflow.component.polygon;

import dk.dmi.lib.common.GeoTransform;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.ParameterController;

import java.util.List;

import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridCell;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.publicc.common.ClimadbInterface;
import dk.dmi.lib.persistence.database.climadb.publicc.controller.AddBasisDailyDkController;
import dk.dmi.lib.persistence.database.climadb.publicc.controller.AddBasisHourlyDkController;
import dk.dmi.lib.persistence.database.climadb.publicc.controller.BasisDailyDkController;
import dk.dmi.lib.persistence.database.climadb.publicc.controller.BasisHourlyDkInterpolationController;
import dk.dmi.lib.persistence.database.climadb.statcat.controller.StatCatController;
import dk.dmi.lib.persistence.database.climadb.statcat.entity.StatCat;
import dk.dmi.lib.polygon.gridproduction.locations.GridPoint;
import dk.dmi.lib.polygon.gridproduction.locations.GridPointIDW;
import dk.dmi.lib.polygon.gridproduction.locations.Station;
import dk.dmi.lib.polygon.gridproduction.processing.GridPointManager;
import dk.dmi.lib.polygon.gridproduction.processing.InterpolateData;
import dk.dmi.lib.polygon.gridproduction.processing.InterpolateWindDirection;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;

@Component(
	name = "Create Interpolated Grid Content",
	category = "Polygon",
	description = "Creates interpolated grid content, specified by parameter, timestamp and list of gridpoints.",
	version = 1)
public class CreateInterpolatedGridContent extends BaseComponent {

    @ExecuteMethod(
	    argumentDescriptions = {"Parameter", "Zero padded timestamp to be interpolated", "GridCells to be interpolated"},
	    returnDescription = "List of grid cell values containing id, easting, northing and interpolated data")
    public List<String> execute(Parameter parameter, Date timeStamp, List<GridCell> gridCells) throws Exception {
    	List<String> gridPointsTextList = createGridFileFromStations(parameter, timeStamp, gridCells);
		return gridPointsTextList;
	}

	List<String> createGridFileFromStations(Parameter parameter, Date timeStamp, List<GridCell> gridCells) {
		List<String> gridPointsTextList = null;
		List<? extends ClimadbInterface> basisDkList = getBasisDkList(parameter, timeStamp);
		
		if(basisDkList != null && basisDkList.size() > 0) {
			List<Station> stationList = createStationsList(timeStamp, basisDkList);
			List<GridPoint> interpolatedGridPointList = createInterpolatedGridPointList(parameter, gridCells, stationList);
			gridPointsTextList = createGridPointListText(interpolatedGridPointList);
		}
		
		return gridPointsTextList;
	}
    
    List<? extends ClimadbInterface> getBasisDkList(Parameter parameter, Date timeStamp) {
		List<? extends ClimadbInterface> basisDkList = null;
		ParameterController parameterController = ParameterController.getInstance();
		
		if(parameterController.isHourlyParameter(parameter)) {
			if(parameterController.isBasisClimaParameter(parameter) || parameterController.isCalculatedClimaStationHourParameter(parameter)) {
				basisDkList =  BasisHourlyDkInterpolationController.getInstance().getBasisHourlyDKByDateAndElementNumber(timeStamp, parameter.getElementNumber().intValue());
			} else if(parameterController.isAdditionalClimaParameter(parameter)) {
				basisDkList =  AddBasisHourlyDkController.getInstance().getAddBasisHourlyDKByDateAndElementNumber(timeStamp, parameter.getElementNumber());
			}
    	} else if(parameterController.isDailyParameter(parameter)) {
    		if(parameterController.isBasisClimaParameter(parameter) || parameterController.isCalculatedClimaStationDayParameter(parameter) || parameterController.isCalculatedClimaStationDayFromHourParameter(parameter)) {
    			basisDkList =  BasisDailyDkController.getInstance().getBasisDailyDkByDateAndElementNumber(timeStamp, parameter.getElementNumber());
    		} else if(parameterController.isAdditionalClimaParameter(parameter)) {
    			basisDkList =  AddBasisDailyDkController.getInstance().getAddBasisDailyDKByDateAndElementNumber(timeStamp, parameter.getElementNumber());
    		}
    	}
		
		return basisDkList;
	}
    
    List<Station> createStationsList(Date timeStamp, List<? extends ClimadbInterface> basisDkList) {
		List<Station> stationList = new ArrayList<Station>(300);
		List<StatCat> statCatList = StatCatController.getInstance().getStatCatsActiveInPeriodExclusiveDateInterval(timeStamp, timeStamp);
		
		for (ClimadbInterface climadbInterface : basisDkList) {
		    for (StatCat statCat : statCatList) {
				if (climadbInterface.getStatid().intValue() == statCat.getStatid().intValue()) {
				    Point p = GeoTransform.transformGeoWGS84ToUTMWGS84(statCat.getLat(), statCat.getLong_());
				    Station s = new Station(statCat.getStatid(), (int) Math.round(p.getX()), (int) Math.round(p.getY()));
				    s.setValue(climadbInterface.getValue());
				    stationList.add(s);
				}
		    }
		}
		
		return stationList;
	}
    
	List<GridPoint> createInterpolatedGridPointList(Parameter parameter, List<GridCell> gridCells, List<Station> stationList) {
		List<GridPoint> gpsInterpolated = getGridPointIDW(gridCells);
		GridPointManager.setTerrPercentValuesForStations(stationList, gridCells);
		
		if (stationList.size() > 0) {
		    if (parameter.getElementNumber() == 371) {
				gpsInterpolated = InterpolateWindDirection.interpolateWindDirection(stationList, gpsInterpolated);
			} else {
				InterpolateData id = new InterpolateData();
				int decimals = 1;
				
				if (parameter.getElementNumber() == 251) {
				    decimals = 2;
				}
				
				gpsInterpolated = id.interpolateGridWithGaus(stationList, gpsInterpolated, true, decimals);
		    }
		}
		
		return gpsInterpolated;
	}
    
	List<GridPoint> getGridPointIDW(List<GridCell> gridCells) {
		List<GridPoint> gridPoints = new ArrayList<>(gridCells.size());

		for (GridCell gridCell : gridCells) {
			int gridCellValue = gridCell.getId().intValue();
			Integer gridCellEastings = gridCell.getEastings();
			Integer gridCellNorthings = gridCell.getNorthings();
			float gridCellInlandClimatePercent = gridCell.getInlandClimatePercent();
			
			GridPointIDW gridPointIDW = new GridPointIDW(gridCellValue, gridCellEastings, gridCellNorthings, gridCellInlandClimatePercent);
			gridPoints.add(gridPointIDW);
		}

		return gridPoints;
	}
	
	List<String> createGridPointListText(List<GridPoint> interpolatedGridPointList) {
		ArrayList<String> gridPointsTextList = new ArrayList<String>(interpolatedGridPointList.size());
		
		for(GridPoint gridPoint : interpolatedGridPointList){
			gridPointsTextList.add(gridPoint.toString());
		}
			
		return gridPointsTextList;
	}
	
}
