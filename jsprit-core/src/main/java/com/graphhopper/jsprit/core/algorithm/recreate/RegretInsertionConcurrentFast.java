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
import com.graphhopper.jsprit.core.problem.constraint.DependencyType;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Insertion based on regret approach.
 * <p>
 * <p>Basically calculates the insertion cost of the firstBest and the secondBest alternative. The score is then calculated as difference
 * between secondBest and firstBest, plus additional scoring variables that can defined in this.ScoringFunction.
 * The idea is that if the cost of the secondBest alternative is way higher than the first best, it seems to be important to insert this
 * customer immediatedly. If difference is not that high, it might not impact solution if this customer is inserted later.
 *
 * @author stefan schroeder
 */
public class RegretInsertionConcurrentFast extends AbstractInsertionStrategy {


    private static final Logger logger = LoggerFactory.getLogger(RegretInsertionConcurrentFast.class);

    private RegretScoringFunction regretScoringFunction;

    private RegretKScoringFunction regretKScoringFunction;

    private int regretK = 2;

    private final JobInsertionCostsCalculator insertionCostsCalculator;

    private final ExecutorService executor;

    private final VehicleFleetManager fleetManager;

    private final Set<String> initialVehicleIds;

    private boolean switchAllowed = true;

    private DependencyType[] dependencyTypes = null;

    private AdaptiveSpatialFilter spatialFilter = null;


    /**
     * Sets the scoring function.
     * <p>
     * <p>By default, the this.TimeWindowScorer is used.
     *
     * @param scoringFunction to score
     */
    public void setScoringFunction(ScoringFunction scoringFunction) {
        this.regretScoringFunction = new DefaultRegretScoringFunction(scoringFunction);
    }

    public void setRegretScoringFunction(RegretScoringFunction regretScoringFunction) {
        this.regretScoringFunction = regretScoringFunction;
    }

    public void setRegretKScoringFunction(RegretKScoringFunction regretKScoringFunction) {
        this.regretKScoringFunction = regretKScoringFunction;
    }

    public void setRegretK(int k) {
        this.regretK = k;
    }

    public RegretInsertionConcurrentFast(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem, ExecutorService executorService, VehicleFleetManager fleetManager) {
        super(vehicleRoutingProblem);
        this.regretScoringFunction = new DefaultRegretScoringFunction(new DefaultScorer(vehicleRoutingProblem));
        this.insertionCostsCalculator = jobInsertionCalculator;
        this.executor = executorService;
        this.fleetManager = fleetManager;
        this.initialVehicleIds = getInitialVehicleIds(vehicleRoutingProblem);
        logger.debug("initialise " + this);
    }

    @Override
    public String toString() {
        return "[name=regretInsertion][additionalScorer=" + regretScoringFunction + "]";
    }

    public void setSwitchAllowed(boolean switchAllowed) {
        this.switchAllowed = switchAllowed;
    }

    private Set<String> getInitialVehicleIds(VehicleRoutingProblem vehicleRoutingProblem) {
        Set<String> ids = new HashSet<>();
        for(VehicleRoute r : vehicleRoutingProblem.getInitialVehicleRoutes()){
            ids.add(r.getVehicle().getId());
        }
        return ids;
    }

    public void setDependencyTypes(DependencyType[] dependencyTypes){
        this.dependencyTypes = dependencyTypes;
    }

    public void setSpatialFilter(AdaptiveSpatialFilter spatialFilter) {
        this.spatialFilter = spatialFilter;
    }

    public AdaptiveSpatialFilter getSpatialFilter() {
        return spatialFilter;
    }


    /**
     * Runs insertion.
     * <p>
     * <p>Before inserting a job, all unassigned jobs are scored according to its best- and secondBest-insertion plus additional scoring variables.
     *
     * @throws java.lang.RuntimeException if smth went wrong with thread execution
     */
    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<>(unassignedJobs.size());

        // Use LinkedHashSet for O(1) removal while preserving insertion order
        Set<Job> jobs = new LinkedHashSet<>(unassignedJobs);

        // Handle breaks first (without modifying the input collection)
        for (Job job : unassignedJobs) {
            if (job.getJobType().isBreak()) {
                VehicleRoute route = InsertionDataUpdater.findRoute(routes, job);
                if (route == null) {
                    badJobs.add(job);
                } else {
                    InsertionData iData = insertionCostsCalculator.getInsertionData(route, job, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
                    if (iData instanceof InsertionData.NoInsertionFound) {
                        badJobs.add(job);
                    } else {
                        insertJob(job, iData, route);
                    }
                }
                jobs.remove(job);
            }
        }

        // Use BoundedInsertionQueue instead of TreeSet - O(R) per job instead of O(n*R)
        BoundedInsertionQueue[] queues = new BoundedInsertionQueue[vrp.getJobs().values().size() + 2];
        VehicleRoute lastModified = null;
        boolean firstRun = true;
        while (!jobs.isEmpty()) {
            List<ScoredJob> badJobList = new ArrayList<>();
            if(!firstRun && lastModified == null) throw new IllegalStateException("ho. this must not be.");
            updateInsertionData(queues, routes, jobs, firstRun, lastModified);
            if(firstRun) firstRun = false;
            ScoredJob bestScoredJob;
            if (regretKScoringFunction != null && regretK != 2) {
                bestScoredJob = InsertionDataUpdater.getBestWithKBounded(switchAllowed, initialVehicleIds, fleetManager, insertionCostsCalculator, regretKScoringFunction, regretK, queues, jobs, badJobList, vrp);
            } else {
                bestScoredJob = InsertionDataUpdater.getBestBounded(switchAllowed, initialVehicleIds, fleetManager, insertionCostsCalculator, regretScoringFunction, queues, jobs, badJobList, vrp);
            }
            if (bestScoredJob != null) {
                if (bestScoredJob.isNewRoute()) {
                    routes.add(bestScoredJob.getRoute());
                }
                insertJob(bestScoredJob.getJob(), bestScoredJob.getInsertionData(), bestScoredJob.getRoute());
                jobs.remove(bestScoredJob.getJob());
                lastModified = bestScoredJob.getRoute();
            }
            else lastModified = null;
            for (ScoredJob bad : badJobList) {
                Job unassigned = bad.getJob();
                jobs.remove(unassigned);
                badJobs.add(unassigned);
                markUnassigned(unassigned, bad.getInsertionData().getFailedConstraintNames());
            }
        }
        return badJobs;
    }

    private void updateInsertionData(final BoundedInsertionQueue[] queues, final Collection<VehicleRoute> routes, Collection<Job> unassignedJobs, final boolean firstRun, final VehicleRoute lastModified) {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (final Job unassignedJob : unassignedJobs) {
            int jobIndex = vrp.getJobIndex(unassignedJob);
            if (queues[jobIndex] == null) {
                queues[jobIndex] = new BoundedInsertionQueue();
            }
            if(firstRun) {
                // First run: calculate insertions for all routes
                makeCallables(tasks, true, queues[jobIndex], unassignedJob, routes, lastModified);
            }
            else{
                if (dependencyTypes == null || dependencyTypes[jobIndex] == null) {
                    // Only update the modified route
                    makeCallables(tasks, false, queues[jobIndex], unassignedJob, routes, lastModified);
                }
                else {
                    DependencyType dependencyType = dependencyTypes[jobIndex];
                    if (dependencyType.equals(DependencyType.INTER_ROUTE) || dependencyType.equals(DependencyType.INTRA_ROUTE)) {
                        // Dependencies require updating all routes
                        makeCallables(tasks, true, queues[jobIndex], unassignedJob, routes, lastModified);
                    } else {
                        // Only update the modified route
                        makeCallables(tasks, false, queues[jobIndex], unassignedJob, routes, lastModified);
                    }
                }
            }
        }
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void makeCallables(List<Callable<Void>> tasks, boolean updateAll, final BoundedInsertionQueue queue, final Job unassignedJob, final Collection<VehicleRoute> routes, final VehicleRoute lastModified) {
        if(updateAll) {
            // Use spatial filtering when updating all routes
            final AdaptiveSpatialFilter filter = this.spatialFilter;
            tasks.add(() -> {
                InsertionDataUpdater.updateBoundedWithFilter(switchAllowed, initialVehicleIds, fleetManager, insertionCostsCalculator, queue, unassignedJob, routes, filter);
                return null;
            });
        }
        else {
            // No spatial filtering for single route update
            tasks.add(() -> {
                InsertionDataUpdater.updateBounded(switchAllowed, initialVehicleIds, fleetManager, insertionCostsCalculator, queue, unassignedJob, Arrays.asList(lastModified));
                return null;
            });
        }
    }


}
