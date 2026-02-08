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

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


/**
 * @author stefan schroeder
 */

public final class BestInsertionConcurrent extends AbstractInsertionStrategy {

    static class Insertion {

        private final VehicleRoute route;

        private final InsertionData insertionData;

        public Insertion(VehicleRoute vehicleRoute, InsertionData insertionData) {
            super();
            this.route = vehicleRoute;
            this.insertionData = insertionData;
        }

        public VehicleRoute getRoute() {
            return route;
        }

        public InsertionData getInsertionData() {
            return insertionData;
        }

    }

    private final static Logger logger = LoggerFactory.getLogger(BestInsertionConcurrent.class);

    private final static double NO_NEW_DEPARTURE_TIME_YET = -12345.12345;

    private final static Vehicle NO_NEW_VEHICLE_YET = null;

    private final static Driver NO_NEW_DRIVER_YET = null;

    private final JobInsertionCostsCalculator bestInsertionCostCalculator;

    private final ExecutorService executorService;

    /**
     * @deprecated use {@link #BestInsertionConcurrent(JobInsertionCostsCalculator, ExecutorService, VehicleRoutingProblem)} instead.
     * The nuOfBatches parameter is unused.
     */
    @Deprecated
    public BestInsertionConcurrent(JobInsertionCostsCalculator jobInsertionCalculator, ExecutorService executorService, int nuOfBatches, VehicleRoutingProblem vehicleRoutingProblem) {
        this(jobInsertionCalculator, executorService, vehicleRoutingProblem);
    }

    public BestInsertionConcurrent(JobInsertionCostsCalculator jobInsertionCalculator, ExecutorService executorService, VehicleRoutingProblem vehicleRoutingProblem) {
        super(vehicleRoutingProblem);
        bestInsertionCostCalculator = jobInsertionCalculator;
        this.executorService = executorService;
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=bestInsertion]";
    }

    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<>(unassignedJobs.size());
        List<Job> unassignedJobList = new ArrayList<>(unassignedJobs);
        Collections.shuffle(unassignedJobList, random);
        unassignedJobList.sort(new AccordingToPriorities());
        List<Callable<Insertion>> tasks = new ArrayList<>();
        for (final Job unassignedJob : unassignedJobList) {
            List<String> failedConstraintNames = new ArrayList<>();
            Insertion bestInsertion = null;
            double bestInsertionCost = Double.MAX_VALUE;
            tasks.clear();
            for (VehicleRoute route : vehicleRoutes) {
                tasks.add(() -> getBestInsertion(route, unassignedJob));
            }
            try {
                List<Future<Insertion>> futureResponses = executorService.invokeAll(tasks);
                for (Future<Insertion> futureResponse : futureResponses) {
                    Insertion insertion = futureResponse.get();
                    if (insertion.insertionData instanceof NoInsertionFound) {
                        failedConstraintNames.addAll(insertion.getInsertionData().getFailedConstraintNames());
                        continue;
                    }
                    if (insertion.getInsertionData().getInsertionCost() < bestInsertionCost) {
                        bestInsertion = insertion;
                        bestInsertionCost = insertion.getInsertionData().getInsertionCost();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            VehicleRoute newRoute = VehicleRoute.emptyRoute();
            InsertionData newIData = bestInsertionCostCalculator.getInsertionData(newRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost);
            if (newIData.getInsertionCost() < bestInsertionCost) {
                bestInsertion = new Insertion(newRoute, newIData);
                vehicleRoutes.add(newRoute);
            } else if (newIData instanceof NoInsertionFound) {
                failedConstraintNames.addAll(newIData.getFailedConstraintNames());
            }
            if (bestInsertion == null) {
                badJobs.add(unassignedJob);
                markUnassigned(unassignedJob, failedConstraintNames);
            } else {
                insertJob(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
            }
        }
        return badJobs;
    }


    private Insertion getBestInsertion(VehicleRoute vehicleRoute, Job unassignedJob) {
        InsertionData empty = new InsertionData.NoInsertionFound();
        InsertionData iData = bestInsertionCostCalculator.getInsertionData(vehicleRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
        if (iData instanceof NoInsertionFound) {
            empty.getFailedConstraintNames().addAll(iData.getFailedConstraintNames());
            return new Insertion(null, empty);
        } else {
            return new Insertion(vehicleRoute, iData);
        }
    }


}
