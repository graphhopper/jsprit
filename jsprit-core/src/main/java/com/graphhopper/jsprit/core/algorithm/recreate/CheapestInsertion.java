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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * True Best Insertion (Cheapest Insertion) as defined in VRP literature.
 * <p>
 * This algorithm repeatedly:
 * <ol>
 *   <li>Evaluates ALL unassigned jobs at ALL possible positions</li>
 *   <li>Selects the (job, position) pair with the globally minimum insertion cost</li>
 *   <li>Inserts that job</li>
 *   <li>Repeats until all jobs are inserted or no feasible insertion exists</li>
 * </ol>
 * </p>
 * <p>
 * This differs from {@link BestInsertion} which processes jobs in random order,
 * inserting each at its best position (Sequential/Random Order Insertion).
 * </p>
 * <p>
 * Time complexity: O(J² × R × P) where J = jobs, R = routes, P = positions per route.
 * This is more expensive than random order insertion but typically produces better solutions.
 * </p>
 *
 * @author schroeder
 */
public final class CheapestInsertion extends AbstractInsertionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CheapestInsertion.class);

    private final JobInsertionCostsCalculator insertionCostsCalculator;

    /**
     * Holds the best insertion found for a job.
     */
    private static class JobInsertion {
        final Job job;
        final VehicleRoute route;
        final InsertionData insertionData;
        final boolean isNewRoute;

        JobInsertion(Job job, VehicleRoute route, InsertionData insertionData, boolean isNewRoute) {
            this.job = job;
            this.route = route;
            this.insertionData = insertionData;
            this.isNewRoute = isNewRoute;
        }

        double getCost() {
            return insertionData.getInsertionCost();
        }
    }

    public CheapestInsertion(JobInsertionCostsCalculator insertionCostsCalculator, VehicleRoutingProblem vrp) {
        super(vrp);
        this.insertionCostsCalculator = insertionCostsCalculator;
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=cheapestInsertion]";
    }

    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<>();
        Set<Job> remainingJobs = new LinkedHashSet<>(unassignedJobs);

        while (!remainingJobs.isEmpty()) {
            // Find the globally best insertion across ALL remaining jobs
            JobInsertion bestGlobal = findGloballyBestInsertion(remainingJobs, vehicleRoutes);

            if (bestGlobal == null) {
                // No feasible insertion found for any remaining job
                for (Job job : remainingJobs) {
                    badJobs.add(job);
                    InsertionData noInsertion = findBestInsertionForJob(job, vehicleRoutes, null);
                    markUnassigned(job, noInsertion.getFailedConstraintNames());
                }
                break;
            }

            // Add new route to collection if needed
            if (bestGlobal.isNewRoute) {
                vehicleRoutes.add(bestGlobal.route);
            }

            // Insert the job
            insertJob(bestGlobal.job, bestGlobal.insertionData, bestGlobal.route);
            remainingJobs.remove(bestGlobal.job);

            logger.trace("Inserted job {} with cost {} into route {}",
                    bestGlobal.job.getId(),
                    bestGlobal.getCost(),
                    bestGlobal.route.getVehicle().getId());
        }

        return badJobs;
    }

    /**
     * Finds the globally best insertion across all jobs and all routes.
     *
     * @param jobs   the jobs to consider
     * @param routes the existing routes
     * @return the best insertion, or null if no feasible insertion exists
     */
    private JobInsertion findGloballyBestInsertion(Set<Job> jobs, Collection<VehicleRoute> routes) {
        JobInsertion globalBest = null;
        double globalBestCost = Double.MAX_VALUE;

        for (Job job : jobs) {
            // Find best insertion for this job across all existing routes
            for (VehicleRoute route : routes) {
                InsertionData iData = insertionCostsCalculator.getInsertionData(
                        route, job, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, globalBestCost);

                if (!(iData instanceof InsertionData.NoInsertionFound)) {
                    if (iData.getInsertionCost() < globalBestCost) {
                        globalBest = new JobInsertion(job, route, iData, false);
                        globalBestCost = iData.getInsertionCost();
                    }
                }
            }

            // Also try inserting into a new route
            VehicleRoute newRoute = VehicleRoute.emptyRoute();
            InsertionData newRouteData = insertionCostsCalculator.getInsertionData(
                    newRoute, job, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, globalBestCost);

            if (!(newRouteData instanceof InsertionData.NoInsertionFound)) {
                if (newRouteData.getInsertionCost() < globalBestCost) {
                    globalBest = new JobInsertion(job, newRoute, newRouteData, true);
                    globalBestCost = newRouteData.getInsertionCost();
                }
            }
        }

        return globalBest;
    }

    /**
     * Finds the best insertion for a single job (used for collecting failed constraint names).
     */
    private InsertionData findBestInsertionForJob(Job job, Collection<VehicleRoute> routes, Double benchmark) {
        InsertionData best = new InsertionData.NoInsertionFound();
        double bestCost = benchmark != null ? benchmark : Double.MAX_VALUE;

        for (VehicleRoute route : routes) {
            InsertionData iData = insertionCostsCalculator.getInsertionData(
                    route, job, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestCost);

            if (iData instanceof InsertionData.NoInsertionFound) {
                best.getFailedConstraintNames().addAll(iData.getFailedConstraintNames());
            } else if (iData.getInsertionCost() < bestCost) {
                best = iData;
                bestCost = iData.getInsertionCost();
            }
        }

        // Try new route
        VehicleRoute newRoute = VehicleRoute.emptyRoute();
        InsertionData newRouteData = insertionCostsCalculator.getInsertionData(
                newRoute, job, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestCost);

        if (newRouteData instanceof InsertionData.NoInsertionFound) {
            best.getFailedConstraintNames().addAll(newRouteData.getFailedConstraintNames());
        } else if (newRouteData.getInsertionCost() < bestCost) {
            best = newRouteData;
        }

        return best;
    }
}
