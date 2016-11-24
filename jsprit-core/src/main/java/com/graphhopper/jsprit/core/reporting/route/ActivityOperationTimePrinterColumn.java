package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.IntColumnType;

public class ActivityOperationTimePrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, Integer> {

    public ActivityOperationTimePrinterColumn() {
        super();
    }

    public ActivityOperationTimePrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new IntColumnType("-"), "opTime");
    }

    @Override
    public Integer getData(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        return (int) act.getOperationTime();
    }

}
