package dk.dmi.lib.workflow.component.polygon;

import java.util.Map.Entry;
import java.util.NavigableMap;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Get Grid File Value For Coordinates", 
		category = "Polygon",
		description = "Extract grid file cell value from a navigable grid file map, based on eastings and northings.",
        version = 1)
public class GetGridCellForCoordinates extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Grid file map of maps. Must be a NavigableMap", "Eastings coodinates", "Northings coodinates"},
			returnDescription = "The grid file cell value closest to the given coodinates. If the coodinates are outside the 1x1 km grid cell map, null is returned")
    public Double execute(NavigableMap<Double, NavigableMap<Double, Double>> gridFileMapMap, Double eastings, Double northings) throws Exception {
		NavigableMap<Double, Double> northingsResult = getClosetNorthingsValue(gridFileMapMap, northings);
		
		if(northingsResult != null) {
			Double eastingsResult = getClosetEastingsValue(northingsResult, eastings);
			return eastingsResult.doubleValue();
		} else {
			return null;
		}
    }

	NavigableMap<Double, Double> getClosetNorthingsValue(NavigableMap<Double, NavigableMap<Double, Double>> gridFileMapMap, Double northings) {
		Entry<Double, NavigableMap<Double, Double>> resultEntry = getClosestNorthingsEntry(gridFileMapMap, northings);
		NavigableMap<Double, Double> result = getNorthingsEntryValue(resultEntry, northings);
		return result;
	}

	Entry<Double, NavigableMap<Double, Double>> getClosestNorthingsEntry(NavigableMap<Double, NavigableMap<Double, Double>> gridFileMapMap, Double northings) {
		Entry<Double, NavigableMap<Double, Double>> resultEntry = null;
		
		if(gridFileMapMap != null) {
			Entry<Double, NavigableMap<Double, Double>> low = gridFileMapMap.floorEntry(northings);
			Entry<Double, NavigableMap<Double, Double>> high = gridFileMapMap.ceilingEntry(northings);
			
			if (low != null && high != null) {
				resultEntry = Math.abs(northings-low.getKey()) < Math.abs(northings-high.getKey()) ? low : high;
			} else if (low != null || high != null) {
				resultEntry = low != null ? low : high;
			}
		}
			
		return resultEntry;
	}
	
	NavigableMap<Double, Double> getNorthingsEntryValue(Entry<Double, NavigableMap<Double, Double>> resultEntry, Double northings) {
		NavigableMap<Double, Double> result = null;
			
		if(resultEntry != null) {
			double maxNothings = resultEntry.getKey()+500.0;
			double minNothings = resultEntry.getKey()-500.0;
			
			if(northings >= minNothings && northings < maxNothings) {
				result = resultEntry.getValue();
			}
		}
			
		return result;
	}
	
	Double getClosetEastingsValue(NavigableMap<Double, Double> gridFileMap, Double eastings) {
		Entry<Double, Double> resultEntry = getClosestEastingsEntry(gridFileMap, eastings);
		Double result = getEastingsEntryValue(resultEntry, eastings);
		return result;
	}

	Entry<Double, Double> getClosestEastingsEntry(NavigableMap<Double, Double> gridFileMap, Double eastings) {
		Entry<Double, Double> resultEntry = null;
		
		if(gridFileMap != null) {
			Entry<Double, Double> low = gridFileMap.floorEntry(eastings);
			Entry<Double, Double> high = gridFileMap.ceilingEntry(eastings);
			
			if (low != null && high != null) {
				resultEntry = Math.abs(eastings-low.getKey()) < Math.abs(eastings-high.getKey()) ? low : high;
			} else if (low != null || high != null) {
				resultEntry = low != null ? low : high;
			}
		}
		
		return resultEntry;
	}
	
	Double getEastingsEntryValue(Entry<Double, Double> resultEntry, Double eastings) {
		Double result = null;
			
		if(resultEntry != null) {
			double maxEastings = resultEntry.getKey()+500.0;
			double minEastings = resultEntry.getKey()-500.0;
			
			if(eastings >= minEastings && eastings < maxEastings) {
				result = resultEntry.getValue();
			}
		}
			
		return result;
	}
	
}
