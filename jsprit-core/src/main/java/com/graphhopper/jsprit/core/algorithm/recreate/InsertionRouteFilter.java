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

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;

/**
 * Interface for filtering routes to consider during insertion.
 * <p>
 * Route filtering reduces the number of routes evaluated for each job insertion,
 * reducing complexity from O(R) to O(k) where R is the total number of routes
 * and k is the number of routes selected by the filter.
 * <p>
 * Implementations can use different strategies:
 * <ul>
 *   <li>Spatial filtering - select routes nearest to the job location</li>
 *   <li>Capacity-based filtering - select routes with sufficient remaining capacity</li>
 *   <li>Time-based filtering - select routes compatible with job time windows</li>
 *   <li>Custom strategies</li>
 * </ul>
 *
 * @author stefan
 * @see AdaptiveSpatialFilter
 */
public interface InsertionRouteFilter {

    /**
     * Check if filtering is currently enabled.
     * When disabled, all routes should be evaluated.
     *
     * @return true if filtering is enabled
     */
    boolean isFilteringEnabled();

    /**
     * Filter routes to consider for a job insertion.
     * <p>
     * Returns the subset of routes that should be evaluated for inserting the given job.
     *
     * @param job       the job to insert
     * @param allRoutes all available routes
     * @return filtered collection of routes to evaluate, or the original collection if no filtering
     */
    Collection<VehicleRoute> filterRoutes(Job job, Collection<VehicleRoute> allRoutes);
}
