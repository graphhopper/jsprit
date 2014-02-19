package jsprit.core.problem.solution.route.activity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jsprit.core.problem.job.Shipment;

import org.junit.Test;

public class DefaultShipmentActivityFactoryTest {
	
	@Test
	public void whenCreatingPickupActivityWithShipment_itShouldReturnPickupShipment(){
		DefaultShipmentActivityFactory factory = new DefaultShipmentActivityFactory();
		Shipment shipment = Shipment.Builder.newInstance("s")
				.setPickupLocation("pLoc").setDeliveryLocation("dLoc").build();
		TourActivity act = factory.createPickup(shipment);
		assertNotNull(act);
		assertTrue(act instanceof PickupShipment);
	}
	
	@Test
	public void whenCreatingDeliverActivityWithShipment_itShouldReturnDeliverShipment(){
		DefaultShipmentActivityFactory factory = new DefaultShipmentActivityFactory();
		Shipment shipment = Shipment.Builder.newInstance("s")
				.setPickupLocation("pLoc").setDeliveryLocation("dLoc").build();
		TourActivity act = factory.createDelivery(shipment);
		assertNotNull(act);
		assertTrue(act instanceof DeliverShipment);
	}
}
