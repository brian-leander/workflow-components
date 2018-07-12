package dk.dmi.lib.workflow.component.grib;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import dk.dmi.lib.geo.GeoPoint;
import dk.dmi.lib.geo.GeoPolygon;
import dk.dmi.lib.grib.GribField;
import dk.dmi.lib.grib.GridPoint;
import dk.dmi.lib.grib.HasNoValueException;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

/**
 * Class to filter those Gridpoint's in a GribFied in different ways
 * 
 * @author mlm
 *
 */
@Component(
		name = "Grid point filter", 
		category = "Grib",
		description = "Get GridPoint's inside a polygon and/or representeted by an index list, which has a value that is OK compared to the threshold and operator. Use property include none value points, to add grid points with no value to the result list.",
        version = 1)
public class GridPointFilter extends BaseComponent {
  
  public final static int LESS_THAN = 1;
  public final static int GREATHER_THAN = 2;
  
  private final String LESS_THAN_STRING    = "Less than";
  private final String GREATER_THAN_STRING = "Greather than";
  
  
  @ArgumentListGetMethod(
			argumentIndex = "3")
  public String[] getListOfDatabases(String ignore) {
	  String[] operatorOptions = {LESS_THAN_STRING, GREATER_THAN_STRING};
	  return operatorOptions;
  }
  
  //////////////////////////////////////////////////////////////////////////////
  @ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX},
			argumentDescriptions = {"Grib field",
			                        "Ordered list of polygon points (use null to ignore) BE AWARE x is longitude and y is latitude",
			                        "List of grid point indexes (use null to ignore)",
			                        "Operator to threshold, indicates if the gridpoint value should be greather or less than the thresshold value to be accepted (will be ignored if threshold is null)",
			                        "Threshold value to be compared with the gridpoint value (use null to ignore)",
			                        "Should gridpoints with no value be included in the result list"}, 
			returnDescription = "List of GridPoint's positioned inside the polygon specified or in the List of grid-indexies.")
  public List<GridPoint> execute(GribField gribField,
                                  List<Point2D.Double> polygonPoints,
                                  List<Integer> gridPointIndexs,
                                  String thresholdOperator,
                                  Double thresholdValue,
                                  boolean includeNoneValuePoints)
    {
    
    //
    // Check arguments. One and only one of the two polygon or gridIndexList
    //                  must be null
    //
    if (polygonPoints == null && gridPointIndexs == null)
      throw new IllegalArgumentException("Only one of polygon or gridIndexList must be null");
    
    if (polygonPoints != null && gridPointIndexs != null)
      throw new IllegalArgumentException("Only one of polygon or gridIndexList must be different from null");
    
    
    //
    // The thresholdOperator argument: Shall the Gridpoint value be greather than
    // or less than the thresshold value to be included in the returned list.
    //
    boolean greaterThan = true;
    if (thresholdOperator.equals(GREATER_THAN_STRING))
    {
      greaterThan = true;
    }
    else if (thresholdOperator.equals(LESS_THAN_STRING))
    {
      greaterThan = false;
    }
    else
    {
      String mess = "Value must be either '"+GREATER_THAN_STRING+"' or '"+LESS_THAN_STRING+"'";
      throw new IllegalArgumentException(mess);
    }

    //
    // Get list of gridpoints from geo-polygon or indexies
    //
    List<GridPoint> gridPointsContained = null;
    
    if (polygonPoints != null)
    {
      //
      // Get gridpoints inside polygon with value is OK
      //
      
      //
      // Make a GeoPolygon from the List of GeoPoint's
      //
      List<GeoPoint> polygonVertexes = getGeoPointList(polygonPoints);
      
      GeoPolygon geoPolygon = new GeoPolygon(polygonVertexes);
      gridPointsContained = getGridPointsContainedAndValueOK(gribField,
	                                                     geoPolygon,
	                                                     thresholdValue,
	                                                     greaterThan,
	                                                     includeNoneValuePoints);
    }
    if (gridPointIndexs != null)
    {
      //
      // Get gridpoints represented by the list og gridindexs
      // and with which value is OK
      //
      gridPointsContained = getGridPointsFromIndexs(gribField,
                                                    gridPointIndexs,
                                                    thresholdValue,
                                                    greaterThan,
                                                    includeNoneValuePoints);
      
    }
    
    return gridPointsContained;
  }

  //////////////////////////////////////////////////////////////////////////////
  /**
   * Get GridPoint's inside the polygon and which has a OK value 
   * @param gribField Contains the Gridpoint's
   * @param polygon
   * @return
   */
  private List<GridPoint> getGridPointsContainedAndValueOK(GribField gribField,
                                                            GeoPolygon geoPolygon,
                                                            Double threshold,
                                                            boolean greaterThan,
                                                            boolean acceptPointsWithoutValue)
  {
    //
    // Add GridPoint's from the Gribfield that is positioned inside geoPolygon
    // and which has a value {greather than | less than} the thress value
    // except those gridpoint represented in the gridindex-list
    //
    List<GridPoint> pointsContainedAndValueOK = new LinkedList<>();
    
    for (GridPoint gridPoint : gribField)
    {
      // Make sure gridPoint's longitude is in the interval ]-180, +180]
      normalizeLongitude(gridPoint);
      
      //
      // Exclude geoPoints outside the geo-polygon
      //
      if ( ! geoPolygon.contains(gridPoint)) continue;
      
      //
      // Test the gridpoint value with the threshold value
      //
      boolean valueOK = isValueOK(gridPoint, threshold, greaterThan , acceptPointsWithoutValue);
      if (valueOK)
	pointsContainedAndValueOK.add(gridPoint);
    }
    
    return pointsContainedAndValueOK;
  }

  
  //////////////////////////////////////////////////////////////////////////////
  /**
   * Get GridPoint's inside the polygon and/or representeted by the index-list
   *  which has a value that is OK compared to the threshold and operator
   * @param gribField Contains the Gridpoint's
   * @param polygon
   * @param thresholdOperator Indicates if the gridpoint value shall be greather than 
   * or less than the thresshold value to be included in the returned list.
   * Allowed values: {"GreatherThan" | "LessThan"}
   * shall the gridpoint value if any be greater than or less than the threshold
   * @param threshold the value to be compared with the gridpoint value
   * @return List of GridPoint's inside the polygon 
   */
  private List<GridPoint> getGridPointsFromIndexs(GribField gribField,
                                                   List<Integer> gridIndexList,
                                                   Double threshold,
                                                   boolean greaterThan,
                                                   boolean includePointsWithoutValue)
  {
    
    // List of gridpoints contained in the gridIndex-list
    List<GridPoint> pointsContained = new LinkedList<>();
    
    //
    // Get the gridPoint's represented in the gridIndexList and test its value
    // if any against the threhold-value and the operator
    //
    for (Integer index : gridIndexList)
    {
    	
      GridPoint gridPoint = gribField.getGridPoint(index);

      //
      // Compare the gridpoints value aganst threshold and operator
      //
      if (isValueOK(gridPoint, threshold, greaterThan, includePointsWithoutValue) )
	pointsContained.add(gridPoint);
    }
    
    return pointsContained;
  }

  //////////////////////////////////////////////////////////////////////////////
  /**
   * test if the gridpoint's value is OK
   * @param gridPoint The grid point
   * @param threshold Threshold to be compared with the gridpoint's value
   * If null any existing gridpoint-value is accepted
   * @param acceptPointsWithoutValue If true a point without a values is accepted.
   * @param greaterThan
   * @return true if the gridpoints value is accepted. Otherwise false
   */
  private boolean isValueOK(GridPoint gridPoint,
                              Double thresholdValue,
                              boolean greaterThan,
                              boolean acceptPointsWithoutValue)
  {
    //
    // Test if the gridpoint has a value 
    // if gridpoints without a value is not accepted
    //
    if ( ! acceptPointsWithoutValue)
      if ( ! gridPoint.hasValue()) return false;
    
    
    //
    // Accept all values if threshold is not specified
    // and the gridpoint has a value
    //
    if (thresholdValue == null && gridPoint.hasValue()) return true;
    
    //
    // Get the gridpoint's value if any
    // Set it to 0 if it not has a value and no value is accepted
    //
    double gridPointValue = 0;
    try
    {
      gridPointValue = gridPoint.getValue();
    }
    catch(HasNoValueException e)
    {
      // The gridpoint has no value. Return true if acceptable
      if (acceptPointsWithoutValue)
	return true;
    }
    
    
    boolean valueOK = false;

    if (thresholdValue != null)
    {
      if (gridPointValue >= thresholdValue)
      {
	if (greaterThan)
	  valueOK = true;
      }
      else
      {
	if ( ! greaterThan)
	  valueOK = true;	  
      }
    }
    else
      valueOK = true; 

    return valueOK;
  }
  
 //////////////////////////////////////////////////////////////////////////////
  /**
   * Convert a List of Point2D.Double's to a list of GeoPoint's
   * 
   * @param pointList The List of Point2D.Double's
   * @return List of GeoPoint's
   */
  private List<GeoPoint> getGeoPointList(List<Point2D.Double> pointList)
  {
    List<GeoPoint> geoPointList = new LinkedList<>();
    
    for (Point2D.Double vertexPoint : pointList)
    {
      double lat = vertexPoint.getY();
      double lon = vertexPoint.getX();
      GeoPoint geoPoint = new GeoPoint(lat, lon);      
      // make sure that longitude is in the interval ]-180, +180] degree
      normalizeLongitude(geoPoint);
      
      geoPointList.add(geoPoint);
    }
    
    return geoPointList;
  }
  
  //////////////////////////////////////////////////////////////////////////////
  /**
   * Set the GeoPoint's longitude inside the interval 9-180, +180]
   * 
   * @param geoPoint
   */
  private void normalizeLongitude(GeoPoint geoPoint)
  {
    double lat = geoPoint.getLat();
    double lon = geoPoint.getLon();
    
    //
    // make sure that longitude is in the interval ]-180, +180] degree
    //
    while (lon > 180.)
	lon -= 360.;
    while (lon <= -180.)
	lon += 360.;
        
    geoPoint.setLocation(lat, lon);
  }
  //////////////////////////////////////////////////////////////////////////////
  public static void main(String[] args)
  {
    GridPointFilter gridPointFilter = new GridPointFilter();
    gridPointFilter.getGeoPointListTest(gridPointFilter);
    gridPointFilter.isValueOkTest(gridPointFilter);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private void isValueOkTest(GridPointFilter gridPointFilter)
  {
    //
    // Make gridPoint with value = 1.00 and 0.00
    //
    double landValue = 1.00;
    double seaValue  = 0.00;
    GeoPoint position = new GeoPoint(56,12);
    GridPoint landPoint = new GridPoint(position, landValue);
    GridPoint seaPoint  = new GridPoint(position, seaValue);
    
    double thresholdValue = 0.5;
    boolean greaterThan = false;
    boolean acceptPointsWithoutValue = false;
    
    LOGGER.info("-------------------------------------------------" + System.lineSeparator());    
    LOGGER.info(String.format("value OK for landPoint = " + gridPointFilter.isValueOK(landPoint, thresholdValue, greaterThan, acceptPointsWithoutValue)));
    LOGGER.info(String.format("value OK for seaPoint  = " + gridPointFilter.isValueOK(seaPoint, thresholdValue, greaterThan, acceptPointsWithoutValue)));
  }
  
  
  //////////////////////////////////////////////////////////////////////////////
  private void getGeoPointListTest(GridPointFilter gridPointFilter)
  {
	LOGGER.info("Testing GridPointFilter");
    
    Point2D.Double sw = new Point2D.Double(11d, 55d);   // (x,y) = (lon, lat)
    Point2D.Double nw = new Point2D.Double(11d, 57d);
    Point2D.Double ne = new Point2D.Double(13d, 57d);
    Point2D.Double se = new Point2D.Double(13d, 55d);
    List<Point2D.Double> xyPointList = new LinkedList<>(); 
    xyPointList.add(sw);
    xyPointList.add(nw);
    xyPointList.add(ne);
    xyPointList.add(se);
    
    //
    // make GeopointList and GeoPolygon
    //
    List<GeoPoint> geoPointList = gridPointFilter.getGeoPointList(xyPointList);
    GeoPolygon geoPolygon = new GeoPolygon(geoPointList);
    
    //
    // Print size of lists
    //
    LOGGER.info("Number points in xyPointList  = " + xyPointList.size());
    LOGGER.info("Number points in geoPointList = " + geoPointList.size());
    
    //
    // Print xyPointList and geoPointList
    //    
    LOGGER.info("xyPointList:");
    for (GeoPoint geoPont : geoPointList)
    {
    	LOGGER.info(geoPont.toString());
    }
        
    LOGGER.info(System.lineSeparator() + "xyPointList:");
    for (Point2D.Double xyPont : xyPointList)
    {      
      LOGGER.info(xyPont.toString());
    }
    
    //
    // TestPoint (56,12)
    //
    GeoPoint geoTestPoint = new GeoPoint(56d, 12d);
    GridPoint gridTestPoint = new GridPoint(geoTestPoint);
        
    LOGGER.info(System.lineSeparator() + "geoTestPoint inside  = " + geoPolygon.contains(geoTestPoint));
    LOGGER.info("gridTestPoint inside = " + geoPolygon.contains(gridTestPoint));
  }

}
