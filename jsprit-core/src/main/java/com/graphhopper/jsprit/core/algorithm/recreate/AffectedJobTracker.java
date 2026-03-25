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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.Coordinate;

import java.util.*;

/**
 * Tracks which jobs are affected by route modifications for efficient regret updates.
 *
 * <p>Key optimization: After inserting a job into route R, only jobs that had R
 * in their top-2 (best or second-best) routes need full recalculation. Other jobs
 * can use cheap lower-bound checks to determine if R became competitive.</p>
 *
 * <p>Combined with spatial filtering:</p>
 * <ul>
 *   <li>Case 1: R not in job's spatial neighborhood → skip entirely</li>
 *   <li>Case 2: R in neighborhood but not in top-2 → cheap lower-bound check</li>
 *   <li>Case 3: R in neighborhood AND in top-2 → full recalculation</li>
 * </ul>
 *
 * <p>Expected speedup: 5-10x for large instances with many jobs and routes.</p>
 */
class AffectedJobTracker {

    private final VehicleRoutingProblem vrp;

    // Reverse index: route → jobs with this route as best
    private final Map<VehicleRoute, Set<Job>> bestRouteJobs;

    // Reverse index: route → jobs with this route as 2nd-best
    private final Map<VehicleRoute, Set<Job>> secondBestRouteJobs;

    // Reverse index: route → jobs that have this route in their spatial neighborhood
    private final Map<VehicleRoute, Set<Job>> routeNeighborJobs;

    // For each job: current best route
    private final Map<Job, VehicleRoute> jobBestRoute;

    // For each job: current 2nd-best route
    private final Map<Job, VehicleRoute> jobSecondBestRoute;

    // For each job: current 2nd-best cost (for lower-bound pruning)
    private final Map<Job, Double> jobSecondBestCost;

    // Cached route centroids for lower-bound calculations
    private final Map<VehicleRoute, Coordinate> routeCentroids;

    AffectedJobTracker(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
        this.bestRouteJobs = new HashMap<>();
        this.secondBestRouteJobs = new HashMap<>();
        this.routeNeighborJobs = new HashMap<>();
        this.jobBestRoute = new HashMap<>();
        this.jobSecondBestRoute = new HashMap<>();
        this.jobSecondBestCost = new HashMap<>();
        this.routeCentroids = new HashMap<>();
    }

    /**
     * Updates tracking for a job based on its current BoundedInsertionQueue state.
     * Call this after computing insertion costs for a job.
     *
     * @param job the job
     * @param queue the job's insertion queue
     */
    void updateJobTracking(Job job, BoundedInsertionQueue queue) {
        // Remove from old reverse indices
        VehicleRoute oldBest = jobBestRoute.remove(job);
        VehicleRoute oldSecondBest = jobSecondBestRoute.remove(job);

        if (oldBest != null) {
            Set<Job> jobs = bestRouteJobs.get(oldBest);
            if (jobs != null) jobs.remove(job);
        }
        if (oldSecondBest != null) {
            Set<Job> jobs = secondBestRouteJobs.get(oldSecondBest);
            if (jobs != null) jobs.remove(job);
        }

        // Get current best and 2nd-best
        BoundedInsertionQueue.Entry best = queue.getBest();
        BoundedInsertionQueue.Entry secondBest = queue.getSecondBest();

        // Update forward indices
        if (best != null) {
            VehicleRoute bestRoute = best.getRoute();
            jobBestRoute.put(job, bestRoute);
            bestRouteJobs.computeIfAbsent(bestRoute, k -> new HashSet<>()).add(job);
        }

        if (secondBest != null) {
            VehicleRoute secondBestRoute = secondBest.getRoute();
            jobSecondBestRoute.put(job, secondBestRoute);
            secondBestRouteJobs.computeIfAbsent(secondBestRoute, k -> new HashSet<>()).add(job);
            jobSecondBestCost.put(job, secondBest.getCost());
        } else if (best != null) {
            // Only one option - use MAX_VALUE for 2nd best cost
            jobSecondBestCost.put(job, Double.MAX_VALUE);
        }
    }

    /**
     * Registers a job's spatial neighborhood (which routes it considers).
     * Call this during initial computation.
     *
     * @param job the job
     * @param routes the routes in the job's spatial neighborhood
     */
    void registerSpatialNeighborhood(Job job, Collection<VehicleRoute> routes) {
        for (VehicleRoute route : routes) {
            routeNeighborJobs.computeIfAbsent(route, k -> new HashSet<>()).add(job);
        }
    }

    /**
     * Gets the set of jobs that are "affected" by a route modification.
     * These are jobs that had the route in their top-2 (best or second-best).
     *
     * @param modifiedRoute the route that was modified
     * @return set of affected jobs (may be empty but never null)
     */
    Set<Job> getAffectedJobs(VehicleRoute modifiedRoute) {
        Set<Job> affected = new HashSet<>();

        Set<Job> bestJobs = bestRouteJobs.get(modifiedRoute);
        if (bestJobs != null) {
            affected.addAll(bestJobs);
        }

        Set<Job> secondBestJobs = secondBestRouteJobs.get(modifiedRoute);
        if (secondBestJobs != null) {
            affected.addAll(secondBestJobs);
        }

        return affected;
    }

    /**
     * Gets jobs that have the route in their spatial neighborhood but NOT in top-2.
     * These jobs only need a cheap lower-bound check.
     *
     * @param modifiedRoute the route that was modified
     * @param affectedJobs jobs already identified as affected (in top-2)
     * @return set of jobs needing cheap check
     */
    Set<Job> getJobsNeedingCheapCheck(VehicleRoute modifiedRoute, Set<Job> affectedJobs) {
        Set<Job> neighborJobs = routeNeighborJobs.get(modifiedRoute);
        if (neighborJobs == null || neighborJobs.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Job> result = new HashSet<>(neighborJobs);
        result.removeAll(affectedJobs);
        return result;
    }

    /**
     * Checks if a route could be competitive for a job using a cheap lower-bound.
     * Returns true if the route might improve the job's top-2, false if definitely not.
     *
     * @param job the job
     * @param route the route to check
     * @return true if route might be competitive, false if definitely not
     */
    boolean couldBeCompetitive(Job job, VehicleRoute route) {
        Double secondBestCost = jobSecondBestCost.get(job);
        if (secondBestCost == null || secondBestCost == Double.MAX_VALUE) {
            return true; // No 2nd-best, any insertion could help
        }

        // Use distance lower-bound
        double lowerBound = computeDistanceLowerBound(job, route);
        return lowerBound < secondBestCost;
    }

    /**
     * Computes a lower-bound on insertion cost based on distance.
     * This is a cheap approximation - actual cost may be higher due to time windows, etc.
     */
    private double computeDistanceLowerBound(Job job, VehicleRoute route) {
        Location jobLocation = getJobLocation(job);
        if (jobLocation == null || jobLocation.getCoordinate() == null) {
            return 0; // Can't compute bound without coordinates
        }

        Coordinate routeCentroid = getRouteCentroid(route);
        if (routeCentroid == null) {
            return 0;
        }

        // Minimum marginal distance: at best, job is inserted between two adjacent points
        // Lower bound is approximately the distance to the nearest point in the route
        return minDistanceToRoute(jobLocation.getCoordinate(), route);
    }

    private double minDistanceToRoute(Coordinate jobCoord, VehicleRoute route) {
        double minDist = Double.MAX_VALUE;

        if (route.getStart() != null && route.getStart().getLocation() != null) {
            Coordinate c = route.getStart().getLocation().getCoordinate();
            if (c != null) {
                minDist = Math.min(minDist, euclideanDistance(jobCoord, c));
            }
        }

        for (TourActivity act : route.getActivities()) {
            if (act.getLocation() != null && act.getLocation().getCoordinate() != null) {
                minDist = Math.min(minDist, euclideanDistance(jobCoord, act.getLocation().getCoordinate()));
            }
        }

        if (route.getEnd() != null && route.getEnd().getLocation() != null) {
            Coordinate c = route.getEnd().getLocation().getCoordinate();
            if (c != null) {
                minDist = Math.min(minDist, euclideanDistance(jobCoord, c));
            }
        }

        return minDist == Double.MAX_VALUE ? 0 : minDist;
    }

    private Coordinate getRouteCentroid(VehicleRoute route) {
        Coordinate cached = routeCentroids.get(route);
        if (cached != null) {
            return cached;
        }

        double sumX = 0, sumY = 0;
        int count = 0;

        for (TourActivity act : route.getActivities()) {
            if (act.getLocation() != null && act.getLocation().getCoordinate() != null) {
                sumX += act.getLocation().getCoordinate().getX();
                sumY += act.getLocation().getCoordinate().getY();
                count++;
            }
        }

        if (count == 0) {
            return null;
        }

        Coordinate centroid = Coordinate.newInstance(sumX / count, sumY / count);
        routeCentroids.put(route, centroid);
        return centroid;
    }

    private Location getJobLocation(Job job) {
        if (job instanceof Service) {
            return ((Service) job).getLocation();
        } else if (job instanceof Shipment) {
            return ((Shipment) job).getPickupLocation();
        }
        return null;
    }

    private double euclideanDistance(Coordinate c1, Coordinate c2) {
        double dx = c1.getX() - c2.getX();
        double dy = c1.getY() - c2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Clears the route centroid cache for a modified route.
     * Call this after a route is modified.
     */
    void invalidateRouteCentroid(VehicleRoute route) {
        routeCentroids.remove(route);
    }

    /**
     * Removes all tracking for a job (when it gets inserted).
     */
    void removeJob(Job job) {
        VehicleRoute bestRoute = jobBestRoute.remove(job);
        VehicleRoute secondBestRoute = jobSecondBestRoute.remove(job);
        jobSecondBestCost.remove(job);

        if (bestRoute != null) {
            Set<Job> jobs = bestRouteJobs.get(bestRoute);
            if (jobs != null) jobs.remove(job);
        }
        if (secondBestRoute != null) {
            Set<Job> jobs = secondBestRouteJobs.get(secondBestRoute);
            if (jobs != null) jobs.remove(job);
        }

        // Remove from all spatial neighborhoods
        for (Set<Job> neighbors : routeNeighborJobs.values()) {
            neighbors.remove(job);
        }
    }

    /**
     * Clears all tracking data.
     */
    void clear() {
        bestRouteJobs.clear();
        secondBestRouteJobs.clear();
        routeNeighborJobs.clear();
        jobBestRoute.clear();
        jobSecondBestRoute.clear();
        jobSecondBestCost.clear();
        routeCentroids.clear();
    }

    /**
     * Returns statistics for debugging/logging.
     */
    String getStats() {
        int totalJobsTracked = jobBestRoute.size();
        int avgJobsPerRoute = bestRouteJobs.isEmpty() ? 0 :
            bestRouteJobs.values().stream().mapToInt(Set::size).sum() / bestRouteJobs.size();
        return String.format("jobs=%d, avgJobsPerRoute=%d, routesTracked=%d",
            totalJobsTracked, avgJobsPerRoute, bestRouteJobs.size());
    }
}
