package com.graphhopper.jsprit.core.reporting.vehicle;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * Travel duration toward the location of the activity.
 * <p>
 * The time it takes to travel to the location of the activity. The value is
 * undefined for route start activity (null).
 * </p>
 *
 * @author balage
 *
 */
public class VehicleRouteDurationPrinterColumn extends AbstractVehicleDurationPrinterColumn<VehicleRouteDurationPrinterColumn> {


    /**
     * Constructor.
     */
    public VehicleRouteDurationPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public VehicleRouteDurationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getDefaultTitleBase() {
        return "route";
    }

    @Override
    public Long getValue(VehicleStatisticsContext context) {
        return context.getRouteDuration();
    }


}
