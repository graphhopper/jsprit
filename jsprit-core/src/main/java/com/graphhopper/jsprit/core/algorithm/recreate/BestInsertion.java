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
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.util.NoiseMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Best insertion that insert the job where additional costs are minimal.
 *
 * @author stefan schroeder
 */
public final class BestInsertion extends AbstractInsertionStrategy {

    private static Logger logger = LoggerFactory.getLogger(BestInsertion.class);

    private JobInsertionCostsCalculator bestInsertionCostCalculator;

    private NoiseMaker noiseMaker = new NoiseMaker() {

        @Override
        public double makeNoise() {
            return 0;
        }

    };

    public BestInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem) {
        super(vehicleRoutingProblem);
        bestInsertionCostCalculator = jobInsertionCalculator;
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=bestInsertion]";
    }

    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<Job>(unassignedJobs.size());
        List<Job> unassignedJobList = new ArrayList<Job>(unassignedJobs);
        Collections.shuffle(unassignedJobList, random);
        Collections.sort(unassignedJobList, new AccordingToPriorities());
        for (Job unassignedJob : unassignedJobList) {
            Insertion bestInsertion = null;
            InsertionData empty = new InsertionData.NoInsertionFound();
            double bestInsertionCost = Double.MAX_VALUE;
            for (VehicleRoute vehicleRoute : vehicleRoutes) {
                InsertionData iData = bestInsertionCostCalculator.getInsertionData(vehicleRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost);
                if (iData instanceof InsertionData.NoInsertionFound) {
                    empty.getFailedConstraintNames().addAll(iData.getFailedConstraintNames());
                    continue;
                }
                if (iData.getInsertionCost() < bestInsertionCost + noiseMaker.makeNoise()) {
                    bestInsertion = new Insertion(vehicleRoute, iData);
                    bestInsertionCost = iData.getInsertionCost();
                }
            }
            VehicleRoute newRoute = VehicleRoute.emptyRoute();
            InsertionData newIData = bestInsertionCostCalculator.getInsertionData(newRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost);
            if (!(newIData instanceof InsertionData.NoInsertionFound)) {
                if (newIData.getInsertionCost() < bestInsertionCost + noiseMaker.makeNoise()) {
                    bestInsertion = new Insertion(newRoute, newIData);
                    vehicleRoutes.add(newRoute);
                }
            } else {
                empty.getFailedConstraintNames().addAll(newIData.getFailedConstraintNames());
            }
            if (bestInsertion == null) {
                badJobs.add(unassignedJob);
                markUnassigned(unassignedJob, empty.getFailedConstraintNames());
            }
            else insertJob(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
        }
        return badJobs;
    }

}
