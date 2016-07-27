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
package com.graphhopper.jsprit.core.algorithm.acceptor;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SchrimpfAcceptanceTest {

    protected SchrimpfAcceptance schrimpfAcceptance;
    protected Collection<VehicleRoutingProblemSolution> memory;

    protected static VehicleRoutingProblemSolution createSolutionWithCost(double cost) {
        return when(mock(VehicleRoutingProblemSolution.class).getCost()).thenReturn(cost).getMock();
    }

    @SuppressWarnings("deprecation")
    @Before
    public void setup() {
        schrimpfAcceptance = new SchrimpfAcceptance(1, 0.3);
        // we skip the warmup, but still want to test that the initialThreshold is set
        schrimpfAcceptance.setInitialThreshold(0.0);
        // create empty memory with an initial capacity of 1
        memory = new ArrayList<VehicleRoutingProblemSolution>(1);
        // insert the initial (worst) solution, will be accepted anyway since its the first in the memory
        assertTrue("Solution (initial cost = 2.0) should be accepted since the memory is empty", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.0)));
    }

    @Test
    public void respectsTheZeroThreshold_usingWorstCostSolution() {
        assertFalse("Worst cost solution (2.1 > 2.0) should not be accepted", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.1)));
    }

    @Test
    public void respectsTheZeroThreshold_usingBetterCostSolution() {
        assertTrue("Better cost solution (1.9 < 2.0) should be accepted", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(1.9)));
    }

    @Test
    public void respectsTheZeroThreshold_usingSameCostSolution() {
        assertFalse("Same cost solution (2.0 == 2.0) should not be accepted", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.0)));
    }

    @Test
    public void respectsTheNonZeroThreshold_usingWorstCostSolution() {
        schrimpfAcceptance.setInitialThreshold(0.5);
        /*
         * it should be accepted since 2.1 < 2.0 + 0.5 (2.0 is the best solution found so far and 0.5 the ini threshold
		 * since the threshold of 0.5 allows new solutions to be <0.5 worse than the current best solution
		 */
        assertTrue("Worst cost solution (2.1 > 2.0) should be accepted", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.1)));
    }

    @Test
    public void respectsTheNonZeroThreshold_usingBetterCostSolution() {
        schrimpfAcceptance.setInitialThreshold(0.5);
        assertTrue("Better cost solution (1.0 < 2.0) should be accepted since the better cost bust the threshold", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(1.0)));
    }

    @Test
    public void respectsTheNonZeroThreshold_usingBetterButBelowTheThresholdCostSolution() {
        schrimpfAcceptance.setInitialThreshold(0.5);
        //new solution can also be in between 2.0 and 2.5, but it is even better than 2.0 --> thus true
        assertTrue("Better cost solution (1.9 < 2.0) should not be accepted since the better cost is still below the threshold", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(1.9)));
    }

    @Test
    public void respectsTheNonZeroThreshold_usingSameCostSolution() {
        schrimpfAcceptance.setInitialThreshold(0.5);
        assertTrue("Same cost solution (2.0 == 2.0) should not be accepted", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.0)));
    }

    @Test
    public void whenIniThresholdIsSetAndCurrentIterationIs0_itShouldJustAcceptSolution() {
        schrimpfAcceptance.setInitialThreshold(0.5);
        schrimpfAcceptance.informIterationStarts(0, mock(VehicleRoutingProblem.class), Collections.<VehicleRoutingProblemSolution>emptyList());
        boolean accepted = schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.499999));
        assertTrue(accepted);
    }

    @Test
    public void whenIniThresholdIsSetAndCurrentIterationIs500_itShouldJustAcceptSolution() {
        //1000 is the default totalNuOfIterations
        schrimpfAcceptance.setInitialThreshold(0.5);
        schrimpfAcceptance.informIterationStarts(500, mock(VehicleRoutingProblem.class), Collections.<VehicleRoutingProblemSolution>emptyList());
        //according to the acceptance-function, it should just accept every solution less than 2.0 + 0.15749013123
        //threshold(500) = 0.15749013123
        boolean accepted = schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.15748));
        assertTrue(accepted);
    }

    @Test
    public void whenIniThresholdIsSetAndCurrentIterationIs500_itShouldJustNotAcceptSolution() {
        //1000 is the default totalNuOfIterations
        schrimpfAcceptance.setInitialThreshold(0.5);
        schrimpfAcceptance.informIterationStarts(500, mock(VehicleRoutingProblem.class), Collections.<VehicleRoutingProblemSolution>emptyList());
        //according to the acceptance-function, it should just accept every solution less than 2.0 + 0.15749013123
        //threshold(500) = 0.15749013123
        boolean accepted = schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.1575));
        assertFalse(accepted);
    }

    @Test
    public void whenIniThresholdIsSetAndCurrentIterationIs1000_itShouldJustAcceptSolution() {
        //1000 is the default totalNuOfIterations
        schrimpfAcceptance.setInitialThreshold(0.5);
        schrimpfAcceptance.informIterationStarts(1000, mock(VehicleRoutingProblem.class), Collections.<VehicleRoutingProblemSolution>emptyList());
        //according to the acceptance-function, it should just accept every solution less than 2.0 + 0.04960628287
        //threshold(1000)= 0.04960628287
        boolean accepted = schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.0496));
        assertTrue(accepted);
    }

    @Test
    public void whenIniThresholdIsSetAndCurrentIterationIs1000_itShouldJustNotAcceptSolution() {
        //1000 is the default totalNuOfIterations
        schrimpfAcceptance.setInitialThreshold(0.5);
        schrimpfAcceptance.informIterationStarts(1000, mock(VehicleRoutingProblem.class), Collections.<VehicleRoutingProblemSolution>emptyList());
        //according to the acceptance-function, it should just accept every solution less than 2.0 + 0.04960628287
        //threshold(1000)=0.04960628287
        boolean accepted = schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.0497));
        assertFalse(accepted);
    }


}
