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
package com.graphhopper.jsprit.core.algorithm.listener.events;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionCostBreakdown;
import com.graphhopper.jsprit.core.problem.job.Job;

import java.util.List;

/**
 * Event emitted when an insertion is evaluated for a job.
 * <p>
 * This provides full transparency into the insertion decision,
 * including cost breakdown and why certain routes were rejected.
 *
 * @param iteration         The iteration number
 * @param timestamp         When the event occurred
 * @param job               The job being inserted
 * @param routeId           The route being evaluated (vehicle ID)
 * @param position          The insertion position in the route
 * @param cost              The total insertion cost (Double.MAX_VALUE if infeasible)
 * @param breakdown         The cost breakdown (null if infeasible)
 * @param feasible          Whether the insertion is feasible
 * @param failedConstraints Names of constraints that failed (empty if feasible)
 * @param chosen            Whether this insertion was chosen as the best
 */
public record InsertionEvaluated(
        int iteration,
        long timestamp,
        Job job,
        String routeId,
        int position,
        double cost,
        InsertionCostBreakdown breakdown,
        boolean feasible,
        List<String> failedConstraints,
        boolean chosen
) implements AlgorithmEvent {
}
