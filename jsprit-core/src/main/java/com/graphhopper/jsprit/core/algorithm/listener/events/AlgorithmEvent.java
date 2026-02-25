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
 * Base interface for all algorithm events.
 * <p>
 * This sealed interface defines all possible events that can occur during
 * algorithm execution, providing full transparency into the search process.
 * <p>
 * Events are organized into categories:
 * - Iteration lifecycle: {@link IterationStarted}, {@link IterationCompleted}
 * - Strategy selection: {@link StrategySelected}
 * - Ruin phase: {@link RuinStarted}, {@link JobRemoved}, {@link RuinCompleted}
 * - Recreate phase: {@link RecreateStarted}, {@link InsertionEvaluated}, {@link JobInserted}, {@link JobUnassigned}, {@link RecreateCompleted}
 * - Acceptance: {@link AcceptanceDecision}
 */
public sealed interface AlgorithmEvent
        permits IterationStarted, StrategySelected, IterationCompleted,
        RuinStarted, JobRemoved, RuinCompleted,
        RecreateStarted, InsertionEvaluated, JobInserted, JobUnassigned, RecreateCompleted,
        AcceptanceDecision {

    /**
     * Get the iteration number this event occurred in.
     * Returns -1 for events that occur outside of iterations (e.g., algorithm start/end).
     */
    int iteration();

    /**
     * Get the timestamp when this event occurred (milliseconds since epoch).
     */
    long timestamp();
}
