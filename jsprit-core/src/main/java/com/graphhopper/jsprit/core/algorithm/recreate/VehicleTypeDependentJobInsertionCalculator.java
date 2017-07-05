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
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


final class VehicleTypeDependentJobInsertionCalculator implements JobInsertionCostsCalculator {

    private Logger logger = LoggerFactory.getLogger(VehicleTypeDependentJobInsertionCalculator.class);

    private final VehicleFleetManager fleetManager;

    private final JobInsertionCostsCalculator insertionCalculator;

    private final VehicleRoutingProblem vrp;

    private Set<String> initialVehicleIds = new HashSet<String>();

    /**
     * true if a vehicle(-type) is allowed to take over the whole route that was previously served by another vehicle
     * <p>
     * <p>vehicleSwitch allowed makes sense if fleet consists of vehicles with different capacities such that one
     * can start with a small vehicle, but as the number of customers grows bigger vehicles can be operated, i.e.
     * bigger vehicles can take over the route that was previously served by a small vehicle.
     */
    private boolean vehicleSwitchAllowed = false;

    public VehicleTypeDependentJobInsertionCalculator(final VehicleRoutingProblem vrp, final VehicleFleetManager fleetManager, final JobInsertionCostsCalculator jobInsertionCalc) {
        this.fleetManager = fleetManager;
        this.insertionCalculator = jobInsertionCalc;
        this.vrp = vrp;
        getInitialVehicleIds();
        logger.debug("initialise " + this);
    }

    private void getInitialVehicleIds() {
        Collection<VehicleRoute> initialVehicleRoutes = vrp.getInitialVehicleRoutes();
        for (VehicleRoute initialRoute : initialVehicleRoutes) {
            initialVehicleIds.add(initialRoute.getVehicle().getId());
        }
    }

    @Override
    public String toString() {
        return "[name=vehicleTypeDependentServiceInsertion]";
    }

    /**
     * @return the vehicleSwitchAllowed
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean isVehicleSwitchAllowed() {
        return vehicleSwitchAllowed;
    }

    /**
     * default is true
     *
     * @param vehicleSwitchAllowed the vehicleSwitchAllowed to set
     */
    public void setVehicleSwitchAllowed(boolean vehicleSwitchAllowed) {
        logger.debug("set vehicleSwitchAllowed to " + vehicleSwitchAllowed);
        this.vehicleSwitchAllowed = vehicleSwitchAllowed;
    }

    public InsertionData getInsertionData(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle vehicle, double newVehicleDepartureTime, final Driver driver, final double bestKnownCost) {
        if(vehicle != null){
            return insertionCalculator.getInsertionData(currentRoute, jobToInsert, vehicle, newVehicleDepartureTime, driver, bestKnownCost);
        }
        Vehicle selectedVehicle = currentRoute.getVehicle();
        Driver selectedDriver = currentRoute.getDriver();
        InsertionData bestIData = new InsertionData.NoInsertionFound();
        double bestKnownCost_ = bestKnownCost;
        Collection<Vehicle> relevantVehicles = new ArrayList<Vehicle>();
        if (!(selectedVehicle instanceof VehicleImpl.NoVehicle)) {
            relevantVehicles.add(selectedVehicle);
            if (vehicleSwitchAllowed && !isVehicleWithInitialRoute(selectedVehicle)) {
                relevantVehicles.addAll(fleetManager.getAvailableVehicles(selectedVehicle));
            }
        } else { //if no vehicle has been assigned, i.e. it is an empty route
            relevantVehicles.addAll(fleetManager.getAvailableVehicles());
        }
        for (Vehicle v : relevantVehicles) {
            double depTime;
            if (v == selectedVehicle) depTime = currentRoute.getDepartureTime();
            else depTime = v.getEarliestDeparture();
            InsertionData iData = insertionCalculator.getInsertionData(currentRoute, jobToInsert, v, depTime, selectedDriver, bestKnownCost_);
            if (iData instanceof InsertionData.NoInsertionFound) {
                bestIData.getFailedConstraintNames().addAll(iData.getFailedConstraintNames());
                continue;
            }
            if (iData.getInsertionCost() < bestKnownCost_) {
                bestIData = iData;
                bestKnownCost_ = iData.getInsertionCost();
            }
        }
        return bestIData;
    }

    VehicleFleetManager getFleetManager(){
        return fleetManager;
    }

    private boolean isVehicleWithInitialRoute(Vehicle selectedVehicle) {
        return initialVehicleIds.contains(selectedVehicle.getId());
    }

}
