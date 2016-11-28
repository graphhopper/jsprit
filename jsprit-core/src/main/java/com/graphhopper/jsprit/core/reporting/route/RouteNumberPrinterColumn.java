package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.IntColumnType;

/**
 * The order number of the route.
 *
 * <p>
 * This is the ordinal of the route.
 * </p>
 *
 * @author balage
 */
public class RouteNumberPrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, Integer, RouteNumberPrinterColumn> {

    /**
     * Constructor.
     */
    public RouteNumberPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public RouteNumberPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
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
