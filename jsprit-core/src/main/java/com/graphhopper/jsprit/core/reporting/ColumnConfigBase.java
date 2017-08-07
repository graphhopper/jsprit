package com.graphhopper.jsprit.core.reporting;

import java.util.Collection;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

public abstract class ColumnConfigBase {

    private HumanReadableTimeFormatter timeFormatter;
    private HumanReadableDurationFormatter durationFormatter;

    public HumanReadableTimeFormatter getTimeFormatter() {
        return timeFormatter;
    }

    protected void setTimeFormatter(HumanReadableTimeFormatter timeFormatter) {
        this.timeFormatter = timeFormatter;
    }

    public HumanReadableDurationFormatter getDurationFormatter() {
        return durationFormatter;
    }

    protected void setDurationFormatter(HumanReadableDurationFormatter durationFormatter) {
        this.durationFormatter = durationFormatter;
    }

    protected String formatTimeWindowsNumeric(Collection<TimeWindow> timeWindows) {
        if (timeWindows == null || timeWindows.isEmpty())
            return "";
        return timeWindows.stream().map(tw -> formatTimeWindowNumeric(tw))
                .collect(Collectors.joining());
    }

    protected String formatTimeWindowsHuman(Collection<TimeWindow> timeWindows) {
        if (timeWindows == null || timeWindows.isEmpty())
            return "";
        return timeWindows.stream().map(tw -> formatTimeWindowHuman(tw))
                .collect(Collectors.joining());
    }

    protected String formatTimeWindowNumeric(TimeWindow tw) {
        String res = "";
        if (tw != null) {
            res = "[" + (long) tw.getStart() + "-";
            if (tw.getEnd() == Double.MAX_VALUE) {
                res += "";
            } else {
                res += (long) tw.getEnd();
            }
            res += "]";
        }
        return res;
    }

    protected String formatTimeWindowHuman(TimeWindow tw) {
        String res = "";
        if (tw != null) {
            res = "[" + timeFormatter.convert((long) tw.getStart()) + "-";
            if (tw.getEnd() == Double.MAX_VALUE) {
                res += "";
            } else {
                res += timeFormatter.convert((long) tw.getEnd());
            }
            res += "]";
        }
        return res;
    }

    protected String formatDurationHuman(Long data) {
        return durationFormatter.convert(data);
    }

    protected String formatTimeHuman(Long data) {
        return timeFormatter.convert(data);
    }

}
