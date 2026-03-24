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
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.Collections;
import java.util.List;


public interface JobInsertionCostsCalculator {

    InsertionData getInsertionData(VehicleRoute currentRoute, Job newJob, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownCosts);

    /**
     * Returns all feasible insertion positions for a job in a route, considering all available vehicles.
     *
     * <p>This is the preferred entry point for position-based regret calculation.
     * It iterates over all available vehicles and collects all feasible positions.</p>
     *
     * @param currentRoute the route to evaluate
     * @param newJob the job to insert
     * @return list of all feasible insertion positions across all vehicles, empty if none feasible
     */
    default List<InsertionData> getAllInsertionPositions(VehicleRoute currentRoute, Job newJob) {
        return getAllInsertionPositions(currentRoute, newJob, null, 0.0, null);
    }

    /**
     * Returns all feasible insertion positions for a job in a route with specific vehicle.
     *
     * <p>Unlike {@link #getInsertionData} which returns only the best position,
     * this returns ALL feasible positions for position-based regret calculation.</p>
     *
     * <p>Position-based regret (ranked #1 in Voigt 2025) compares individual positions
     * across all routes, not just the best position per route.</p>
     *
     * @param currentRoute the route to evaluate
     * @param newJob the job to insert
     * @param newVehicle the vehicle (or null to iterate available vehicles)
     * @param newVehicleDepartureTime departure time
     * @param newDriver the driver
     * @return list of all feasible insertion positions, empty if none feasible
     */
    default List<InsertionData> getAllInsertionPositions(VehicleRoute currentRoute, Job newJob,
            Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver) {
        InsertionData best = getInsertionData(currentRoute, newJob, newVehicle, newVehicleDepartureTime, newDriver, Double.MAX_VALUE);
        if (!best.isFound()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(best);
    }

}
