package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

public class StartTimePrinterColumn extends AbstractTimePrinterColumn<StartTimePrinterColumn> {

    public StartTimePrinterColumn() {
        super();
    }

    public StartTimePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    protected String getDefaultTitle() {
        return "startTime";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof End) {
            return null;
        } else {
            return (long) (act.getEndTime() - act.getOperationTime());
        }
    }


}
