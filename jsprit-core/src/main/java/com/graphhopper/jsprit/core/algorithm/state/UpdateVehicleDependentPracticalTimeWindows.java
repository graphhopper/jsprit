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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.RouteVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class UpdateVehicleDependentPracticalTimeWindows implements RouteVisitor, StateUpdater {

    @Override
    public void visit(VehicleRoute route) {
        begin(route);
        List<TourActivity> activities = new ArrayList<>();
        activities.add(route.getStart());
        activities.addAll(route.getTourActivities().getActivities());

        for (int i = activities.size() - 1; i > 0; --i)
            visit(activities.get(i), activities.get(i - 1));

        finish();
    }

    public static interface VehiclesToUpdate {

        public Collection<Vehicle> get(VehicleRoute route);

    }

    private VehiclesToUpdate vehiclesToUpdate = new VehiclesToUpdate() {

        @Override
        public Collection<Vehicle> get(VehicleRoute route) {
            return Arrays.asList(route.getVehicle());
        }

    };

    private final StateManager stateManager;

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private VehicleRoute route;

    protected double[] latest_arrTimes_at_prevAct;

    private Location[] location_of_prevAct;

    private Collection<Vehicle> vehicles;

    public UpdateVehicleDependentPracticalTimeWindows(StateManager stateManager, VehicleRoutingTransportCosts tpCosts, VehicleRoutingActivityCosts activityCosts) {
        super();
        this.stateManager = stateManager;
        this.transportCosts = tpCosts;
        this.activityCosts = activityCosts;
        latest_arrTimes_at_prevAct = new double[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        location_of_prevAct = new Location[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
    }

    public void setVehiclesToUpdate(VehiclesToUpdate vehiclesToUpdate) {
        this.vehiclesToUpdate = vehiclesToUpdate;
    }


    public void begin(VehicleRoute route) {
        this.route = route;
        vehicles = vehiclesToUpdate.get(route);
        for (Vehicle vehicle : vehicles) {
            latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = vehicle.getLatestArrival();
            Location location = vehicle.getEndLocation();
            if(!vehicle.isReturnToDepot()){
                location = route.getEnd().getLocation();
            }
            location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = location;
        }
    }


    public void visit(TourActivity activity, TourActivity prev) {
        for (Vehicle vehicle : vehicles) {
            double latestArrTimeAtPrevAct = latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()];
            Location prevLocation = location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()];
            double potentialLatestArrivalTimeAtCurrAct = latestArrTimeAtPrevAct - transportCosts.getTransportTime(activity.getLocation(), prevLocation,
                latestArrTimeAtPrevAct, route.getDriver(), vehicle) - activityCosts.getActivityDuration(prev, activity, latestArrTimeAtPrevAct, route.getDriver(), route.getVehicle());
            double latestArrivalTime = getLatestArrival(activity, potentialLatestArrivalTimeAtCurrAct);
            if (latestArrivalTime < activity.getTheoreticalEarliestOperationStartTime()) {
                stateManager.putTypedInternalRouteState(route, vehicle, InternalStates.SWITCH_NOT_FEASIBLE, true);
            }
            stateManager.putInternalTypedActivityState(activity, vehicle, InternalStates.LATEST_OPERATION_START_TIME, latestArrivalTime);
            latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = latestArrivalTime;
            location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = activity.getLocation();
        }
    }

    protected double getLatestArrival(TourActivity activity, double potentialLatestArrivalTimeAtCurrAct) {
        return Math.min(activity.getTheoreticalLatestOperationStartTime(), potentialLatestArrivalTimeAtCurrAct);
    }


    public void finish() {
    }

}

