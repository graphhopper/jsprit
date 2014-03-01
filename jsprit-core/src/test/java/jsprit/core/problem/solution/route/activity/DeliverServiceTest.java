package jsprit.core.problem.solution.route.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jsprit.core.problem.job.Delivery;

import org.junit.Before;
import org.junit.Test;

public class DeliverServiceTest {

	private Delivery service;
	
	private DeliverService deliver;
	
	@Before
	public void doBefore(){
		service = (Delivery) Delivery.Builder.newInstance("service").setLocationId("loc").
				setTimeWindow(TimeWindow.newInstance(1., 2.)).
				addSizeDimension(0, 10).addSizeDimension(1, 100).addSizeDimension(2, 1000).build();
		deliver = new DeliverService(service);
	}
	
	@Test
	public void whenCallingCapacity_itShouldReturnCorrectCapacity(){
		assertEquals(-10,deliver.getSize().get(0));
		assertEquals(-100,deliver.getSize().get(1));
		assertEquals(-1000,deliver.getSize().get(2));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void whenCallingCapacityDemand_itShouldReturnCapDimWithIndex0(){
		assertEquals(-10,deliver.getCapacityDemand());
	}
	
	@Test
	public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly(){
		assertEquals(1.,deliver.getTheoreticalEarliestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly(){
		assertEquals(2.,deliver.getTheoreticalLatestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenSettingArrTime_itShouldBeSetCorrectly(){
		deliver.setArrTime(4.0);
		assertEquals(4.,deliver.getArrTime(),0.01);
	}
	
	@Test
	public void whenSettingEndTime_itShouldBeSetCorrectly(){
		deliver.setEndTime(5.0);
		assertEquals(5.,deliver.getEndTime(),0.01);
	}
	
	@Test
	public void whenIniLocationId_itShouldBeSetCorrectly(){
		assertEquals("loc",deliver.getLocationId());
	}
	
	@Test
	public void whenCopyingStart_itShouldBeDoneCorrectly(){
		DeliverService copy = (DeliverService) deliver.duplicate();
		assertEquals(1.,copy.getTheoreticalEarliestOperationStartTime(),0.01);
		assertEquals(2.,copy.getTheoreticalLatestOperationStartTime(),0.01);
		assertEquals("loc",copy.getLocationId());
		assertEquals(-10,copy.getSize().get(0));
		assertEquals(-100,copy.getSize().get(1));
		assertEquals(-1000,copy.getSize().get(2));
		assertTrue(copy!=deliver);
	}

}
