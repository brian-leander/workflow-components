package dk.dmi.lib.workflow.component.polygon;

import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.common.FileUtils;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.GridFileController;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.ParameterController;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.TimeResolutionController;
import java.util.List;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFile;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.TimeResolution;
import dk.dmi.lib.polygon.data.DataFilePolygonValue;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@Component(
	name = "Create Aggregated Grid Content",
	category = "Polygon",
	description = "Creates aggregated grid content based on daily grid content, specified by parameter, timestamp and grid files base location.",
	version = 3)
public class CreateAggregatedGridContentFromDaily3 extends BaseComponent {

    @ExecuteMethod(
	    argumentDescriptions = {"Parameter", "Zero-padded timestamp to be aggregated", "Grid files base location"},
	    returnDescription = "List of grid cell values containing id, eastings, northings and aggregated value")
    public List<String> execute(Parameter parameter, Date timeStamp, String gridFileLocation) throws Exception {
    	List<String> gridPointsTextList = null;
    	ParameterController parameterController = ParameterController.getInstance();
		
    	if(parameterController.isMonthlyParameter(parameter) || parameterController.isYearlyParameter(parameter)) {
    		gridPointsTextList = createGridFileFromGridFiles(parameter, timeStamp, gridFileLocation);
    	} else {
    		throw new Exception("Error: Unable to aggregate grid content, wrong parameter time resolution must be monthly or yearly.");
    	}
		
    	if(gridPointsTextList != null && gridPointsTextList.size() == 0) {
    		gridPointsTextList = null;
    	}
    	
		return gridPointsTextList;
	}

	List<String> createGridFileFromGridFiles(Parameter parameter, Date timeStamp, String gridFileLocation) throws Exception {
		TimeResolutionController timeResolutionController = TimeResolutionController.getInstance();
		TimeResolution dailyTimeResolution = timeResolutionController.getTimeResolutionById(TimeResolutionController.DAY);
		
		ParameterController parameterController = ParameterController.getInstance();
		Parameter dailyParameterForElemNo = parameterController.getParametersByTimeResolutionAndElementNumber(dailyTimeResolution, parameter.getElementNumber()).get(0);
		Date timeTo = createToDate(parameter, timeStamp);
		
		GridFileController gridFileController = GridFileController.getInstance();
		List<GridFile> gridFileList = gridFileController.getGridFileForParameterAndTimeObs(dailyParameterForElemNo, timeStamp, timeTo);
		
		insureAllGridFilesExistLocally(gridFileList);
		boolean allDailyGridFilesAvailable = isAllDailyGridFilesAvailable(parameter, timeStamp, gridFileList);
		List<String> gridPointsTextList = null;
		
		if(allDailyGridFilesAvailable) {
			Map<String, GridFileData> gridFileDataMap = createGridFileDataMap(parameter, gridFileLocation, gridFileList);
			gridPointsTextList = createGridPointsTextList(gridFileDataMap);
		}
		
		return gridPointsTextList;
	}

	Date createToDate(Parameter parameter, Date timeStamp) throws Exception {
		ParameterController parameterController = ParameterController.getInstance();
		Date timeTo = null;
		
		if(parameterController.isMonthlyParameter(parameter)) {
			timeTo = DateUtils.alterDate(timeStamp, DateUtils.DATE_FIELD_TEXT_MONTH, 1);
		} else if(parameterController.isYearlyParameter(parameter)) {
			timeTo = DateUtils.alterDate(timeStamp, DateUtils.DATE_FIELD_TEXT_YEAR, 1);
		} else {
			throw new Exception("Error creating period to date, wrong parameter time resolution.");
		}
		
		timeTo = DateUtils.alterDate(timeTo, DateUtils.DATE_FIELD_TEXT_MINUTE, -1);
		
		if(timeTo.after(new Date())) {
			timeTo = DateUtils.alterDate(new Date(), DateUtils.DATE_FIELD_TEXT_DAY, -1);
		}
		
		return timeTo;
	}
	
	void insureAllGridFilesExistLocally(List<GridFile> gridFileList) throws Exception {
		InsureGridFileExistLocally insureGridFileExistLocally = new InsureGridFileExistLocally();
		
		for (GridFile gridFile : gridFileList) {
			insureGridFileExistLocally.execute(gridFile);
		}
	}
	
	boolean isAllDailyGridFilesAvailable(Parameter parameter, Date timeStamp, List<GridFile> gridFileList) throws Exception {
		GregorianCalendar currentCalendar = new GregorianCalendar();
		GregorianCalendar timeStampCalendar = new GregorianCalendar();
		timeStampCalendar.setTime(timeStamp);
		
		ParameterController parameterController = ParameterController.getInstance();
		int expectedGridFiles = 0;
		
		if(parameterController.isMonthlyParameter(parameter)) {
			int currentYear = currentCalendar.get(GregorianCalendar.YEAR);
			int currentMonth = currentCalendar.get(GregorianCalendar.MONTH);
			int timeStampYear = timeStampCalendar.get(GregorianCalendar.YEAR);
			int timeStampMonth = timeStampCalendar.get(GregorianCalendar.MONTH);
			
			if(currentYear == timeStampYear && currentMonth == timeStampMonth) {
				expectedGridFiles = currentCalendar.get(GregorianCalendar.DAY_OF_MONTH);
			} else {
				expectedGridFiles = timeStampCalendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
			}
		} else if(parameterController.isYearlyParameter(parameter)) {
			int currentYear = currentCalendar.get(GregorianCalendar.YEAR);
			int timeStampYear = timeStampCalendar.get(GregorianCalendar.YEAR);
			
			if(currentYear == timeStampYear) {
				expectedGridFiles = currentCalendar.get(GregorianCalendar.DAY_OF_YEAR);
			} else {
				expectedGridFiles = timeStampCalendar.getActualMaximum(GregorianCalendar.DAY_OF_YEAR);
			}
		} else {
			throw new Exception("Error checking available daily grid file, wrong parameter time resolution.");
		}
		
		boolean gridFileListContainsTodayDay = doesGridFileListContainTodaysDay(gridFileList);
		
		if(!gridFileListContainsTodayDay) {
			expectedGridFiles--;
		}
		
		boolean allGridFilesAvailable = gridFileList.size() >= expectedGridFiles;
		return allGridFilesAvailable;
	}
	
	boolean doesGridFileListContainTodaysDay(List<GridFile> gridFileList) throws Exception {
		Date currentDate = DateUtils.zeroPadDate(new Date(), DateUtils.DATE_FIELD_TEXT_DAY);
		currentDate = DateUtils.alterDate(currentDate, DateUtils.DATE_FIELD_TEXT_MILLISECOND, -1);
		
		for (GridFile gridFile : gridFileList) {
			Date gridFileTimeObs = gridFile.getTimeObs();
			
			if(gridFileTimeObs.after(currentDate)) {
				return true;
			}
		}
		
		return false;
	}
	
	Map<String, GridFileData> createGridFileDataMap(Parameter parameter, String gridFileLocation, List<GridFile> gridFileList) throws IOException, Exception {
		Map<String, GridFileData> gridFileDataMap = new HashMap<String, GridFileData>();
		
		for (GridFile gridFile : gridFileList) {
			List<List<String>> lineList = FileUtils.textFileToListListReader(gridFileLocation+gridFile.getFileUri(), " ", FileUtils.COMPRESSION_GZIP_KEY, 0);
			
			if(lineList != null) {
				for (List<String> gridFileLine : lineList) {
					String gridFileLineId = gridFileLine.get(0);
					GridFileData currGridFileData = gridFileDataMap.get(gridFileLineId);
					
					if(currGridFileData == null) {
						String gridFileLineEastings = gridFileLine.get(1);
						String gridFileLineNorthings = gridFileLine.get(2);
						currGridFileData = new GridFileData(parameter, gridFileLineId, gridFileLineEastings, gridFileLineNorthings);
						gridFileDataMap.put(gridFileLineId, currGridFileData);
					}
					
					double gridFileLineValue = Double.parseDouble(gridFileLine.get(3));
					DataFilePolygonValue dataFilePolygonValue = currGridFileData.getDataFilePolygonValue();
					dataFilePolygonValue.updateValue(gridFileLineValue, 1.0f);
				}
			}
		}
		return gridFileDataMap;
	}
	
	List<String> createGridPointsTextList(Map<String, GridFileData> gridFileDataMap) {
		List<String> gridPointsTextList = new ArrayList<String>();
		Iterator<Entry<String, GridFileData>> gridFileDataMapIterator = gridFileDataMap.entrySet().iterator();
		
	    while (gridFileDataMapIterator.hasNext()) {
	        Map.Entry<String, GridFileData> pair = (Entry<String, GridFileData>) gridFileDataMapIterator.next();
	        GridFileData gridFileData = pair.getValue();
	        gridPointsTextList.add(gridFileData.toString());
	    }
	    
		return gridPointsTextList;
	}
	
	private class GridFileData {
		int id;
		int eastings;
		int northings;
		DataFilePolygonValue dataFilePolygonValue;
		
		public GridFileData(Parameter parameter, String id, String eastings, String northings) {
			this.id = Integer.parseInt(id);
			this.eastings = Integer.parseInt(eastings);
			this.northings = Integer.parseInt(northings);
			dataFilePolygonValue = new DataFilePolygonValue(parameter.getBaseCalculateMethod());
		}

		public DataFilePolygonValue getDataFilePolygonValue() {
			return dataFilePolygonValue;
		}

		public String toString() {
			float dataFileValue = 0.0f;
			
			try {
				dataFileValue = dataFilePolygonValue.getValue();
			} catch (Exception ignore) {}

	        Object[] lineData = new Object[]{id, eastings, northings, dataFileValue};
	        String lineFormat = "%1$8d %2$6d %3$7d %4$6.1f";
	        String line = String.format(lineFormat, lineData);
	        return line;
		}
	}
	
}
