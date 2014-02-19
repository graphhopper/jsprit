package jsprit.core.problem.solution.route.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jsprit.core.problem.job.Service;

import org.junit.Before;
import org.junit.Test;

public class PickupServiceTest {
	
	private Service service;
	
	private PickupService pickup;
	
	@Before
	public void doBefore(){
		service = Service.Builder.newInstance("service").setLocationId("loc").
				setTimeWindow(TimeWindow.newInstance(1., 2.)).
				addCapacityDimension(0, 10).addCapacityDimension(1, 100).addCapacityDimension(2, 1000).build();
		pickup = new PickupService(service);
	}
	
	@Test
	public void whenCallingCapacity_itShouldReturnCorrectCapacity(){
		assertEquals(10,pickup.getCapacity().get(0));
		assertEquals(100,pickup.getCapacity().get(1));
		assertEquals(1000,pickup.getCapacity().get(2));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void whenCallingCapacityDemand_itShouldReturnCapDimWithIndex0(){
		assertEquals(10,pickup.getCapacityDemand());
	}
	
	@Test
	public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly(){
		assertEquals(1.,pickup.getTheoreticalEarliestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly(){
		assertEquals(2.,pickup.getTheoreticalLatestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenSettingArrTime_itShouldBeSetCorrectly(){
		pickup.setArrTime(4.0);
		assertEquals(4.,pickup.getArrTime(),0.01);
	}
	
	@Test
	public void whenSettingEndTime_itShouldBeSetCorrectly(){
		pickup.setEndTime(5.0);
		assertEquals(5.,pickup.getEndTime(),0.01);
	}
	
	@Test
	public void whenIniLocationId_itShouldBeSetCorrectly(){
		assertEquals("loc",pickup.getLocationId());
	}
	
	@Test
	public void whenCopyingStart_itShouldBeDoneCorrectly(){
		PickupService copy = (PickupService) pickup.duplicate();
		assertEquals(1.,copy.getTheoreticalEarliestOperationStartTime(),0.01);
		assertEquals(2.,copy.getTheoreticalLatestOperationStartTime(),0.01);
		assertEquals("loc",copy.getLocationId());
		assertEquals(10,copy.getCapacity().get(0));
		assertEquals(100,copy.getCapacity().get(1));
		assertEquals(1000,copy.getCapacity().get(2));
		assertTrue(copy!=pickup);
	}

}
