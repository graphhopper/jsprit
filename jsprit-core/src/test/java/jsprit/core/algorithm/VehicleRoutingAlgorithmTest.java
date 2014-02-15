package jsprit.core.algorithm;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import org.junit.Test;

public class VehicleRoutingAlgorithmTest {
	
	@Test
	public void whenSettingIterations_itIsSetCorrectly(){
		VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class),
				mock(SearchStrategyManager.class));
		algorithm.setNuOfIterations(50);
		assertEquals(50,algorithm.getNuOfIterations());
	}
	
	private static class CountIterations implements IterationStartsListener {

		private int countIterations = 0;
		
		@Override
		public void informIterationStarts(int i, VehicleRoutingProblem problem,Collection<VehicleRoutingProblemSolution> solutions) {
			countIterations++;
		}

		public int getCountIterations() {
			return countIterations;
		}
		
	}
	
	@Test
	public void whenSettingIterations_iterAreExecutedCorrectly(){
		VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class),
				mock(SearchStrategyManager.class));
		algorithm.setNuOfIterations(100);
		CountIterations counter = new CountIterations();
		algorithm.addListener(counter);
		algorithm.searchSolutions();
		assertEquals(100,counter.getCountIterations());
	}

}
