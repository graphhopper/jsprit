package jsprit.core.problem.solution.route.activity;

import static org.junit.Assert.*;

import org.junit.Test;

public class StartTest {
	
	@Test
	public void whenCallingCapacity_itShouldReturnEmptyCapacity(){
		Start start = Start.newInstance("loc", 0., 0.);
		assertEquals(0,start.getSize().get(0));
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
	
	@Test
	public void whenSettingStartEndTime_itShouldBeSetCorrectly(){
		Start start = Start.newInstance("loc", 1., 2.);
		start.setEndTime(4.0);
		assertEquals(4.,start.getEndTime(),0.01);
	}
	
	@Test
	public void whenSettingLocationId_itShouldBeSetCorrectly(){
		Start start = Start.newInstance("loc", 1., 2.);
		start.setLocationId("newLoc");
		assertEquals("newLoc",start.getLocationId());
	}
	
	@Test
	public void whenSettingEarliestStart_itShouldBeSetCorrectly(){
		Start start = Start.newInstance("loc", 1., 2.);
		start.setTheoreticalEarliestOperationStartTime(5.);
		assertEquals(5.,start.getTheoreticalEarliestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenSettingLatestStart_itShouldBeSetCorrectly(){
		Start start = Start.newInstance("loc", 1., 2.);
		start.setTheoreticalLatestOperationStartTime(5.);
		assertEquals(5.,start.getTheoreticalLatestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenCopyingStart_itShouldBeDoneCorrectly(){
		Start start = Start.newInstance("loc", 1., 2.);
		start.setTheoreticalEarliestOperationStartTime(3.);
		start.setTheoreticalLatestOperationStartTime(5.);
		
		Start copy = Start.copyOf(start);
		assertEquals(3.,copy.getTheoreticalEarliestOperationStartTime(),0.01);
		assertEquals(5.,copy.getTheoreticalLatestOperationStartTime(),0.01);
		assertEquals("loc",copy.getLocationId());
		assertTrue(copy!=start);
	}
	
}
