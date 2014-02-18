package jsprit.core.problem.solution.route.activity;

import static org.junit.Assert.*;

import org.junit.Test;

public class StartTest {
	
	@Test
	public void whenCallingCapacity_itShouldReturnEmptyCapacity(){
		Start start = Start.newInstance("loc", 0., 0.);
		assertEquals(0,start.getCapacity().get(0));
	}

	@Test
	public void whenCallingCapacityDemand_itShouldReturnEmptyCapacity(){
		Start start = Start.newInstance("loc", 0., 0.);
		assertEquals(0,start.getCapacityDemand());
	}
	
	@Test
	public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly(){
		Start start = Start.newInstance("loc", 1., 2.);
		assertEquals(1.,start.getTheoreticalEarliestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly(){
		Start start = Start.newInstance("loc", 1., 2.);
		
		assertEquals(2.,start.getTheoreticalLatestOperationStartTime(),0.01);
	}
	
	
}
