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

/**
 * Event emitted when a job is successfully inserted into a route.
 *
 * @param iteration      The iteration number
 * @param timestamp      When the event occurred
 * @param job            The job that was inserted
 * @param routeId        The ID of the vehicle/route the job was inserted into
 * @param position       The position in the route where the job was inserted
 * @param insertionCost  The total insertion cost
 * @param costBreakdown  The breakdown of insertion costs by component (may be null)
 * @param insertionOrder The order in which this job was inserted (1-based)
 */
public record JobInserted(
        int iteration,
        long timestamp,
        Job job,
        String routeId,
        int position,
        double insertionCost,
        InsertionCostBreakdown costBreakdown,
        int insertionOrder
) implements AlgorithmEvent {
}
