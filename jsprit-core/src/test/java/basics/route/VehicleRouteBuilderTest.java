package basics.route;

import org.junit.Test;

import basics.Shipment;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class VehicleRouteBuilderTest {
	
	@Test(expected=IllegalStateException.class)
	public void whenDeliveryIsAddedBeforePickup_throwsException(){
		Shipment s = mock(Shipment.class);
		VehicleRouteBuilder builder = new VehicleRouteBuilder(mock(Vehicle.class), mock(Driver.class));
		builder.addDelivery(s);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenPickupIsAddedTwice_throwsException(){
		Shipment s = mock(Shipment.class);
		VehicleRouteBuilder builder = new VehicleRouteBuilder(mock(Vehicle.class), mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenShipmentIsPickedDeliveredAndDeliveredAgain_throwsException(){
		Shipment s = mock(Shipment.class);
		VehicleRouteBuilder builder = new VehicleRouteBuilder(mock(Vehicle.class), mock(Driver.class));
		builder.addPickup(s);
		builder.addDelivery(s);
		builder.addDelivery(s);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenShipmentIsPickedUpThoughButHasNotBeenDeliveredAndRouteIsBuilt_throwsException(){
		Shipment s = mock(Shipment.class);
		VehicleRouteBuilder builder = new VehicleRouteBuilder(mock(Vehicle.class), mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(mock(Shipment.class));
		builder.addDelivery(s);
		builder.build();
	}
	
	@Test
	public void whenTwoShipmentsHaveBeenAdded_nuOfActivitiesMustEqualFour(){
		Shipment s = mock(Shipment.class);
		Shipment s2 = mock(Shipment.class);
		VehicleRouteBuilder builder = new VehicleRouteBuilder(mock(Vehicle.class), mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s2);
		builder.addDelivery(s);
		builder.addDelivery(s2);
		VehicleRoute route = builder.build();
		assertEquals(4,route.getTourActivities().getActivities().size());
	}

}
