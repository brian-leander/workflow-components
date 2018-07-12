package dk.dmi.lib.workflow.component.grib;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import java.nio.file.*;
import java.nio.charset.Charset;

import dk.dmi.lib.grib.GribField;
import dk.dmi.lib.grib.GridPoint;
import dk.dmi.lib.grib.HasNoValueException;
import dk.dmi.lib.grib.InvalidMethodException;
import dk.dmi.lib.grib.NotImplementedException;
import dk.dmi.lib.util.DateTime;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Write Grid Points To Text File", 
		category = "Grib",
		description = "Write Grid Points To Text File",
        version = 1)
public class WriteGridPointsToTextFile extends BaseComponent {
	boolean isUnique = true;
	 
	 private static DecimalFormat df2 = new DecimalFormat("##d.##");
	 
	@SuppressWarnings("null")
	@ArgumentListGetMethod(
			argumentIndex = "1")

	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"ArrayList of List of Grid Points", "Absolute Path to output textFile", "List of GridPoints Array"},
	 		returnDescription = "")
	public void execute(List<List<GridPoint>> gridPointListList, String absStrOutPath, GribField[] gribField) throws IOException, NotImplementedException, InvalidMethodException {	
		BufferedWriter writer = null; 
		String[] fileRecord  = null; 
		final String sep = ",  ";
		Path absOutPath = Paths.get(absStrOutPath);
		Integer key;
		DateTime analysisTime = new DateTime();
		System.out.println("forecastTimeUnit: " + gribField[0].getForecastTimeUnit());
		// String psign;
		Locale.setDefault(Locale.ENGLISH);
		df2.setRoundingMode(RoundingMode.UP);
		Integer u = new Integer(33);
		Integer v = new Integer(34);
		Integer m = new Integer(81); // land sea mask dummy -- not used
		
			try {			
				writer = Files.newBufferedWriter(absOutPath, Charset.forName("UTF-8"));
				Path path = absOutPath.getFileName();
				String fileName = path.toString();
				
				int no_lists = gridPointListList.size(); // gridPointListList.size() has to be 3
			
				HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
				for (int i = 0; i < no_lists; i++) {
					int indicator = gribField[i].getParUnitIndicator();
					hm.put(indicator, i);
				}
					
				analysisTime = gribField[0].getReferenceTime();  // dk.dmi.lib.util.DateTime
				
				// Make and write header
				 String[] fileHeader = {"Model: ECMWF 10 km wind ", "analysis_time: ", "analysis_length: ", "parameters: ", "no_points: "}; 
				
				fileHeader[1] = fileHeader[1] + analysisTime.getDateTimeString("YYYY-MM-dd HH"); 
				
				fileHeader[2] = fileHeader[2]  + gribField[0].getForecastTimeUnit();
				String paramStr = " 33, 81, 34";
				fileHeader[3] = fileHeader[3] + paramStr;  // to be corrected
	
				int size = gridPointListList.get(0).size();
				
				System.out.println("Size of GridPointList: " + size);
				fileHeader[4] = fileHeader[4] + " " + String.valueOf(size);
									
				for (int j = 0; j < fileHeader.length; j++) {
					writer.write(fileHeader[j]);
					writer.newLine();
				}			
				writer.newLine();
					
				// Make and write body	
					List<GridPoint> gridPointList_u = gridPointListList.get((int)hm.get(u));								  
					List<GridPoint> gridPointList_v = gridPointListList.get((int)hm.get(v));					
								
					Iterator<GridPoint> gridPointIter_u = gridPointList_u.iterator();			// <GridPoint> added to iterator		
					Iterator<GridPoint> gridPointIter_v = gridPointList_v.iterator();
										
					while (gridPointIter_u.hasNext()) {
						while (gridPointIter_v.hasNext()) {
							GridPoint gridPointValue_u = (GridPoint) gridPointIter_u.next();
							GridPoint gridPointValue_v = (GridPoint) gridPointIter_v.next();
							
							try {
								double lat = gridPointValue_u.getLat();
								double lon = gridPointValue_u.getLon();
																									
								double u_value = gridPointValue_u.getValue();															
								double v_value = gridPointValue_v.getValue();
																		
								String field0 = String.format("%7.2f", lat);
								String field1 = String.format("%7.2f", lon);
															
								String field2 = String.format("%7.2f", u_value);
								String field3 = String.format("%7.2f", v_value); 
																
								String joined = field1 + sep + field0 + sep + field2 + sep + field3;
											
								writer.write(joined);
								writer.newLine();
								
							} 
							catch (HasNoValueException e) {
									e.printStackTrace();
									// skip gridPoints without values
									continue;
							}
					}
					writer.newLine();
					writer.write("End of section " + 2);
					writer.newLine();
			    }
			}
	        catch (IOException | NullPointerException  e ) {
	            e.printStackTrace();
	        }
	        finally {   // unnecessary in Java 8, nio.2 that automatically closes files?
	            if (writer != null) {
	                writer.flush();
	            	writer.close();
	        }
         }		
	 }
	
		
	public int hashCode(String x, String y) {	
		return (x + "," + y).hashCode();
	}
	
	public List<String> listFileNamesInDirectory(String dir) throws IOException {
		List<String> fileNamesList = new ArrayList<String>();
		Files.newDirectoryStream(Paths.get(dir),
				path -> path.toString().endsWith(".grib")).forEach(filePath -> fileNamesList.add(filePath.toString()));
		return fileNamesList;
	}
}

