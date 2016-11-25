package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.AbstractActivity;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.StringColumnType;

public class ActivityTypePrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, String, ActivityTypePrinterColumn> {

    public ActivityTypePrinterColumn() {
        super();
    }

    public ActivityTypePrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType());
    }

    @Override
    public String getData(RoutePrinterContext context) {
        return ((AbstractActivity) context.getActivity()).getType();
    }

    @Override
    protected String getDefaultTitle() {
        return "activity";
    }

}
