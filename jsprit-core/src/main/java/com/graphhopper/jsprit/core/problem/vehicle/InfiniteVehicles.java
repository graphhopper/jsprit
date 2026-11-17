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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Infinite fleet: one vehicle per class, never in use.
 */
class InfiniteVehicles implements VehicleFleetManager {

    private final VehicleClasses classes;

    InfiniteVehicles(Collection<Vehicle> vehicles) {
        this(VehicleClasses.fleetDefault(vehicles));
    }

    InfiniteVehicles(VehicleClasses classes) {
        this.classes = classes;
    }

    @Override
    public String toString() {
        return "[name=infiniteVehicle]";
    }

    @Override
    public boolean isInUse(Vehicle vehicle) {
        return false;
    }

    @Override
    public Collection<Vehicle> getAvailableVehicles() {
        return classes.representatives();
    }

    @Override
    public Collection<Vehicle> getAvailableVehicles(Vehicle withoutThisClass) {
        int exclude = classes.classOf(withoutThisClass);
        List<Vehicle> vehicles = new ArrayList<>(classes.size());
        for (Vehicle v : classes.representatives()) {
            if (classes.classOf(v) != exclude) vehicles.add(v);
        }
        return vehicles;
    }

    @Override
    public Vehicle getAvailableVehicle(Vehicle sameClassAs) {
        return classes.members(classes.classOf(sameClassAs)).get(0);
    }

    @Override
    public void reset(Collection<VehicleRoute> routes) {
    }

    @Override
    public void sync(VehicleRoute route) {
    }

    @Override
    public void release(VehicleRoute route) {
    }

    @Override
    public void verify(VehicleRoute route) {
    }

}
