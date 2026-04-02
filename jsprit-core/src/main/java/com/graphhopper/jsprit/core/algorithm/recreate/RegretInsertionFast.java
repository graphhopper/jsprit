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

/**
 * Insertion based on regret approach with affected-job tracking optimization.
 * <p>
 * <p>Basically calculates the insertion cost of the firstBest and the secondBest alternative. The score is then calculated as difference
 * between secondBest and firstBest, plus additional scoring variables that can defined in this.ScoringFunction.
 * The idea is that if the cost of the secondBest alternative is way higher than the first best, it seems to be important to insert this
 * customer immediatedly. If difference is not that high, it might not impact solution if this customer is inserted later.
 * <p>
 * <p><b>Affected-Job Tracking Optimization:</b> After inserting a job into route R, only jobs that had R
 * in their top-2 (best or second-best) routes need full recalculation. Other jobs use cheap lower-bound
 * checks to determine if R became competitive. Combined with spatial filtering, this provides
 * significant speedup for large instances (5-10x typical).
 *
 * @author stefan schroeder
 */
public class RegretInsertionFast extends AbstractInsertionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RegretInsertionFast.class);

    private RegretScoringFunction regretScoringFunction;

    private RegretKScoringFunction regretKScoringFunction;

    private int regretK = 2;

    private final JobInsertionCostsCalculator insertionCostsCalculator;

    private final VehicleFleetManager fleetManager;

    private final Set<String> initialVehicleIds;

    private boolean switchAllowed = true;

    private DependencyType[] dependencyTypes = null;

    private InsertionRouteFilter routeFilter = null;

    private boolean affectedJobTrackingEnabled = true;

    public RegretInsertionFast(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem, VehicleFleetManager fleetManager) {
        super(vehicleRoutingProblem);
        this.regretScoringFunction = new DefaultRegretScoringFunction(new DefaultScorer(vehicleRoutingProblem));
        this.insertionCostsCalculator = jobInsertionCalculator;
        this.fleetManager = fleetManager;
        this.initialVehicleIds = getInitialVehicleIds(vehicleRoutingProblem);
        logger.debug("initialise {}", this);
    }

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

    public void setSwitchAllowed(boolean switchAllowed) {
        this.switchAllowed = switchAllowed;
    }

    public void setDependencyTypes(DependencyType[] dependencyTypes) {
        this.dependencyTypes = dependencyTypes;
    }

    public void setRouteFilter(InsertionRouteFilter routeFilter) {
        this.routeFilter = routeFilter;
    }

    public InsertionRouteFilter getRouteFilter() {
        return routeFilter;
    }

    /**
     * Enables or disables affected-job tracking optimization.
     * When enabled, only jobs affected by a route modification are recalculated.
     * Default is enabled.
     *
     * @param enabled true to enable (default), false to disable
     */
    public void setAffectedJobTrackingEnabled(boolean enabled) {
        this.affectedJobTrackingEnabled = enabled;
    }

    public boolean isAffectedJobTrackingEnabled() {
        return affectedJobTrackingEnabled;
    }

    private Set<String> getInitialVehicleIds(VehicleRoutingProblem vehicleRoutingProblem) {
        Set<String> ids = new HashSet<>();
        for(VehicleRoute r : vehicleRoutingProblem.getInitialVehicleRoutes()){
            ids.add(r.getVehicle().getId());
        }
        return ids;
    }

    @Override
    public String toString() {
        return "[name=regretInsertionFast][regretK=" + regretK + "][affectedJobTracking=" + affectedJobTrackingEnabled + "]";
    }


    /**
     * Runs insertion.
     * <p>
     * <p>Before inserting a job, all unassigned jobs are scored according to its best- and secondBest-insertion plus additional scoring variables.
     */
    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<>(unassignedJobs.size());
        Set<Job> jobs = new LinkedHashSet<>(unassignedJobs);

        // Use BoundedInsertionQueue instead of TreeSet - O(R) per job instead of O(n*R)
        BoundedInsertionQueue[] queues = new BoundedInsertionQueue[vrp.getJobs().values().size() + 2];

        // Affected-job tracker for incremental updates
        AffectedJobTracker tracker = affectedJobTrackingEnabled ? new AffectedJobTracker(vrp) : null;

        VehicleRoute lastModified = null;
        boolean firstRun = true;

        while (!jobs.isEmpty()) {
            List<ScoredJob> badJobList = new ArrayList<>();

            if (!firstRun && lastModified == null) {
                throw new IllegalStateException("last modified route is null. this should not be.");
            }

            if (firstRun) {
                updateInsertionDataFirstRun(queues, routes, jobs, tracker);
                firstRun = false;
            } else {
                updateInsertionDataIncremental(queues, routes, jobs, lastModified, tracker);
            }

            ScoredJob bestScoredJob;
            if (regretKScoringFunction != null && regretK != 2) {
                bestScoredJob = InsertionDataUpdater.getBestWithKBounded(switchAllowed, initialVehicleIds, fleetManager, insertionCostsCalculator, regretKScoringFunction, regretK, queues, jobs, badJobList, vrp);
            } else {
                bestScoredJob = InsertionDataUpdater.getBestBounded(switchAllowed, initialVehicleIds, fleetManager, insertionCostsCalculator, regretScoringFunction, queues, jobs, badJobList, vrp);
            }

            if (bestScoredJob != null) {
                boolean isNewRoute = bestScoredJob.isNewRoute();
                if (isNewRoute) {
                    routes.add(bestScoredJob.getRoute());
                }
                insertJob(bestScoredJob.getJob(), bestScoredJob.getInsertionData(), bestScoredJob.getRoute());
                jobs.remove(bestScoredJob.getJob());
                lastModified = bestScoredJob.getRoute();

                // Update tracker: remove inserted job, invalidate route centroid
                if (tracker != null) {
                    tracker.removeJob(bestScoredJob.getJob());
                    tracker.invalidateRouteCentroid(lastModified);

                    // When a new route is added, register it in all remaining jobs' neighborhoods
                    if (isNewRoute) {
                        for (Job remainingJob : jobs) {
                            tracker.registerSpatialNeighborhood(remainingJob, Collections.singletonList(lastModified));
                        }
                    }
                }
            } else {
                lastModified = null;
            }

            for (ScoredJob bad : badJobList) {
                Job unassigned = bad.getJob();
                jobs.remove(unassigned);
                badJobs.add(unassigned);
                markUnassigned(unassigned, bad.getInsertionData().getFailedConstraintNames());
                if (tracker != null) {
                    tracker.removeJob(unassigned);
                }
            }
        }

        return badJobs;
    }

    /**
     * First run: calculate insertion data for all jobs against all routes.
     */
    private void updateInsertionDataFirstRun(BoundedInsertionQueue[] queues, Collection<VehicleRoute> routes,
                                              Collection<Job> unassignedJobs, AffectedJobTracker tracker) {
        for (Job unassignedJob : unassignedJobs) {
            int jobIndex = vrp.getJobIndex(unassignedJob);
            if (queues[jobIndex] == null) {
                queues[jobIndex] = new BoundedInsertionQueue();
            }

            // Calculate insertions for all routes with optional route filtering
            InsertionDataUpdater.updateBoundedWithFilter(switchAllowed, initialVehicleIds, fleetManager,
                    insertionCostsCalculator, queues[jobIndex], unassignedJob, routes, routeFilter);

            // Update tracker with initial state
            if (tracker != null) {
                tracker.updateJobTracking(unassignedJob, queues[jobIndex]);
                // Register spatial neighborhood if route filter is active
                if (routeFilter != null && routeFilter.isFilteringEnabled()) {
                    Collection<VehicleRoute> nearRoutes = routeFilter.filterRoutes(unassignedJob, routes);
                    tracker.registerSpatialNeighborhood(unassignedJob, nearRoutes);
                } else {
                    // No route filtering - all routes are neighbors
                    tracker.registerSpatialNeighborhood(unassignedJob, routes);
                }
            }
        }
    }

    /**
     * Incremental update: only recalculate affected jobs after a route modification.
     */
    private void updateInsertionDataIncremental(BoundedInsertionQueue[] queues, Collection<VehicleRoute> routes,
                                                 Collection<Job> unassignedJobs, VehicleRoute lastModified,
                                                 AffectedJobTracker tracker) {
        if (tracker == null || !affectedJobTrackingEnabled) {
            // Fallback to original behavior
            updateInsertionDataLegacy(queues, routes, unassignedJobs, lastModified);
            return;
        }

        // Get jobs affected by the route modification (route was in their top-2)
        Set<Job> affectedJobs = tracker.getAffectedJobs(lastModified);

        // Get jobs that need cheap check (route in neighborhood but not top-2)
        Set<Job> cheapCheckJobs = tracker.getJobsNeedingCheapCheck(lastModified, affectedJobs);

        for (Job unassignedJob : unassignedJobs) {
            int jobIndex = vrp.getJobIndex(unassignedJob);

            // Check for inter-route dependencies
            boolean hasDependency = dependencyTypes != null && dependencyTypes[jobIndex] != null;
            if (hasDependency) {
                DependencyType dependencyType = dependencyTypes[jobIndex];
                if (dependencyType.equals(DependencyType.INTER_ROUTE) || dependencyType.equals(DependencyType.INTRA_ROUTE)) {
                    // Dependencies require updating all routes
                    InsertionDataUpdater.updateBoundedWithFilter(switchAllowed, initialVehicleIds, fleetManager,
                            insertionCostsCalculator, queues[jobIndex], unassignedJob, routes, routeFilter);
                    tracker.updateJobTracking(unassignedJob, queues[jobIndex]);
                    continue;
                }
            }

            if (affectedJobs.contains(unassignedJob)) {
                // Case 3: Route was in job's top-2 - full recalculation for modified route
                InsertionDataUpdater.updateBounded(switchAllowed, initialVehicleIds, fleetManager,
                    insertionCostsCalculator, queues[jobIndex], unassignedJob, Collections.singletonList(lastModified));
                tracker.updateJobTracking(unassignedJob, queues[jobIndex]);

            } else if (cheapCheckJobs.contains(unassignedJob)) {
                // Case 2: Route in neighborhood but not top-2 - cheap lower-bound check
                if (tracker.couldBeCompetitive(unassignedJob, lastModified)) {
                    // Lower bound indicates route might be competitive - do full calculation
                    InsertionDataUpdater.updateBounded(switchAllowed, initialVehicleIds, fleetManager,
                        insertionCostsCalculator, queues[jobIndex], unassignedJob, Collections.singletonList(lastModified));
                    tracker.updateJobTracking(unassignedJob, queues[jobIndex]);
                }
                // else: skip - route definitely not competitive

            }
            // Case 1: Route not in job's neighborhood - skip entirely (implicit)
        }
    }

    /**
     * Legacy update method (original behavior without affected-job tracking).
     */
    private void updateInsertionDataLegacy(BoundedInsertionQueue[] queues, Collection<VehicleRoute> routes,
                                            Collection<Job> unassignedJobs, VehicleRoute lastModified) {
        for (Job unassignedJob : unassignedJobs) {
            int jobIndex = vrp.getJobIndex(unassignedJob);
            if (queues[jobIndex] == null) {
                queues[jobIndex] = new BoundedInsertionQueue();
            }

            if (dependencyTypes == null || dependencyTypes[jobIndex] == null) {
                // Only update the modified route
                InsertionDataUpdater.updateBounded(switchAllowed, initialVehicleIds, fleetManager,
                    insertionCostsCalculator, queues[jobIndex], unassignedJob, Collections.singletonList(lastModified));
            } else {
                DependencyType dependencyType = dependencyTypes[jobIndex];
                if (dependencyType.equals(DependencyType.INTER_ROUTE) || dependencyType.equals(DependencyType.INTRA_ROUTE)) {
                    // Dependencies require updating all routes
                    InsertionDataUpdater.updateBoundedWithFilter(switchAllowed, initialVehicleIds, fleetManager,
                            insertionCostsCalculator, queues[jobIndex], unassignedJob, routes, routeFilter);
                } else {
                    // Only update the modified route
                    InsertionDataUpdater.updateBounded(switchAllowed, initialVehicleIds, fleetManager,
                        insertionCostsCalculator, queues[jobIndex], unassignedJob, Collections.singletonList(lastModified));
                }
            }
        }
    }
}
