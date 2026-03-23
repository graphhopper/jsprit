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

/**
 * Adapter that wraps a legacy RegretScoringFunction to work with the new
 * RegretKScoringFunction interface. Enables backward compatibility.
 */
public class RegretKScoringFunctionAdapter implements RegretKScoringFunction {

    private final RegretScoringFunction legacyScoringFunction;

    public RegretKScoringFunctionAdapter(RegretScoringFunction legacyScoringFunction) {
        this.legacyScoringFunction = legacyScoringFunction;
    }

    @Override
    public double score(RegretKAlternatives alternatives, Job job) {
        if (alternatives == null || alternatives.isEmpty()) {
            throw new IllegalStateException("cannot score job " + job.getId() + " - no alternatives");
        }

        RegretKAlternatives.Alternative best = alternatives.getBest();
        RegretKAlternatives.Alternative secondBest = alternatives.getSecondBest();

        InsertionData bestData = best != null ? best.getInsertionData() : null;
        InsertionData secondBestData = secondBest != null ? secondBest.getInsertionData() : null;

        return legacyScoringFunction.score(bestData, secondBestData, job);
    }

    public RegretScoringFunction getLegacyScoringFunction() {
        return legacyScoringFunction;
    }
}
