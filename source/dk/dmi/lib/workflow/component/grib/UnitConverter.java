package dk.dmi.lib.workflow.component.grib;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.dmi.lib.common.GeoTransform;
import dk.dmi.lib.grib.GridPoint;
import dk.dmi.lib.grib.HasNoValueException;
import dk.dmi.lib.persistence.common.EntityUtils;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Unit Converter", 
		category = "Grib",
		description = "Converts a List of gridpoints between units",
        version = 1)
public class UnitConverter extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	static final String CELCIUS = "Celcius";
	static final String KELVIN = "Kelvin";	
	final String CEL_KEL = "CelKel";
	final String KEL_CEL = "KelCel";
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListUnitFrom(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return generateUnitArray();
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "2")
	public String[] getListUnitTo(String ignore) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return generateUnitArray();
	}

	private String[] generateUnitArray() {
		return new String[] {CELCIUS, KELVIN};
	}
	
	private String[] getConverter() {
		return new String[] {CEL_KEL, KEL_CEL};
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, 
					WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST}, argumentDescriptions = {"Input GridPoint List", "Input Unit", "Output Unit"}, 
					returnDescription = "Converted Gridpoint List Values")
    public List<GridPoint> execute(List<GridPoint> gridPoints, String unitFrom, String unitTo) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, HasNoValueException {

		List<GridPoint> resultGridPointList = new ArrayList<GridPoint>();
								
		for (GridPoint inputGridPoint : gridPoints) {
			GridPoint resultPoint = genericTransform(inputGridPoint, unitFrom, unitTo);
			resultGridPointList.add(resultPoint);
		}
						
        return resultGridPointList;
    }
	
	public GridPoint genericTransform(GridPoint gridPoint, String unitFrom, String unitTo ) throws HasNoValueException {
		double gpValue;
						
    	gpValue = gridPoint.getValue();
    	
	    if ((unitFrom == CELCIUS) && (unitTo == KELVIN)) {	    

	    	gpValue = convertF2C(gpValue);
	    	}
	    
	    if ((unitFrom == KELVIN) && (unitTo == KELVIN)) {
	    	gpValue = convertC2F(gpValue);	
	    }
	    gridPoint.setValue(gpValue);
 	    				
		return gridPoint;
	}
	
	public double convertF2C(double degreesFahrenheit) {
		return (degreesFahrenheit -32.0) / 9.0 * 5.0;
	}
	
	public double convertC2F(double degreesCelcius) {
		return (degreesCelcius / 5.0 * 9.0 + 32.0);
	}
 
	
}
