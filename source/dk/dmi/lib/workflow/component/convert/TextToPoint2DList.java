package dk.dmi.lib.workflow.component.convert;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Text to Point2D list", 
		category = "Convert",
		description = "Converts a text to a list of points2D object, specified by point class type, text point seperator and text axis seperator.",
        version = 1)
public class TextToPoint2DList extends BaseComponent {
	
	static final String POINT_CLASS_TYPE_DOUBLE = "Double";
	static final String POINT_CLASS_TYPE_FLOAT = "Float";
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfDatabases(String ignore) {
		String[] pointClassTypeList = {POINT_CLASS_TYPE_DOUBLE, POINT_CLASS_TYPE_FLOAT};
		return pointClassTypeList;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Text of points to be converted", "Point class type", "Characters separating points in text", "Characters separating each point axis in text"},
			returnDescription = "List of point2D object created from the text")
    public List<Point2D> execute(String text, String pointClassType, String pointSeperator, String axisSeperator) {
		List<Point2D> pointList = new ArrayList<Point2D>();
		String[] keyValuePairs = text.trim().split(pointSeperator);
		
		if(keyValuePairs.length > 0 && !keyValuePairs[0].equals("")) {
			for (String keyValuePair : keyValuePairs) {
				String[] keyValueList = keyValuePair.trim().split(axisSeperator);
				
				Point2D newPoint = createNewPointOfType(pointClassType, keyValueList[0].trim(), keyValueList[1].trim());
				pointList.add(newPoint);
			}
		}
		
		return pointList;
    }

	Point2D createNewPointOfType(String pointClassType, String xStr, String yStr) {
		Point2D newPoint = null;
		
		if(POINT_CLASS_TYPE_DOUBLE.equals(pointClassType)) {
			double x = Double.parseDouble(xStr);
			double y = Double.parseDouble(yStr);
			newPoint = new Point2D.Double(x, y);
		} else if(POINT_CLASS_TYPE_FLOAT.equals(pointClassType)) {
			float x = Float.parseFloat(xStr);
			float y = Float.parseFloat(yStr);
			newPoint = new Point2D.Float(x, y);
		}
		
		return newPoint;
	}
	
}
