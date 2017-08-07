package com.graphhopper.jsprit.core.reporting;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import hu.vissy.texttable.dataconverter.DataConverter;

/**
 * Duration formatter for human readable format.
 * <p>
 * The formatter uses the {@linkplain DateTimeFormatter} for time value to
 * string formatting. The default format is the standard ISO time format (
 * <code>"HH:mm:ss"</code>). If the input long value is X, the time value is
 * calculated by adding X of the units to a predefined origin. The default unit
 * is {@linkplain ChronoUnit#SECONDS}.
 * </p>
 *
 * @author balage
 *
 */
public class HumanReadableDurationFormatter implements DataConverter<Long> {

    private static class UnitInfo {
        private ChronoUnit unit;
        private int exchange;
        private String format;
        private String prefix;
        private String postfix;

        public UnitInfo(ChronoUnit unit, int exchange, String format, String prefix,
                String postfix) {
            super();
            this.unit = unit;
            this.exchange = exchange;
            this.format = format;
            this.prefix = prefix;
            this.postfix = postfix;
        }
        public ChronoUnit getUnit() {
            return unit;
        }
        public int getExchange() {
            return exchange;
        }
        public String getFormat() {
            return format;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getPostfix() {
            return postfix;
        }

    }

    private static final List<UnitInfo> UNIT_INFO;
    private static final Set<ChronoUnit> VALID_UNITS;


    static {
        UNIT_INFO = new ArrayList<>();
        UNIT_INFO.add(new UnitInfo(ChronoUnit.SECONDS, 60, "%02d", ":", ""));
        UNIT_INFO.add(new UnitInfo(ChronoUnit.MINUTES, 60, "%02d", ":", ""));
        UNIT_INFO.add(new UnitInfo(ChronoUnit.HOURS, 24, "%02d", " ", ""));
        UNIT_INFO.add(new UnitInfo(ChronoUnit.DAYS, Integer.MAX_VALUE, "%d", "", " d"));

        VALID_UNITS = UNIT_INFO.stream().map(ui -> ui.getUnit()).collect(Collectors.toSet());
    }

    // The time unit
    private ChronoUnit lowUnit = ChronoUnit.SECONDS;
    // The highest unit
    private ChronoUnit highUnit = ChronoUnit.DAYS;

    /**
     * Constructor with default settings. See
     * {@linkplain HumanReadableTimeFormatter} for default values.
     */
    public HumanReadableDurationFormatter() {
    }


    public HumanReadableDurationFormatter(ChronoUnit lowUnit, ChronoUnit highUnit) {
        if (!VALID_UNITS.contains(lowUnit))
            throw new IllegalArgumentException(
                    lowUnit + " is not allowed. Only: " + VALID_UNITS + " units allowed.");
        if (!VALID_UNITS.contains(highUnit))
            throw new IllegalArgumentException(
                    highUnit + " is not allowed. Only: " + VALID_UNITS + " units allowed.");
        if (indexOf(lowUnit) > indexOf(highUnit))
            throw new IllegalArgumentException(
                    lowUnit + " should be not higher than " + highUnit + ".");
        this.lowUnit = lowUnit;
        this.highUnit = highUnit;
    }

    private int indexOf(ChronoUnit unit) {
        for (int i = 0; i < UNIT_INFO.size(); i++)
            if (UNIT_INFO.get(i).getUnit().equals(unit))
                return i;
        throw new IllegalArgumentException("Unit " + unit + " is not valid");
    }

    @Override
    public String convert(Long data) {
        if (data == null)
            return "";
        else {
            long val = data;
            String res = "";
            int i = indexOf(lowUnit);
            int highIndex = indexOf(highUnit);
            while (i <= highIndex) {
                String s = "";
                UnitInfo unitInfo = UNIT_INFO.get(i);
                if (val >= unitInfo.getExchange() && i == highIndex) {
                    s = String.format("%d", val);
                } else {
                    s = String.format(unitInfo.getFormat(), val % unitInfo.exchange);
                }
                s = s + unitInfo.getPostfix();

                if (i != highIndex) {
                    s = unitInfo.getPrefix() + s;
                }
                res = s + res;
                val = val / unitInfo.exchange;
                i++;
            }

            return res;
        }
    }

}
