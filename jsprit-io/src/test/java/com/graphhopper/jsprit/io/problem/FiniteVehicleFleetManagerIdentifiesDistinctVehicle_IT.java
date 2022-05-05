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
package com.graphhopper.jsprit.io.problem;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.recreate.NoSolutionFoundException;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class FiniteVehicleFleetManagerIdentifiesDistinctVehicle_IT {

    @Test
    public void whenEmployingVehicleWhereOnlyOneDistinctVehicleCanServeAParticularJobWith_jspritAlgorithmShouldFoundDistinctSolution() {
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        for (int i = 0; i < 10; i++) {
            VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
            new VrpXMLReader(vrpBuilder).read(getClass().getResourceAsStream("biggerProblem.xml"));
            VehicleRoutingProblem vrp = vrpBuilder.build();

            VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
            vra.setMaxIterations(10);
            try {
                @SuppressWarnings("unused")
                Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            } catch (NoSolutionFoundException e) {
                testFailed.add(true);
            }
        }
        assertTrue(testFailed.isEmpty());
    }

}
