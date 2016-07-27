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
class InsertionNoiseMaker implements SoftActivityConstraint, IterationStartsListener {

    private final double noiseProbability;

    private boolean makeNoise = false;

    private double noiseLevel = 0.1;

    private Random random = RandomNumberGeneration.newInstance();

//    private Random[] randomArray;

    private double maxCosts;

    InsertionNoiseMaker(VehicleRoutingProblem vrp, double maxCosts, double noiseLevel, double noiseProbability) {
        this.noiseLevel = noiseLevel;
        this.noiseProbability = noiseProbability;
        this.maxCosts = maxCosts;
//        randomArray = new Random[vrp.getNuActivities() + 2];
//        for (int i = 0; i < randomArray.length; i++) {
//            Random r = new Random();
//            r.setSeed(random.nextLong());
//            randomArray[i] = r;
//        }
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        if (random.nextDouble() < noiseProbability) {
            makeNoise = true;
        } else makeNoise = false;
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if (makeNoise) {
            return noiseLevel * maxCosts * random.nextDouble();
        }
        return 0;
    }

    public void setRandom(Random random) {
        this.random = random;
    }
}
