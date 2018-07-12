package dk.dmi.lib.workflow.component.convert;

import java.awt.geom.Point2D;
import java.util.Map;
import dk.dmi.lib.common.GeoTransform;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Transform coordinate point", 
		category = "Convert",
		description = "Transforms a coordinate points2D.Double from one format to another.",
        version = 1)
public class TransformCoordinatePoint extends BaseComponent {
	
	@ArgumentListGetMethod(
			argumentIndex = "1")
	public String[] getSourceCRMList(String ignore) {
		return getCrmFormatMapKeyList();
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "2")
	public String[] getTargetCRMList(String ignore)  {
		return getCrmFormatMapKeyList();
	}
	
	String[] getCrmFormatMapKeyList() {
		Map<String, String> crmFormatMap = GeoTransform.getCrmFormatMap();
		String[] crmFormatKeys = new String[crmFormatMap.size()];
		int index = 0;
		
		for (Map.Entry<String, String> mapEntry : crmFormatMap.entrySet()) {
		    crmFormatKeys[index] = mapEntry.getKey();
		    index++;
		}
		
		return crmFormatKeys;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Coordinate points to transform", "Source coordinate reference system", "Target coordinate reference system"}, 
			returnDescription = "Transformed coordinate points")
    public Point2D execute(Point2D inputPoint, String sourceFormat, String targetFormat) throws Exception {
		Map<String, String> crmFormatMap = GeoTransform.getCrmFormatMap();
		
		String sourceFormatCrm = crmFormatMap.get(sourceFormat);
		String targetFormatCrm = crmFormatMap.get(targetFormat);
		Point2D resultPoint = GeoTransform.genericTransform(inputPoint, sourceFormatCrm, targetFormatCrm);
		
        return resultPoint;
    }
	
}
