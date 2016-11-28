package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.StringColumnType;

/**
 * The name of the vehicle associated by this route.
 *
 * <p>
 * This colum returns the id of the vehicle of the route.
 * </p>
 *
 * @author balage
 */
public class VehicleNamePrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, String, VehicleNamePrinterColumn> {

    /**
     * Constructor.
     */
    public VehicleNamePrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public VehicleNamePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType());
    }

    @Override
    protected String getDefaultTitle() {
        return "vehicle";
    }

    @Override
    public String getData(RoutePrinterContext context) {
        return context.getRoute().getVehicle().getId();
    }

}
