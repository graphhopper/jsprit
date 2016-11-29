package com.graphhopper.jsprit.core.reporting.columndefinition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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
public class HumanReadableDurationFormatter extends HumanReadableTimeFormatter {

    // Default origin
    public static final LocalDateTime DEFAULT_ORIGIN = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);

    /**
     * Constructor with default settings. See
     * {@linkplain HumanReadableDurationFormatter} for default values.
     */
    public HumanReadableDurationFormatter() {
    }

    /**
     * Constructor with time mapping values, but with default formatting.
     *
     * @param unit
     *            The unit used to map the numerical value to the time value.
     */
    public HumanReadableDurationFormatter(ChronoUnit unit) {
        super(DEFAULT_ORIGIN, unit);
    }

    /**
     * Constructor with user-defined formatting.
     *
     * @param dateFormatter
     *            The date formatter.
     */
    public HumanReadableDurationFormatter(DateTimeFormatter dateFormatter) {
        super(dateFormatter);
    }

    /**
     * Constructor with both time mapping values and user-defined formatting.
     *
     * @param dateFormatter
     *            The date formatter.
     * @param unit
     *            The unit used to map the numerical value to the time value.
     */
    public HumanReadableDurationFormatter(DateTimeFormatter dateFormatter, ChronoUnit unit) {
        super(dateFormatter, DEFAULT_ORIGIN, unit);
    }

}
