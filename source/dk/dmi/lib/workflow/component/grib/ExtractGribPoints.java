package dk.dmi.lib.workflow.component.grib;

// import java.awt.geom.Point2D;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dk.dmi.lib.geo.GeoPoint;
import dk.dmi.lib.grib.GribField;
import dk.dmi.lib.grib.GridPoint;
import dk.dmi.lib.grib.InvalidMethodException;
import dk.dmi.lib.grib.NotImplementedException;
import dk.dmi.lib.grib.OutsideAreaException;
import dk.dmi.lib.grib.util.GribFieldExtended;
import dk.dmi.lib.util.DateTime;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
		name = "Extract GRIB Points from Grib Field", 
		category = "Grib",
		description = "Extracts List of GRIB Points from Grib Field",
        version = 1)
// public class ExtractGribPoints extends BaseComponent {
	public class ExtractGribPoints {
    Logger logger = LoggerFactory.getLogger(ExtractGribPoints.class);
    
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"List of GridPoints Array", "List of Points"},
			returnDescription = "")  // List of GridPoints Array

		public List<List<GridPoint>> execute(GribField[] gribField, List<GeoPoint> pointList) throws NotImplementedException, InvalidMethodException {		
		GeoPoint geoPoint = null;
		List<GridPoint> gridPointList = null;
		// List<List<GridPoint>> gridPointListList = new ArrayList<List<GridPoint>>();  
		List<List<GridPoint>> gridPointListList = null;
		gridPointListList = new ArrayList<List<GridPoint>>(); 
		
		DateTime analysisTime = new DateTime();
		int[] indicators = new int[3];
			
		for (int i = 0; i < 3; i++) {
			geoPoint = null;
			gridPointList = null;
			Iterator<GeoPoint> iterator = pointList.iterator();
			gridPointList = new ArrayList<GridPoint>(); 
			
			indicators[i] = gribField[i].getParUnitIndicator();
		
			System.out.println("indicator for list[" + i + "]: " + indicators[i]);
			logger.info("indicator for list[\" + i + \"]: "  + indicators[i]);
		   
			analysisTime = gribField[i].getReferenceTime();  // dk.dmi.lib.util.DateTime
			System.out.println("analysisTime: " + analysisTime.getDateTimeString("YYYY-MM-dd HH"));
			logger.info("analysisTime: " + analysisTime.getDateTimeString("YYYY-MM-dd HH"));
			// logger.debug("analysisTime: " + analysisTime.getDateTimeString("YYYY-MM-dd HH"));
			
			System.out.println("forecastTimeUnit: " + gribField[i].getForecastTimeUnit());	
			logger.info("forecastTimeUnit: " + gribField[i].getForecastTimeUnit());
			// logger.debug("forecastTimeUnit: " + gribField[i].getForecastTimeUnit());
			
			while (iterator.hasNext()) {
				try {
			    	geoPoint = iterator.next();	    	
					GridPoint gridPoint = gribField[i].getNearestGridPoint(geoPoint);  // alt.: use getInterpolatedVectorGridPoint
		
					double lon = gridPoint.getLon();
					lon = fix(lon);  // <-- New
					gridPoint.setLocation(gridPoint.getLat(), lon);
					// System.out.println("In " + geoPoint.getLat() + ", " + geoPoint.getLon() + "  out " + gridPoint.getLat() + ", " + gridPoint.getLon());
	
					gridPointList.add(gridPoint);  
				}
				catch (OutsideAreaException e) {
					
					System.out.println("GeoPoint Outside Area: " + geoPoint.toString());
					continue;
				}
			}
			gridPointListList.add(i, gridPointList);  
		}
		
		return gridPointListList;
	}
	
	
	double fix(double lat) {
		while (lat > 180.0) lat -= 180.0;
		while (lat <= -180.0) lat += 180.0;
		
		return lat;
	}
	
	
}
