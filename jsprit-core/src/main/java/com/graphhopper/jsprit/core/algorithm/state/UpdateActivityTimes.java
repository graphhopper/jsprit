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
package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.cost.ForwardTransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.ActivityTimeTracker;


/**
 * Updates arrival and end times of activities.
 * <p>
 * <p>Note that this modifies arrTime and endTime of each activity in a route.
 *
 * @author stefan
 */
public class UpdateActivityTimes implements ActivityVisitor, StateUpdater {

    private ActivityTimeTracker timeTracker;

    private VehicleRoute route;

    /**
     * Updates arrival and end times of activities.
     * <p>
     * <p>Note that this modifies arrTime and endTime of each activity in a route.
     * <p>
     * <p>ArrTimes and EndTimes can be retrieved by <br>
     * <code>activity.getArrTime()</code> and
     * <code>activity.getEndTime()</code>
     */
    public UpdateActivityTimes(ForwardTransportTime transportTime, VehicleRoutingActivityCosts activityCosts) {
        super();
        timeTracker = new ActivityTimeTracker(transportTime,activityCosts );
    }

    public UpdateActivityTimes(ForwardTransportTime transportTime, ActivityTimeTracker.ActivityPolicy activityPolicy, VehicleRoutingActivityCosts activityCosts) {
        timeTracker = new ActivityTimeTracker(transportTime, activityPolicy, activityCosts);
    }

    @Override
    public void begin(VehicleRoute route) {
        timeTracker.begin(route);
        this.route = route;
        route.getStart().setEndTime(timeTracker.getActEndTime());
    }

    @Override
    public void visit(TourActivity activity) {
        timeTracker.visit(activity);
        activity.setArrTime(timeTracker.getActArrTime());
        activity.setEndTime(timeTracker.getActEndTime());
    }

    @Override
    public void finish() {
        timeTracker.finish();
        route.getEnd().setArrTime(timeTracker.getActArrTime());
    }

}
