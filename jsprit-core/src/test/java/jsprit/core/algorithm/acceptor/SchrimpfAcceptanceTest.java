/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.algorithm.acceptor;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
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
