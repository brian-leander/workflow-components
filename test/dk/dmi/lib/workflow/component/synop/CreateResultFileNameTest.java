package dk.dmi.lib.workflow.component.synop;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dk.dmi.lib.workflow.component.synop.CreateResultFileName;

public class CreateResultFileNameTest {

	CreateResultFileName createResultFileName;
	Calendar calendar;
	
	@Before
	public void setup() {
		createResultFileName = new CreateResultFileName();		
		calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
	}
	
	@Test
	public void testExecute_checkFileName() {		
		Assert.assertEquals(timeFileName(calendar), createResultFileName.getFileName(calendar));
	}
	
	// 2017-09-04 14:48:23 1234567
	// 201709041448231234567	
	
	 private String timeFileName(Calendar gregor) {
        String YMD = new DecimalFormat("0000").format(gregor.get(Calendar.YEAR)) +
                     new DecimalFormat("00").format(gregor.get(Calendar.MONTH)+1) +
                     new DecimalFormat("00").format(gregor.get(Calendar.DAY_OF_MONTH));
        String HMS = new DecimalFormat("00").format(gregor.get(Calendar.HOUR_OF_DAY)) +
                     new DecimalFormat("00").format(gregor.get(Calendar.MINUTE)) +
                     new DecimalFormat("00").format(gregor.get(Calendar.SECOND));
        String sss = new DecimalFormat("0000000").format(gregor.get(Calendar.MILLISECOND));

        String Milli = new String( YMD + HMS + "." + sss );


        return( Milli );
    }	
}
