package com.graphhopper.jsprit.core.reporting.route;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class HumanReadableDurationFormatter extends HumanReadableTimeFormatter {

    public static final LocalDateTime DEFAULT_DUARATION_ORIGIN = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);

    public HumanReadableDurationFormatter() {
    }

    public HumanReadableDurationFormatter(ChronoUnit unit) {
        super(DEFAULT_DUARATION_ORIGIN, unit);
    }

    public HumanReadableDurationFormatter(DateTimeFormatter dateFormatter) {
        super(dateFormatter);
    }

    public HumanReadableDurationFormatter(DateTimeFormatter dateFormatter, ChronoUnit unit) {
        super(dateFormatter, DEFAULT_DUARATION_ORIGIN, unit);
    }

}
