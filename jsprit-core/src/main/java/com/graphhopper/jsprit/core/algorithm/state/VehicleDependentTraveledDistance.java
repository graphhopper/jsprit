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
import com.graphhopper.jsprit.core.problem.vehicle.VehicleDependent;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;

import java.util.*;

/**
 * Created by schroeder on 17/05/16.
 */
public class VehicleDependentTraveledDistance implements StateUpdater, ActivityVisitor, VehicleDependent {

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

    private final Collection<Vehicle> allVehicles;

    private List<Vehicle> uniqueVehicles;

    private State[] states;

    public VehicleDependentTraveledDistance(TransportDistance transportCostMatrices, StateManager stateManager, StateId distanceInRouteId, Collection<Vehicle> vehicles) {
        this.transportDistance = transportCostMatrices;
        this.stateManager = stateManager;
        this.traveledDistanceId = distanceInRouteId;
        this.allVehicles = vehicles;
    }

    /** what the travelled distance reads from a vehicle, directly and through the distance function */
    @Override
    public Object vehicleKey(Vehicle vehicle) {
        return Arrays.asList(VehicleDependent.locationKey(transportDistance, vehicle.getStartLocation()), VehicleDependent.locationKey(transportDistance, vehicle.getEndLocation()),
            vehicle.isReturnToDepot(), VehicleDependent.keyOf(transportDistance, vehicle));
    }

    private int classOf(Vehicle v) {
        return stateManager.getVehicleClasses().classOf(v);
    }

    private List<Vehicle> getUniqueVehicles(Collection<Vehicle> vehicles) {
        // one vehicle per state class: vehicles of a class share the state
        Set<Integer> classes = new HashSet<>();
        List<Vehicle> uniqueVehicles = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (classes.add(stateManager.getVehicleClasses().classOf(v))) uniqueVehicles.add(v);
        }
        return uniqueVehicles;
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
        if (uniqueVehicles == null) {
            // not in the constructor: the vehicle classes may only be known once all updaters are registered
            uniqueVehicles = getUniqueVehicles(allVehicles);
            states = new State[stateManager.getVehicleClasses().size()];
        }
        for (Vehicle v : uniqueVehicles) {
            states[classOf(v)] = new State(v.getStartLocation(), 0);
        }
    }

    @Override
    public void visit(TourActivity activity) {
        for (Vehicle v : uniqueVehicles) {
            State old = states[classOf(v)];
            double distance = old.getDistance();
            distance += transportDistance.getDistance(old.getPrevLocation(), activity.getLocation(), 0, v);
            stateManager.putActivityDouble(activity, v, traveledDistanceId, distance);
            states[classOf(v)] = new State(activity.getLocation(), distance);
        }
    }

    @Override
    public void finish() {
        for (Vehicle v : uniqueVehicles) {
            State old = states[classOf(v)];
            double distance = old.getDistance();
            if (v.isReturnToDepot()) {
                distance += transportDistance.getDistance(old.getPrevLocation(), v.getEndLocation(), 0, v);
            }
            stateManager.putRouteDouble(route, v, traveledDistanceId, distance);
        }
    }

}
