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

import java.util.Collection;

/**
 * Offers the vehicles an insertion may use: per class of interchangeable vehicles one that is free.
 * <p>
 * <b>Classes.</b> A {@link VehicleClasses} partition of the fleet (default
 * {@link VehicleClasses#fleetDefault(Collection)}): two vehicles belong to one class only if no constraint, cost
 * function or state updater can tell them apart.
 * <p>
 * <b>Vehicles in use.</b> There is no lock / unlock: the fleet manager reads from the routes which vehicles are
 * in use. It is told about routes by the {@link com.graphhopper.jsprit.core.algorithm.state.StateManager}, with
 * the calls every insertion path makes anyway to keep the route states right:
 * <ul>
 * <li>{@code informInsertionStarts(routes, ..)} -> {@link #reset(Collection)}: the routes are the solution</li>
 * <li>{@code informJobInserted(.., route, ..)} and {@code reCalculateStates(route)} -> {@link #sync(VehicleRoute)}</li>
 * </ul>
 * The two are joined where an insertion calculator is built from a fleet manager and a state manager
 * ({@code JobInsertionCostsCalculatorBuilder}). All three updates are idempotent and independent of the order
 * they are called in.
 */
public interface VehicleFleetManager {

    /**
     * One free vehicle per class: the free vehicle that comes first in the fleet. Classes without a free
     * vehicle are left out.
     */
    Collection<Vehicle> getAvailableVehicles();

    /**
     * As {@link #getAvailableVehicles()}, without the class of the given vehicle.
     */
    Collection<Vehicle> getAvailableVehicles(Vehicle withoutThisClass);

    /**
     * The first free vehicle of the class of the given vehicle, or null if all vehicles of the class are in use.
     */
    Vehicle getAvailableVehicle(Vehicle sameClassAs);

    /**
     * @return true if a route has this vehicle
     */
    boolean isInUse(Vehicle vehicle);

    /**
     * The given routes are the solution: their vehicles are in use, every other vehicle is free.
     */
    void reset(Collection<VehicleRoute> routes);

    /**
     * The route may have changed its vehicle: the vehicle it had when it was last seen is free, the one it has
     * now is in use. A route that has not been seen before is added.
     */
    void sync(VehicleRoute route);

    /**
     * The route is no longer part of the solution: its vehicle is free.
     */
    void release(VehicleRoute route);

    /**
     * Verification mode only (system property {@code jsprit.states.failOnMissing}): throws if the route has a
     * vehicle that is not recorded as used by this route, i.e. the route got its vehicle without one of the
     * updates above. No-op otherwise.
     */
    void verify(VehicleRoute route);
}
