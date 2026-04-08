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

import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.List;

/**
 * Interface for filtering insertion positions within a route.
 * <p>
 * Position filtering reduces the O(p^2) complexity of shipment insertion by only evaluating
 * a subset of candidate positions. This is especially beneficial for routes with many activities.
 * <p>
 * Implementations can use different strategies:
 * <ul>
 *   <li>Spatial filtering - select positions nearest to the job location</li>
 *   <li>Cost-based filtering - select positions with lowest marginal insertion cost</li>
 *   <li>Custom strategies</li>
 * </ul>
 * <p>
 * Filter parameters can be adjusted at runtime to expand/contract the search space during optimization.
 *
 * @author stefan
 */
public interface InsertionPositionFilter {

    /**
     * Check if filtering is currently enabled.
     * When disabled, all positions should be evaluated.
     *
     * @return true if filtering is enabled
     */
    boolean isFilteringEnabled();

    /**
     * Filter pickup positions for a shipment insertion.
     * <p>
     * Position i means inserting between activity i-1 and activity i.
     * Valid positions are 0 to activities.size() (inclusive).
     *
     * @param shipment   the shipment to insert
     * @param route      the route to insert into
     * @param activities the current activities in the route (excluding start/end)
     * @return list of position indices to evaluate, or null to evaluate all positions
     */
    List<Integer> filterPickupPositions(Shipment shipment, VehicleRoute route, List<TourActivity> activities);

    /**
     * Filter delivery positions for a shipment insertion.
     * <p>
     * Position j means inserting between activity j-1 and activity j (after pickup insertion).
     * Valid positions are pickupPos to activities.size() (inclusive).
     *
     * @param shipment   the shipment to insert
     * @param route      the route to insert into
     * @param activities the current activities in the route (excluding start/end)
     * @param pickupPos  the position where pickup will be inserted
     * @return list of position indices to evaluate, or null to evaluate all positions
     */
    List<Integer> filterDeliveryPositions(Shipment shipment, VehicleRoute route, List<TourActivity> activities, int pickupPos);

    /**
     * Filter positions for a service insertion.
     * <p>
     * Default implementation returns null (no filtering) since services are O(p) not O(p^2).
     *
     * @param service    the service to insert
     * @param route      the route to insert into
     * @param activities the current activities in the route (excluding start/end)
     * @return list of position indices to evaluate, or null to evaluate all positions
     */
    default List<Integer> filterServicePositions(Service service, VehicleRoute route, List<TourActivity> activities) {
        return null;
    }

    /**
     * Check if timing propagation is required for constraint checking.
     * <p>
     * When false, the calculator can jump directly to any position without computing
     * intermediate timing. This is valid for problems without time windows or
     * time-based constraints (e.g., pure capacitated VRP).
     * <p>
     * Default is true (conservative - always propagate timing).
     *
     * @return true if timing must be propagated sequentially
     */
    default boolean isTimingRequired() {
        return true;
    }
}
