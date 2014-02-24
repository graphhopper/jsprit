package jsprit.core.algorithm.acceptor;

import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

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

	@Before
	public void setup() {
		schrimpfAcceptance = new SchrimpfAcceptance(1, 0.3, 100);
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
		assertFalse("Worst cost solution (2.1 > 2.0) should not be accepted", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.1)));
	}

	@Test
	public void respectsTheNonZeroThreshold_usingBetterCostSolution() {
		schrimpfAcceptance.setInitialThreshold(0.5);
		assertTrue("Better cost solution (1.0 < 2.0) should be accepted since the better cost bust the threshold", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(1.0)));
	}

	@Test
	public void respectsTheNonZeroThreshold_usingBetterButBelowTheThresholdCostSolution() {
		schrimpfAcceptance.setInitialThreshold(0.5);
		assertFalse("Better cost solution (1.9 < 2.0) should not be accepted since the better cost is still below the threshold", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(1.9)));
	}

	@Test
	public void respectsTheNonZeroThreshold_usingSameCostSolution() {
		schrimpfAcceptance.setInitialThreshold(0.5);
		assertFalse("Same cost solution (2.0 == 2.0) should not be accepted", schrimpfAcceptance.acceptSolution(memory, createSolutionWithCost(2.0)));
	}
}
