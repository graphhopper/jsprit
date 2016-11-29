package com.graphhopper.jsprit.core.reporting.vehicle;

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
public class VehicleRouteNumberPrinterColumn extends AbstractPrinterColumn<VehicleStatisticsContext, Integer, VehicleRouteNumberPrinterColumn> {

    /**
     * Constructor.
     */
    public VehicleRouteNumberPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public VehicleRouteNumberPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new IntColumnType());
    }

    @Override
    protected String getDefaultTitle() {
        return "route nr";
    }

    @Override
    public Integer getData(VehicleStatisticsContext context) {
        return context.getRouteNr();
    }

}
