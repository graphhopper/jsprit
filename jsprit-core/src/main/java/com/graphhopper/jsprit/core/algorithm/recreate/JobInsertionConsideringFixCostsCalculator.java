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

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.constraint.SoftRouteConstraint;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class JobInsertionConsideringFixCostsCalculator implements JobInsertionCostsCalculator, SoftRouteConstraint {

    private static final Logger logger = LoggerFactory.getLogger(JobInsertionConsideringFixCostsCalculator.class);

    private final JobInsertionCostsCalculator standardInsertion;

    private double weightDeltaFixCost = 0.5;

    private double solutionCompletenessRatio = 0.5;

    private RouteAndActivityStateGetter stateGetter;

    public JobInsertionConsideringFixCostsCalculator(final JobInsertionCostsCalculator standardCalculator, RouteAndActivityStateGetter stateGetter) {
        super();
        this.standardInsertion = standardCalculator;
        this.stateGetter = stateGetter;
        logger.debug("initialise {}", this);
    }

    @Override
    public InsertionData getInsertionData(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double bestKnownPrice) {
        double fixedCostContribution = getFixCostContribution(currentRoute, jobToInsert, newVehicle);
        if (fixedCostContribution > bestKnownPrice) {
            return InsertionData.createEmptyInsertionData();
        }
        InsertionData iData = standardInsertion.getInsertionData(currentRoute, jobToInsert, newVehicle, newVehicleDepartureTime, newDriver, bestKnownPrice);
        if (iData instanceof InsertionData.NoInsertionFound) {
            return iData;
        }
        double totalInsertionCost = iData.getInsertionCost() + fixedCostContribution;
        InsertionData insertionData = new InsertionData(totalInsertionCost, iData.getPickupInsertionIndex(), iData.getDeliveryInsertionIndex(), newVehicle, newDriver);
        insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
        insertionData.getEvents().addAll(iData.getEvents());
        return insertionData;
    }

    private double getFixCostContribution(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle) {
        Capacity currentMaxLoadInRoute = getCurrentMaxLoadInRoute(currentRoute);
        double relFixCost = getDeltaRelativeFixCost(currentRoute, newVehicle, jobToInsert,currentMaxLoadInRoute);
        double absFixCost = getDeltaAbsoluteFixCost(currentRoute, newVehicle);
        double deltaFixCost = (1 - solutionCompletenessRatio) * relFixCost + solutionCompletenessRatio * absFixCost;
        return weightDeltaFixCost * solutionCompletenessRatio * deltaFixCost;
    }

    public void setWeightOfFixCost(double weight) {
        weightDeltaFixCost = weight;
        logger.debug("set weightOfFixCostSaving to {}", weight);
    }

    @Override
    public String toString() {
        return "[name=calculatesServiceInsertionConsideringFixCost][weightOfFixedCostSavings=" + weightDeltaFixCost + "]";
    }

    public void setSolutionCompletenessRatio(double ratio) {
        solutionCompletenessRatio = ratio;
    }

    public double getSolutionCompletenessRatio() { return solutionCompletenessRatio; }

    private double getDeltaAbsoluteFixCost(VehicleRoute route, Vehicle newVehicle) {
        double currentFix = 0d;
        if (route.getVehicle() != null && !(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
            currentFix = route.getVehicle().getType().getVehicleCostParams().fix;
        }
        return newVehicle.getType().getVehicleCostParams().fix - currentFix;
    }

    private double getDeltaRelativeFixCost(VehicleRoute route, Vehicle newVehicle, Job job, Capacity currentLoad) {
        Capacity load = Capacity.addup(currentLoad, job.getSize());
        double currentRelFix = 0d;
        if (route.getVehicle() != null && !(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
            currentRelFix = route.getVehicle().getType().getVehicleCostParams().fix * Capacity.divide(currentLoad, route.getVehicle().getType().getCapacityDimensions());
        }
        return newVehicle.getType().getVehicleCostParams().fix * (Capacity.divide(load, newVehicle.getType().getCapacityDimensions())) - currentRelFix;
    }

    private Capacity getCurrentMaxLoadInRoute(VehicleRoute route) {
        Capacity maxLoad = stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class);
        if (maxLoad == null) maxLoad = Capacity.Builder.newInstance().build();
        return maxLoad;
    }

    @Override
    public double getCosts(JobInsertionContext insertionContext) {
        return getFixCostContribution(insertionContext.getRoute(), insertionContext.getJob(), insertionContext.getNewVehicle());
    }

}
