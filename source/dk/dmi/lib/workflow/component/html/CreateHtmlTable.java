package dk.dmi.lib.workflow.component.html;

import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.ContextHelper;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Create Html Table", 
		category = "Html",
		description = "Generate html table from table data",
        version = 1)
public class CreateHtmlTable extends BaseComponent {
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_AREA_MEDIUM},
			argumentDescriptions = {"table data text defined by: \nrow1Col1, row1Col2, row1Col3;\nrow2Col1, row2Col2, row2Col3"}, 
			returnDescription = "Html table")
	public String execute(String tableData) {
		tableData = ContextHelper.evaluateAllContextKeysInText(workflowContextController.getWorkflowContextMap(), tableData);
		
		
		String[] rowDataList = tableData.split(";");
		
		String htmlTable = ""
				+ "<table border=\"1\">"
				+ "<tbody>";
		
		for (String rowData : rowDataList) {
			htmlTable += createTableRow(rowData);
		}
		
		htmlTable += ""
				+ "</tbody>"
				+ "</table>";
		
		return htmlTable;
	}
	
	String createTableRow(String rowData) {
		String htmlRow = "<tr>";
		String[] cellValues = rowData.split(",");
		
		for (String cellValue : cellValues) {
			htmlRow += createHtmlTableCell(cellValue);
		}
		
		htmlRow += "</tr>";
		
		return htmlRow;
	}
	
	String createHtmlTableCell(String value) {
		String htmlTableRow = ""
				+ "<td>"
				+ "<div>"
				+ value
				+ "</div>"
				+ "</td>";
		
		return htmlTableRow;
	}
	
}
