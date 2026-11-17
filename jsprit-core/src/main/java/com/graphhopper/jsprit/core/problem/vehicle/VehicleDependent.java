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
package com.graphhopper.jsprit.core.problem.vehicle;

/**
 * Implemented by whatever reads a vehicle while vehicle-dependent states are computed: a state updater, a
 * transport cost or an activity cost function. It names what it reads, so that the state manager can tell
 * which vehicles are the same for all of them and compute their states once, see
 * {@link com.graphhopper.jsprit.core.algorithm.state.StateManager#withDeclaredVehicleClasses}.
 */
public interface VehicleDependent {

    /**
     * Everything this object reads from the vehicle for the values that end up in states, as a value with equals
     * and hashCode (a list of the attributes, for instance). For a cost function these are the times, durations
     * and distances it returns; what it reads for the costs only (cost parameters of the type) does not belong
     * here as long as no vehicle-dependent state stores costs. Two vehicles with equal keys must be
     * indistinguishable in this respect.
     */
    Object vehicleKey(Vehicle vehicle);

    /**
     * The key of a collaborator, such as the cost function an updater computes with: what it declares, or,
     * if it declares nothing, the vehicle's type key, which is what all vehicle-dependent states were
     * keyed by before.
     */
    static Object keyOf(Object collaborator, Vehicle vehicle) {
        if (collaborator instanceof VehicleDependent) return ((VehicleDependent) collaborator).vehicleKey(vehicle);
        return vehicle.getVehicleTypeIdentifier();
    }

    /**
     * The key of a vehicle's start or end location for a collaborator that computes with it: the class the
     * collaborator puts the location in if it implements
     * {@link com.graphhopper.jsprit.core.problem.cost.EquivalentLocations}, the location's id otherwise.
     */
    static Object locationKey(Object collaborator, com.graphhopper.jsprit.core.problem.Location location) {
        if (location == null) return null;
        if (collaborator instanceof com.graphhopper.jsprit.core.problem.cost.EquivalentLocations)
            return ((com.graphhopper.jsprit.core.problem.cost.EquivalentLocations) collaborator).locationKey(location);
        return location.getId();
    }
}
