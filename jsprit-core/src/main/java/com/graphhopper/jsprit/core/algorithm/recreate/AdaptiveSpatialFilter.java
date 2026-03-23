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
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adaptive spatial filter that learns whether spatial filtering is effective for a given problem.
 * <p>
 * During the learning phase, it compares filtered vs full search results to determine:
 * - Hit rate: How often filtering finds the same best route as full search
 * - Cost deviation: When filtering misses, how much worse is the cost
 * - Miss rate: How often filtering misses feasible insertions entirely
 * <p>
 * Based on learning results, it adaptively enables/disables filtering:
 * - If filtering works well (high hit rate, low cost deviation): use 95% filtering
 * - If filtering works partially: use 50% filtering with larger k
 * - If filtering doesn't work: disable entirely
 * <p>
 * Thread-safety: This class is NOT thread-safe. Each thread should have its own instance,
 * or external synchronization is required.
 */
public class AdaptiveSpatialFilter {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveSpatialFilter.class);

    // Configuration
    private final int learningRounds;
    private final double acceptableHitRate;
    private final double acceptableCostDeviation;
    private final int initialK;
    private final int maxK;

    // Learning state
    private int totalComparisons = 0;
    private int filteringHits = 0;
    private double cumulativeCostDeviation = 0;
    private int filteringMisses = 0;

    // Adaptive state (volatile for thread visibility)
    private volatile boolean learningComplete = false;
    private volatile boolean filteringEnabled = true;
    private volatile int k;

    /**
     * Result of filtering decision.
     */
    public static class FilterResult {
        private final List<VehicleRoute> routes;
        private final boolean needsComparison;
        private final boolean wasFiltered;

        FilterResult(Collection<VehicleRoute> routes, boolean needsComparison, boolean wasFiltered) {
            this.routes = new ArrayList<>(routes);
            this.needsComparison = needsComparison;
            this.wasFiltered = wasFiltered;
        }

        public List<VehicleRoute> getRoutes() {
            return routes;
        }

        public boolean needsComparison() {
            return needsComparison;
        }

        public boolean wasFiltered() {
            return wasFiltered;
        }
    }

    /**
     * Creates an adaptive spatial filter with default settings.
     */
    public AdaptiveSpatialFilter() {
        this(50, 5);
    }

    /**
     * Creates an adaptive spatial filter.
     *
     * @param learningRounds number of comparison rounds for learning
     * @param initialK       initial number of nearest routes to consider
     */
    public AdaptiveSpatialFilter(int learningRounds, int initialK) {
        this.learningRounds = learningRounds;
        this.initialK = initialK;
        this.k = initialK;
        this.maxK = initialK * 4;
        this.acceptableHitRate = 0.85;
        this.acceptableCostDeviation = 0.02;
    }

    /**
     * Decides whether to use filtering for this job and returns relevant routes.
     * <p>
     * During learning phase: returns full routes but marks for comparison.
     * After learning: deterministically applies filtering based on learned effectiveness.
     *
     * @param job    the job to find routes for
     * @param routes all available routes
     * @return FilterResult containing routes to consider and whether comparison is needed
     */
    public FilterResult getRelevantRoutes(Job job, Collection<VehicleRoute> routes) {
        // Not enough routes to benefit from filtering
        if (routes.size() <= k) {
            return new FilterResult(routes, false, false);
        }

        // Can't filter if job has no location
        Location jobLocation = getJobLocation(job);
        if (jobLocation == null || jobLocation.getCoordinate() == null) {
            return new FilterResult(routes, false, false);
        }

        if (!learningComplete) {
            return learningPhase(job, routes);
        } else {
            return productionPhase(job, routes);
        }
    }

    private FilterResult learningPhase(Job job, Collection<VehicleRoute> routes) {
        // During learning, always return full routes but mark for comparison
        return new FilterResult(routes, true, false);
    }

    private FilterResult productionPhase(Job job, Collection<VehicleRoute> routes) {
        if (!filteringEnabled) {
            return new FilterResult(routes, false, false);
        }

        // After learning, always use filtering deterministically (no random sampling)
        // This ensures same input produces same output
        List<VehicleRoute> filtered = getNearestRoutes(job, routes, k);
        return new FilterResult(filtered, false, true);
    }

    /**
     * Gets the k nearest routes to a job based on minimum distance to any activity.
     */
    public List<VehicleRoute> getNearestRoutes(Job job, Collection<VehicleRoute> routes, int limit) {
        Location jobLocation = getJobLocation(job);
        if (jobLocation == null || jobLocation.getCoordinate() == null) {
            return new ArrayList<>(routes);
        }

        Coordinate jobCoord = jobLocation.getCoordinate();

        return routes.stream()
                .map(route -> new AbstractMap.SimpleEntry<>(route, distanceToRoute(jobCoord, route)))
                .sorted(Map.Entry.comparingByValue())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Calculates minimum distance from a coordinate to any activity in a route.
     */
    private double distanceToRoute(Coordinate jobCoord, VehicleRoute route) {
        double minDistance = Double.MAX_VALUE;

        // Check start location
        if (route.getStart() != null && route.getStart().getLocation() != null) {
            Coordinate startCoord = route.getStart().getLocation().getCoordinate();
            if (startCoord != null) {
                minDistance = Math.min(minDistance, euclideanDistance(jobCoord, startCoord));
            }
        }

        // Check all activities
        for (TourActivity activity : route.getActivities()) {
            if (activity.getLocation() != null && activity.getLocation().getCoordinate() != null) {
                double dist = euclideanDistance(jobCoord, activity.getLocation().getCoordinate());
                minDistance = Math.min(minDistance, dist);
            }
        }

        // Check end location
        if (route.getEnd() != null && route.getEnd().getLocation() != null) {
            Coordinate endCoord = route.getEnd().getLocation().getCoordinate();
            if (endCoord != null) {
                minDistance = Math.min(minDistance, euclideanDistance(jobCoord, endCoord));
            }
        }

        return minDistance;
    }

    private double euclideanDistance(Coordinate c1, Coordinate c2) {
        double dx = c1.getX() - c2.getX();
        double dy = c1.getY() - c2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Records comparison data for learning.
     * This method is thread-safe for use in concurrent execution.
     *
     * @param filteredBest  best insertion from filtered routes (null if none found)
     * @param filteredRoute route for filtered best (null if none found)
     * @param fullBest      best insertion from full search (null if none found)
     * @param fullRoute     route for full best (null if none found)
     */
    public synchronized void recordComparison(InsertionData filteredBest, VehicleRoute filteredRoute,
                                              InsertionData fullBest, VehicleRoute fullRoute) {
        if (learningComplete) {
            return; // No need to record after learning is complete
        }

        totalComparisons++;

        boolean filteredFound = filteredBest != null && !(filteredBest instanceof InsertionData.NoInsertionFound);
        boolean fullFound = fullBest != null && !(fullBest instanceof InsertionData.NoInsertionFound);

        if (!fullFound) {
            // No feasible insertion exists - filtering can't miss what doesn't exist
            filteringHits++;
        } else if (!filteredFound) {
            // Filtering missed a feasible insertion entirely - this is bad
            filteringMisses++;
        } else {
            // Both found something - compare
            boolean sameRoute = (filteredRoute == fullRoute);
            double filteredCost = filteredBest.getInsertionCost();
            double fullCost = fullBest.getInsertionCost();
            boolean sameCost = Math.abs(filteredCost - fullCost) < 0.001;

            if (sameRoute || sameCost) {
                filteringHits++;
            } else {
                // Different result - track cost deviation
                double deviation = (filteredCost - fullCost) / Math.abs(fullCost);
                cumulativeCostDeviation += Math.max(0, deviation);
            }
        }

        // Check if learning is complete
        if (totalComparisons >= learningRounds) {
            completeLearning();
        }
    }

    private void completeLearning() {
        learningComplete = true;

        double hitRate = (double) filteringHits / totalComparisons;
        double avgCostDeviation = cumulativeCostDeviation / totalComparisons;
        double missRate = (double) filteringMisses / totalComparisons;

        if (missRate > 0.05) {
            // Filtering misses feasible insertions too often - dangerous
            filteringEnabled = false;
            logger.info("Spatial filtering DISABLED: miss rate {}% too high (learned from {} comparisons)",
                    String.format("%.1f", missRate * 100), totalComparisons);
        } else if (hitRate >= acceptableHitRate && avgCostDeviation <= acceptableCostDeviation) {
            // Filtering works well - enable at 100% (deterministic)
            filteringEnabled = true;
            logger.info("Spatial filtering ENABLED: hit rate {}%, cost deviation {}%, k={}",
                    String.format("%.1f", hitRate * 100),
                    String.format("%.2f", avgCostDeviation * 100), k);
        } else if (hitRate >= 0.7) {
            // Filtering works but not great - enable with larger k
            filteringEnabled = true;
            int oldK = k;
            k = Math.min(k * 2, maxK);
            logger.info("Spatial filtering ENABLED with larger k: hit rate {}%, k increased from {} to {}",
                    String.format("%.1f", hitRate * 100), oldK, k);
        } else {
            // Filtering doesn't work well for this problem
            filteringEnabled = false;
            logger.info("Spatial filtering DISABLED: hit rate {}% too low (learned from {} comparisons)",
                    String.format("%.1f", hitRate * 100), totalComparisons);
        }
    }

    /**
     * Gets the primary location of a job.
     */
    private Location getJobLocation(Job job) {
        if (job instanceof Service) {
            return ((Service) job).getLocation();
        } else if (job instanceof Shipment) {
            return ((Shipment) job).getPickupLocation();
        }
        return null;
    }

    /**
     * Returns whether learning is complete.
     */
    public boolean isLearningComplete() {
        return learningComplete;
    }

    /**
     * Returns whether filtering is currently enabled.
     */
    public boolean isFilteringEnabled() {
        return filteringEnabled;
    }

    /**
     * Returns the current k value (number of nearest routes to consider).
     */
    public int getK() {
        return k;
    }

    /**
     * Returns learning statistics for debugging.
     */
    public String getStats() {
        if (totalComparisons == 0) {
            return "No comparisons yet";
        }
        double hitRate = (double) filteringHits / totalComparisons;
        double avgCostDeviation = cumulativeCostDeviation / totalComparisons;
        double missRate = (double) filteringMisses / totalComparisons;
        return String.format("comparisons=%d, hitRate=%.1f%%, missRate=%.1f%%, avgCostDev=%.2f%%, enabled=%s, k=%d",
                totalComparisons, hitRate * 100, missRate * 100, avgCostDeviation * 100,
                filteringEnabled, k);
    }

    /**
     * Resets the filter to initial state for a new problem.
     */
    public void reset() {
        totalComparisons = 0;
        filteringHits = 0;
        cumulativeCostDeviation = 0;
        filteringMisses = 0;
        learningComplete = false;
        filteringEnabled = true;
        k = initialK;
    }
}
