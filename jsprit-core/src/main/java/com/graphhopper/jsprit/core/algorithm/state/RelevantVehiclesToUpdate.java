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

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.VehicleSwitchedListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The vehicles whose vehicle-dependent states an insertion can ask about for a route: the route's own vehicle and one
 * vehicle per vehicle type that still has an unlocked vehicle (the candidates of a vehicle switch, see
 * VehicleTypeDependentJobInsertionCalculator and VehicleFleetManager.getAvailableVehicles). Updating only these instead of
 * every vehicle type of the fleet cuts the state update of a route from O(activities x types) to O(activities x (1 + free
 * types)); with a fleet in full use that is the route's own vehicle alone.
 * <p>
 * Which types are free is counted from the routes handed over at insertion start (what ResetAndIniFleetManager locks),
 * so it does not depend on listener order. Inserting into a new route or switching a route to another vehicle takes a
 * vehicle out of the free pool, which only shrinks the set that has to be current. A switch also releases the route's
 * old vehicle: if its type had no free vehicle before, the other routes have no states for it yet, and the registered
 * {@link ReleaseListener}s bring them up to date for exactly that vehicle. With an infinite fleet every type is always
 * free and all types are returned, as before.
 * <p>
 * {@link #of(StateManager)} registers one instance per state manager (as a state updater, which makes it an insertion
 * listener there); the updaters hand it over with their setVehiclesToUpdate and recompute a released vehicle on request.
 */
public class RelevantVehiclesToUpdate implements UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate, StateUpdater, InsertionStartsListener, VehicleSwitchedListener {

    /** called when a vehicle's type becomes available again during a recreate; the switched route itself is updated by the job insertion that follows */
    public interface ReleaseListener {
        void vehicleReleased(Vehicle vehicle, Collection<VehicleRoute> routes, VehicleRoute switchedRoute);
    }

    private final VehicleRoutingProblem vrp;
    private final boolean infinite;
    /** one vehicle per type key index, null where the fleet has none */
    private final Vehicle[] representative;
    private final int[] typeCount;
    private final int[] usedCount;
    private final List<ReleaseListener> releaseListeners = new ArrayList<>();
    private Collection<VehicleRoute> routes;

    public RelevantVehiclesToUpdate(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
        this.infinite = vrp.getFleetSize() == VehicleRoutingProblem.FleetSize.INFINITE;
        int types = 0;
        for (Vehicle v : vrp.getVehicles()) types = Math.max(types, typeIndex(v));
        representative = new Vehicle[types + 1];
        typeCount = new int[types + 1];
        usedCount = new int[types + 1];
        for (Vehicle v : vrp.getVehicles()) {
            int t = typeIndex(v);
            if (representative[t] == null) representative[t] = v;
            typeCount[t]++;
        }
    }

    public void addReleaseListener(ReleaseListener listener) {
        releaseListeners.add(listener);
    }

    @SuppressWarnings("deprecation")
    private int typeIndex(Vehicle vehicle) {
        return vrp.getVehicleTypeKeyIndex(vehicle.getVehicleTypeIdentifier());
    }

    private boolean known(Vehicle vehicle) {
        if (vehicle == null || vehicle instanceof VehicleImpl.NoVehicle) return false;
        int t = typeIndex(vehicle);
        return t >= 0 && t < typeCount.length && typeCount[t] > 0;
    }

    private boolean free(int t) {
        return representative[t] != null && (infinite || usedCount[t] < typeCount[t]);
    }

    /** the instance registered with the state manager, created and registered on first use so that every updater shares one count */
    public static RelevantVehiclesToUpdate of(StateManager stateManager) {
        for (StateUpdater u : stateManager.getStateUpdaters()) {
            if (u instanceof RelevantVehiclesToUpdate) return (RelevantVehiclesToUpdate) u;
        }
        RelevantVehiclesToUpdate relevant = new RelevantVehiclesToUpdate(stateManager.getVrp());
        stateManager.addStateUpdater(relevant);
        return relevant;
    }

    /** a new list per call: activity visitors hold it from begin() to finish() while other visitors ask for theirs */
    @Override
    public Collection<Vehicle> get(VehicleRoute route) {
        List<Vehicle> result = new ArrayList<>(4);
        Vehicle own = route.getVehicle();
        result.add(own);
        int ownType = known(own) ? typeIndex(own) : -1;
        for (int t = 0; t < representative.length; t++) {
            if (t == ownType || representative[t] == null) continue;
            if (free(t)) result.add(representative[t]);
        }
        return result;
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        routes = vehicleRoutes;
        java.util.Arrays.fill(usedCount, 0);
        for (VehicleRoute route : vehicleRoutes) {
            if (known(route.getVehicle())) usedCount[typeIndex(route.getVehicle())]++;
        }
    }

    @Override
    public void vehicleSwitched(VehicleRoute vehicleRoute, Vehicle oldVehicle, Vehicle newVehicle) {
        if (known(newVehicle)) usedCount[typeIndex(newVehicle)]++;
        if (!known(oldVehicle) || infinite) return;
        int t = typeIndex(oldVehicle);
        boolean wasFullyUsed = usedCount[t] >= typeCount[t];
        usedCount[t]--;
        if (wasFullyUsed && routes != null) {
            for (ReleaseListener l : releaseListeners) l.vehicleReleased(representative[t], routes, vehicleRoute);
        }
    }
}
