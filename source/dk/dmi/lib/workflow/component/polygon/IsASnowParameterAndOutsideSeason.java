package dk.dmi.lib.workflow.component.polygon;

import java.util.Date;
import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Is a snow parameter and outside season", 
		category = "Polygon",
		description = "Check if the given parameter is regarding snow and the given date is outside snow season, i.e. outside from 1 October to 31 of April.",
        version = 1)
public class IsASnowParameterAndOutsideSeason extends BaseComponent {
	
	public static final int SNOW_DEPTH_ELEMENT_NUMBER = 906; 
	public static final String SUMMER_SEASON_START_DATE = "2015-04-30 23:59";
	public static final String SUMMER_SEASON_STOP_DATE = "2015-10-01 00:00";
	
	@ExecuteMethod(
			argumentDescriptions = {"The parameter to check", "The observation date to check"}, 
			returnDescription = "True if snow parameter & outside snow season, else false")
	public boolean execute(Parameter parameter, Date obsDate) throws Exception {
		boolean isSnowParameter = isParameterRegardingSnow(parameter);
		
		if(isSnowParameter) {
			boolean isInSummerSeason = isObsDateInSummerSeason(obsDate);
			return isInSummerSeason;
		}
		
		return false;
	}
	
	boolean isParameterRegardingSnow(Parameter parameter) {
		boolean isSnowParameter = false;
		
		if(parameter.getElementNumber().intValue() == SNOW_DEPTH_ELEMENT_NUMBER) {
			isSnowParameter = true;
		}
		
		return isSnowParameter;
	}

	boolean isObsDateInSummerSeason(Date obsDate) throws Exception {
		int obsDateYear = DateUtils.getDateFieldValue(obsDate, DateUtils.DATE_FIELD_TEXT_YEAR);
		
		Date summerFromDate = DateUtils.parseStringToDate(SUMMER_SEASON_START_DATE, DateUtils.DATE_FORMAT_DEFAULT);
		summerFromDate = DateUtils.setDateFieldValue(summerFromDate, DateUtils.DATE_FIELD_TEXT_YEAR, obsDateYear);
		
		Date summerToDate = DateUtils.parseStringToDate(SUMMER_SEASON_STOP_DATE, DateUtils.DATE_FORMAT_DEFAULT);
		summerToDate = DateUtils.setDateFieldValue(summerToDate, DateUtils.DATE_FIELD_TEXT_YEAR, obsDateYear);
		
		boolean isInSummerSeason = obsDate.after(summerFromDate) && obsDate.before(summerToDate);
		return isInSummerSeason;
	}

}
