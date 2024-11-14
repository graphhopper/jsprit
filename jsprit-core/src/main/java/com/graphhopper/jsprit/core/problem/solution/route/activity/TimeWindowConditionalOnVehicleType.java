package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;

/**
 * A time window that is only applicable for a specific type of vehicle.
 * When using this, beware of vehicle switching.
 */
public class TimeWindowConditionalOnVehicleType extends TimeWindow {
    private final String vehicleTypeId;

     /**
     * Returns new instance of TimeWindowConditionalOnVehicleType.
     *
     * @param start
     * @param end
     * @param vehicleTypeId
     * @return TimeWindow
     * @throw IllegalArgumentException either if start or end < 0.0 or end < start
     */
    public static TimeWindow newInstance(double start, double end, String vehicleTypeId) {
        return new TimeWindowConditionalOnVehicleType(start, end, vehicleTypeId);
    }

    public TimeWindowConditionalOnVehicleType(double start, double end, String vehicleTypeId) {
        super(start, end);
        this.vehicleTypeId = vehicleTypeId;
    }

    @Override
    public boolean isApplicable(JobInsertionContext insertionContext) {
        return insertionContext.getNewVehicle().getType().getTypeId().equals(vehicleTypeId);
    }
}
