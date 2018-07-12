package dk.dmi.lib.workflow.component.grib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import dk.dmi.lib.grib.GaussException;
import dk.dmi.lib.grib.GribField;
//import dk.dmi.lib.grib.util.GribFieldExtended;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
		name = "Get Multi Section GRIB field From Path", 
		category = "ECMWF",
		description = "Fetches GRIB field with multiple sections from Grib file in assigned path",
        version = 1)
public class GetMultiSectionGribFieldFromPath {
	
	WorkflowContextController workflowContextController;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
			argumentDescriptions = {"Path including Grib file name"},
			returnDescription = "Grib Field array")
	

	
	public GribField[] execute(String pathToFile) throws IOException, GaussException {
		GribField[] gribField = getMultiSectionGribField(pathToFile);
		return gribField;
	}
	
    List<byte[]> rawGribRecords = null;
	
	public GribField[] getMultiSectionGribField(String pathToFile) throws IOException, GaussException {		
		
		GribField[] gribFields = new GribField[3];
//		System.out.println("Processing file: " + pathToFile);
//
//		GribFieldExtended gribFieldExtended = new GribFieldExtended();
//		gribFieldExtended.getGribField(pathToFile);    // <-- Calling grib utils
//
//		rawGribRecords = gribFieldExtended.getRawGribRecords();
//
//		 int i = 0;
//		for (byte[] rawGribRecord : rawGribRecords)	{
//			gribFields[i] = new GribField(rawGribRecord);
//			i++;
//		}
		return gribFields;	
	}	

}
