package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

public class OperationDurationPrinterColumn extends AbstractDurationPrinterColumn<OperationDurationPrinterColumn> {

    public OperationDurationPrinterColumn() {
        super();
    }

    public OperationDurationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getDefaultTitle() {
        return "opTime";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        return (long) act.getOperationTime();
    }

}
