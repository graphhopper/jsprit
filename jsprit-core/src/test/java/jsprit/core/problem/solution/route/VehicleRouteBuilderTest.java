package jsprit.core.problem.solution.route;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.vehicle.Vehicle;

import org.junit.Test;


public class VehicleRouteBuilderTest {
	
	@Test(expected=IllegalStateException.class)
	public void whenDeliveryIsAddedBeforePickup_throwsException(){
		Shipment s = mock(Shipment.class);
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
		builder.addDelivery(s);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenPickupIsAddedTwice_throwsException(){
		Shipment s = mock(Shipment.class);
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenShipmentIsPickedDeliveredAndDeliveredAgain_throwsException(){
		Shipment s = mock(Shipment.class);
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
		builder.addPickup(s);
		builder.addDelivery(s);
		builder.addDelivery(s);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenShipmentIsPickedUpThoughButHasNotBeenDeliveredAndRouteIsBuilt_throwsException(){
		Shipment s = mock(Shipment.class);
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(mock(Shipment.class));
		builder.addDelivery(s);
		builder.build();
	}
	
	@Test
	public void whenTwoShipmentsHaveBeenAdded_nuOfActivitiesMustEqualFour(){
		Shipment s = mock(Shipment.class);
		Shipment s2 = mock(Shipment.class);
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s2);
		builder.addDelivery(s);
		builder.addDelivery(s2);
		VehicleRoute route = builder.build();
		assertEquals(4,route.getTourActivities().getActivities().size());
	}

	@Test
	public void whenBuildingOpenRoute(){
		assertTrue(false);
	}
	
	@Test
	public void whenSettingDepartureTime(){
		assertTrue(false);
	}
	
	
	@Test
	public void whenSettingEndTime(){
		assertTrue(false);
	}
}
