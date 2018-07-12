package dk.dmi.lib.workflow.component.convert;

//import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import dk.dmi.lib.geo.GeoPoint;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Text to GeoPoint list", 
		category = "Convert",
		description = "Converts a text to a list of objects specified by GeoPoint class type, text point seperator and text axis seperator.",
        version = 1)

public class TextToGeoPointList {
	
	static final String POINT_CLASS_TYPE_DOUBLE = "Double";
	
	String pointClass = POINT_CLASS_TYPE_DOUBLE;
	
	
	@ExecuteMethod(
			argumentDescriptions = {"List of points to convert", "Characters to use for separating points", "Characters to use for separating point axis", "Decimal pattern to use for number formatting, e.g. ##0.### (use null to ignore)"},
			returnDescription = "List of GeoPoint object created from the text")
    
	public List<GeoPoint> execute(String text, String pointClassType, String pointSeparator, String axisSeparator) {
		List<GeoPoint> pointList = new ArrayList<GeoPoint>();
		String[] keyValuePairs = text.trim().split(pointSeparator);
		
		if(keyValuePairs.length > 0 && !keyValuePairs[0].equals("")) {
			for (String keyValuePair : keyValuePairs) {
				String[] keyValueList = keyValuePair.trim().split(axisSeparator);	
				
			//	keyValueList[0] = fix_degree(keyValueList[0]);  // New
				
				GeoPoint newPoint = createNewPointOfType(pointClassType, keyValueList[0].trim(), keyValueList[1].trim());
				pointList.add(newPoint);
			}
		}
		
		// pointList = confineGeoPoints(pointList); 
		
		return pointList;
   }
	
	
	GeoPoint createNewPointOfType(String pointClassType, String xStr, String yStr ) {
		GeoPoint newPoint = null; // Check, om x og y ligger korrekt
				
		double x = Double.parseDouble(xStr);
		double y = Double.parseDouble(yStr);		
		newPoint = new GeoPoint(y, x);
		
		return newPoint;
	}
	
//	List <GeoPoint> confineGeoPoints(List<GeoPoint>geoPointList) {
//		GeoPoint newGeoPoint = null;;
//		List<GeoPoint> geoList = new ArrayList<GeoPoint>();
//		for (GeoPoint geoPoint : geoPointList ) {	
//			newGeoPoint = geoPoint.confine();
//			geoList.add(newGeoPoint);
//		}
//		return geoList;
//	} 
	
//	String[] fix_degree(String[] x) {
//		for (int i = 0; i < x.length; i++) {
//			boolean correctValue = false;
//			double val = Double.parseDouble(x[i]);
//			if ((val < -180.0) || (val > 180.0)) 
//				correctValue = true;
//			while (val > 180.0) val -= 360.0;
//			while (val <= -180.0)  val += 360.0; // val += 360							
//
//			if (correctValue)
//				x[i] = Double.toString(val);
//		}
//		return x;
//	}
	
	String fix_degree(String x) {
		for (int i = 0; i < x.length(); i++) {
			boolean correctValue = false;
			double val = Double.parseDouble(x);
			if ((val < -180.00) || (val > 180.00)) 
				correctValue = true;
			while (val > 180.00) val -= 360.00;
			while (val <= -180.00)  val += 360.00; // val += 360							

			if (correctValue)
				x = Double.toString(val);
		}
		return x;
	}
	
}
