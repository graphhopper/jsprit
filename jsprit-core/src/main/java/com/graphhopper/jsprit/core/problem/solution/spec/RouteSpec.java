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

package com.graphhopper.jsprit.core.problem.solution.spec;

import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Specifies one route in a solution.
 * <p>
 * This is pure data - no actual VehicleRoute or Activity objects are referenced.
 * The spec is materialized into a real route when building the solution.
 *
 * @param vehicleId  the ID of the vehicle for this route
 * @param activities the ordered list of activities in this route
 */
public record RouteSpec(
        String vehicleId,
        List<ActivitySpec> activities
) {
    /**
     * Creates a route spec with the given activities.
     */
    public static RouteSpec of(String vehicleId, ActivitySpec... activities) {
        return new RouteSpec(vehicleId, Arrays.asList(activities));
    }

    /**
     * Creates a route spec with the given activities.
     */
    public static RouteSpec of(String vehicleId, List<ActivitySpec> activities) {
        return new RouteSpec(vehicleId, List.copyOf(activities));
    }

    /**
     * Extracts a route spec from an existing route.
     *
     * @param route the route to extract from
     * @return a spec representing the route's structure
     */
    public static RouteSpec from(VehicleRoute route) {
        String vehicleId = route.getVehicle().getId();
        List<ActivitySpec> activitySpecs = new ArrayList<>();

        for (TourActivity activity : route.getActivities()) {
            if (activity instanceof TourActivity.JobActivity jobActivity) {
                ActivitySpec spec = ActivitySpec.from(jobActivity);
                activitySpecs.add(spec);
            }
        }

        return new RouteSpec(vehicleId, activitySpecs);
    }
}
