package dk.dmi.lib.workflow.component.convert;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Point2D list to text", 
		category = "Convert",
		description = "Converts a list of points2D object to a text representation, specified by text point and axis seperator and how the coordinates should be formatted.",
        version = 1)
public class Point2DListToText extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"List of points to convert", "Characters to use for separating points", "Characters to use for separating point axis", "Decimal pattern to use for number formatting, e.g. ##0.### (use null to ignore)"},
			returnDescription = "List of point2D object created from the text")
    public String execute(List<Point2D> pointList, String pointSeperator, String axisSeperator, String decimalFormatPattern) {
		StringBuffer pointStringBuffer = new StringBuffer();
		NumberFormat numberFormat = creteDecimalFormatter(decimalFormatPattern);
		
		for (Point2D point2d : pointList) {
			if(pointStringBuffer.length() > 0) {
				pointStringBuffer.append(pointSeperator);
			}
			
			pointStringBuffer.append(getFormattedNumber(numberFormat, point2d.getX()));
			pointStringBuffer.append(axisSeperator);
			pointStringBuffer.append(getFormattedNumber(numberFormat, point2d.getY()));
		}
		
		return pointStringBuffer.toString();
    }

	DecimalFormat creteDecimalFormatter(String decimalFormatPattern) {
		
		DecimalFormat decimalFormat = null;
		
		if(decimalFormatPattern != null) {
			Locale locale  = new Locale("en", "UK");
			decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
			decimalFormat.applyPattern(decimalFormatPattern);
		}
		
		return decimalFormat;
	}
	
	Object getFormattedNumber(NumberFormat numberFormat, Double axisValue) {
		if(numberFormat != null) {
			return numberFormat.format(axisValue);
		} else {
			return axisValue;
		}
	}
	
}
