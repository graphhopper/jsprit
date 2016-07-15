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
            futureWaiting += Math.max(activity.getTheoreticalEarliestOperationStartTime() - activity.getReadyTime(), 0);
		}
    }

    @Override
    public void finish() {
    }
}
