/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.cost.ForwardTransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class ActivityTimeTracker implements ActivityVisitor {

    public static enum ActivityPolicy {

        AS_SOON_AS_TIME_WINDOW_OPENS, AS_SOON_AS_ARRIVED, AS_SOON_AS_TIME_WINDOW_OPENS_WITHIN_GROUP

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
        double transportTime = this.transportTime.getTransportTime(prevAct.getLocation(), activity.getLocation(), startAtPrevAct, route.getDriver(), route.getVehicle());
        double arrivalTimeAtCurrAct = startAtPrevAct + transportTime;

        // modify the activity arrival time if this activity can be grouped with the previous one
        // they will both have the same arrival and end times afterwards
        if (canGroupActivities(activity, arrivalTimeAtCurrAct)) {
            actArrTime = arrivalTimeAtCurrAct - prevAct.getOperationTime();
        } else {
            actArrTime = arrivalTimeAtCurrAct;
        }
        double operationStartTime;

        if (activityPolicy.equals(ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS)) {
            operationStartTime = Math.max(activity.getTheoreticalEarliestOperationStartTime(), arrivalTimeAtCurrAct);
        } else if (activityPolicy.equals(ActivityPolicy.AS_SOON_AS_ARRIVED)) {
            operationStartTime = actArrTime;
        } else if (activityPolicy.equals(ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS_WITHIN_GROUP)) {
            operationStartTime = Math.max(activity.getTheoreticalEarliestOperationStartTime(), actArrTime);
        } else operationStartTime = actArrTime;

        double operationEndTime;
        // if the current activity can be grouped with the previous one adjust the operation end time
        // select the operation time which is bigger
        // as we iterate over each activity, we need to change the operation end time of the previous activity so that they have the same end time
        // (we didn't know when inserting the previous activity, that we should use the operating time of the current activity)
        if (canGroupActivities(activity, arrivalTimeAtCurrAct)) {
            operationEndTime = operationStartTime + Math.max(prevAct.getOperationTime(), activity.getOperationTime());
            prevAct.setEndTime(operationEndTime);
        } else {
            operationEndTime = operationStartTime + activity.getOperationTime();
        }

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


    private boolean canGroupActivities(TourActivity activity, double arrivalTimeAtCurrAct) {
        if (!activityPolicy.equals(ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS_WITHIN_GROUP)) {
            return false;
        }

        // group activities if the end time of the previous activity is matching the arrival time of the current activity
        if (Double.compare(arrivalTimeAtCurrAct, startAtPrevAct) == 0) {
            // check if the current activity could start at the same time as the previous activity by subtracting the operation time / service time
            // and compare this time to the lower bound of the time window.
            double theoreticalArrivalTimeAtCurrActWithoutPrevOperatingTime = arrivalTimeAtCurrAct - prevAct.getOperationTime();
            return theoreticalArrivalTimeAtCurrActWithoutPrevOperatingTime >= activity.getTheoreticalEarliestOperationStartTime();
        }
        return false;
    }

}
