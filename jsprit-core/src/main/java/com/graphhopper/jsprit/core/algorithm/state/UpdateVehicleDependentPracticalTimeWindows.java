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

import java.util.*;

public class UpdateVehicleDependentPracticalTimeWindows implements RouteVisitor, StateUpdater {

    @Override
    public void visit(VehicleRoute route) {
        begin(route);
        Iterator<TourActivity> revIterator = route.getTourActivities().reverseActivityIterator();
        while (revIterator.hasNext()) {
            visit(revIterator.next());
        }
        finish();

        Iterator<TourActivity> tourIterator = route.getTourActivities().iterator();
        while (tourIterator.hasNext()) {
            visitForward(tourIterator.next());
        }
        finishForward();
    }

    public interface VehiclesToUpdate {

        Collection<Vehicle> get(VehicleRoute route);

    }

    private VehiclesToUpdate vehiclesToUpdate = route -> Collections.singletonList(route.getVehicle());

    private final StateManager stateManager;

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private VehicleRoute route;

    private final double[] vehicleDependentLatestArrTimesPrevAct;

    private final double[] vehicleDependentEarliestDepartureTimesPrevAct;

    private final Location[] vehicleDependentLocationPrevActBackward;

    private final Location[] vehicleDependentLocationPrevActForward;

    private final TourActivity[] vehicleDependentPrevActForward;

    private Collection<Vehicle> vehicles;

    private List<TourActivity> activities;

    private Map<TourActivity, Integer> activityIndex;

    public UpdateVehicleDependentPracticalTimeWindows(StateManager stateManager, VehicleRoutingTransportCosts tpCosts, VehicleRoutingActivityCosts activityCosts) {
        super();
        this.stateManager = stateManager;
        this.transportCosts = tpCosts;
        this.activityCosts = activityCosts;
        vehicleDependentLatestArrTimesPrevAct = new double[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        vehicleDependentEarliestDepartureTimesPrevAct = new double[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        vehicleDependentLocationPrevActBackward = new Location[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        vehicleDependentLocationPrevActForward = new Location[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        vehicleDependentPrevActForward = new TourActivity[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
    }

    public void setVehiclesToUpdate(VehiclesToUpdate vehiclesToUpdate) {
        this.vehiclesToUpdate = vehiclesToUpdate;
    }


    void begin(VehicleRoute route) {
        this.route = route;
        vehicles = vehiclesToUpdate.get(route);

        // Store activities in a list for backward pass to look up previous activity
        activities = new ArrayList<>(route.getTourActivities().getActivities());
        activityIndex = new HashMap<>();
        for (int i = 0; i < activities.size(); i++) {
            activityIndex.put(activities.get(i), i);
        }

        for (Vehicle vehicle : vehicles) {
            vehicleDependentLatestArrTimesPrevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = vehicle.getLatestArrival();
            vehicleDependentEarliestDepartureTimesPrevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = vehicle.getEarliestDeparture();
            Location location = vehicle.getEndLocation();
            if(!vehicle.isReturnToDepot()){
                location = route.getEnd().getLocation();
            }
            vehicleDependentLocationPrevActBackward[vehicle.getVehicleTypeIdentifier().getIndex()] = location;
            Location startLocation = vehicle.getStartLocation();
            if (vehicle.getStartLocation() == null) {
                startLocation = route.getStart().getLocation();
            }
            vehicleDependentLocationPrevActForward[vehicle.getVehicleTypeIdentifier().getIndex()] = startLocation;
            vehicleDependentPrevActForward[vehicle.getVehicleTypeIdentifier().getIndex()] = route.getStart();
        }
    }


    void visit(TourActivity activity) {
        // Find the previous activity in forward time order for setup_time calculation
        Integer idx = activityIndex.get(activity);
        TourActivity prevActInForwardOrder = (idx != null && idx > 0) ? activities.get(idx - 1) : route.getStart();

        for (Vehicle vehicle : vehicles) {
            double latestArrTimeAtPrevAct = vehicleDependentLatestArrTimesPrevAct[vehicle.getVehicleTypeIdentifier().getIndex()];
            Location prevLocation = vehicleDependentLocationPrevActBackward[vehicle.getVehicleTypeIdentifier().getIndex()];
            double potentialLatestArrivalTimeAtCurrAct = latestArrTimeAtPrevAct - transportCosts.getBackwardTransportTime(activity.getLocation(), prevLocation,
                    latestArrTimeAtPrevAct, route.getDriver(), vehicle) - activityCosts.getActivityDuration(prevActInForwardOrder, activity, latestArrTimeAtPrevAct, route.getDriver(), vehicle);
            double latestArrivalTime = Math.min(activity.getTheoreticalLatestOperationStartTime(), potentialLatestArrivalTimeAtCurrAct);
            if (latestArrivalTime < activity.getTheoreticalEarliestOperationStartTime()) {
                stateManager.putTypedInternalRouteState(route, vehicle, InternalStates.SWITCH_NOT_FEASIBLE, true);
            }
            stateManager.putInternalTypedActivityState(activity, vehicle, InternalStates.LATEST_OPERATION_START_TIME, latestArrivalTime);
            vehicleDependentLatestArrTimesPrevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = latestArrivalTime;
            vehicleDependentLocationPrevActBackward[vehicle.getVehicleTypeIdentifier().getIndex()] = activity.getLocation();

        }
    }

    void visitForward(TourActivity activity) {
        for (Vehicle vehicle : vehicles) {
            int vehicleIdx = vehicle.getVehicleTypeIdentifier().getIndex();
            double earliestDepartureAtPrevAct = vehicleDependentEarliestDepartureTimesPrevAct[vehicleIdx];
            Location earliestLocation = vehicleDependentLocationPrevActForward[vehicleIdx];
            TourActivity prevAct = vehicleDependentPrevActForward[vehicleIdx];
            double potentialEarliestArrivalTimeAtCurrAct = earliestDepartureAtPrevAct + transportCosts.getTransportTime(earliestLocation, activity.getLocation(), earliestDepartureAtPrevAct, route.getDriver(), vehicle);
            if (potentialEarliestArrivalTimeAtCurrAct > activity.getTheoreticalLatestOperationStartTime()) {
                stateManager.putTypedInternalRouteState(route, vehicle, InternalStates.SWITCH_NOT_FEASIBLE, true);
            }
            double earliestStart = Math.max(potentialEarliestArrivalTimeAtCurrAct, activity.getTheoreticalEarliestOperationStartTime());
            double earliestDeparture = earliestStart + activityCosts.getActivityDuration(prevAct, activity, earliestStart, route.getDriver(), vehicle);
            vehicleDependentEarliestDepartureTimesPrevAct[vehicleIdx] = earliestDeparture;
            vehicleDependentLocationPrevActForward[vehicleIdx] = activity.getLocation();
            vehicleDependentPrevActForward[vehicleIdx] = activity;
        }
    }


    void finish() {
    }

    void finishForward() {
    }

}

