package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public class WaitingDurationPrinterColumn extends AbstractDurationPrinterColumn<WaitingDurationPrinterColumn> {

    public WaitingDurationPrinterColumn() {
        super();
    }

    public WaitingDurationPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getTitle() {
        return "waiting";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start || act instanceof End) {
            return null;
        } else {
            return (long) (act.getEndTime() - act.getOperationTime() - act.getArrTime());
        }
    }


}
