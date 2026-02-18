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

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Specifies a complete solution.
 * <p>
 * This is pure data - no actual Solution, Route, or Activity objects are referenced.
 * The spec is materialized into a real solution when building against a VRP.
 * <p>
 * Unassigned jobs are derived during materialization: any job in the VRP that
 * is not referenced in any route spec is considered unassigned.
 *
 * @param routes the list of route specifications
 */
public record SolutionSpec(
        List<RouteSpec> routes
) {
    /**
     * Creates a solution spec with the given routes.
     */
    public static SolutionSpec of(RouteSpec... routes) {
        return new SolutionSpec(Arrays.asList(routes));
    }

    /**
     * Creates a solution spec with the given routes.
     */
    public static SolutionSpec of(List<RouteSpec> routes) {
        return new SolutionSpec(List.copyOf(routes));
    }

    /**
     * Creates an empty solution spec (all jobs unassigned).
     */
    public static SolutionSpec empty() {
        return new SolutionSpec(List.of());
    }

    /**
     * Extracts a solution spec from an existing solution.
     * <p>
     * This allows reusing a solution's structure as an initial solution
     * for another VRP run or a modified problem.
     *
     * @param solution the solution to extract from
     * @return a spec representing the solution's structure
     */
    public static SolutionSpec from(VehicleRoutingProblemSolution solution) {
        List<RouteSpec> routeSpecs = new ArrayList<>();

        for (VehicleRoute route : solution.getRoutes()) {
            RouteSpec routeSpec = RouteSpec.from(route);
            if (!routeSpec.activities().isEmpty()) {
                routeSpecs.add(routeSpec);
            }
        }

        return new SolutionSpec(routeSpecs);
    }
}
