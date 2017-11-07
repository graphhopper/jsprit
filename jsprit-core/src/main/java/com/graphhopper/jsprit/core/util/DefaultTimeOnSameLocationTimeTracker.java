package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.cost.ForwardTransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class DefaultTimeOnSameLocationTimeTracker extends ActivityTimeTracker {
    private final double activityDurationOnSameLocation;

    public DefaultTimeOnSameLocationTimeTracker(ForwardTransportTime transportTime,
                                                ActivityPolicy activityPolicy,
                                                VehicleRoutingActivityCosts activityCosts,
                                                double activityDurationOnSameLocation) {
        super(transportTime, activityPolicy, activityCosts);
        this.activityDurationOnSameLocation = activityDurationOnSameLocation;

    }
    public DefaultTimeOnSameLocationTimeTracker(ForwardTransportTime transportTime,
                                                VehicleRoutingActivityCosts activityCosts,
                                                double activityDurationOnSameLocation) {
        super(transportTime, activityCosts);
        this.activityDurationOnSameLocation = activityDurationOnSameLocation;

    }

    @Override
    public void visit(TourActivity activity) {
        super.visit(activity);
        if(activity.getLocation().getCoordinate().equals(prevAct.getLocation().getCoordinate())) {
            double activityTime = activityDurationOnSameLocation - activityCosts.getActivityDuration(activity,actArrTime,route.getDriver(),route.getVehicle());
            actEndTime += activityTime;
            startAtPrevAct += activityTime;
        }
    }
}
