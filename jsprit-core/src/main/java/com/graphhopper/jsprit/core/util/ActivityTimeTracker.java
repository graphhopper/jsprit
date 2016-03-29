/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.cost.ForwardTransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class ActivityTimeTracker implements ActivityVisitor {

    public static enum ActivityPolicy {

        AS_SOON_AS_TIME_WINDOW_OPENS, AS_SOON_AS_ARRIVED

    }

    private final ForwardTransportTime transportTime;

    private final VehicleRoutingActivityCosts activityCosts;

    private TourActivity prevAct = null;

    private double startAtPrevAct;

    private VehicleRoute route;

    private boolean beginFirst = false;

    private double actArrTime;

    private double actEndTime;

    private ActivityPolicy activityPolicy = ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS;

    public ActivityTimeTracker(ForwardTransportTime transportTime, VehicleRoutingActivityCosts activityCosts) {
        super();
        this.transportTime = transportTime;
        this.activityCosts = activityCosts;
    }

    public ActivityTimeTracker(ForwardTransportTime transportTime, ActivityPolicy activityPolicy, VehicleRoutingActivityCosts activityCosts) {
        super();
        this.transportTime = transportTime;
        this.activityPolicy = activityPolicy;
        this.activityCosts = activityCosts;
    }

    public double getActArrTime() {
        return actArrTime;
    }

    public double getActEndTime() {
        return actEndTime;
    }

    @Override
    public void begin(VehicleRoute route) {
        prevAct = route.getStart();
        startAtPrevAct = prevAct.getEndTime();
        actEndTime = startAtPrevAct;
        this.route = route;
        beginFirst = true;
    }

    @Override
    public void visit(TourActivity activity) {
        if (!beginFirst) throw new IllegalStateException("never called begin. this however is essential here");
        double setup_time_prevAct_activity = 0.0;
        double coef = 1.0;
        if(route.getVehicle() != null)
        	coef = route.getVehicle().getCoefSetupTime();

        if(!prevAct.getLocation().equals(activity.getLocation()))
        	setup_time_prevAct_activity = activity.getSetupTime() * coef;

        double transportTime = setup_time_prevAct_activity + this.transportTime.getTransportTime(prevAct.getLocation(), activity.getLocation(), startAtPrevAct, route.getDriver(), route.getVehicle());
    
        double arrivalTimeAtCurrAct = startAtPrevAct + transportTime;

        actArrTime = arrivalTimeAtCurrAct;
        double operationStartTime;

        if (activityPolicy.equals(ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS)) {
            operationStartTime = Math.max(activity.getTheoreticalEarliestOperationStartTime(), arrivalTimeAtCurrAct);
        } else if (activityPolicy.equals(ActivityPolicy.AS_SOON_AS_ARRIVED)) {
            operationStartTime = actArrTime;
        } else operationStartTime = actArrTime;

        double operationEndTime = operationStartTime + activityCosts.getActivityDuration(activity,actArrTime,route.getDriver(),route.getVehicle());

        actEndTime = operationEndTime;

        prevAct = activity;
        startAtPrevAct = operationEndTime;

    }

    @Override
    public void finish() {
        double transportTime = this.transportTime.getTransportTime(prevAct.getLocation(), route.getEnd().getLocation(), startAtPrevAct, route.getDriver(), route.getVehicle());
        double arrivalTimeAtCurrAct = startAtPrevAct + transportTime;

        actArrTime = arrivalTimeAtCurrAct;
        actEndTime = arrivalTimeAtCurrAct;

        beginFirst = false;
    }


}
