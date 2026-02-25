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

/**
 * Event emitted when an iteration completes.
 *
 * @param iteration        The iteration number (1-based)
 * @param timestamp        When the event occurred
 * @param newSolutionCost  The cost of the newly created solution
 * @param bestSolutionCost The cost of the best solution so far
 * @param accepted         Whether the new solution was accepted
 * @param strategyName     The name of the strategy used in this iteration
 */
public record IterationCompleted(
        int iteration,
        long timestamp,
        double newSolutionCost,
        double bestSolutionCost,
        boolean accepted,
        String strategyName
) implements AlgorithmEvent {
}
