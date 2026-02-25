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
 * Emitted when a search strategy is selected for the current iteration,
 * before the strategy runs (ruin/recreate).
 * <p>
 * This event allows listeners to know which strategy will be used for
 * the upcoming ruin/recreate operations.
 *
 * @param iteration  The iteration number
 * @param timestamp  When the strategy was selected
 * @param strategyId The ID/name of the selected strategy
 */
public record StrategySelected(
        int iteration,
        long timestamp,
        String strategyId
) implements AlgorithmEvent {
}
