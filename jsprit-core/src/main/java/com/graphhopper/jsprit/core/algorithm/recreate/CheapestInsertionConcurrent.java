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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Concurrent version of {@link CheapestInsertion}.
 * <p>
 * This implementation parallelizes the evaluation of jobs across multiple threads,
 * significantly improving performance on multi-core machines.
 * </p>
 * <p>
 * Each iteration evaluates all remaining jobs in parallel, then selects the globally
 * cheapest (job, position) pair and inserts it.
 * </p>
 *
 * @author schroeder
 * @see CheapestInsertion
 */
public final class CheapestInsertionConcurrent extends AbstractInsertionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CheapestInsertionConcurrent.class);

    private final JobInsertionCostsCalculator insertionCostsCalculator;
    private final ExecutorService executorService;

    /**
     * Holds the best insertion found for a job.
     */
    private static class JobInsertion {
        final Job job;
        final VehicleRoute route;
        final InsertionData insertionData;
        final boolean isNewRoute;
        final List<String> failedConstraintNames;

        JobInsertion(Job job, VehicleRoute route, InsertionData insertionData, boolean isNewRoute) {
            this.job = job;
            this.route = route;
            this.insertionData = insertionData;
            this.isNewRoute = isNewRoute;
            this.failedConstraintNames = new ArrayList<>();
        }

        // Constructor for failed insertion
        JobInsertion(Job job, List<String> failedConstraintNames) {
            this.job = job;
            this.route = null;
            this.insertionData = null;
            this.isNewRoute = false;
            this.failedConstraintNames = failedConstraintNames;
        }

        double getCost() {
            return insertionData != null ? insertionData.getInsertionCost() : Double.MAX_VALUE;
        }

        boolean isFeasible() {
            return insertionData != null && !(insertionData instanceof InsertionData.NoInsertionFound);
        }
    }

    public CheapestInsertionConcurrent(JobInsertionCostsCalculator insertionCostsCalculator,
                                       ExecutorService executorService,
                                       VehicleRoutingProblem vrp) {
        super(vrp);
        this.insertionCostsCalculator = insertionCostsCalculator;
        this.executorService = executorService;
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=cheapestInsertionConcurrent]";
    }

    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<>();
        Set<Job> remainingJobs = new LinkedHashSet<>(unassignedJobs);

        while (!remainingJobs.isEmpty()) {
            // Find the globally best insertion across ALL remaining jobs (in parallel)
            JobInsertion bestGlobal = findGloballyBestInsertionConcurrent(remainingJobs, vehicleRoutes);

            if (bestGlobal == null || !bestGlobal.isFeasible()) {
                // No feasible insertion found for any remaining job
                for (Job job : remainingJobs) {
                    badJobs.add(job);
                    // Find failed constraints for this job
                    JobInsertion jobResult = findBestInsertionForJob(job, vehicleRoutes);
                    markUnassigned(job, jobResult.failedConstraintNames);
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
     * Finds the globally best insertion across all jobs and all routes using parallel execution.
     */
    private JobInsertion findGloballyBestInsertionConcurrent(Set<Job> jobs, Collection<VehicleRoute> routes) {
        // Create tasks for parallel evaluation - one task per job
        List<Callable<JobInsertion>> tasks = new ArrayList<>(jobs.size());
        List<VehicleRoute> routeList = new ArrayList<>(routes);

        for (Job job : jobs) {
            tasks.add(() -> findBestInsertionForJobInternal(job, routeList));
        }

        JobInsertion globalBest = null;
        double globalBestCost = Double.MAX_VALUE;

        try {
            List<Future<JobInsertion>> futures = executorService.invokeAll(tasks);

            for (Future<JobInsertion> future : futures) {
                JobInsertion insertion = future.get();
                if (insertion.isFeasible() && insertion.getCost() < globalBestCost) {
                    globalBest = insertion;
                    globalBestCost = insertion.getCost();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while finding best insertion", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error during parallel insertion evaluation", e);
        }

        return globalBest;
    }

    /**
     * Finds the best insertion for a single job across all routes (called in parallel).
     */
    private JobInsertion findBestInsertionForJobInternal(Job job, List<VehicleRoute> routes) {
        JobInsertion best = null;
        double bestCost = Double.MAX_VALUE;
        List<String> failedConstraints = new ArrayList<>();

        // Evaluate insertion into existing routes
        for (VehicleRoute route : routes) {
            InsertionData iData = insertionCostsCalculator.getInsertionData(
                    route, job, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestCost);

            if (iData instanceof InsertionData.NoInsertionFound) {
                failedConstraints.addAll(iData.getFailedConstraintNames());
            } else if (iData.getInsertionCost() < bestCost) {
                best = new JobInsertion(job, route, iData, false);
                bestCost = iData.getInsertionCost();
            }
        }

        // Evaluate insertion into a new route
        VehicleRoute newRoute = VehicleRoute.emptyRoute();
        InsertionData newRouteData = insertionCostsCalculator.getInsertionData(
                newRoute, job, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestCost);

        if (!(newRouteData instanceof InsertionData.NoInsertionFound)) {
            if (newRouteData.getInsertionCost() < bestCost) {
                best = new JobInsertion(job, newRoute, newRouteData, true);
            }
        } else {
            failedConstraints.addAll(newRouteData.getFailedConstraintNames());
        }

        if (best == null) {
            return new JobInsertion(job, failedConstraints);
        }

        return best;
    }

    /**
     * Finds the best insertion for a single job (used for collecting failed constraint names).
     */
    private JobInsertion findBestInsertionForJob(Job job, Collection<VehicleRoute> routes) {
        return findBestInsertionForJobInternal(job, new ArrayList<>(routes));
    }
}
