package com.graphhopper.jsprit.core.reporting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Time value or duration formatter for human readable format.
 * <p>
 * The formatter uses the {@linkplain DateTimeFormatter} for time value to
 * string formatting. The default format is the standard ISO time format (
 * <code>"HH:mm:ss"</code>). If the input long value is X, the time value is
 * calculated by adding X of the units to the origin. The default value for
 * origin is midnight (00:00) of the current day (note, that the default
 * formatting ignores the date value), the default unit is
 * {@linkplain ChronoUnit#SECONDS}.
 * </p>
 *
 * @author balage
 *
 */
public class HumanReadableTimeFormatter {

    // Default origin
    public static final LocalDateTime DEFAULT_ORIGIN = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);

    // The formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    // The origin
    private LocalDateTime origin = DEFAULT_ORIGIN;
    // The time unit
    private ChronoUnit unit = ChronoUnit.SECONDS;

    /**
     * Constructor with default settings. See
     * {@linkplain HumanReadableTimeFormatter} for default values.
     */
    public HumanReadableTimeFormatter() {
    }

    /**
     * Constructor with time mapping values, but with default formatting.
     *
     * @param origin
     *            The origin data and time of the time mapping. (Note that with
     *            default formatter, the date part is ignored.)
     * @param unit
     *            The unit used to map the numerical value to the time value.
     */
    public HumanReadableTimeFormatter(LocalDateTime origin, ChronoUnit unit) {
        this.origin = origin;
        this.unit = unit;
    }

    /**
     * Constructor with user-defined formatting.
     *
     * @param dateFormatter
     *            The date formatter.
     */
    public HumanReadableTimeFormatter(DateTimeFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    /**
     * Constructor with both time mapping values and user-defined formatting.
     *
     * @param dateFormatter
     *            The date formatter.
     * @param origin
     *            The origin data and time of the time mapping.
     * @param unit
     *            The unit used to map the numerical value to the time value.
     */
    public HumanReadableTimeFormatter(DateTimeFormatter dateFormatter, LocalDateTime origin, ChronoUnit unit) {
        this.dateFormatter = dateFormatter;
        this.origin = origin;
        this.unit = unit;
    }

    /**
     * Formats a numerical value into a human readable time value.
     * <p>
     * First a time value is calculated by adding <code>timeValue</code> of the
     * units to the origin. Then the time value is formatted by the formatter.
     * </p>
     *
     * @param timeValue
     *            The value to convert.
     * @return The converted value.
     */
    public String format(Long timeValue) {
        if (timeValue == null) {
            return null;
        } else {
            LocalDateTime dt = origin.plus(timeValue, unit);
            return dateFormatter.format(dt);
        }
    }
}
