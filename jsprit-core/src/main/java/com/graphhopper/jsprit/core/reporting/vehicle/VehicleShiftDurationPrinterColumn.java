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
public class VehicleShiftDurationPrinterColumn extends AbstractVehicleDurationPrinterColumn<VehicleShiftDurationPrinterColumn> {


    /**
     * Constructor.
     */
    public VehicleShiftDurationPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public VehicleShiftDurationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getDefaultTitleBase() {
        return "shift";
    }

    @Override
    public Long getValue(VehicleSummaryContext context) {
        return context.getShiftDuration();
    }


}
