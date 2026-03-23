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
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;

import java.util.*;

/**
 * Created by schroeder on 15/10/15.
 */
class InsertionDataUpdater {

    static boolean update(boolean addAllAvailable, Set<String> initialVehicleIds, VehicleFleetManager fleetManager, JobInsertionCostsCalculator insertionCostsCalculator, TreeSet<VersionedInsertionData> insertionDataSet, int updateRound, Job unassignedJob, Collection<VehicleRoute> routes) {
        for(VehicleRoute route : routes) {
            Collection<Vehicle> relevantVehicles = new ArrayList<>();
            if (!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
                relevantVehicles.add(route.getVehicle());
                if(addAllAvailable && !initialVehicleIds.contains(route.getVehicle().getId())){
                    relevantVehicles.addAll(fleetManager.getAvailableVehicles(route.getVehicle()));
                }
            } else relevantVehicles.addAll(fleetManager.getAvailableVehicles());
            for (Vehicle v : relevantVehicles) {
                double depTime = v.getEarliestDeparture();
                InsertionData iData = insertionCostsCalculator.getInsertionData(route, unassignedJob, v, depTime, route.getDriver(), Double.MAX_VALUE);
                if (iData instanceof InsertionData.NoInsertionFound) {
                    continue;
                }
                insertionDataSet.add(new VersionedInsertionData(iData, updateRound, route));
            }
        }
        return true;
    }



    static VehicleRoute findRoute(Collection<VehicleRoute> routes, Job job) {
        for(VehicleRoute r : routes){
            if(r.getVehicle().getBreak() == job) return r;
        }
        return null;
    }

    static Comparator<VersionedInsertionData> getComparator(){
        return (o1, o2) -> {
            if (o1.getiData().getInsertionCost() < o2.getiData().getInsertionCost()) return -1;
            return 1;
        };
    }

    static ScoredJob getBest(boolean switchAllowed, Set<String> initialVehicleIds, VehicleFleetManager fleetManager, JobInsertionCostsCalculator insertionCostsCalculator, RegretScoringFunction scoringFunction, TreeSet<VersionedInsertionData>[] priorityQueues, Map<VehicleRoute, Integer> updates, Collection<Job> unassignedJobs, List<ScoredJob> badJobs, VehicleRoutingProblem vrp) {
        ScoredJob bestScoredJob = null;
        for (Job j : unassignedJobs) {
            VehicleRoute bestRoute = null;
            InsertionData best = null;
            InsertionData secondBest = null;
            TreeSet<VersionedInsertionData> priorityQueue = priorityQueues[vrp.getJobIndex(j)];
            Iterator<VersionedInsertionData> iterator = priorityQueue.iterator();
            List<String> failedConstraintNames = new ArrayList<>();
            while (iterator.hasNext()) {
                VersionedInsertionData versionedIData = iterator.next();
                if(bestRoute != null){
                    if(versionedIData.getRoute() == bestRoute){
                        continue;
                    }
                }
                if (versionedIData.getiData() instanceof InsertionData.NoInsertionFound) {
                    failedConstraintNames.addAll(versionedIData.getiData().getFailedConstraintNames());
                    continue;
                }
                if(!(versionedIData.getRoute().getVehicle() instanceof VehicleImpl.NoVehicle)) {
                    if (versionedIData.getiData().getSelectedVehicle() != versionedIData.getRoute().getVehicle()) {
                        if (!switchAllowed) continue;
                        if (initialVehicleIds.contains(versionedIData.getRoute().getVehicle().getId())) continue;
                    }
                }
                if(versionedIData.getiData().getSelectedVehicle() != versionedIData.getRoute().getVehicle()) {
                    if (fleetManager.isLocked(versionedIData.getiData().getSelectedVehicle())) {
                        Vehicle available = fleetManager.getAvailableVehicle(versionedIData.getiData().getSelectedVehicle().getVehicleTypeIdentifier());
                        if (available != null) {
                            InsertionData oldData = versionedIData.getiData();
                            InsertionData newData = new InsertionData(oldData.getInsertionCost(), oldData.getPickupInsertionIndex(),
                                oldData.getDeliveryInsertionIndex(), available, oldData.getSelectedDriver());
                            newData.setVehicleDepartureTime(oldData.getVehicleDepartureTime());
                            for(Event e : oldData.getEvents()){
                                if(e instanceof SwitchVehicle){
                                    newData.getEvents().add(new SwitchVehicle(versionedIData.getRoute(),available,oldData.getVehicleDepartureTime()));
                                }
                                else newData.getEvents().add(e);
                            }
                            versionedIData = new VersionedInsertionData(newData, versionedIData.getVersion(), versionedIData.getRoute());
                        } else continue;
                    }
                }
                int currentDataVersion = updates.get(versionedIData.getRoute());
                if(versionedIData.getVersion() == currentDataVersion){
                    if(best == null) {
                        best = versionedIData.getiData();
                        bestRoute = versionedIData.getRoute();
                    }
                    else {
                        secondBest = versionedIData.getiData();
                        break;
                    }
                }
            }
            VehicleRoute emptyRoute = VehicleRoute.emptyRoute();
            InsertionData iData = insertionCostsCalculator.getInsertionData(emptyRoute, j, null, -1, null, Double.MAX_VALUE);
            if(!(iData instanceof InsertionData.NoInsertionFound)){
                if (best == null) {
                    best = iData;
                    bestRoute = emptyRoute;
                } else if (iData.getInsertionCost() < best.getInsertionCost()) {
                    secondBest = best;
                    best = iData;
                    bestRoute = emptyRoute;
                } else if (secondBest == null || (iData.getInsertionCost() < secondBest.getInsertionCost())) {
                    secondBest = iData;
                }
            } else failedConstraintNames.addAll(iData.getFailedConstraintNames());
            if (best == null) {
                badJobs.add(new ScoredJob.BadJob(j, failedConstraintNames));
                continue;
            }
            double score = scoringFunction.score(best, secondBest, j);
            ScoredJob scoredJob;
            if (bestRoute == emptyRoute) {
                scoredJob = new ScoredJob(j, score, best, bestRoute, true);
            } else scoredJob = new ScoredJob(j, score, best, bestRoute, false);

            if(bestScoredJob == null){
                bestScoredJob = scoredJob;
            }
            else if(scoredJob.getScore() > bestScoredJob.getScore()){
                bestScoredJob = scoredJob;
            }
        }
        return bestScoredJob;
    }

    /**
     * Gets the best scored job using k-best alternatives for regret-k scoring.
     *
     * @param switchAllowed whether vehicle switching is allowed
     * @param initialVehicleIds IDs of vehicles from initial routes
     * @param fleetManager the fleet manager
     * @param insertionCostsCalculator calculator for insertion costs
     * @param scoringFunction the k-best scoring function
     * @param k the number of alternatives to consider (-1 or Integer.MAX_VALUE for all)
     * @param priorityQueues priority queues of insertion data per job
     * @param updates map of route update versions
     * @param unassignedJobs jobs to score
     * @param badJobs list to collect infeasible jobs
     * @param vrp the vehicle routing problem
     * @return the best scored job, or null if none found
     */
    // ==================== BOUNDED QUEUE METHODS ====================

    /**
     * Updates a BoundedInsertionQueue with insertion data for a job.
     * Unlike the TreeSet version, this replaces entries for routes rather than accumulating them.
     *
     * @param addAllAvailable          whether to consider all available vehicles
     * @param initialVehicleIds        IDs of vehicles from initial routes
     * @param fleetManager             the fleet manager
     * @param insertionCostsCalculator calculator for insertion costs
     * @param queue                    the bounded queue to update
     * @param unassignedJob            the job to calculate insertions for
     * @param routes                   the routes to consider
     */
    static void updateBounded(boolean addAllAvailable, Set<String> initialVehicleIds, VehicleFleetManager fleetManager,
                              JobInsertionCostsCalculator insertionCostsCalculator, BoundedInsertionQueue queue,
                              Job unassignedJob, Collection<VehicleRoute> routes) {
        for (VehicleRoute route : routes) {
            Collection<Vehicle> relevantVehicles = new ArrayList<>();
            if (!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
                relevantVehicles.add(route.getVehicle());
                if (addAllAvailable && !initialVehicleIds.contains(route.getVehicle().getId())) {
                    relevantVehicles.addAll(fleetManager.getAvailableVehicles(route.getVehicle()));
                }
            } else {
                relevantVehicles.addAll(fleetManager.getAvailableVehicles());
            }

            // Find best insertion for this route (across all relevant vehicles)
            InsertionData bestForRoute = null;
            for (Vehicle v : relevantVehicles) {
                double depTime = v.getEarliestDeparture();
                InsertionData iData = insertionCostsCalculator.getInsertionData(route, unassignedJob, v, depTime, route.getDriver(), Double.MAX_VALUE);
                if (iData instanceof InsertionData.NoInsertionFound) {
                    continue;
                }
                if (bestForRoute == null || iData.getInsertionCost() < bestForRoute.getInsertionCost()) {
                    bestForRoute = iData;
                }
            }

            if (bestForRoute != null) {
                queue.addOrReplace(bestForRoute, route);
            } else {
                // No valid insertion found - remove any stale entry for this route
                queue.remove(route);
            }
        }
    }

    /**
     * Updates a BoundedInsertionQueue with adaptive spatial filtering.
     * During learning phase, computes both filtered and full results for comparison.
     * After learning, applies filtering based on learned effectiveness.
     *
     * @param addAllAvailable          whether to consider all available vehicles
     * @param initialVehicleIds        IDs of vehicles from initial routes
     * @param fleetManager             the fleet manager
     * @param insertionCostsCalculator calculator for insertion costs
     * @param queue                    the bounded queue to update
     * @param unassignedJob            the job to calculate insertions for
     * @param routes                   all routes to potentially consider
     * @param filter                   the adaptive spatial filter
     */
    static void updateBoundedWithFilter(boolean addAllAvailable, Set<String> initialVehicleIds, VehicleFleetManager fleetManager,
                                        JobInsertionCostsCalculator insertionCostsCalculator, BoundedInsertionQueue queue,
                                        Job unassignedJob, Collection<VehicleRoute> routes, AdaptiveSpatialFilter filter) {
        if (filter == null) {
            updateBounded(addAllAvailable, initialVehicleIds, fleetManager, insertionCostsCalculator, queue, unassignedJob, routes);
            return;
        }

        AdaptiveSpatialFilter.FilterResult filterResult = filter.getRelevantRoutes(unassignedJob, routes);

        if (filterResult.needsComparison()) {
            // Learning/validation mode: compute both filtered and full
            List<VehicleRoute> nearRoutes = filter.getNearestRoutes(unassignedJob, routes, filter.getK());

            // Compute best insertion from filtered routes
            InsertionData filteredBest = null;
            VehicleRoute filteredRoute = null;
            for (VehicleRoute route : nearRoutes) {
                InsertionData best = computeBestInsertionForRoute(addAllAvailable, initialVehicleIds, fleetManager,
                        insertionCostsCalculator, route, unassignedJob);
                if (best != null && (filteredBest == null || best.getInsertionCost() < filteredBest.getInsertionCost())) {
                    filteredBest = best;
                    filteredRoute = route;
                }
            }

            // Compute best insertion from all routes
            InsertionData fullBest = null;
            VehicleRoute fullRoute = null;
            for (VehicleRoute route : routes) {
                InsertionData best = computeBestInsertionForRoute(addAllAvailable, initialVehicleIds, fleetManager,
                        insertionCostsCalculator, route, unassignedJob);
                if (best != null && (fullBest == null || best.getInsertionCost() < fullBest.getInsertionCost())) {
                    fullBest = best;
                    fullRoute = route;
                }
            }

            // Record comparison for learning
            filter.recordComparison(filteredBest, filteredRoute, fullBest, fullRoute);

            // Use full results and update queue
            updateBounded(addAllAvailable, initialVehicleIds, fleetManager, insertionCostsCalculator, queue, unassignedJob, routes);
        } else {
            // Production mode with filtering or full search
            Collection<VehicleRoute> routesToUse = filterResult.getRoutes();

            // Update queue with selected routes
            for (VehicleRoute route : routesToUse) {
                InsertionData best = computeBestInsertionForRoute(addAllAvailable, initialVehicleIds, fleetManager,
                        insertionCostsCalculator, route, unassignedJob);
                if (best != null) {
                    queue.addOrReplace(best, route);
                } else {
                    queue.remove(route);
                }
            }

            // If filtering was applied and found nothing, fall back to full search
            if (filterResult.wasFiltered() && queue.isEmpty()) {
                updateBounded(addAllAvailable, initialVehicleIds, fleetManager, insertionCostsCalculator, queue, unassignedJob, routes);
            }
        }
    }

    /**
     * Computes the best insertion for a single route across all relevant vehicles.
     */
    private static InsertionData computeBestInsertionForRoute(boolean addAllAvailable, Set<String> initialVehicleIds,
                                                              VehicleFleetManager fleetManager, JobInsertionCostsCalculator calculator,
                                                              VehicleRoute route, Job job) {
        Collection<Vehicle> relevantVehicles = new ArrayList<>();
        if (!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
            relevantVehicles.add(route.getVehicle());
            if (addAllAvailable && !initialVehicleIds.contains(route.getVehicle().getId())) {
                relevantVehicles.addAll(fleetManager.getAvailableVehicles(route.getVehicle()));
            }
        } else {
            relevantVehicles.addAll(fleetManager.getAvailableVehicles());
        }

        InsertionData bestForRoute = null;
        for (Vehicle v : relevantVehicles) {
            double depTime = v.getEarliestDeparture();
            InsertionData iData = calculator.getInsertionData(route, job, v, depTime, route.getDriver(), Double.MAX_VALUE);
            if (iData instanceof InsertionData.NoInsertionFound) {
                continue;
            }
            if (bestForRoute == null || iData.getInsertionCost() < bestForRoute.getInsertionCost()) {
                bestForRoute = iData;
            }
        }
        return bestForRoute;
    }

    /**
     * Gets the best scored job using bounded queues (no version tracking needed).
     */
    static ScoredJob getBestBounded(boolean switchAllowed, Set<String> initialVehicleIds, VehicleFleetManager fleetManager,
                                    JobInsertionCostsCalculator insertionCostsCalculator, RegretScoringFunction scoringFunction,
                                    BoundedInsertionQueue[] queues, Collection<Job> unassignedJobs, List<ScoredJob> badJobs,
                                    VehicleRoutingProblem vrp) {
        ScoredJob bestScoredJob = null;

        for (Job j : unassignedJobs) {
            BoundedInsertionQueue queue = queues[vrp.getJobIndex(j)];
            VehicleRoute bestRoute = null;
            InsertionData best = null;
            InsertionData secondBest = null;
            List<String> failedConstraintNames = new ArrayList<>();

            // Iterate through sorted entries (already in cost order, one per route)
            for (BoundedInsertionQueue.Entry entry : queue.getSortedEntries()) {
                InsertionData iData = entry.getInsertionData();
                VehicleRoute route = entry.getRoute();

                if (iData instanceof InsertionData.NoInsertionFound) {
                    failedConstraintNames.addAll(iData.getFailedConstraintNames());
                    continue;
                }

                // Check vehicle switch constraints
                if (!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
                    if (iData.getSelectedVehicle() != route.getVehicle()) {
                        if (!switchAllowed) continue;
                        if (initialVehicleIds.contains(route.getVehicle().getId())) continue;
                    }
                }

                // Handle locked vehicles
                InsertionData effectiveData = iData;
                if (iData.getSelectedVehicle() != route.getVehicle()) {
                    if (fleetManager.isLocked(iData.getSelectedVehicle())) {
                        Vehicle available = fleetManager.getAvailableVehicle(iData.getSelectedVehicle().getVehicleTypeIdentifier());
                        if (available != null) {
                            effectiveData = createReplacementData(iData, route, available);
                        } else {
                            continue;
                        }
                    }
                }

                if (best == null) {
                    best = effectiveData;
                    bestRoute = route;
                } else {
                    secondBest = effectiveData;
                    break; // We only need best and second-best for regret-2
                }
            }

            // Consider empty route (new vehicle)
            VehicleRoute emptyRoute = VehicleRoute.emptyRoute();
            InsertionData emptyRouteData = insertionCostsCalculator.getInsertionData(emptyRoute, j, null, -1, null, Double.MAX_VALUE);
            if (!(emptyRouteData instanceof InsertionData.NoInsertionFound)) {
                if (best == null) {
                    best = emptyRouteData;
                    bestRoute = emptyRoute;
                } else if (emptyRouteData.getInsertionCost() < best.getInsertionCost()) {
                    secondBest = best;
                    best = emptyRouteData;
                    bestRoute = emptyRoute;
                } else if (secondBest == null || emptyRouteData.getInsertionCost() < secondBest.getInsertionCost()) {
                    secondBest = emptyRouteData;
                }
            } else {
                failedConstraintNames.addAll(emptyRouteData.getFailedConstraintNames());
            }

            if (best == null) {
                badJobs.add(new ScoredJob.BadJob(j, failedConstraintNames));
                continue;
            }

            double score = scoringFunction.score(best, secondBest, j);
            ScoredJob scoredJob = new ScoredJob(j, score, best, bestRoute, bestRoute == emptyRoute);

            if (bestScoredJob == null || scoredJob.getScore() > bestScoredJob.getScore()) {
                bestScoredJob = scoredJob;
            }
        }
        return bestScoredJob;
    }

    /**
     * Gets the best scored job using k-best alternatives with bounded queues.
     */
    static ScoredJob getBestWithKBounded(boolean switchAllowed, Set<String> initialVehicleIds, VehicleFleetManager fleetManager,
                                         JobInsertionCostsCalculator insertionCostsCalculator, RegretKScoringFunction scoringFunction,
                                         int k, BoundedInsertionQueue[] queues, Collection<Job> unassignedJobs,
                                         List<ScoredJob> badJobs, VehicleRoutingProblem vrp) {
        ScoredJob bestScoredJob = null;
        int effectiveK = (k <= 0) ? Integer.MAX_VALUE : k;

        for (Job j : unassignedJobs) {
            BoundedInsertionQueue queue = queues[vrp.getJobIndex(j)];
            RegretKAlternatives alternatives = new RegretKAlternatives();
            List<String> failedConstraintNames = new ArrayList<>();

            // Collect k-best alternatives from queue (already sorted, one per route)
            for (BoundedInsertionQueue.Entry entry : queue.getSortedEntries()) {
                if (alternatives.size() >= effectiveK) break;

                InsertionData iData = entry.getInsertionData();
                VehicleRoute route = entry.getRoute();

                if (iData instanceof InsertionData.NoInsertionFound) {
                    failedConstraintNames.addAll(iData.getFailedConstraintNames());
                    continue;
                }

                // Check vehicle switch constraints
                if (!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
                    if (iData.getSelectedVehicle() != route.getVehicle()) {
                        if (!switchAllowed) continue;
                        if (initialVehicleIds.contains(route.getVehicle().getId())) continue;
                    }
                }

                // Handle locked vehicles
                InsertionData effectiveData = iData;
                if (iData.getSelectedVehicle() != route.getVehicle()) {
                    if (fleetManager.isLocked(iData.getSelectedVehicle())) {
                        Vehicle available = fleetManager.getAvailableVehicle(iData.getSelectedVehicle().getVehicleTypeIdentifier());
                        if (available != null) {
                            effectiveData = createReplacementData(iData, route, available);
                        } else {
                            continue;
                        }
                    }
                }

                alternatives.add(effectiveData, route);
            }

            // Consider empty route (new vehicle)
            VehicleRoute emptyRoute = VehicleRoute.emptyRoute();
            InsertionData emptyRouteData = insertionCostsCalculator.getInsertionData(emptyRoute, j, null, -1, null, Double.MAX_VALUE);
            if (!(emptyRouteData instanceof InsertionData.NoInsertionFound)) {
                alternatives.add(emptyRouteData, emptyRoute);
            } else {
                failedConstraintNames.addAll(emptyRouteData.getFailedConstraintNames());
            }

            if (alternatives.isEmpty()) {
                badJobs.add(new ScoredJob.BadJob(j, failedConstraintNames));
                continue;
            }

            double score = scoringFunction.score(alternatives, j);
            RegretKAlternatives.Alternative best = alternatives.getBest();
            VehicleRoute bestRoute = best.getRoute();

            ScoredJob scoredJob = new ScoredJob(j, score, best.getInsertionData(), bestRoute, bestRoute == emptyRoute);

            if (bestScoredJob == null || scoredJob.getScore() > bestScoredJob.getScore()) {
                bestScoredJob = scoredJob;
            }
        }
        return bestScoredJob;
    }

    /**
     * Creates replacement InsertionData with a different vehicle.
     */
    private static InsertionData createReplacementData(InsertionData oldData, VehicleRoute route, Vehicle newVehicle) {
        InsertionData newData = new InsertionData(oldData.getInsertionCost(), oldData.getPickupInsertionIndex(),
                oldData.getDeliveryInsertionIndex(), newVehicle, oldData.getSelectedDriver());
        newData.setVehicleDepartureTime(oldData.getVehicleDepartureTime());
        for (Event e : oldData.getEvents()) {
            if (e instanceof SwitchVehicle) {
                newData.getEvents().add(new SwitchVehicle(route, newVehicle, oldData.getVehicleDepartureTime()));
            } else {
                newData.getEvents().add(e);
            }
        }
        return newData;
    }

    // ==================== ORIGINAL TREESET METHODS ====================

    static ScoredJob getBestWithK(boolean switchAllowed, Set<String> initialVehicleIds, VehicleFleetManager fleetManager,
                                  JobInsertionCostsCalculator insertionCostsCalculator, RegretKScoringFunction scoringFunction,
                                  int k, TreeSet<VersionedInsertionData>[] priorityQueues, Map<VehicleRoute, Integer> updates,
                                  Collection<Job> unassignedJobs, List<ScoredJob> badJobs, VehicleRoutingProblem vrp) {
        ScoredJob bestScoredJob = null;
        int effectiveK = (k <= 0) ? Integer.MAX_VALUE : k;

        for (Job j : unassignedJobs) {
            RegretKAlternatives alternatives = new RegretKAlternatives();
            Set<VehicleRoute> seenRoutes = new HashSet<>();
            TreeSet<VersionedInsertionData> priorityQueue = priorityQueues[vrp.getJobIndex(j)];
            Iterator<VersionedInsertionData> iterator = priorityQueue.iterator();
            List<String> failedConstraintNames = new ArrayList<>();

            // Collect k-best alternatives from priority queue
            while (iterator.hasNext() && alternatives.size() < effectiveK) {
                VersionedInsertionData versionedIData = iterator.next();

                // Skip if we already have an alternative from this route (ensure route diversity)
                if (seenRoutes.contains(versionedIData.getRoute())) {
                    continue;
                }

                if (versionedIData.getiData() instanceof InsertionData.NoInsertionFound) {
                    failedConstraintNames.addAll(versionedIData.getiData().getFailedConstraintNames());
                    continue;
                }

                // Check vehicle switch constraints
                if (!(versionedIData.getRoute().getVehicle() instanceof VehicleImpl.NoVehicle)) {
                    if (versionedIData.getiData().getSelectedVehicle() != versionedIData.getRoute().getVehicle()) {
                        if (!switchAllowed) continue;
                        if (initialVehicleIds.contains(versionedIData.getRoute().getVehicle().getId())) continue;
                    }
                }

                // Handle locked vehicles
                VersionedInsertionData effectiveData = versionedIData;
                if (versionedIData.getiData().getSelectedVehicle() != versionedIData.getRoute().getVehicle()) {
                    if (fleetManager.isLocked(versionedIData.getiData().getSelectedVehicle())) {
                        Vehicle available = fleetManager.getAvailableVehicle(versionedIData.getiData().getSelectedVehicle().getVehicleTypeIdentifier());
                        if (available != null) {
                            InsertionData oldData = versionedIData.getiData();
                            InsertionData newData = new InsertionData(oldData.getInsertionCost(), oldData.getPickupInsertionIndex(),
                                    oldData.getDeliveryInsertionIndex(), available, oldData.getSelectedDriver());
                            newData.setVehicleDepartureTime(oldData.getVehicleDepartureTime());
                            for (Event e : oldData.getEvents()) {
                                if (e instanceof SwitchVehicle) {
                                    newData.getEvents().add(new SwitchVehicle(versionedIData.getRoute(), available, oldData.getVehicleDepartureTime()));
                                } else {
                                    newData.getEvents().add(e);
                                }
                            }
                            effectiveData = new VersionedInsertionData(newData, versionedIData.getVersion(), versionedIData.getRoute());
                        } else {
                            continue;
                        }
                    }
                }

                // Check version freshness
                int currentDataVersion = updates.get(effectiveData.getRoute());
                if (effectiveData.getVersion() == currentDataVersion) {
                    alternatives.add(effectiveData.getiData(), effectiveData.getRoute());
                    seenRoutes.add(effectiveData.getRoute());
                }
            }

            // Consider empty route (new vehicle)
            VehicleRoute emptyRoute = VehicleRoute.emptyRoute();
            InsertionData iData = insertionCostsCalculator.getInsertionData(emptyRoute, j, null, -1, null, Double.MAX_VALUE);
            if (!(iData instanceof InsertionData.NoInsertionFound)) {
                alternatives.add(iData, emptyRoute);
            } else {
                failedConstraintNames.addAll(iData.getFailedConstraintNames());
            }

            if (alternatives.isEmpty()) {
                badJobs.add(new ScoredJob.BadJob(j, failedConstraintNames));
                continue;
            }

            double score = scoringFunction.score(alternatives, j);
            RegretKAlternatives.Alternative best = alternatives.getBest();
            VehicleRoute bestRoute = best.getRoute();

            ScoredJob scoredJob;
            if (bestRoute == emptyRoute) {
                scoredJob = new ScoredJob(j, score, best.getInsertionData(), bestRoute, true);
            } else {
                scoredJob = new ScoredJob(j, score, best.getInsertionData(), bestRoute, false);
            }

            if (bestScoredJob == null) {
                bestScoredJob = scoredJob;
            } else if (scoredJob.getScore() > bestScoredJob.getScore()) {
                bestScoredJob = scoredJob;
            }
        }
        return bestScoredJob;
    }


}
