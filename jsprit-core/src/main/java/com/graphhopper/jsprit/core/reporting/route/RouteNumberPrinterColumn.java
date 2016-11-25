package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.IntColumnType;

public class RouteNumberPrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, Integer, RouteNumberPrinterColumn> {

    public RouteNumberPrinterColumn() {
        super();
    }

    public RouteNumberPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new IntColumnType());
    }

    @Override
    protected String getDefaultTitle() {
        return "route";
    }

    @Override
    public Integer getData(RoutePrinterContext context) {
        return context.getRouteNr();
    }

}
