package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public class ArrivalTimePrinterColumn extends AbstractTimePrinterColumn<ArrivalTimePrinterColumn> {

    public ArrivalTimePrinterColumn() {
        super();
    }

    public ArrivalTimePrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getTitle() {
        return "arrTime";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start) {
            return null;
        } else {
            return (long) act.getArrTime();
        }
    }


}
