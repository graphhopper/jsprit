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

import com.graphhopper.jsprit.core.problem.job.Job;

import java.util.List;

/**
 * Max-based regret-k scoring function.
 * <p>
 * Computes: score = c_k - c_1 (maximum difference)
 * <p>
 * This variant uses the maximum regret - the difference between the k-th best
 * alternative and the best. This focuses on the worst-case scenario.
 */
public class MaxRegretKScoringFunction implements RegretKScoringFunction {

    private final ScoringFunction additionalScorer;
    private final int k;

    /**
     * Creates a max regret-k scoring function.
     *
     * @param additionalScorer additional scoring function for tie-breaking (can be null)
     * @param k                the number of alternatives to consider (use Integer.MAX_VALUE for "all")
     */
    public MaxRegretKScoringFunction(ScoringFunction additionalScorer, int k) {
        this.additionalScorer = additionalScorer;
        this.k = k;
    }

    @Override
    public double score(RegretKAlternatives alternatives, Job job) {
        if (alternatives == null || alternatives.isEmpty()) {
            throw new IllegalStateException("cannot score job " + job.getId());
        }

        RegretKAlternatives.Alternative best = alternatives.getBest();
        double bestCost = best.getCost();

        List<RegretKAlternatives.Alternative> topK = alternatives.getTopK(k);
        double score;

        if (topK.size() == 1) {
            // Only one alternative - give highest priority
            score = (11 - job.getPriority()) * (Integer.MAX_VALUE - bestCost);
        } else {
            // Max regret: c_k - c_1 (using the last element of topK)
            double kthCost = topK.get(topK.size() - 1).getCost();
            score = (11 - job.getPriority()) * (kthCost - bestCost);
        }

        // Add additional scoring for tie-breaking
        if (additionalScorer != null) {
            score += additionalScorer.score(best.getInsertionData(), job);
        }

        return score;
    }

    public int getK() {
        return k;
    }
}
