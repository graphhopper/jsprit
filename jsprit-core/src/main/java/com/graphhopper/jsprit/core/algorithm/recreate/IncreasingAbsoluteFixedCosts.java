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

import com.graphhopper.jsprit.core.problem.constraint.SoftRouteConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class IncreasingAbsoluteFixedCosts extends SolutionCompletenessRatio implements SoftRouteConstraint {

    private static final Logger logger = LoggerFactory.getLogger(IncreasingAbsoluteFixedCosts.class);

    private double weightDeltaFixCost = 0.5;

    public IncreasingAbsoluteFixedCosts(int noJobs) {
        super(noJobs);
        logger.debug("initialise {}", this);
    }


    public void setWeightOfFixCost(double weight) {
        weightDeltaFixCost = weight;
        logger.debug("set weightOfFixCostSaving to {}", weight);
    }

    @Override
    public String toString() {
        return "[name=IncreasingAbsoluteFixedCosts][weightOfFixedCostSavings=" + weightDeltaFixCost + "]";
    }

    @Override
    public double getCosts(JobInsertionContext insertionContext) {
        final VehicleRoute currentRoute = insertionContext.getRoute();
        double currentFix = 0d;
        if (currentRoute.getVehicle() != null && !(currentRoute.getVehicle() instanceof VehicleImpl.NoVehicle)) {
            currentFix = currentRoute.getVehicle().getType().getVehicleCostParams().fix;
        }
        double increasingAbsoluteFixedCosts = solutionCompletenessRatio * (insertionContext.getNewVehicle().getType().getVehicleCostParams().fix - currentFix);
        return weightDeltaFixCost * solutionCompletenessRatio * increasingAbsoluteFixedCosts;
    }

}
