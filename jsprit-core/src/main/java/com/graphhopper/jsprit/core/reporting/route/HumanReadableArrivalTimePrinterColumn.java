package com.graphhopper.jsprit.core.reporting.route;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public class HumanReadableArrivalTimePrinterColumn extends AbstractHumanReadableTimePrinterColumn {

    public HumanReadableArrivalTimePrinterColumn() {
        super();
    }

    public HumanReadableArrivalTimePrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    public HumanReadableArrivalTimePrinterColumn withDateFormat(String pattern) {
        return (HumanReadableArrivalTimePrinterColumn) super.withDateFormat(pattern);
    }

    @Override
    public HumanReadableArrivalTimePrinterColumn withOrigin(LocalDateTime origin) {
        return (HumanReadableArrivalTimePrinterColumn) super.withOrigin(origin);
    }

    @Override
    public HumanReadableArrivalTimePrinterColumn withUnit(ChronoUnit unit) {
        return (HumanReadableArrivalTimePrinterColumn) super.withUnit(unit);
    }

    @Override
    protected Long getTimeValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start) {
            return null;
        } else {
            return (long) act.getArrTime();
        }
    }

    @Override
    protected String getTitle() {
        return "arrival";
    }

}
