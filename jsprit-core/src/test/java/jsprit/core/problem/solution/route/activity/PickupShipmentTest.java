package jsprit.core.problem.solution.route.activity;

import static org.junit.Assert.*;
import jsprit.core.problem.job.Shipment;

import org.junit.Test;

public class PickupShipmentTest {
	
	@Test
	public void whenGettingCapacity_itShouldReturnItCorrectly(){
		Shipment shipment = Shipment.Builder.newInstance("s").setPickupLocation("pickLoc").setDeliveryLocation("delLoc")
				.addCapacityDimension(0, 10).addCapacityDimension(1, 100).build();
		PickupShipment pick = new PickupShipment(shipment);
		assertEquals(10,pick.getCapacity().get(0));
		assertEquals(100,pick.getCapacity().get(1));
	}
	
	@Test
	public void whenCopyingAct_itShouldCopyItCorrectly(){
		
	}

	
}
