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

import com.graphhopper.jsprit.core.problem.cost.ForwardTransportCost;
import com.graphhopper.jsprit.core.problem.cost.SetupTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.ActivityTimeTracker;


/**
 * Updates total costs (i.e. transport and activity costs) at route and activity level.
 * <p>
 * <p>Thus it modifies <code>stateManager.getRouteState(route, StateTypes.COSTS)</code> and <br>
 * <code>stateManager.getActivityState(activity, StateTypes.COSTS)</code>
 */
public class UpdateVariableCosts implements ActivityVisitor, StateUpdater {

    private VehicleRoutingActivityCosts activityCost;

    private ForwardTransportCost transportCost;

    private SetupTime setupCosts = new SetupTime();

    private StateManager states;

    private double totalOperationCost = 0.0;

    private VehicleRoute vehicleRoute = null;

    private TourActivity prevAct = null;

    private double startTimeAtPrevAct = 0.0;

    private ActivityTimeTracker timeTracker;

    /**
     * Updates total costs (i.e. transport and activity costs) at route and activity level.
     * <p>
     * <p>Thus it modifies <code>stateManager.getRouteState(route, StateTypes.COSTS)</code> and <br>
     * <code>stateManager.getActivityState(activity, StateTypes.COSTS)</code>
     *
     * @param activityCost
     * @param transportCost
     * @param states
     */
    public UpdateVariableCosts(VehicleRoutingActivityCosts activityCost, VehicleRoutingTransportCosts transportCost, StateManager states) {
        super();
        this.activityCost = activityCost;
        this.transportCost = transportCost;
        this.states = states;
        timeTracker = new ActivityTimeTracker(transportCost, activityCost);
    }

    public UpdateVariableCosts(VehicleRoutingActivityCosts activityCosts, VehicleRoutingTransportCosts transportCosts, StateManager stateManager, ActivityTimeTracker.ActivityPolicy activityPolicy) {
        this.activityCost = activityCosts;
        this.transportCost = transportCosts;
        this.states = stateManager;
        timeTracker = new ActivityTimeTracker(transportCosts, activityPolicy, activityCosts);
    }

    @Override
    public void begin(VehicleRoute route) {
        vehicleRoute = route;
        timeTracker.begin(route);
        prevAct = route.getStart();
        startTimeAtPrevAct = timeTracker.getActEndTime();
    }

    @Override
    public void visit(TourActivity act) {
        timeTracker.visit(act);

        double transportCost = this.transportCost.getTransportCost(prevAct.getLocation(), act.getLocation(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
        double setupTime = setupCosts.getSetupTime(prevAct, act, vehicleRoute.getVehicle());
        double setupCost = setupCosts.getSetupCost(setupTime, vehicleRoute.getVehicle());
        double actReadyTime = timeTracker.getActArrTime() + setupTime;
        double actCost = activityCost.getActivityCost(act, actReadyTime, vehicleRoute.getDriver(), vehicleRoute.getVehicle());

        totalOperationCost += transportCost;
        totalOperationCost += setupCost;
        totalOperationCost += actCost;

        states.putInternalTypedActivityState(act, InternalStates.COSTS, totalOperationCost);

        prevAct = act;
        startTimeAtPrevAct = timeTracker.getActEndTime();
    }

    @Override
    public void finish() {
        timeTracker.finish();
        double transportCost = this.transportCost.getTransportCost(prevAct.getLocation(), vehicleRoute.getEnd().getLocation(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
        double actCost = activityCost.getActivityCost(vehicleRoute.getEnd(), timeTracker.getActEndTime(), vehicleRoute.getDriver(), vehicleRoute.getVehicle());

        totalOperationCost += transportCost;
        totalOperationCost += actCost;

        states.putTypedInternalRouteState(vehicleRoute, InternalStates.COSTS, totalOperationCost);

        startTimeAtPrevAct = 0.0;
        prevAct = null;
        vehicleRoute = null;
        totalOperationCost = 0.0;
    }

}
