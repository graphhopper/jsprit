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

        ByTypeKey(VehicleRoutingProblem vrp) {
            this.vrp = vrp;
            int max = 0;
            Map<VehicleTypeKey, Vehicle> byKey = new LinkedHashMap<>();
            for (Vehicle v : vrp.getVehicles()) {
                max = Math.max(max, classOf(v));
                byKey.putIfAbsent(v.getVehicleTypeIdentifier(), v);
            }
            size = max + 1;
            representatives = Collections.unmodifiableList(new ArrayList<>(byKey.values()));
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
                }
                classOfVehicle.put(v, c);
            }
            representatives = Collections.unmodifiableList(reps);
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
