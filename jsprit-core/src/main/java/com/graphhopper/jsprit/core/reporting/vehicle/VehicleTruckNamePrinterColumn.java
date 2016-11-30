package com.graphhopper.jsprit.core.reporting.vehicle;

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
public class VehicleTruckNamePrinterColumn extends AbstractPrinterColumn<VehicleSummaryContext, String, VehicleTruckNamePrinterColumn> {

    /**
     * Constructor.
     */
    public VehicleTruckNamePrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public VehicleTruckNamePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
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
    public String getData(VehicleSummaryContext context) {
        return context.getVehicle().getId();
    }

}
