package dk.dmi.lib.workflow.component.grib;

import dk.dmi.lib.geo.GeoPoint;
import dk.dmi.lib.grib.GribField;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Has GRIB fields same grid and scan directions", 
		category = "Grib",
		description = "tests if the 2 GRIB fields has same grid and scan directions.",
        version = 1)
public class HasSameGrid extends BaseComponent {
  
	@ExecuteMethod(
			argumentDescriptions = {"first GribField", "second GribField"},
			returnDescription = "True if the 2 GribField's has same grid and scan directions. Otherwise false")
	public boolean execute(GribField gribField1, GribField gribField2) {
	  
	  //
	  // Same grid type ?
	  //
	  int gridType1 = gribField1.getDataRepresentationType();
	  int gridType2 = gribField1.getDataRepresentationType();
	  
	  if (gridType1 != gridType2)
	    return false;
	  
	  
	  //
	  // Same number of gridpoints along a latitude ?
	  //
	  if (gribField1.getNi() != gribField2.getNi())
	    return false;
	  
	  //
	  // Same number of gridpoints along a longitude ?
	  //
	  if (gribField1.getNj() != gribField2.getNj())
	    return false;
	  
	  //
	  // Same first gridpoint position ?
	  //
	  GeoPoint firstGridPoint1 = gribField1.getFirstGridPoint();
	  GeoPoint firstGridPoint2 = gribField2.getFirstGridPoint();
	  	  
	  if ( ! hasSamePosition(firstGridPoint1, firstGridPoint2))
	    return false;
	  
	  //
	  // Same last gridpoint position ?
	  //
	  GeoPoint lastGridPoint1 = gribField1.getLastGridPoint();
	  GeoPoint lastGridPoint2 = gribField2.getLastGridPoint();
	  	  
	  if ( ! hasSamePosition(lastGridPoint1, lastGridPoint2))		
	    return false;
	  
	  return true;
	}
	
	/**
	 * Test if the 2 geopoints has same latitude and longitude
	 * @param gp1 first geopoint
	 * @param gp2 socond geopoint
	 * @return true if the 2 geopoints has same latitude and longitude 
	 * in millidegrees
	 */
	private boolean hasSamePosition(GeoPoint gp1, GeoPoint gp2)
	{
	  // latitude and longitude are saved in the field in milli-degrees
	  //
	  // Same latitude ?
	  //
	  int lat1milli = (int) (1000*gp1.getLat());
	  int lat2milli = (int) (1000*gp2.getLat());
	  if (lat1milli != lat2milli)
	    return false;
	  
	  //
	  // Same longitude ?
	  //
	  int lon1milli = (int) (1000*gp1.getLon());
	  int lon2milli = (int) (1000*gp2.getLon());
	  if (lon1milli != lon2milli)
	    return false;
	 
	  return true;
	}
	

	
}
