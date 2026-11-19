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

package com.graphhopper.jsprit.core.algorithm.box;

import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.SoftActivityConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;

import java.util.Collection;
import java.util.Random;

/**
 * Created by schroeder on 16/01/15.
 */
class ConcurrentInsertionNoiseMaker implements SoftActivityConstraint, IterationStartsListener {

    private final double noiseProbability;

    private volatile boolean makeNoise = false;

    private final double noiseLevel;

    private Random random = RandomNumberGeneration.newInstance();

    private final double maxCosts;

    /** Drawn once per iteration; with the three activities of a position it determines the position's noise. */
    private volatile long iterationSeed;

    ConcurrentInsertionNoiseMaker(VehicleRoutingProblem vrp, double maxCosts, double noiseLevel, double noiseProbability) {
        this.noiseLevel = noiseLevel;
        this.noiseProbability = noiseProbability;
        this.maxCosts = maxCosts;
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        makeNoise = random.nextDouble() < noiseProbability;
        iterationSeed = random.nextLong();
    }

    /**
     * The noise of a position is a function of the iteration and of the activities before, at and behind the
     * position - not the next value of a random generator. Positions are evaluated by several threads, and with a
     * generator the noise of a position depended on how many evaluations had run before it and in which order, so
     * two runs of the same problem with the same seed differed.
     */
    @Override
    public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if (!makeNoise) return 0;
        long h = iterationSeed;
        h = mix(h ^ (newAct.getIndex() * 0x9E3779B97F4A7C15L));
        h = mix(h ^ (prevAct.getIndex() * 0xC2B2AE3D27D4EB4FL));
        h = mix(h ^ (nextAct.getIndex() * 0x165667B19E3779F9L));
        return noiseLevel * maxCosts * ((h >>> 11) * 0x1.0p-53);
    }

    /** The finalizer of SplitMix64: spreads the bits of its argument over the whole word. */
    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public void setRandom(Random random) {
        this.random = random;
    }

}
