package dk.dmi.lib.workflow.component.date;

import java.util.Date;
import java.util.GregorianCalendar;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Deprecated // New version available
@Component(
        name = "Create Date",
        category = "Date",
        description = "Creates a date, based on an excisting or new date. Eash of the specified fields will be set on the date, use null to ignore a field",
        version = 2)
public class CreateDate2 extends BaseComponent {

    @ExecuteMethod(
            argumentDescriptions = {"Initial date (Use null for current date)", "Year (Min value 0 or null to ignore)", "Month (Min value 1 or null to ignore)", "Day of month (Min value 1 or null to ignore)", "Hour of day (Min value 0 or null to ignore)", "Minute (Min value 0 or null to ignore)", "Second (Min value 0 or null to ignore)"},
            returnDescription = "Newly created date")
    public Date execute(Date date, Integer year, Integer month, Integer day, Integer hour, Integer minute, Integer second) {
        if (date == null) {
            date = new Date();
        }

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);

        if (year != null) {
            gregorianCalendar.set(GregorianCalendar.YEAR, year);
        }
        if (month != null) {
            gregorianCalendar.set(GregorianCalendar.MONTH, month - 1);
        }
        if (day != null) {
            gregorianCalendar.set(GregorianCalendar.DAY_OF_MONTH, day);
        }
        if (hour != null) {
            gregorianCalendar.set(GregorianCalendar.HOUR_OF_DAY, hour);
        }
        if (minute != null) {
            gregorianCalendar.set(GregorianCalendar.MINUTE, minute);
        }
        if (second != null) {
            gregorianCalendar.set(GregorianCalendar.SECOND, second);
        }

        gregorianCalendar.set(GregorianCalendar.MILLISECOND, 0);

        return gregorianCalendar.getTime();
    }

}
