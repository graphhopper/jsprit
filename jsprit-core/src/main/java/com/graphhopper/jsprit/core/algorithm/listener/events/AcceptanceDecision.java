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
 * Event emitted when a solution acceptance decision is made.
 *
 * @param iteration  The iteration number
 * @param timestamp  When the event occurred
 * @param oldCost    The cost of the previous solution
 * @param newCost    The cost of the new solution
 * @param accepted   Whether the new solution was accepted
 * @param strategyId The ID of the search strategy that produced this solution
 * @param isNewBest  Whether this is a new best solution overall
 */
public record AcceptanceDecision(
        int iteration,
        long timestamp,
        double oldCost,
        double newCost,
        boolean accepted,
        String strategyId,
        boolean isNewBest
) implements AlgorithmEvent {
}
