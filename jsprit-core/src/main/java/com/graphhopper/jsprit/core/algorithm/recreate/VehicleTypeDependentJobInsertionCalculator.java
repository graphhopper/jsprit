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

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Orchestrates job insertion by:
 * <ul>
 *   <li>Dispatching to job-type-specific calculators (Service, Shipment, Break)</li>
 *   <li>Iterating over available vehicles when no specific vehicle is provided</li>
 * </ul>
 */
final class VehicleTypeDependentJobInsertionCalculator implements JobInsertionCostsCalculator {

    private static final Logger logger = LoggerFactory.getLogger(VehicleTypeDependentJobInsertionCalculator.class);

    private final VehicleFleetManager fleetManager;
    private final VehicleRoutingProblem vrp;
    private final Map<Class<? extends Job>, JobInsertionCostsCalculator> calculators;
    private final Set<String> initialVehicleIds;

    /**
     * If true, a vehicle(-type) is allowed to take over a route previously served by another vehicle.
     * This makes sense when the fleet has vehicles with different capacities.
     */
    private boolean vehicleSwitchAllowed = false;

    public VehicleTypeDependentJobInsertionCalculator(
            VehicleRoutingProblem vrp,
            VehicleFleetManager fleetManager,
            Map<Class<? extends Job>, JobInsertionCostsCalculator> calculators) {
        this.vrp = vrp;
        this.fleetManager = fleetManager;
        this.calculators = calculators;
        this.initialVehicleIds = collectInitialVehicleIds();
        logger.debug("initialise {}", this);
    }

    private Set<String> collectInitialVehicleIds() {
        Set<String> ids = new HashSet<>();
        for (VehicleRoute route : vrp.getInitialVehicleRoutes()) {
            ids.add(route.getVehicle().getId());
        }
        return ids;
    }

    @Override
    public String toString() {
        return "[name=vehicleTypeDependentJobInsertion]";
    }

    public boolean isVehicleSwitchAllowed() {
        return vehicleSwitchAllowed;
    }

    public void setVehicleSwitchAllowed(boolean vehicleSwitchAllowed) {
        logger.debug("set vehicleSwitchAllowed to {}", vehicleSwitchAllowed);
        this.vehicleSwitchAllowed = vehicleSwitchAllowed;
    }

    @Override
    public InsertionData getInsertionData(VehicleRoute currentRoute, Job jobToInsert,
            Vehicle vehicle, double newVehicleDepartureTime, Driver driver, double bestKnownCost) {

        JobInsertionCostsCalculator calculator = getCalculatorFor(jobToInsert);

        // If specific vehicle provided, delegate directly
        if (vehicle != null) {
            return calculator.getInsertionData(currentRoute, jobToInsert, vehicle,
                    newVehicleDepartureTime, driver, bestKnownCost);
        }

        // Iterate over relevant vehicles, find best insertion
        Driver selectedDriver = currentRoute.getDriver();
        InsertionData bestIData = new InsertionData.NoInsertionFound();
        double bestCost = bestKnownCost;

        for (Vehicle v : getRelevantVehicles(currentRoute)) {
            double depTime = getDepartureTime(currentRoute, v);
            InsertionData iData = calculator.getInsertionData(currentRoute, jobToInsert,
                    v, depTime, selectedDriver, bestCost);

            if (!iData.isFound()) {
                bestIData.getFailedConstraintNames().addAll(iData.getFailedConstraintNames());
                continue;
            }
            if (iData.getInsertionCost() < bestCost) {
                bestIData = iData;
                bestCost = iData.getInsertionCost();
            }
        }
        return bestIData;
    }

    @Override
    public List<InsertionData> getAllInsertionPositions(VehicleRoute currentRoute, Job jobToInsert,
            Vehicle vehicle, double newVehicleDepartureTime, Driver driver) {

        JobInsertionCostsCalculator calculator = getCalculatorFor(jobToInsert);

        // If specific vehicle provided, delegate directly
        if (vehicle != null) {
            return calculator.getAllInsertionPositions(currentRoute, jobToInsert, vehicle,
                    newVehicleDepartureTime, driver);
        }

        // Iterate over relevant vehicles, collect all positions
        Driver selectedDriver = currentRoute.getDriver();
        List<InsertionData> allPositions = new ArrayList<>();

        for (Vehicle v : getRelevantVehicles(currentRoute)) {
            double depTime = getDepartureTime(currentRoute, v);
            List<InsertionData> positions = calculator.getAllInsertionPositions(currentRoute,
                    jobToInsert, v, depTime, selectedDriver);
            allPositions.addAll(positions);
        }
        return allPositions;
    }

    private JobInsertionCostsCalculator getCalculatorFor(Job job) {
        JobInsertionCostsCalculator calculator = calculators.get(job.getClass());
        if (calculator == null) {
            throw new IllegalStateException("No calculator registered for job type: " + job.getClass().getName());
        }
        return calculator;
    }

    private Collection<Vehicle> getRelevantVehicles(VehicleRoute route) {
        List<Vehicle> relevantVehicles = new ArrayList<>();

        if (route.hasVehicle()) {
            Vehicle currentVehicle = route.getVehicle();
            relevantVehicles.add(currentVehicle);
            if (vehicleSwitchAllowed && !isVehicleWithInitialRoute(currentVehicle)) {
                relevantVehicles.addAll(fleetManager.getAvailableVehicles(currentVehicle));
            }
        } else {
            // Empty route - consider all available vehicles
            relevantVehicles.addAll(fleetManager.getAvailableVehicles());
        }
        return relevantVehicles;
    }

    private double getDepartureTime(VehicleRoute route, Vehicle vehicle) {
        if (vehicle == route.getVehicle()) {
            return route.getDepartureTime();
        }
        return vehicle.getEarliestDeparture();
    }

    private boolean isVehicleWithInitialRoute(Vehicle vehicle) {
        return initialVehicleIds.contains(vehicle.getId());
    }

    VehicleFleetManager getFleetManager() {
        return fleetManager;
    }
}
