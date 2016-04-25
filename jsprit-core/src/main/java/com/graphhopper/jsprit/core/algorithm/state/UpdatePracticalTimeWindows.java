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
package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.cost.SetupTime;
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

    private SetupTime setupCosts = new SetupTime();

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
        double setup_time_activity_prevAct = setupCosts.getSetupTime(activity, prevAct, route.getVehicle());
        double transport_time_activity_prevAct = setup_time_activity_prevAct + transportCosts.getBackwardTransportTime(activity.getLocation(), prevAct.getLocation(), latestArrTimeAtPrevAct, route.getDriver(), route.getVehicle());
        double potentialLatestArrivalTimeAtCurrAct = latestArrTimeAtPrevAct - transport_time_activity_prevAct  - activityCosts.getActivityDuration(activity,latestArrTimeAtPrevAct,route.getDriver(),route.getVehicle());
        double latestArrivalTime = Math.min(activity.getTheoreticalLatestOperationStartTime(), potentialLatestArrivalTimeAtCurrAct);

        states.putInternalTypedActivityState(activity, InternalStates.LATEST_OPERATION_START_TIME, latestArrivalTime);

        latestArrTimeAtPrevAct = latestArrivalTime;
        prevAct = activity;
    }

    @Override
    public void finish() {
    }
}
