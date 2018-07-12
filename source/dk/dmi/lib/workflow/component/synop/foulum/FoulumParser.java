package dk.dmi.lib.workflow.component.synop.foulum;

import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.workflow.component.synop.asiaq.ObservationWithType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FoulumParser {
    private static final String DATE_FORMAT = "dd-MM-yyyy HH.mm.ss"; // 04-10-2017 12.27.35
    private static final String FOULUM_STATION_NUMBER = "1";
    private static String timeZone = "UTC";

    public static List<ObservationWithType> getObservationWithTypes(List<String> foulumObservations) throws NumberFormatException, MissingFormatArgumentException {
        final List<ObservationWithType> observations = new ArrayList<>();

        foulumObservations.stream()
                .map(String::trim)
                .map(r -> r.split(","))
                .filter(rowData -> rowData.length == 13)
                .forEach(observation ->
                        observations.addAll(Arrays.stream(ObservationType.values())
                                .map(observationType -> new ObservationWithType(
                                        FOULUM_STATION_NUMBER,
                                        observationType.getLabel(),
                                        1,
                                        DateUtils.getUtcDateTime(observation[0], timeZone, DATE_FORMAT).toEpochSecond(),
                                        observation[observationType.getKey()].trim(),
                                        observationType.getUnit()))
                                .filter(observationWithType -> observationWithType.getValue().length() > 0)
                                .collect(Collectors.toList()) )
                );

        return observations;
    }

    public enum ObservationType {
        WIND_DIRECTION(1, "Vindretning", "°"),
        WIND_SPEED(2, "Vindhastighed", "knob"),
        TEMPERATURE(3, "Temperatur", "°C"),
        RELATIVE_HUMIDITY(4, "Relativ luftfugtighed", "%"),
        AIR_PRESSURE(5, "Lufttryk", "hPa"),
        ACCUMULATED_PRECIPITATION(6, "Akkumuleret nedbør", "mm"),
        GRASS_TEMPERATURE(7, "Græstemperatur", "°C"),
        LEAVE_HUMIDITY(8, "Bladfugt", "minutter"),
        EARTH_TEMPERATURE_10(9, "Jordtemperatur 10 cm", "°C"),
        EARTH_TEMPERATURE_30(10, "Jordtemperatur 30 cm", "°C"),
        RADIATION(11, "Globalstråling", "W/m2");
//        PRECIPITATION(12, "Nedbør", "Ja/nej");

        private static final Map<String, ObservationType> LABEL_MAP = Collections.unmodifiableMap(
                Stream.of(ObservationType.values())
                        .collect(Collectors.toMap(ObservationType::getLabel, o -> o)));

        public static final Set<ObservationType> USE_CURRENT_VALUES;
        static {
            final Set<ObservationType> temp = new HashSet<>();
            temp.add(TEMPERATURE);
            temp.add(RELATIVE_HUMIDITY);
            temp.add(AIR_PRESSURE);
            temp.add(GRASS_TEMPERATURE);
            temp.add(EARTH_TEMPERATURE_10);
            temp.add(EARTH_TEMPERATURE_30);

            USE_CURRENT_VALUES = Collections.unmodifiableSet(temp);
        }

        public static final Set<ObservationType> USE_ACCUMULATED_SUM_VALUES;
        static {
            final Set<ObservationType> temp = new HashSet<>();
            temp.add(ACCUMULATED_PRECIPITATION);
            temp.add(LEAVE_HUMIDITY);

            USE_ACCUMULATED_SUM_VALUES = Collections.unmodifiableSet(temp);
        }

        public static final Set<ObservationType> USE_ACCUMULATED_AVERAGE_VALUES;
        static {
            final Set<ObservationType> temp = new HashSet<>();
            temp.add(WIND_DIRECTION);
            temp.add(WIND_SPEED);
            temp.add(RADIATION);

            USE_ACCUMULATED_AVERAGE_VALUES = Collections.unmodifiableSet(temp);
        }

        private final int key;
        private final String label;
        private final String unit;

        ObservationType(int key, String label, String unit) {
            this.key = key;
            this.label = label;
            this.unit = unit;
        }

        public int getKey() {
            return key;
        }

        public String getLabel() {
            return label;
        }

        public String getUnit() {
            return unit;
        }

        public static ObservationType getByLabel(String label) {
            return LABEL_MAP.get(label);
        }
    }
}
