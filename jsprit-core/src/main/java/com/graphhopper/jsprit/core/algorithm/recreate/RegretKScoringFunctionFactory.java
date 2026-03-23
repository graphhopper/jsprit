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

/**
 * Factory for creating RegretKScoringFunction instances.
 */
public class RegretKScoringFunctionFactory {

    /**
     * Available regret-k scoring strategies.
     */
    public enum Strategy {
        /**
         * Sum of regrets: score = sum(c_i - c_1) for i=2..k
         */
        SUM,
        /**
         * Maximum regret: score = c_k - c_1
         */
        MAX,
        /**
         * Average regret: score = mean(c_2..c_k) - c_1
         */
        AVG
    }

    /**
     * Creates a regret-k scoring function with the specified strategy.
     *
     * @param strategy         the scoring strategy to use
     * @param k                the number of alternatives to consider (use -1 or Integer.MAX_VALUE for "all")
     * @param additionalScorer additional scoring function for tie-breaking (can be null)
     * @return the configured scoring function
     */
    public static RegretKScoringFunction create(Strategy strategy, int k, ScoringFunction additionalScorer) {
        // Normalize k: -1 means "all"
        int effectiveK = (k <= 0) ? Integer.MAX_VALUE : k;

        switch (strategy) {
            case SUM:
                return new SumRegretKScoringFunction(additionalScorer, effectiveK);
            case MAX:
                return new MaxRegretKScoringFunction(additionalScorer, effectiveK);
            case AVG:
                return new AverageRegretKScoringFunction(additionalScorer, effectiveK);
            default:
                throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }
    }

    /**
     * Creates a regret-k scoring function from a strategy name string.
     *
     * @param strategyName     the strategy name ("sum", "max", or "avg")
     * @param k                the number of alternatives to consider
     * @param additionalScorer additional scoring function for tie-breaking (can be null)
     * @return the configured scoring function
     */
    public static RegretKScoringFunction create(String strategyName, int k, ScoringFunction additionalScorer) {
        Strategy strategy = parseStrategy(strategyName);
        return create(strategy, k, additionalScorer);
    }

    /**
     * Parses a strategy name string to a Strategy enum.
     *
     * @param strategyName the strategy name (case-insensitive)
     * @return the corresponding Strategy enum
     * @throws IllegalArgumentException if the strategy name is not recognized
     */
    public static Strategy parseStrategy(String strategyName) {
        if (strategyName == null) {
            return Strategy.SUM; // default
        }
        switch (strategyName.toLowerCase().trim()) {
            case "sum":
                return Strategy.SUM;
            case "max":
                return Strategy.MAX;
            case "avg":
            case "average":
                return Strategy.AVG;
            default:
                throw new IllegalArgumentException("Unknown regret-k strategy: " + strategyName +
                        ". Valid options are: sum, max, avg");
        }
    }

    /**
     * Creates a backward-compatible regret-2 scoring function using the sum strategy.
     *
     * @param additionalScorer additional scoring function for tie-breaking
     * @return a regret-2 sum scoring function
     */
    public static RegretKScoringFunction createRegret2(ScoringFunction additionalScorer) {
        return create(Strategy.SUM, 2, additionalScorer);
    }
}
