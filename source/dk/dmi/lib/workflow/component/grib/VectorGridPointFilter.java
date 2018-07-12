package dk.dmi.lib.workflow.component.grib;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import dk.dmi.lib.geo.GeoPoint;
import dk.dmi.lib.geo.GeoPolygon;
import dk.dmi.lib.grib.GribField;
import dk.dmi.lib.grib.GribUVVectorField;
import dk.dmi.lib.grib.GribVectorField;
import dk.dmi.lib.grib.GridPoint;
import dk.dmi.lib.grib.HasNoValueException;
import dk.dmi.lib.grib.UserErrorException;
import dk.dmi.lib.grib.VectorGridPoint;
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
		name = "Vector Grid point filter", 
		category = "Grib",
		description = "Get u- and v- composants of the VectorGridPoint's inside a polygon and/or representeted by an index list, which has a size value that is OK compared to the threshold and operator. Use property 'include none value points'"
		    + " to add grid points with no value to the result list.",
        version = 1)
public class VectorGridPointFilter extends BaseComponent {
  
  public final static int LESS_THAN = 1;
  public final static int GREATHER_THAN = 2;
  
  private final String LESS_THAN_STRING    = "Less than";
  private final String GREATER_THAN_STRING = "Greather than";
  
  
  @ArgumentListGetMethod(
			argumentIndex = "4")
  public String[] getListOfOptions(String ignore) {
	  String[] operatorOptions = {LESS_THAN_STRING, GREATER_THAN_STRING};
	  return operatorOptions;
  }
  
  //////////////////////////////////////////////////////////////////////////////
  @ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, 
					WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_CHECK_BOX},
			argumentDescriptions = {"u-composants grib field",
			                        "v-composants grib field",
			                        "Ordered list of polygon points (use null to ignore) BE AWARE x is longitude and y is latitude",
			                        "List of grid point indexes (use null to ignore)",
			                        "Operator to threshold, indicates if the gridpoint value should be greather or less than the thresshold value to be accepted (will be ignored if threshold is null)",
			                        "Threshold value to be compared with the gridpoint value (use null to ignore)",
			                        "Should gridpoints with no value be included in the result list"}, 
			returnDescription = "Vector containing 2 lists of GridPoint's. First list with size values. Second list with direction values. All positioned inside the polygon specified or in the List of grid-indexies.")
  public Vector<List<GridPoint>> execute(GribField uField,
                                          GribField vField,
                                          List<Point2D.Double> polygonPoints,
                                          List<Integer> gridPointIndexs,
                                          String thresholdOperator,
                                          Double thresholdValue,
                                          boolean includeNoneValuePoints) throws UserErrorException
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
    List<VectorGridPoint> vectorGridPointsContained = null;
    
    if (polygonPoints != null)
    {
      //
      // Get gridpoints inside polygon with value is OK
      //
      
      //
      // Make a GeoPolygon from the List of Point2D.Double's
      //
      GeoPolygon geoPolygon = getGeoPolygon(polygonPoints);
      
      
      vectorGridPointsContained = getVectorGridPointsContainedAndVectorSizeOK(uField,
	                                                                     vField,
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
      vectorGridPointsContained = getVectorGridPointsFromIndexs(uField,
	                                                        vField,
	                                                        gridPointIndexs,
	                                                        thresholdValue,
	                                                        greaterThan,
	                                                        includeNoneValuePoints);
    }

    //
    // Convert List<VectorGridPoint> to List<List<GridPoint>>
    // where the first List<GridPoint> contains the vector size-values
    // and the second List<GridPoint> contains the vector direction-values
    //
    Vector<List<GridPoint>> sizeListDirecListVector = new Vector<>(2);
    List<GridPoint> sizeGridPontList  = new LinkedList<>();
    List<GridPoint> direcGridPontList = new LinkedList<>();

    
    for (VectorGridPoint vectorGp : vectorGridPointsContained)
    {
      int gridIndex = vectorGp.getGridIndex();
      
      GeoPoint position = (GeoPoint) vectorGp;
      GridPoint sizeGridPoint  = null;
      GridPoint direcGridPoint = null;
      try
      {
	double size  = vectorGp.getSize();
	double direc = vectorGp.getDirec();
	sizeGridPoint  = new GridPoint(position, size);
	direcGridPoint = new GridPoint(position, direc);
      }
      catch (HasNoValueException e)
      {
	sizeGridPoint  = new GridPoint(position);
	direcGridPoint = new GridPoint(position);	  
      }
      sizeGridPoint.setGridIndex(gridIndex);
      direcGridPoint.setGridIndex(gridIndex);
      sizeGridPontList.add(sizeGridPoint);
      direcGridPontList.add(direcGridPoint);
    }

    sizeListDirecListVector.add(sizeGridPontList);
    sizeListDirecListVector.add(direcGridPontList);

    return sizeListDirecListVector;
  }

  //////////////////////////////////////////////////////////////////////////////
  /**
   * Get GridPoint's inside the polygon and which has a OK value 
   * @param gribField Contains the Gridpoint's
   * @param polygon
   * @return
   * @throws UserErrorException 
   */
  private List<VectorGridPoint> getVectorGridPointsContainedAndVectorSizeOK(GribField uField,
                                                                             GribField vField,
                                                                             GeoPolygon geoPolygon,
                                                                             Double threshold,
                                                                             boolean greaterThan,
                                                                             boolean acceptPointsWithoutValue) throws UserErrorException
  {
    
    //
    // create vector field from u- and v-field's
    //
    GribVectorField vectorField = new GribUVVectorField(uField, vField);

    //
    // Add GridPoint's from the Gribfield that is positioned inside geoPolygon
    // and which has a value {greather than | less than} the thress value
    // except those gridpoint represented in the gridindex-list
    //
    List<VectorGridPoint> pointsContainedAndVectorSizeOK = new LinkedList<>();
    
    for (VectorGridPoint vectorGridPoint : vectorField)
    {
      //
      // Make sure gridPoint's longitude is in the interval ]-180, +180]
      //
      double lat = vectorGridPoint.getLat();
      double lon = vectorGridPoint.getLon();
      while (lon > 180.)
	lon -= 360.;
      while (lon <= -180.)
	lon += 360.;
      vectorGridPoint.setLocation(lat, lon);
      
      
      //
      // Exclude geoPoints outside the geo-polygon
      //
      if ( ! geoPolygon.contains(vectorGridPoint)) continue;
      
      //
      // Test the VectorGridPoint's vector size with the threshold value
      //
      boolean vectorSizeOK = isVectorSizeOK(vectorGridPoint, threshold, greaterThan , acceptPointsWithoutValue);
      if (vectorSizeOK)
	pointsContainedAndVectorSizeOK.add(vectorGridPoint);
    }
    
    return pointsContainedAndVectorSizeOK;
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
   * @throws UserErrorException 
   */
  private List<VectorGridPoint> getVectorGridPointsFromIndexs(GribField uField,
                                                               GribField vField,
                                                               List<Integer> gridIndexList,
                                                               Double threshold,
                                                               boolean greaterThan,
                                                               boolean includePointsWithoutValue) throws UserErrorException
  {
    
    // List of VectorGridPoint's contained in the gridIndex-list
    List<VectorGridPoint> pointsContained = new LinkedList<>();
    
    //
    // create vector field from u- and v-field's
    //
    GribVectorField vectorField = new GribUVVectorField(uField, vField);
    
    
    //
    // Get the gridPoint's represented in the gridIndexList and test its value
    // if any against the threhold-value and the operator
    //
    for (Integer gridIndex : gridIndexList)
    {
      VectorGridPoint vectorGridPoint = vectorField.getVectorGridPoint(gridIndex);

      //
      // Compare the VectorGidpoint's vector-size aganst threshold and operator
      //
      if (isVectorSizeOK(vectorGridPoint, threshold, greaterThan, includePointsWithoutValue) )
	pointsContained.add(vectorGridPoint);
    }
    
    return pointsContained;
  }

  //////////////////////////////////////////////////////////////////////////////
  /**
   * test if the gridpoint's vector size is OK
   * @param vectorGridPoint The grid point vector
   * @param threshold Threshold to be compared with the gridpoint's value
   * If null any existing gridpoint-value is accepted
   * @param acceptPointsWithoutValue If true a point without a values is accepted.
   * @param greaterThan
   * @return true if the gridpoints vector size is accepted. Otherwise false
   */
  private boolean isVectorSizeOK(VectorGridPoint vectorGridPoint,
                              Double thresholdValue,
                              boolean greaterThan,
                              boolean acceptPointsWithoutValue)
  {
    
    //
    // Accept all values if threshold is not specified
    // and the gridpoint has a value
    //
    if (thresholdValue == null && vectorGridPoint.hasValue()) return true;
    
    //
    // Get the gridpoint's value if any
    // Set it to 0 if it not has a value and no value is accepted
    //
    double vectorSize = 0;
    try
    {
      vectorSize = vectorGridPoint.getSize();
    }
    catch(HasNoValueException e)
    {
      // The vectorGridpoint has no vector. Return true if acceptable
      if (acceptPointsWithoutValue)
	return true;
    }
    
    
    boolean vectorSizeOK = false;

    if (vectorSize >= thresholdValue)
    {
      if (greaterThan)
	vectorSizeOK = true;
    }
    else
    {
      if ( ! greaterThan)
	vectorSizeOK = true;	  
    }

    return vectorSizeOK;
  }
  
  
  //////////////////////////////////////////////////////////////////////////////
  /**
   * Create a GeoPolygon from a List of Point2D.Double's
   * 
   * @param polygonpoints The List of Point2D.Double's
   * @return geopolygon
   */
  public GeoPolygon getGeoPolygon(List<Point2D.Double> polygonPoints)
  {
    List<GeoPoint> geoPointList = getGeoPointList(polygonPoints);
    GeoPolygon geoPolygon = new GeoPolygon(geoPointList);
    
    return geoPolygon;
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
      
      //
      // make sure that longitude is in the interval ]-180, +180] degree
      //
      while (lon > 180.)
	lon -= 360.;
      while (lon <= -180.)
	lon += 360.;
      
      GeoPoint geoPoint = new GeoPoint(lat, lon);
      geoPointList.add(geoPoint);
    }
    
    return geoPointList;
  }
  

}
