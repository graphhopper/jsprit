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
package com.graphhopper.jsprit.core.algorithm.selector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;


public class SelectRandomly implements SolutionSelector {

    private static SelectRandomly selector = null;

    public static SelectRandomly getInstance() {
        if (selector == null) {
            selector = new SelectRandomly();
            return selector;
        }
        return selector;
    }

    private Random random = RandomNumberGeneration.getRandom();

    @Override
    public VehicleRoutingProblemSolution selectSolution(Collection<VehicleRoutingProblemSolution> solutions) {
        if (solutions.isEmpty()) return null;
        List<VehicleRoutingProblemSolution> solList = new ArrayList<VehicleRoutingProblemSolution>(solutions);
        int randomIndex = random.nextInt(solutions.size());
        return solList.get(randomIndex);
    }

    public void setRandom(Random random) {
        this.random = random;
    }

}
