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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Schrimpf Acceptance Test")
class SchrimpfAcceptanceTest {

    protected SchrimpfAcceptance schrimpfAcceptance;

    protected Collection<VehicleRoutingProblemSolution> memory;

    protected static VehicleRoutingProblemSolution createSolutionWithCost(double cost) {
        return when(mock(VehicleRoutingProblemSolution.class).getCost()).thenReturn(cost).getMock();
    }

    @SuppressWarnings("deprecation")
    @BeforeEach
    void setup() {
        schrimpfAcceptance = new SchrimpfAcceptance(1, 0.3);
        // we skip the warmup, but still want to test that the initialThreshold is set
        schrimpfAcceptance.setInitialThreshold(0.0);
        // create empty memory with an initial capacity of 1
        memory = new ArrayList<VehicleRoutingProblemSolution>(1);
        // insert the initial (worst) solution, will be accepted anyway since its the first in the memory
        assertTrue(schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.0)), "Solution (initial cost = 2.0) should be accepted since the memory is empty");
    }

    @Test
    @DisplayName("Respects The Zero Threshold _ using Worst Cost Solution")
    void respectsTheZeroThreshold_usingWorstCostSolution() {
        assertFalse(schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.1)), "Worst cost solution (2.1 > 2.0) should not be accepted");
    }

    @Test
    @DisplayName("Respects The Zero Threshold _ using Better Cost Solution")
    void respectsTheZeroThreshold_usingBetterCostSolution() {
        assertTrue(schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(1.9)), "Better cost solution (1.9 < 2.0) should be accepted");
    }

    @Test
    @DisplayName("Respects The Zero Threshold _ using Same Cost Solution")
    void respectsTheZeroThreshold_usingSameCostSolution() {
        assertFalse(schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.0)), "Same cost solution (2.0 == 2.0) should not be accepted");
    }

    @Test
    @DisplayName("Respects The Non Zero Threshold _ using Worst Cost Solution")
    void respectsTheNonZeroThreshold_usingWorstCostSolution() {
        schrimpfAcceptance.setInitialThreshold(0.5);
        /*
         * it should be accepted since 2.1 < 2.0 + 0.5 (2.0 is the best solution found so far and 0.5 the ini threshold
		 * since the threshold of 0.5 allows new solutions to be <0.5 worse than the current best solution
		 */
        assertTrue(schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.1)), "Worst cost solution (2.1 > 2.0) should be accepted");
    }

    @Test
    @DisplayName("Respects The Non Zero Threshold _ using Better Cost Solution")
    void respectsTheNonZeroThreshold_usingBetterCostSolution() {
        schrimpfAcceptance.setInitialThreshold(0.5);
        assertTrue(schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(1.0)), "Better cost solution (1.0 < 2.0) should be accepted since the better cost bust the threshold");
    }

    @Test
    @DisplayName("Respects The Non Zero Threshold _ using Better But Below The Threshold Cost Solution")
    void respectsTheNonZeroThreshold_usingBetterButBelowTheThresholdCostSolution() {
        schrimpfAcceptance.setInitialThreshold(0.5);
        // new solution can also be in between 2.0 and 2.5, but it is even better than 2.0 --> thus true
        assertTrue(schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(1.9)), "Better cost solution (1.9 < 2.0) should not be accepted since the better cost is still below the threshold");
    }

    @Test
    @DisplayName("Respects The Non Zero Threshold _ using Same Cost Solution")
    void respectsTheNonZeroThreshold_usingSameCostSolution() {
        schrimpfAcceptance.setInitialThreshold(0.5);
        assertTrue(schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.0)), "Same cost solution (2.0 == 2.0) should not be accepted");
    }

    @Test
    @DisplayName("When Ini Threshold Is Set And Current Iteration Is 0 _ it Should Just Accept Solution")
    void whenIniThresholdIsSetAndCurrentIterationIs0_itShouldJustAcceptSolution() {
        schrimpfAcceptance.setInitialThreshold(0.5);
        schrimpfAcceptance.informIterationStarts(0, mock(VehicleRoutingProblem.class), Collections.<VehicleRoutingProblemSolution>emptyList());
        boolean accepted = schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.499999));
        assertTrue(accepted);
    }

    @Test
    @DisplayName("When Ini Threshold Is Set And Current Iteration Is 500 _ it Should Just Accept Solution")
    void whenIniThresholdIsSetAndCurrentIterationIs500_itShouldJustAcceptSolution() {
        // 1000 is the default totalNuOfIterations
        schrimpfAcceptance.setInitialThreshold(0.5);
        schrimpfAcceptance.informIterationStarts(500, mock(VehicleRoutingProblem.class), Collections.<VehicleRoutingProblemSolution>emptyList());
        // according to the acceptance-function, it should just accept every solution less than 2.0 + 0.15749013123
        // threshold(500) = 0.15749013123
        boolean accepted = schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.15748));
        assertTrue(accepted);
    }

    @Test
    @DisplayName("When Ini Threshold Is Set And Current Iteration Is 500 _ it Should Just Not Accept Solution")
    void whenIniThresholdIsSetAndCurrentIterationIs500_itShouldJustNotAcceptSolution() {
        // 1000 is the default totalNuOfIterations
        schrimpfAcceptance.setInitialThreshold(0.5);
        schrimpfAcceptance.informIterationStarts(500, mock(VehicleRoutingProblem.class), Collections.<VehicleRoutingProblemSolution>emptyList());
        // according to the acceptance-function, it should just accept every solution less than 2.0 + 0.15749013123
        // threshold(500) = 0.15749013123
        boolean accepted = schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.1575));
        assertFalse(accepted);
    }

    @Test
    @DisplayName("When Ini Threshold Is Set And Current Iteration Is 1000 _ it Should Just Accept Solution")
    void whenIniThresholdIsSetAndCurrentIterationIs1000_itShouldJustAcceptSolution() {
        // 1000 is the default totalNuOfIterations
        schrimpfAcceptance.setInitialThreshold(0.5);
        schrimpfAcceptance.informIterationStarts(1000, mock(VehicleRoutingProblem.class), Collections.<VehicleRoutingProblemSolution>emptyList());
        // according to the acceptance-function, it should just accept every solution less than 2.0 + 0.04960628287
        // threshold(1000)= 0.04960628287
        boolean accepted = schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.0496));
        assertTrue(accepted);
    }

    @Test
    @DisplayName("When Ini Threshold Is Set And Current Iteration Is 1000 _ it Should Just Not Accept Solution")
    void whenIniThresholdIsSetAndCurrentIterationIs1000_itShouldJustNotAcceptSolution() {
        // 1000 is the default totalNuOfIterations
        schrimpfAcceptance.setInitialThreshold(0.5);
        schrimpfAcceptance.informIterationStarts(1000, mock(VehicleRoutingProblem.class), Collections.<VehicleRoutingProblemSolution>emptyList());
        // according to the acceptance-function, it should just accept every solution less than 2.0 + 0.04960628287
        // threshold(1000)=0.04960628287
        boolean accepted = schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.0497));
        assertFalse(accepted);
    }
}
