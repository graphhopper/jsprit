package jsprit.core.problem.solution.route;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
	public void whenBuildingClosedRoute_routeEndShouldHaveLocationOfVehicle(){
		Shipment s = mock(Shipment.class);
		Shipment s2 = mock(Shipment.class);
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.isReturnToDepot()).thenReturn(true);
		when(vehicle.getLocationId()).thenReturn("vehLoc");
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s2);
		builder.addDelivery(s);
		builder.addDelivery(s2);
		VehicleRoute route = builder.build();
		assertEquals(route.getEnd().getLocationId(), vehicle.getLocationId());
	}
	
	@Test
	public void whenBuildingOpenRoute_routeEndShouldHaveLocationOfLastActivity(){
		Shipment s = mock(Shipment.class);
		Shipment s2 = mock(Shipment.class);
		when(s2.getDeliveryLocation()).thenReturn("delLoc");
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.isReturnToDepot()).thenReturn(false);
		when(vehicle.getLocationId()).thenReturn("vehLoc");
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s2);
		builder.addDelivery(s);
		builder.addDelivery(s2);
		VehicleRoute route = builder.build();
		assertEquals(route.getEnd().getLocationId(), s2.getDeliveryLocation());
	}
	
	@Test
	public void whenSettingDepartureTime(){
		Shipment s = mock(Shipment.class);
		Shipment s2 = mock(Shipment.class);
		when(s2.getDeliveryLocation()).thenReturn("delLoc");
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.isReturnToDepot()).thenReturn(false);
		when(vehicle.getLocationId()).thenReturn("vehLoc");
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s2);
		builder.addDelivery(s);
		builder.addDelivery(s2);
		builder.setDepartureTime(100);
		VehicleRoute route = builder.build();
		assertEquals(100.0,route.getDepartureTime(),0.01);
		assertEquals(100.0,route.getStart().getEndTime(),0.01);
	}
	
	
	@Test
	public void whenSettingEndTime(){
		Shipment s = mock(Shipment.class);
		Shipment s2 = mock(Shipment.class);
		when(s2.getDeliveryLocation()).thenReturn("delLoc");
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.isReturnToDepot()).thenReturn(false);
		when(vehicle.getLocationId()).thenReturn("vehLoc");
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s2);
		builder.addDelivery(s);
		builder.addDelivery(s2);
		builder.setRouteEndArrivalTime(100.0);
		VehicleRoute route = builder.build();
		assertEquals(100.0,route.getEnd().getArrTime(),0.01);
	}
}
