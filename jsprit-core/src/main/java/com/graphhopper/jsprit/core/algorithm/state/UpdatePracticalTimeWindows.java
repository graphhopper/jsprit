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

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Updates and memorizes latest operation start times at activities.
 *
 * @author schroeder
 */
class UpdatePracticalTimeWindows implements ReverseActivityVisitor, StateUpdater {

    private StateManager states;

    private VehicleRoute route;

    private VehicleRoutingTransportCosts transportCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private double latestArrTimeAtPrevAct;

    private TourActivity prevAct;

    public UpdatePracticalTimeWindows(StateManager states, VehicleRoutingTransportCosts tpCosts, VehicleRoutingActivityCosts activityCosts) {
        super();
        this.states = states;
        this.transportCosts = tpCosts;
        this.activityCosts = activityCosts;
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
        latestArrTimeAtPrevAct = route.getEnd().getTheoreticalLatestOperationStartTime();
        prevAct = route.getEnd();
    }

    @Override
    public void visit(TourActivity activity) {
        double potentialLatestArrivalTimeAtCurrAct = latestArrTimeAtPrevAct - transportCosts.getBackwardTransportTime(activity.getLocation(), prevAct.getLocation(), latestArrTimeAtPrevAct, route.getDriver(), route.getVehicle()) - activityCosts.getActivityDuration(activity, latestArrTimeAtPrevAct, route.getDriver(), route.getVehicle());
        double latestArrivalTime = Math.min(activity.getTheoreticalLatestOperationStartTime(), potentialLatestArrivalTimeAtCurrAct);

        states.putInternalTypedActivityState(activity, InternalStates.LATEST_OPERATION_START_TIME, latestArrivalTime);

        latestArrTimeAtPrevAct = latestArrivalTime;
        prevAct = activity;
    }

    @Override
    public void finish() {
    }
}
