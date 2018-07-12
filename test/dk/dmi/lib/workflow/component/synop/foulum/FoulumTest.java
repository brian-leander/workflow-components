package dk.dmi.lib.workflow.component.synop.foulum;

import dk.dmi.lib.synop.observation.CurrentTemperature;
import dk.dmi.lib.synop.observation.Observation;
import dk.dmi.lib.workflow.common.MockLogger;
import dk.dmi.lib.workflow.component.file.TextFileToListReader;
import dk.dmi.lib.workflow.component.synop.asiaq.ObservationWithType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class FoulumTest {

    private static final String line = "04-10-2017 11.53.36, 241,10.1,11.4,90,996.6,0.0,11.3,1,12.3,12.9,61.2,1";
    private static final String line2 = "04-10-2017 11.54.35, 251,11.2,11.4,90,996.6,0.0,11.3,1,12.3,12.9,47.0,1";

    @Test(expected = IllegalArgumentException.class)
    public void testFoulumParser_withNull() {
        ParseFoulumData parseFoulumData = new ParseFoulumData();
        parseFoulumData.injectLogger(new MockLogger());
        parseFoulumData.parseFoulumData(null);
    }

    @Test
    public void testFoulumParser_withEmptyCollection() {
        ParseFoulumData parseFoulumData = new ParseFoulumData();
        List<ObservationWithType> result = parseFoulumData.parseFoulumData(Collections.emptyList());

        Assert.assertEquals(12, result.size());
    }

    @Test
    public void testFoulumParser() {
        List<String> foulumData = new ArrayList<>();
        foulumData.add(line);

        ParseFoulumData parseFoulumData = new ParseFoulumData();
        List<ObservationWithType> result = parseFoulumData.parseFoulumData(foulumData);

        Assert.assertEquals(12, result.size());
    }

    @Test
    public void testFoulumParser_multipleLines() {
        List<String> foulumData = new ArrayList<>();
        foulumData.add(line);
        foulumData.add(line2);

        ParseFoulumData parseFoulumData = new ParseFoulumData();
        List<ObservationWithType> result = parseFoulumData.parseFoulumData(foulumData);

        Assert.assertEquals(24, result.size());

        Assert.assertEquals("47.0", result.get(22).getValue());
    }

    @Test
    public void testFoulumParser_fullFile() throws IOException {
        TextFileToListReader reader = new TextFileToListReader();
        List<String> records = reader.execute("c:/Users/br/tmp/foulum/foulum-test.log", "none", 0);

        Assert.assertEquals(1440, records.size());

        ParseFoulumData parseFoulumData = new ParseFoulumData();
        List<ObservationWithType> result = parseFoulumData.parseFoulumData(records);

        Assert.assertEquals(1440 * 12, result.size());
        Assert.assertEquals("241", result.get(0).getValue() );
    }

    @Test
    public void testFoulumParser_missingValue() {
        String x = "04-10-2017 11.53.36, 241,,11.4,90,996.6,0.0,11.3,1,12.3,12.9,61.2,1";
        String[] l = x.split(",");
        Assert.assertEquals("", l[2].trim());
        Assert.assertEquals(0, l[2].trim().length());
    }

    @Test
    public void testMapStream() {
        Map<Integer, Observation> observations = new HashMap<>();
        observations.put(112, new CurrentTemperature("1", 1507203024, "10.3"));
        observations.put(113, new CurrentTemperature("1", 1507204024, "9.3"));
        observations.put(114, new CurrentTemperature("1", 1507201024, "11.1"));

        final Comparator<Observation> comp = Comparator.comparingLong(Observation::getTimestamp);
        long oldest = observations.entrySet().stream().filter(map -> map.getKey() > 0).map(Map.Entry::getValue).max(comp).get().getTimestamp();
        long youngest = observations.entrySet().stream().filter(map -> map.getKey() > 0).map(map -> map.getValue()).min(comp).get().getTimestamp();

        Assert.assertEquals(1507204024, oldest);
        Assert.assertEquals(1507201024, youngest);
    }
}
