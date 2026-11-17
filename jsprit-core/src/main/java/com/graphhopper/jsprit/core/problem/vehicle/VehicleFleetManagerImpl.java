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

import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.*;

/**
 * Finite fleet: per class the free vehicle that comes first in the fleet. Which vehicles are in use is taken
 * from the routes.
 */
class VehicleFleetManagerImpl implements VehicleFleetManager {

    static final String VERIFY = "jsprit.states.failOnMissing";

    private final VehicleClasses classes;

    /** per class: position in the class' member list -> free */
    private final BitSet[] free;

    /** position of a vehicle in its class' member list */
    private final IdentityHashMap<Vehicle, Integer> position = new IdentityHashMap<>();

    /** the vehicle each known route had when it was last seen */
    private final IdentityHashMap<VehicleRoute, Vehicle> vehicleOfRoute = new IdentityHashMap<>();

    private final boolean empty;

    private boolean verify = Boolean.getBoolean(VERIFY);

    VehicleFleetManagerImpl(Collection<Vehicle> vehicles, VehicleClasses classes) {
        this.classes = classes;
        this.empty = vehicles.isEmpty();
        Set<Vehicle> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Vehicle v : vehicles) {
            if (v.getType() == null) throw new IllegalStateException("vehicle needs type");
            if (!seen.add(v)) throw new IllegalStateException("cannot add vehicle twice " + v.getId());
        }
        free = new BitSet[classes.size()];
        for (int c = 0; c < free.length; c++) {
            List<Vehicle> members = classes.members(c);
            free[c] = new BitSet(members.size());
            for (int i = 0; i < members.size(); i++) {
                if (!seen.contains(members.get(i))) continue;
                position.put(members.get(i), i);
                free[c].set(i);
            }
        }
        for (Vehicle v : vehicles) {
            if (!position.containsKey(v))
                throw new IllegalStateException("vehicle " + v.getId() + " is not part of the fleet the vehicle classes were built from");
        }
    }

    void setVerify(boolean verify) {
        this.verify = verify;
    }

    @Override
    public String toString() {
        return "[name=finiteVehicles][classes=" + classes.size() + "]";
    }

    private boolean notManaged(Vehicle vehicle) {
        return vehicle == null || empty || vehicle instanceof VehicleImpl.NoVehicle;
    }

    private int positionOf(Vehicle vehicle) {
        Integer p = position.get(vehicle);
        if (p == null) throw new IllegalStateException("vehicle " + vehicle.getId() + " is not part of this fleet");
        return p;
    }

    private void take(Vehicle vehicle) {
        if (notManaged(vehicle)) return;
        free[classes.classOf(vehicle)].clear(positionOf(vehicle));
    }

    private void free(Vehicle vehicle) {
        if (notManaged(vehicle)) return;
        free[classes.classOf(vehicle)].set(positionOf(vehicle));
    }

    private Vehicle firstFree(int vehicleClass) {
        int first = free[vehicleClass].nextSetBit(0);
        return first < 0 ? null : classes.members(vehicleClass).get(first);
    }

    @Override
    public Collection<Vehicle> getAvailableVehicles() {
        if (verify) verifyKnownRoutes();
        List<Vehicle> vehicles = new ArrayList<>(free.length);
        for (int c = 0; c < free.length; c++) {
            Vehicle v = firstFree(c);
            if (v != null) vehicles.add(v);
        }
        return vehicles;
    }

    @Override
    public Collection<Vehicle> getAvailableVehicles(Vehicle withoutThisClass) {
        if (verify) verifyKnownRoutes();
        int exclude = classes.classOf(withoutThisClass);
        List<Vehicle> vehicles = new ArrayList<>(free.length);
        for (int c = 0; c < free.length; c++) {
            if (c == exclude) continue;
            Vehicle v = firstFree(c);
            if (v != null) vehicles.add(v);
        }
        return vehicles;
    }

    @Override
    public Vehicle getAvailableVehicle(Vehicle sameClassAs) {
        if (verify) verifyKnownRoutes();
        return firstFree(classes.classOf(sameClassAs));
    }

    @Override
    public boolean isInUse(Vehicle vehicle) {
        if (notManaged(vehicle)) return false;
        return !free[classes.classOf(vehicle)].get(positionOf(vehicle));
    }

    @Override
    public void reset(Collection<VehicleRoute> routes) {
        vehicleOfRoute.clear();
        for (int c = 0; c < free.length; c++) {
            free[c].clear();
            List<Vehicle> members = classes.members(c);
            for (int i = 0; i < members.size(); i++) {
                if (position.containsKey(members.get(i))) free[c].set(i);
            }
        }
        for (VehicleRoute route : routes) sync(route);
    }

    @Override
    public void sync(VehicleRoute route) {
        Vehicle now = route.getVehicle();
        Vehicle before = vehicleOfRoute.get(route);
        if (before == now) return;
        if (before != null) free(before);
        if (notManaged(now)) {
            vehicleOfRoute.remove(route);
        } else {
            take(now);
            vehicleOfRoute.put(route, now);
        }
    }

    @Override
    public void release(VehicleRoute route) {
        Vehicle before = vehicleOfRoute.remove(route);
        if (before != null) free(before);
    }

    @Override
    public void verify(VehicleRoute route) {
        if (!verify || notManaged(route.getVehicle())) return;
        if (vehicleOfRoute.get(route) != route.getVehicle())
            throw new IllegalStateException("fleet usage out of date: route with vehicle " + route.getVehicle().getId()
                + " is " + (vehicleOfRoute.containsKey(route) ? "recorded with vehicle " + vehicleOfRoute.get(route).getId() : "unknown")
                + ". A route got or changed its vehicle without StateManager.informInsertionStarts / informJobInserted / reCalculateStates");
    }

    private void verifyKnownRoutes() {
        for (Map.Entry<VehicleRoute, Vehicle> e : vehicleOfRoute.entrySet()) {
            if (e.getKey().getVehicle() != e.getValue())
                throw new IllegalStateException("fleet usage out of date: a route recorded with vehicle " + e.getValue().getId()
                    + " now has vehicle " + (e.getKey().getVehicle() == null ? null : e.getKey().getVehicle().getId())
                    + ". The vehicle was changed without StateManager.informJobInserted / reCalculateStates");
        }
    }

}
