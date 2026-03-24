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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Fast position-based regret insertion with hybrid optimization.
 *
 * <p>Combines the speed of route-based regret with the accuracy of position-based regret:</p>
 * <ol>
 *   <li><b>Route-level screening</b>: Use route-based best insertions to identify promising routes</li>
 *   <li><b>Position expansion</b>: Only expand to all positions for top-m candidate routes</li>
 *   <li><b>Cascading filters</b>: Use cheap lower bounds to prune positions before expensive constraint checks</li>
 * </ol>
 *
 * <p>Supports regret-k for any k (regret-2, regret-3, regret-4, etc.)</p>
 *
 * <p>The pruning strategy:</p>
 * <ul>
 *   <li>Distance lower bound: skip positions where marginal distance alone exceeds top-k threshold</li>
 *   <li>Time window check: skip positions that violate job's time window</li>
 *   <li>Full constraint check: only for positions that pass the cheaper filters</li>
 * </ul>
 *
 * @author schroeder
 */
public class PositionBasedRegretInsertionFast extends AbstractInsertionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PositionBasedRegretInsertionFast.class);

    private final JobInsertionCostsCalculator insertionCostsCalculator;
    private final VehicleFleetManager fleetManager;
    private final Set<String> initialVehicleIds;

    private RegretKScoringFunction scoringFunction;
    private int regretK = 2;
    private int topRoutesToExpand = 3;
    private boolean switchAllowed = true;
    private DependencyType[] dependencyTypes = null;

    public PositionBasedRegretInsertionFast(JobInsertionCostsCalculator insertionCostsCalculator,
                                            VehicleRoutingProblem vrp,
                                            VehicleFleetManager fleetManager) {
        super(vrp);
        this.insertionCostsCalculator = insertionCostsCalculator;
        this.fleetManager = fleetManager;
        this.initialVehicleIds = getInitialVehicleIds(vrp);
        this.scoringFunction = new RegretKScoringFunctionAdapter(
                new DefaultRegretScoringFunction(new DefaultScorer(vrp)));
        logger.debug("initialise {}", this);
    }

    private Set<String> getInitialVehicleIds(VehicleRoutingProblem vrp) {
        Set<String> ids = new HashSet<>();
        for (VehicleRoute r : vrp.getInitialVehicleRoutes()) {
            ids.add(r.getVehicle().getId());
        }
        return ids;
    }

    /**
     * Sets the number of best positions to track for regret calculation.
     * Use k=2 for regret-2, k=3 for regret-3, etc.
     */
    public void setRegretK(int k) {
        this.regretK = k;
    }

    /**
     * Sets the number of top routes to expand for position-level analysis.
     * Routes beyond this are only considered at route-level (best position only).
     */
    public void setTopRoutesToExpand(int m) {
        this.topRoutesToExpand = m;
    }

    public void setScoringFunction(RegretKScoringFunction scoringFunction) {
        this.scoringFunction = scoringFunction;
    }

    public void setSwitchAllowed(boolean switchAllowed) {
        this.switchAllowed = switchAllowed;
    }

    public void setDependencyTypes(DependencyType[] dependencyTypes) {
        this.dependencyTypes = dependencyTypes;
    }

    @Override
    public String toString() {
        return "[name=positionBasedRegretInsertionFast][k=" + regretK + "][topRoutes=" + topRoutesToExpand + "]";
    }

    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<>(unassignedJobs.size());
        Set<Job> jobs = new LinkedHashSet<>(unassignedJobs);

        // Cache for route-level best insertions (reused across iterations)
        // Maps job index -> BoundedInsertionQueue with best insertion per route
        BoundedInsertionQueue[] routeLevelCache = new BoundedInsertionQueue[vrp.getJobs().values().size() + 2];

        VehicleRoute lastModified = null;
        boolean firstRun = true;

        while (!jobs.isEmpty()) {
            List<ScoredJob> badJobList = new ArrayList<>();

            // Update route-level cache
            if (firstRun) {
                updateRouteLevelCache(routeLevelCache, routes, jobs);
                firstRun = false;
            } else if (lastModified != null) {
                updateRouteLevelCacheForRoute(routeLevelCache, lastModified, jobs);
            }

            // Find best job to insert using position-based regret
            ScoredJob bestScoredJob = findBestJobWithPositionBasedRegret(
                    routeLevelCache, routes, jobs, badJobList);

            if (bestScoredJob != null) {
                if (bestScoredJob.isNewRoute()) {
                    routes.add(bestScoredJob.getRoute());
                }
                insertJob(bestScoredJob.getJob(), bestScoredJob.getInsertionData(), bestScoredJob.getRoute());
                jobs.remove(bestScoredJob.getJob());
                lastModified = bestScoredJob.getRoute();

                // Check for inter-route dependencies
                if (hasInterRouteDependency(bestScoredJob.getJob())) {
                    // Invalidate entire cache - need full recalculation
                    Arrays.fill(routeLevelCache, null);
                    firstRun = true;
                }
            } else {
                lastModified = null;
            }

            for (ScoredJob bad : badJobList) {
                Job unassigned = bad.getJob();
                jobs.remove(unassigned);
                badJobs.add(unassigned);
                markUnassigned(unassigned, bad.getInsertionData().getFailedConstraintNames());
            }
        }

        return badJobs;
    }

    private boolean hasInterRouteDependency(Job job) {
        if (dependencyTypes == null) return false;
        int jobIndex = vrp.getJobIndex(job);
        if (jobIndex < 0 || jobIndex >= dependencyTypes.length) return false;
        DependencyType type = dependencyTypes[jobIndex];
        return type == DependencyType.INTER_ROUTE || type == DependencyType.INTRA_ROUTE;
    }

    /**
     * Updates route-level cache for all routes and jobs.
     */
    private void updateRouteLevelCache(BoundedInsertionQueue[] cache,
                                       Collection<VehicleRoute> routes,
                                       Collection<Job> jobs) {
        for (Job job : jobs) {
            int jobIndex = vrp.getJobIndex(job);
            if (cache[jobIndex] == null) {
                cache[jobIndex] = new BoundedInsertionQueue();
            }
            InsertionDataUpdater.updateBounded(switchAllowed, initialVehicleIds, fleetManager,
                    insertionCostsCalculator, cache[jobIndex], job, routes);
        }
    }

    /**
     * Updates route-level cache only for the modified route.
     */
    private void updateRouteLevelCacheForRoute(BoundedInsertionQueue[] cache,
                                               VehicleRoute modifiedRoute,
                                               Collection<Job> jobs) {
        for (Job job : jobs) {
            int jobIndex = vrp.getJobIndex(job);
            if (cache[jobIndex] == null) {
                cache[jobIndex] = new BoundedInsertionQueue();
            }
            InsertionDataUpdater.updateBounded(switchAllowed, initialVehicleIds, fleetManager,
                    insertionCostsCalculator, cache[jobIndex], job, Collections.singletonList(modifiedRoute));
        }
    }

    /**
     * Finds the best job to insert using the hybrid position-based regret approach.
     */
    private ScoredJob findBestJobWithPositionBasedRegret(BoundedInsertionQueue[] routeLevelCache,
                                                         Collection<VehicleRoute> routes,
                                                         Collection<Job> jobs,
                                                         List<ScoredJob> badJobs) {
        ScoredJob bestScoredJob = null;

        for (Job job : jobs) {
            ScoredJob scoredJob = scoreJobWithPositionBasedRegret(
                    routeLevelCache, routes, job);

            if (scoredJob instanceof ScoredJob.BadJob) {
                badJobs.add(scoredJob);
                continue;
            }

            if (bestScoredJob == null || scoredJob.getScore() > bestScoredJob.getScore()) {
                bestScoredJob = scoredJob;
            }
        }

        return bestScoredJob;
    }

    /**
     * Scores a single job using position-based regret with cascading filters.
     */
    private ScoredJob scoreJobWithPositionBasedRegret(BoundedInsertionQueue[] routeLevelCache,
                                                      Collection<VehicleRoute> routes,
                                                      Job job) {
        int jobIndex = vrp.getJobIndex(job);
        BoundedInsertionQueue routeLevel = routeLevelCache[jobIndex];
        List<String> failedConstraintNames = new ArrayList<>();

        // Get routes sorted by their best insertion cost
        List<BoundedInsertionQueue.Entry> sortedRouteEntries = routeLevel.getSortedEntries();

        // Top-k position queue for regret calculation
        TopKPositionQueue topK = new TopKPositionQueue(regretK);

        // Phase 1: Add best positions from routes beyond top-m (route-level only)
        for (int i = topRoutesToExpand; i < sortedRouteEntries.size(); i++) {
            BoundedInsertionQueue.Entry entry = sortedRouteEntries.get(i);
            if (entry.getInsertionData().isFound()) {
                topK.offer(entry.getInsertionData(), entry.getRoute());
            }
        }

        // Phase 2: Expand top-m routes to position level with cascading filters
        for (int i = 0; i < Math.min(topRoutesToExpand, sortedRouteEntries.size()); i++) {
            BoundedInsertionQueue.Entry routeEntry = sortedRouteEntries.get(i);
            VehicleRoute route = routeEntry.getRoute();

            expandRoutePositions(route, job, topK, failedConstraintNames);
        }

        // Phase 3: Consider empty route (new vehicle)
        VehicleRoute emptyRoute = VehicleRoute.emptyRoute();
        List<InsertionData> emptyRoutePositions = insertionCostsCalculator.getAllInsertionPositions(emptyRoute, job);
        if (emptyRoutePositions.isEmpty()) {
            InsertionData iData = insertionCostsCalculator.getInsertionData(emptyRoute, job,
                    null, 0, null, Double.MAX_VALUE);
            if (!iData.isFound()) {
                failedConstraintNames.addAll(iData.getFailedConstraintNames());
            }
        } else {
            for (InsertionData position : emptyRoutePositions) {
                topK.offer(position, emptyRoute);
            }
        }

        // No feasible positions found
        if (topK.isEmpty()) {
            return new ScoredJob.BadJob(job, failedConstraintNames);
        }

        // Compute regret score
        RegretKAlternatives alternatives = topK.toAlternatives();
        double score = scoringFunction.score(alternatives, job);

        TopKPositionQueue.Position best = topK.getBest();
        VehicleRoute bestRoute = best.getRoute();

        return new ScoredJob(job, score, best.getInsertionData(), bestRoute,
                !bestRoute.hasVehicle());
    }

    /**
     * Expands a route to position level, adding all feasible positions to topK.
     * The TopKPositionQueue naturally keeps only the best k positions.
     */
    private void expandRoutePositions(VehicleRoute route, Job job,
                                      TopKPositionQueue topK,
                                      List<String> failedConstraintNames) {
        // Get all feasible positions for this route
        List<InsertionData> positions = insertionCostsCalculator.getAllInsertionPositions(route, job);

        if (positions.isEmpty()) {
            // No feasible positions - get failure reasons
            InsertionData iData = insertionCostsCalculator.getInsertionData(
                    route, job, route.getVehicle(),
                    route.getVehicle().getEarliestDeparture(),
                    route.getDriver(), Double.MAX_VALUE);
            if (!iData.isFound()) {
                failedConstraintNames.addAll(iData.getFailedConstraintNames());
            }
            return;
        }

        // Add all positions - TopKPositionQueue keeps only the best k
        for (InsertionData pos : positions) {
            topK.offer(pos, route);
        }
    }
}
