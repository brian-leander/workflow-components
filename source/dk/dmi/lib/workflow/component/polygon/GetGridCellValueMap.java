package dk.dmi.lib.workflow.component.polygon;

import java.io.IOException;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import dk.dmi.lib.common.FileUtils;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get Grid Cell Value Map", 
		category = "Polygon",
		description = "Builds a grid file TreeMap of TreeMaps with double values. First key is northings and value is another map. Second key is eastings and value is a double value of the grid cell.",
        version = 1)
public class GetGridCellValueMap extends BaseComponent {
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getListOfEntityNames(String ignore) throws IOException {
		String[] compressionList = FileUtils.getListOfFileCompressionFormats();
		return compressionList;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Grid file location", "File encoding if compressed"},
			returnDescription = "Map of Maps with grid cell values")
    public NavigableMap<Double, NavigableMap<Double, Double>> execute(String gridFileUri, String compression) throws Exception {
		NavigableMap<Double, NavigableMap<Double, Double>> gridFileMapMap = createMapMapValueFromGridFile(gridFileUri, compression);
		return gridFileMapMap;
    }

	NavigableMap<Double, NavigableMap<Double, Double>> createMapMapValueFromGridFile(String gridFileUri, String compression) throws IOException {
		NavigableMap<Double, NavigableMap<Double, Double>> gridFileMapMap = new TreeMap<Double, NavigableMap<Double, Double>>();
		List<List<String>> gridFileListList = FileUtils.textFileToListListReader(gridFileUri, " ", compression, 0);
		
		for (List<String> gridFileLineList : gridFileListList) {
			double eastings = Double.parseDouble(gridFileLineList.get(1));
			double northings = Double.parseDouble(gridFileLineList.get(2));
			double value = Double.parseDouble(gridFileLineList.get(3));
			
			NavigableMap<Double, Double> northingsMap = getOrCreateNorthingsMap(gridFileMapMap, northings);
			northingsMap.put(eastings, value);
		}
		
		return gridFileMapMap;
	}

	NavigableMap<Double, Double> getOrCreateNorthingsMap(NavigableMap<Double, NavigableMap<Double, Double>> gridFileMapMap, double northings) {
		NavigableMap<Double, Double> northingsMap = gridFileMapMap.get(northings);
		
		if(northingsMap == null) {
			northingsMap = new TreeMap<Double, Double>();
			gridFileMapMap.put(northings, northingsMap);
		}
		
		return northingsMap;
	}

}
