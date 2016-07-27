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

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Updates and memorizes latest operation start times at activities.
 *
 * @author schroeder
 */
public class UpdateFutureWaitingTimes implements ReverseActivityVisitor, StateUpdater {

    private StateManager states;

    private VehicleRoute route;

    private VehicleRoutingTransportCosts transportCosts;

    private double futureWaiting;

    public UpdateFutureWaitingTimes(StateManager states, VehicleRoutingTransportCosts tpCosts) {
        super();
        this.states = states;
        this.transportCosts = tpCosts;
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
        this.futureWaiting = 0.;
    }

    @Override
    public void visit(TourActivity activity) {
        states.putInternalTypedActivityState(activity, route.getVehicle(), InternalStates.FUTURE_WAITING, futureWaiting);
		if(!(activity instanceof BreakActivity)) {
            futureWaiting += Math.max(activity.getTheoreticalEarliestOperationStartTime() - activity.getArrTime(), 0);
		}
    }

    @Override
    public void finish() {
    }
}
