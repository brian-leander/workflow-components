package dk.dmi.lib.workflow.component.grib;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import dk.dmi.lib.common.math.Calculator;
import dk.dmi.lib.grib.GribField;
import dk.dmi.lib.grib.GridPoint;
import dk.dmi.lib.grib.HasNoValueException;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Calculate grib field value", 
		category = "Grib",
		description = "Calculate grib field value, based on a list of grid points or a list of grid point indexes.",
        version = 1)
public class CalculateGribFieldValue extends BaseComponent
{

  public static final String CALCULATION_METHOD_SUM = "SUM";
  public static final String CALCULATION_METHOD_AVG = "AVG";
  public static final String CALCULATION_METHOD_MAX = "MAX";
  public static final String CALCULATION_METHOD_MIN = "MIN";
  public static final String CALCULATION_METHOD_QUANTILE = "QUANTILE";

  //////////////////////////////////////////////////////////////////////////////
  @ExecuteMethod(
      argumentDescriptions = {"Grib field", "List of grid points indexes to include in the calculation (Use null to ignore)", "Calculation method, e.g. "+CALCULATION_METHOD_SUM+", "+CALCULATION_METHOD_AVG+", "+CALCULATION_METHOD_MAX+", "+CALCULATION_METHOD_MIN+", "+CALCULATION_METHOD_QUANTILE+"_<fraction> [fraction (1.0=max)]"},
      returnDescription = "Calculated grib field value")
  public float execute(GribField gribField,
	                 List<Integer> gridPointIndexs,
	                 String calculationMethod) throws Exception
  {
    //
    // Split calculationMethod into operator and value  (only value for quantile operation
    //
    String[] operatorAndValue = calculationMethod.split("_");
    String operator = operatorAndValue[0];
    
    
     //
    // Make List of gridpoint values from the gridpoints 
    // specified in the List of grid indexes.
    // The gridpoint without a value are excluded.
    //
    List<Float> valueList = new LinkedList<>();
    for (Integer gridIndex:gridPointIndexs)
    {
      if (gridIndex == null) continue;
      GridPoint gridpoint = gribField.getGridPoint(gridIndex);
      try
      {
	Float gridPointValue = (float) gridpoint.getValue();
	valueList.add(gridPointValue);
      }
      catch(HasNoValueException e)
      {
	// Ignore gridpoints witout a value
      }
    }

    //
    // There must be at least one gridpoint that has a value
    // in order to make the calculations
    //
    if (valueList.size() == 0)
      throw new Exception("Empty List not allowed");
     
    
    float result = -123.456f;

    switch(operator)
    {

      case CALCULATION_METHOD_SUM:
      {
	result = Calculator.calculateSum(valueList);
	break;
      }	    

      case CALCULATION_METHOD_AVG:
      {
	result = Calculator.calculateAvg(valueList);
	break;
      }

      case CALCULATION_METHOD_MAX:
      {
	result = Calculator.calculateMax(valueList);
	break;
      }

      case CALCULATION_METHOD_MIN:
      {
	result = Calculator.calculateMin(valueList);
	break;
      }
      case CALCULATION_METHOD_QUANTILE:
      {
	String sValue   = operatorAndValue[1];
	float fraction = Float.valueOf(sValue);
	result = Calculator.calculateQuantile(valueList, fraction);
	break;
      }
      default:
	throw new IllegalArgumentException("Invalid calculation method: " + operator);
    }

    return result;
  }
  

}
