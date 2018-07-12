package dk.dmi.lib.workflow.component.polygon;

import java.util.ArrayList;
import java.util.List;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create diff grid file", 
		category = "Polygon",
		description = "Create a new grid file list from two grid file lists, substracting value 2 from value 1.",
        version = 1)
public class CreateDiffGridFile extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Grid file list 1", "Grid file list 2"},
			returnDescription = "Diff grid file list")
    public List<String> execute(List<String> gridFileList1, List<String> gridFileList2, String columnSeperator) throws Exception {
		List<String> diffGridFileList = new ArrayList<String>();
		
		try {
			for (int i = 0; i < gridFileList1.size(); i++) {
				String gridFileList1Line = gridFileList1.get(i);
				String gridFileList2Line = gridFileList2.get(i);
				
				String[] gridFileList1LineSplit = gridFileList1Line.split(columnSeperator);
				String[] gridFileList2LineSplit = gridFileList2Line.split(columnSeperator);
				
				double diffValue = Double.parseDouble(gridFileList1LineSplit[3].trim()) - Double.parseDouble(gridFileList2LineSplit[3].trim());
				
				diffGridFileList.add(gridFileList1LineSplit[0] + columnSeperator + gridFileList1LineSplit[1] + columnSeperator + gridFileList1LineSplit[2] + columnSeperator + diffValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return diffGridFileList;
	}
	
}
