package com.graphhopper.jsprit.core.reporting.route;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public class HumanReadableEndTimePrinterColumn extends AbstractHumanReadableTimePrinterColumn {

    public HumanReadableEndTimePrinterColumn() {
        super();
    }

    public HumanReadableEndTimePrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    public HumanReadableEndTimePrinterColumn withDateFormat(String pattern) {
        return (HumanReadableEndTimePrinterColumn) super.withDateFormat(pattern);
    }

    @Override
    public HumanReadableEndTimePrinterColumn withOrigin(LocalDateTime origin) {
        return (HumanReadableEndTimePrinterColumn) super.withOrigin(origin);
    }

    @Override
    public HumanReadableEndTimePrinterColumn withUnit(ChronoUnit unit) {
        return (HumanReadableEndTimePrinterColumn) super.withUnit(unit);
    }

    @Override
    protected Long getTimeValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof End) {
            return null;
        } else {
            return (long) act.getEndTime();
        }
    }

    @Override
    protected String getTitle() {
        return "departure";
    }

}
