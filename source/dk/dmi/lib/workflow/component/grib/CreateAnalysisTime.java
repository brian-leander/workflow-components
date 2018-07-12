package dk.dmi.lib.workflow.component.grib;

import java.util.Date;
import dk.dmi.lib.util.DateTime;
import dk.dmi.lib.util.Period;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create analysis time", 
		category = "Grib",
		description = "Calculate the analysis time of a model, based on the models number of daily runs (first run at 00:00), and the amount of run back from the the given date.",
        version = 1)
public class CreateAnalysisTime extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Number of models daily runs", "Number of models runs back (0 is the latest)", "Starting date to use for calculation (Use null for current date)"},
			returnDescription = "Analysis time of the model run")
    public Date execute(int modelDailyRuns, int latestModelRunNumber, Date basedOnDate) {
		if(basedOnDate == null) {
			basedOnDate = new Date();
		}
		
		DateTime basedOnDateTime = new DateTime(basedOnDate);
		int modelRunPeriodInHours = 24/modelDailyRuns;
		Period modelRunPeriod = new Period(modelRunPeriodInHours,0, 0, 0);
		DateTime analyseDateTime = basedOnDateTime.floorUsingPeriod(modelRunPeriod);
		
		for (int i = 0; i < latestModelRunNumber; i++) {
			analyseDateTime = analyseDateTime.subtract(modelRunPeriod);
		}
		
		Date analyseDate = analyseDateTime.getTime();
		return analyseDate;
	}
	
}
