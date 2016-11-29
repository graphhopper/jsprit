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
public class VehicleTypePrinterColumn extends AbstractPrinterColumn<VehicleStatisticsContext, String, VehicleTypePrinterColumn> {

    /**
     * Constructor.
     */
    public VehicleTypePrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public VehicleTypePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType());
    }

    @Override
    protected String getDefaultTitle() {
        return "vehicle type";
    }

    @Override
    public String getData(VehicleStatisticsContext context) {
        return context.getVehicle().getType().getTypeId();
    }

}
