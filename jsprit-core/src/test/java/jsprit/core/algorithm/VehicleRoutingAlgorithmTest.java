package jsprit.core.algorithm;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import jsprit.core.algorithm.SearchStrategy.DiscoveredSolution;
import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.algorithm.termination.PrematureAlgorithmTermination;
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
		SearchStrategyManager stratManager = mock(SearchStrategyManager.class); 
		VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class),
				stratManager);
		when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
		when(stratManager.getProbabilities()).thenReturn(Arrays.asList(1.0));
		algorithm.setNuOfIterations(1000);
		CountIterations counter = new CountIterations();
		algorithm.addListener(counter);
		algorithm.searchSolutions();
		assertEquals(1000,counter.getCountIterations());
	}
	
	@Test
	public void whenSettingPrematureTermination_itIsExecutedCorrectly(){
		SearchStrategyManager stratManager = mock(SearchStrategyManager.class); 
		VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class),
				stratManager);
		when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
		when(stratManager.getProbabilities()).thenReturn(Arrays.asList(1.0));
		algorithm.setNuOfIterations(1000);
		PrematureAlgorithmTermination termination = new PrematureAlgorithmTermination() {
			
			private int nuOfIterations = 1;
			
			@Override
			public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
				if(nuOfIterations == 50) return true;
				nuOfIterations++;
				return false;
			}
		};
		CountIterations counter = new CountIterations();
		algorithm.addListener(counter);
		algorithm.setPrematureAlgorithmTermination(termination);
		algorithm.searchSolutions();
		assertEquals(50,counter.getCountIterations());
	}

}
