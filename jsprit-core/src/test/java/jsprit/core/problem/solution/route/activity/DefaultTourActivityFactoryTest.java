package jsprit.core.problem.solution.route.activity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;

import org.junit.Test;

public class DefaultTourActivityFactoryTest {
	
	@Test
	public void whenCreatingActivityWithService_itShouldReturnPickupService(){
		DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
		Service service = Service.Builder.newInstance("service").setLocationId("loc").build();
		TourActivity act = factory.createActivity(service);
		assertNotNull(act);
		assertTrue(act instanceof PickupService);
	}
	
	@Test
	public void whenCreatingActivityWithPickup_itShouldReturnPickupService(){
		DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
		Pickup service = (Pickup) Pickup.Builder.newInstance("service").setLocationId("loc").build();
		TourActivity act = factory.createActivity(service);
		assertNotNull(act);
		assertTrue(act instanceof PickupService);
	}
	
	@Test
	public void whenCreatingActivityWithDelivery_itShouldReturnDeliverService(){
		DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
		Delivery service = (Delivery) Delivery.Builder.newInstance("service").setLocationId("loc").build();
		TourActivity act = factory.createActivity(service);
		assertNotNull(act);
		assertTrue(act instanceof DeliverService);
	}

}
