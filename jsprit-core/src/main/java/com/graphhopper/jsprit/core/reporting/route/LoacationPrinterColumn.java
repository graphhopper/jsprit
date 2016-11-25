package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.StringColumnType;

public class LoacationPrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, String> {

    public LoacationPrinterColumn() {
        super();
    }

    public LoacationPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType("-"), "location");
    }

    @Override
    public String getData(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        Location loc = act.getLocation();
        return loc == null ? null : loc.getId();
    }

}
