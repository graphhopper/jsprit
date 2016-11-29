package com.graphhopper.jsprit.core.reporting.vehicle;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
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
public class VehicleShiftTimeWindowPrinterColumn
extends AbstractVehicleTimeWindowPrinterColumn<VehicleShiftTimeWindowPrinterColumn> {

    /**
     * Constructor.
     */
    public VehicleShiftTimeWindowPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public VehicleShiftTimeWindowPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    protected Collection<TimeWindow> getValue(VehicleStatisticsContext context) {
        return Collections.singleton(
                        new TimeWindow(context.getVehicle().getEarliestDeparture(), context.getVehicle().getLatestArrival()));
    }

    @Override
    protected String getDefaultTitleBase() {
        return "shift tw";
    }

}
