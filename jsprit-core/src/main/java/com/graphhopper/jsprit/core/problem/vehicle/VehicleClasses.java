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

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

import java.util.*;
import java.util.function.Function;

/**
 * A fixed partition of a fleet: every vehicle belongs to exactly one class, classes are numbered from 0.
 * <p>
 * The partition says which vehicles are the same with respect to one question. The state manager uses one
 * to decide which vehicles share their vehicle-dependent states: a state is computed once per class, for the
 * class' representative, and read for any vehicle of the class. The key function must therefore cover every
 * vehicle attribute a state updater reads.
 * <p>
 * The partition is built once and does not change during the search.
 */
public abstract class VehicleClasses {

    /** The class of the vehicle, in [0, {@link #size()}). */
    public abstract int classOf(Vehicle vehicle);

    /** Exclusive upper bound of the class numbers. */
    public abstract int size();

    /** One vehicle per class, in the order of the classes' first vehicle in the fleet. */
    public abstract List<Vehicle> representatives();

    /** The vehicles of the class in fleet order; empty for a class number no vehicle has. */
    public abstract List<Vehicle> members(int vehicleClass);

    /**
     * What a fleet manager uses by default to decide which vehicles are interchangeable for an insertion:
     * type, start and end location, earliest start, latest end, skills, return flag (the fields of the
     * deprecated {@link VehicleTypeKey}) and the vehicle's break (time windows and duration). A problem with
     * further vehicle attributes that constraints or costs read has to bring its own classes.
     */
    public static VehicleClasses fleetDefault(Collection<? extends Vehicle> vehicles) {
        return by(vehicles, VehicleClasses::defaultFleetKey);
    }

    private static Object defaultFleetKey(Vehicle v) {
        List<Object> key = new ArrayList<>(9);
        key.add(v.getType().getTypeId());
        key.add(v.getStartLocation() == null ? null : v.getStartLocation().getId());
        key.add(v.getEndLocation() == null ? null : v.getEndLocation().getId());
        key.add(v.getEarliestDeparture());
        key.add(v.getLatestArrival());
        key.add(v.getSkills());
        key.add(v.isReturnToDepot());
        if (v.getBreak() != null) {
            key.add(new ArrayList<>(v.getBreak().getTimeWindows()));
            key.add(v.getBreak().getServiceDuration());
        }
        return key;
    }

    /**
     * Vehicles with equal {@link Vehicle#getVehicleTypeIdentifier()} form a class; the class number is the
     * problem's type key index. This is what the state manager did before classes existed.
     */
    public static VehicleClasses byTypeKey(VehicleRoutingProblem vrp) {
        return new ByTypeKey(vrp);
    }

    /**
     * Vehicles with equal keys form a class. A vehicle that is not one of the given vehicles is classified by
     * its key; if no given vehicle has that key, {@link #classOf(Vehicle)} throws.
     */
    public static VehicleClasses by(Collection<? extends Vehicle> vehicles, Function<Vehicle, ?> key) {
        return new ByKey(vehicles, key);
    }

    private static final class ByTypeKey extends VehicleClasses {

        private final VehicleRoutingProblem vrp;

        private final int size;

        private final List<Vehicle> representatives;

        private final List<List<Vehicle>> members = new ArrayList<>();

        ByTypeKey(VehicleRoutingProblem vrp) {
            this.vrp = vrp;
            int max = 0;
            Map<VehicleTypeKey, Vehicle> byKey = new LinkedHashMap<>();
            for (Vehicle v : vrp.getVehicles()) {
                int c = classOf(v);
                max = Math.max(max, c);
                byKey.putIfAbsent(v.getVehicleTypeIdentifier(), v);
                while (members.size() <= c) members.add(new ArrayList<>());
                members.get(c).add(v);
            }
            size = max + 1;
            while (members.size() < size) members.add(new ArrayList<>());
            representatives = Collections.unmodifiableList(new ArrayList<>(byKey.values()));
        }

        @Override
        public List<Vehicle> members(int vehicleClass) {
            return Collections.unmodifiableList(members.get(vehicleClass));
        }

        @Override
        public int classOf(Vehicle vehicle) {
            return vrp.getVehicleTypeKeyIndex(vehicle.getVehicleTypeIdentifier());
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public List<Vehicle> representatives() {
            return representatives;
        }
    }

    private static final class ByKey extends VehicleClasses {

        private final Function<Vehicle, ?> key;

        private final IdentityHashMap<Vehicle, Integer> classOfVehicle = new IdentityHashMap<>();

        private final Map<Object, Integer> classOfKey = new HashMap<>();

        private final List<Vehicle> representatives;

        private final List<List<Vehicle>> members = new ArrayList<>();

        ByKey(Collection<? extends Vehicle> vehicles, Function<Vehicle, ?> key) {
            this.key = key;
            List<Vehicle> reps = new ArrayList<>();
            for (Vehicle v : vehicles) {
                Object k = key.apply(v);
                Integer c = classOfKey.get(k);
                if (c == null) {
                    c = reps.size();
                    classOfKey.put(k, c);
                    reps.add(v);
                    members.add(new ArrayList<>());
                }
                if (classOfVehicle.put(v, c) == null) members.get(c).add(v);
            }
            representatives = Collections.unmodifiableList(reps);
        }

        @Override
        public List<Vehicle> members(int vehicleClass) {
            return Collections.unmodifiableList(members.get(vehicleClass));
        }

        @Override
        public int classOf(Vehicle vehicle) {
            Integer c = classOfVehicle.get(vehicle);
            if (c != null) return c;
            c = classOfKey.get(key.apply(vehicle));
            if (c == null)
                throw new IllegalArgumentException("vehicle " + vehicle.getId() + " belongs to no vehicle class: it is not part of the fleet the classes were built from and no vehicle of that fleet has its key");
            return c;
        }

        @Override
        public int size() {
            return representatives.size();
        }

        @Override
        public List<Vehicle> representatives() {
            return representatives;
        }
    }
}
