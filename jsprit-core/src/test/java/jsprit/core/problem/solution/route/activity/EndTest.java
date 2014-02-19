package jsprit.core.problem.solution.route.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EndTest {
	
	@Test
	public void whenCallingCapacity_itShouldReturnEmptyCapacity(){
		End end = End.newInstance("loc", 0., 0.);
		assertEquals(0,end.getCapacity().get(0));
	}

	@Test
	public void whenCallingCapacityDemand_itShouldReturnEmptyCapacity(){
		End end = End.newInstance("loc", 0., 0.);
		assertEquals(0,end.getCapacityDemand());
	}
	
	@Test
	public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly(){
		End end = End.newInstance("loc", 1., 2.);
		assertEquals(1.,end.getTheoreticalEarliestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly(){
		End end = End.newInstance("loc", 1., 2.);
		assertEquals(2.,end.getTheoreticalLatestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenSettingEndTime_itShouldBeSetCorrectly(){
		End end = End.newInstance("loc", 1., 2.);
		end.setEndTime(4.0);
		assertEquals(4.,end.getEndTime(),0.01);
	}
	
	@Test
	public void whenSettingLocationId_itShouldBeSetCorrectly(){
		End end = End.newInstance("loc", 1., 2.);
		end.setLocationId("newLoc");
		assertEquals("newLoc",end.getLocationId());
	}
	
	@Test
	public void whenSettingEarliestStart_itShouldBeSetCorrectly(){
		End end = End.newInstance("loc", 1., 2.);
		end.setTheoreticalEarliestOperationStartTime(5.);
		assertEquals(5.,end.getTheoreticalEarliestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenSettingLatestStart_itShouldBeSetCorrectly(){
		End end = End.newInstance("loc", 1., 2.);
		end.setTheoreticalLatestOperationStartTime(5.);
		assertEquals(5.,end.getTheoreticalLatestOperationStartTime(),0.01);
	}

	@Test
	public void whenCopyingEnd_itShouldBeDoneCorrectly(){
		End end = End.newInstance("loc", 1., 2.);
		end.setTheoreticalEarliestOperationStartTime(3.);
		end.setTheoreticalLatestOperationStartTime(5.);
		
		End copy = End.copyOf(end);
		assertEquals(3.,copy.getTheoreticalEarliestOperationStartTime(),0.01);
		assertEquals(5.,copy.getTheoreticalLatestOperationStartTime(),0.01);
		assertEquals("loc",copy.getLocationId());
		assertTrue(copy!=end);
	}

}
