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
package com.graphhopper.jsprit.core.algorithm;

/**
 * Interface for search strategy modules that provide dynamic strategy IDs.
 *
 * <p>Modules implementing this interface can provide a strategy ID that reflects
 * the actual operators used in the last execution. This is useful for modules
 * like {@link com.graphhopper.jsprit.core.algorithm.module.IndependentRuinAndRecreateModule}
 * that select operators dynamically at each iteration.</p>
 *
 * <p>The {@link SearchStrategy} will check if a module implements this interface
 * and use the dynamic ID when reporting to {@link com.graphhopper.jsprit.core.algorithm.listener.StrategySelectedListener}.</p>
 */
public interface DynamicStrategyIdProvider {

    /**
     * Returns the strategy ID for the last execution.
     *
     * <p>This method is called after {@link SearchStrategyModule#runAndGetSolution}
     * completes, so implementations should return the ID reflecting the operators
     * that were actually used.</p>
     *
     * @return the dynamic strategy ID, e.g., "independent:radial+regretFast"
     */
    String getLastExecutionStrategyId();
}
