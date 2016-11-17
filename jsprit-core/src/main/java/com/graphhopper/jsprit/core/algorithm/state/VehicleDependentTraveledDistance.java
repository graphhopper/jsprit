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
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;

import java.util.*;

/**
 * Created by schroeder on 17/05/16.
 */
public class VehicleDependentTraveledDistance implements StateUpdater, ActivityVisitor {

    static class State {

        Location prevLocation;

        double distance;

        public State(Location prevLocation, double distance) {
            this.prevLocation = prevLocation;
            this.distance = distance;
        }

        public Location getPrevLocation() {
            return prevLocation;
        }

        public double getDistance() {
            return distance;
        }
    }

    private final TransportDistance transportDistance;

    private final StateManager stateManager;

    private final StateId traveledDistanceId;

    private VehicleRoute route;

    private List<Vehicle> uniqueVehicles;

    private Map<VehicleTypeKey, State> states;

    public VehicleDependentTraveledDistance(TransportDistance transportCostMatrices, StateManager stateManager, StateId distanceInRouteId, Collection<Vehicle> vehicles) {
        this.transportDistance = transportCostMatrices;
        this.stateManager = stateManager;
        this.traveledDistanceId = distanceInRouteId;
        uniqueVehicles = getUniqueVehicles(vehicles);
    }

    private List<Vehicle> getUniqueVehicles(Collection<Vehicle> vehicles) {
        Set<VehicleTypeKey> types = new HashSet<>();
        List<Vehicle> uniqueVehicles = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (!types.contains(v.getVehicleTypeIdentifier())) {
                types.add(v.getVehicleTypeIdentifier());
                uniqueVehicles.add(v);
            }
        }
        return uniqueVehicles;
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
        states = new HashMap<>();
        for (Vehicle v : uniqueVehicles) {
            State state = new State(v.getStartLocation(), 0);
            states.put(v.getVehicleTypeIdentifier(), state);
        }
    }

    @Override
    public void visit(TourActivity activity) {
        for (Vehicle v : uniqueVehicles) {
            State old = states.get(v.getVehicleTypeIdentifier());
            double distance = old.getDistance();
            distance += transportDistance.getDistance(old.getPrevLocation(), activity.getLocation(), 0, v);
            stateManager.putActivityState(activity, v, traveledDistanceId, distance);
            states.put(v.getVehicleTypeIdentifier(), new State(activity.getLocation(), distance));
        }
    }

    @Override
    public void finish() {
        for (Vehicle v : uniqueVehicles) {
            State old = states.get(v.getVehicleTypeIdentifier());
            double distance = old.getDistance();
            if (v.isReturnToDepot()) {
                distance += transportDistance.getDistance(old.getPrevLocation(), v.getEndLocation(), 0, v);
            }
            stateManager.putRouteState(route, v, traveledDistanceId, distance);
        }
    }

}
