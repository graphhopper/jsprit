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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spatial filter that selects the k nearest routes to a job based on location.
 * <p>
 * For each job, calculates the minimum distance from the job's location to any
 * activity in each route, then returns the k routes with smallest distances.
 * <p>
 * The filter can be tuned during algorithm execution via {@link RouteFilterTuner}
 * or manually by calling {@link #setK(int)} and {@link #setFilteringEnabled(boolean)}.
 * <p>
 * Example usage:
 * <pre>
 * AdaptiveSpatialFilter filter = new AdaptiveSpatialFilter(10);  // k=10 nearest routes
 *
 * // With automatic tuning
 * RouteFilterTuner tuner = new RouteFilterTuner(filter)
 *     .setStagnationThreshold(50)
 *     .setExpansionFactor(1.5);
 *
 * VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
 *     .setRouteFilter(filter)
 *     .buildAlgorithm();
 * vra.addListener(tuner);
 * </pre>
 *
 * @author stefan
 * @see RouteFilterTuner
 */
public class AdaptiveSpatialFilter implements InsertionRouteFilter {

    private final int initialK;
    private int k;
    private boolean filteringEnabled = true;

    /**
     * Creates a spatial filter with default k=5.
     */
    public AdaptiveSpatialFilter() {
        this(5);
    }

    /**
     * Creates a spatial filter.
     *
     * @param k number of nearest routes to consider
     */
    public AdaptiveSpatialFilter(int k) {
        this.initialK = k;
        this.k = k;
    }

    /**
     * @deprecated Use {@link #AdaptiveSpatialFilter(int)} instead.
     * The learningRounds parameter is no longer used - use {@link RouteFilterTuner} for adaptive behavior.
     */
    @Deprecated
    public AdaptiveSpatialFilter(int learningRounds, int k) {
        this(k);
    }

    @Override
    public boolean isFilteringEnabled() {
        return filteringEnabled;
    }

    /**
     * Enables or disables filtering.
     *
     * @param enabled true to enable filtering, false to evaluate all routes
     */
    public void setFilteringEnabled(boolean enabled) {
        this.filteringEnabled = enabled;
    }

    /**
     * Sets the number of nearest routes to consider.
     *
     * @param k the number of routes
     */
    public void setK(int k) {
        this.k = k;
    }

    /**
     * Returns the current k value.
     *
     * @return number of nearest routes to consider
     */
    public int getK() {
        return k;
    }

    /**
     * Expands the search space by a factor.
     *
     * @param factor expansion factor (e.g., 1.5 for 50% more routes)
     */
    public void expandSearchSpace(double factor) {
        this.k = (int) Math.ceil(k * factor);
    }

    /**
     * Resets the filter to its initial state.
     */
    public void reset() {
        this.k = initialK;
        this.filteringEnabled = true;
    }

    @Override
    public Collection<VehicleRoute> filterRoutes(Job job, Collection<VehicleRoute> allRoutes) {
        if (!filteringEnabled || allRoutes.size() <= k) {
            return allRoutes;
        }

        Location jobLocation = getJobLocation(job);
        if (jobLocation == null || jobLocation.getCoordinate() == null) {
            return allRoutes;
        }

        Coordinate jobCoord = jobLocation.getCoordinate();

        return allRoutes.stream()
                .map(route -> new AbstractMap.SimpleEntry<>(route, distanceToRoute(jobCoord, route)))
                .sorted(Map.Entry.comparingByValue())
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Gets the k nearest routes to a job.
     *
     * @param job    the job
     * @param routes all routes
     * @param limit  maximum routes to return
     * @return list of nearest routes
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
}
