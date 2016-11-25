package com.graphhopper.jsprit.core.reporting.route;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class HumanReadableTimeFormatter {

    public static final LocalDateTime DEFAULT_ORIGIN = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private LocalDateTime origin = DEFAULT_ORIGIN;
    private ChronoUnit unit = ChronoUnit.SECONDS;

    public HumanReadableTimeFormatter() {
    }

    public HumanReadableTimeFormatter(LocalDateTime origin, ChronoUnit unit) {
        this.origin = origin;
        this.unit = unit;
    }

    public HumanReadableTimeFormatter(DateTimeFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public HumanReadableTimeFormatter(DateTimeFormatter dateFormatter, LocalDateTime origin, ChronoUnit unit) {
        this.dateFormatter = dateFormatter;
        this.origin = origin;
        this.unit = unit;
    }

    public String format(Long timeValue) {
        if (timeValue == null) {
            return null;
        } else {
            LocalDateTime dt = origin.plus(timeValue, unit);
            return dateFormatter.format(dt);
        }
    }
}
