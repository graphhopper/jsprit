/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.solution.route;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.vehicle.Vehicle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().build());
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenShipmentIsPickedDeliveredAndDeliveredAgain_throwsException(){
		Shipment s = mock(Shipment.class);
		Capacity capacity = Capacity.Builder.newInstance().build();
		when(s.getSize()).thenReturn(capacity);
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
		builder.addPickup(s);
		builder.addDelivery(s);
		builder.addDelivery(s);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenShipmentIsPickedUpThoughButHasNotBeenDeliveredAndRouteIsBuilt_throwsException(){
		Shipment s = mock(Shipment.class);
		Capacity capacity = Capacity.Builder.newInstance().build();
        Shipment s2 = mock(Shipment.class);
        when(s2.getSize()).thenReturn(capacity);
		when(s.getSize()).thenReturn(capacity);
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s2);
		builder.addDelivery(s);
		builder.build();
	}
	
	@Test
	public void whenTwoShipmentsHaveBeenAdded_nuOfActivitiesMustEqualFour(){
		Shipment s = mock(Shipment.class);
		Shipment s2 = mock(Shipment.class);
		Capacity capacity = Capacity.Builder.newInstance().build();
		when(s.getSize()).thenReturn(capacity);
		when(s2.getSize()).thenReturn(capacity);
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
		Capacity capacity = Capacity.Builder.newInstance().build();
		when(s.getSize()).thenReturn(capacity);
		when(s2.getSize()).thenReturn(capacity);
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.isReturnToDepot()).thenReturn(true);
		when(vehicle.getStartLocationId()).thenReturn("vehLoc");
		when(vehicle.getEndLocationId()).thenReturn("vehLoc");
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s2);
		builder.addDelivery(s);
		builder.addDelivery(s2);
		VehicleRoute route = builder.build();
		assertEquals("vehLoc",route.getEnd().getLocationId());
	}
	
	@Test
	public void whenBuildingOpenRoute_routeEndShouldHaveLocationOfLastActivity(){
		Shipment s = mock(Shipment.class);
		Shipment s2 = mock(Shipment.class);
		Capacity capacity = Capacity.Builder.newInstance().build();
		when(s.getSize()).thenReturn(capacity);
		when(s2.getSize()).thenReturn(capacity);
		when(s2.getDeliveryLocationId()).thenReturn("delLoc");
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.isReturnToDepot()).thenReturn(false);
		when(vehicle.getStartLocationId()).thenReturn("vehLoc");
		VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class));
		builder.addPickup(s);
		builder.addPickup(s2);
		builder.addDelivery(s);
		builder.addDelivery(s2);
		VehicleRoute route = builder.build();
		assertEquals(route.getEnd().getLocationId(), s2.getDeliveryLocationId());
	}
	
	@Test
	public void whenSettingDepartureTime(){
		Shipment s = mock(Shipment.class);
		Shipment s2 = mock(Shipment.class);
		Capacity capacity = Capacity.Builder.newInstance().build();
		when(s.getSize()).thenReturn(capacity);
		when(s2.getSize()).thenReturn(capacity);
		when(s2.getDeliveryLocationId()).thenReturn("delLoc");
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.isReturnToDepot()).thenReturn(false);
		when(vehicle.getStartLocationId()).thenReturn("vehLoc");
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
		Capacity capacity = Capacity.Builder.newInstance().build();
		when(s.getSize()).thenReturn(capacity);
		when(s2.getSize()).thenReturn(capacity);
		when(s2.getDeliveryLocationId()).thenReturn("delLoc");
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.isReturnToDepot()).thenReturn(false);
		when(vehicle.getStartLocationId()).thenReturn("vehLoc");
		when(vehicle.getLatestArrival()).thenReturn(200.0);
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
