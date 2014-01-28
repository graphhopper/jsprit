package jsprit.core.algorithm.recreate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import jsprit.core.algorithm.recreate.listener.InsertionListeners;
import jsprit.core.algorithm.state.UpdateEndLocationIfRouteIsOpen;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;

import org.junit.Test;

public class TestInserter {


	@Test
	public void whenInsertingServiceAndRouteIsClosed_itInsertsCorrectly(){
		Service service = mock(Service.class);
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.getStartLocationId()).thenReturn("vehLoc");
		when(vehicle.getEndLocationId()).thenReturn("vehLoc");
		when(vehicle.isReturnToDepot()).thenReturn(true);
		when(vehicle.getId()).thenReturn("vehId");
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addService(service).build();
		//start - pick(shipment) - del(shipment) - end
		Service serviceToInsert = mock(Service.class);
		when(serviceToInsert.getLocationId()).thenReturn("delLoc");
	
		InsertionData iData = mock(InsertionData.class);
		when(iData.getDeliveryInsertionIndex()).thenReturn(1);
		when(iData.getSelectedVehicle()).thenReturn(vehicle);
		
		Inserter inserter = new Inserter(mock(InsertionListeners.class));
		inserter.insertJob(serviceToInsert, iData, route);
		
		assertEquals(2,route.getTourActivities().getActivities().size());
		assertEquals(route.getTourActivities().getActivities().get(1).getLocationId(),serviceToInsert.getLocationId());
		assertEquals(route.getEnd().getLocationId(),vehicle.getEndLocationId());
	}
	
	@Test
	public void whenInsertingServiceAndRouteIsOpen_itInsertsCorrectlyAndSwitchesEndLocation(){
		Service service = mock(Service.class);
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.getStartLocationId()).thenReturn("vehLoc");
		when(vehicle.getEndLocationId()).thenReturn("vehLoc");
		when(vehicle.isReturnToDepot()).thenReturn(false);
		when(vehicle.getId()).thenReturn("vehId");
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addService(service).build();
		Service serviceToInsert = mock(Service.class);
		when(serviceToInsert.getLocationId()).thenReturn("delLoc");
	
		InsertionData iData = mock(InsertionData.class);
		when(iData.getDeliveryInsertionIndex()).thenReturn(1);
		when(iData.getSelectedVehicle()).thenReturn(vehicle);
		
		Inserter inserter = new Inserter(mock(InsertionListeners.class));
		inserter.insertJob(serviceToInsert, iData, route);
		
		assertEquals(2,route.getTourActivities().getActivities().size());
		assertEquals(route.getTourActivities().getActivities().get(1).getLocationId(),serviceToInsert.getLocationId());
		assertEquals(route.getEnd().getLocationId(),serviceToInsert.getLocationId());
	}
	
	
	@Test
	public void whenInsertingShipmentAndRouteIsClosed_itInsertsCorrectly(){
		Shipment shipment = mock(Shipment.class);
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.getStartLocationId()).thenReturn("vehLoc");
		when(vehicle.getEndLocationId()).thenReturn("vehLoc");
		when(vehicle.isReturnToDepot()).thenReturn(true);
		when(vehicle.getId()).thenReturn("vehId");
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addPickup(shipment).addDelivery(shipment).build();
		//start - pick(shipment) - del(shipment) - end
		Shipment shipmentToInsert = mock(Shipment.class);
		when(shipmentToInsert.getDeliveryLocation()).thenReturn("delLoc");
		when(shipmentToInsert.getPickupLocation()).thenReturn("pickLoc");
		InsertionData iData = mock(InsertionData.class);
		when(iData.getPickupInsertionIndex()).thenReturn(2);
		when(iData.getDeliveryInsertionIndex()).thenReturn(2);
		when(iData.getSelectedVehicle()).thenReturn(vehicle);
		
		Inserter inserter = new Inserter(mock(InsertionListeners.class));
		inserter.insertJob(shipmentToInsert, iData, route);
		
		assertEquals(4,route.getTourActivities().getActivities().size());
		assertEquals(route.getTourActivities().getActivities().get(2).getLocationId(),shipmentToInsert.getPickupLocation());
		assertEquals(route.getTourActivities().getActivities().get(3).getLocationId(),shipmentToInsert.getDeliveryLocation());
		assertEquals(route.getEnd().getLocationId(),vehicle.getEndLocationId());
	}
	
	@Test
	public void whenInsertingShipmentAndRouteIsOpen_itInsertsCorrectlyAndSwitchesEndLocation(){
		Shipment shipment = mock(Shipment.class);
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.isReturnToDepot()).thenReturn(false);
		when(vehicle.getId()).thenReturn("vehId");
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addPickup(shipment).addDelivery(shipment).build();
		//start - pick(shipment) - del(shipment) - end
		Shipment shipmentToInsert = mock(Shipment.class);
		when(shipmentToInsert.getDeliveryLocation()).thenReturn("delLoc");
		when(shipmentToInsert.getPickupLocation()).thenReturn("pickLoc");
		InsertionData iData = mock(InsertionData.class);
		when(iData.getPickupInsertionIndex()).thenReturn(2);
		when(iData.getDeliveryInsertionIndex()).thenReturn(2);
		when(iData.getSelectedVehicle()).thenReturn(vehicle);
		
		Inserter inserter = new Inserter(mock(InsertionListeners.class));
		inserter.insertJob(shipmentToInsert, iData, route);
		
		assertEquals(4,route.getTourActivities().getActivities().size());
		assertEquals(route.getTourActivities().getActivities().get(2).getLocationId(),shipmentToInsert.getPickupLocation());
		assertEquals(route.getTourActivities().getActivities().get(3).getLocationId(),shipmentToInsert.getDeliveryLocation());
		assertEquals(route.getEnd().getLocationId(),shipmentToInsert.getDeliveryLocation());
	}
	
	@Test
	public void whenSwitchingVehicleAndRouteIsClosed_newStartAndEndShouldBeTheLocationOfNewVehicle(){
		Shipment shipment = mock(Shipment.class);
		Vehicle vehicle = VehicleImpl.Builder.newInstance("vehId").setLocationId("vehLoc").setType(mock(VehicleType.class)).build(); 
		Vehicle newVehicle = VehicleImpl.Builder.newInstance("newVehId").setLocationId("newVehLoc").setType(mock(VehicleType.class)).build();
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addPickup(shipment).addDelivery(shipment).build();
		//start - pick(shipment) - del(shipment) - end
		Shipment shipmentToInsert = mock(Shipment.class);
		when(shipmentToInsert.getDeliveryLocation()).thenReturn("delLoc");
		when(shipmentToInsert.getPickupLocation()).thenReturn("pickLoc");
		
		InsertionData iData = mock(InsertionData.class);
		when(iData.getPickupInsertionIndex()).thenReturn(2);
		when(iData.getDeliveryInsertionIndex()).thenReturn(2);
		when(iData.getSelectedVehicle()).thenReturn(newVehicle);
		
		Inserter inserter = new Inserter(mock(InsertionListeners.class));
		inserter.insertJob(shipmentToInsert, iData, route);
		
		assertEquals(route.getEnd().getLocationId(),newVehicle.getEndLocationId());
	}
	
	@Test
	public void whenSwitchingVehicleAndRouteIsOpen_endLocationShouldBeTheLocationOfTheLastActivity(){
		Shipment shipment = mock(Shipment.class);
		Vehicle vehicle = VehicleImpl.Builder.newInstance("vehId").setReturnToDepot(false).setLocationId("vehLoc").setType(mock(VehicleType.class)).build(); 
		Vehicle newVehicle = VehicleImpl.Builder.newInstance("newVehId").setReturnToDepot(false).setLocationId("newVehLoc").setType(mock(VehicleType.class)).build();
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addPickup(shipment).addDelivery(shipment).build();
		//start - pick(shipment) - del(shipment) - end
		Shipment shipmentToInsert = mock(Shipment.class);
		when(shipmentToInsert.getDeliveryLocation()).thenReturn("delLoc");
		when(shipmentToInsert.getPickupLocation()).thenReturn("pickLoc");
		
		InsertionData iData = mock(InsertionData.class);
		when(iData.getPickupInsertionIndex()).thenReturn(2);
		when(iData.getDeliveryInsertionIndex()).thenReturn(2);
		when(iData.getSelectedVehicle()).thenReturn(newVehicle);
		
		Inserter inserter = new Inserter(mock(InsertionListeners.class));
		inserter.insertJob(shipmentToInsert, iData, route);
		
		assertEquals("delLoc",route.getEnd().getLocationId());
	}
	
	@Test
	public void whenInsertingShipmentAtBeginningAndSwitchingVehicleAndRouteIsOpen_endLocationShouldBeTheLocationOfTheLastActivity(){
		Shipment shipment = mock(Shipment.class);
		when(shipment.getDeliveryLocation()).thenReturn("oldShipmentDelLoc");
		Vehicle vehicle = VehicleImpl.Builder.newInstance("vehId").setReturnToDepot(false).setLocationId("vehLoc").setType(mock(VehicleType.class)).build(); 
		Vehicle newVehicle = VehicleImpl.Builder.newInstance("newVehId").setReturnToDepot(false).setLocationId("newVehLoc").setType(mock(VehicleType.class)).build();
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addPickup(shipment).addDelivery(shipment).build();
		//start - pick(shipment) - del(shipment) - end
		Shipment shipmentToInsert = mock(Shipment.class);
		when(shipmentToInsert.getDeliveryLocation()).thenReturn("delLoc");
		when(shipmentToInsert.getPickupLocation()).thenReturn("pickLoc");
		
		InsertionData iData = mock(InsertionData.class);
		when(iData.getPickupInsertionIndex()).thenReturn(0);
		when(iData.getDeliveryInsertionIndex()).thenReturn(0);
		when(iData.getSelectedVehicle()).thenReturn(newVehicle);
		
		Inserter inserter = new Inserter(mock(InsertionListeners.class));
		inserter.insertJob(shipmentToInsert, iData, route);
		
		UpdateEndLocationIfRouteIsOpen updateEnd = new UpdateEndLocationIfRouteIsOpen();
		updateEnd.visit(route);
		
		assertEquals("oldShipmentDelLoc",route.getEnd().getLocationId());
	}

}
