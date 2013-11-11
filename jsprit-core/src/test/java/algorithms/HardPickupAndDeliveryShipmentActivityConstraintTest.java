package algorithms;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import algorithms.HardActivityLevelConstraint.ConstraintsStatus;
import basics.Service;
import basics.Shipment;
import basics.route.DeliverShipment;
import basics.route.PickupService;
import basics.route.PickupShipment;
import basics.route.Vehicle;

public class HardPickupAndDeliveryShipmentActivityConstraintTest {

	Vehicle vehicle;
	
	StateManager stateManager;
	
	Shipment shipment;
	
	HardPickupAndDeliveryShipmentActivityLevelConstraint constraint;
	
	InsertionContext iFacts;
	
	@Before
	public void doBefore(){
		vehicle = mock(Vehicle.class);
		when(vehicle.getCapacity()).thenReturn(2);
		stateManager = new StateManager();
		shipment = mock(Shipment.class);
		when(shipment.getCapacityDemand()).thenReturn(1);
		iFacts = new InsertionContext(null, null, vehicle, null, 0.0);
		constraint = new HardPickupAndDeliveryShipmentActivityLevelConstraint(stateManager);
	}
	
	@Test
	public void whenPickupActivityIsInsertedAndLoadIsSufficient_returnFullFilled(){
		PickupService pickupService = new PickupService(mock(Service.class));
		PickupService anotherService = new PickupService(mock(Service.class));
		PickupShipment pickupShipment = new PickupShipment(shipment);
		
		assertEquals(ConstraintsStatus.FULFILLED,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}
	
	@Test
	public void whenPickupActivityIsInsertedAndLoadIsNotSufficient_returnNOT_FullFilled(){
		PickupService pickupService = new PickupService(mock(Service.class));
		PickupService anotherService = new PickupService(mock(Service.class));
		PickupShipment pickupShipment = new PickupShipment(shipment);
		
		stateManager.putActivityState(pickupService, StateFactory.LOAD, StateFactory.createState(2));
		assertEquals(ConstraintsStatus.NOT_FULFILLED,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}
	
	@Test
	public void whenDeliveryActivityIsInsertedAndLoadIsNotSufficient_returnNOT_FullFilled_BREAK(){
		PickupService pickupService = new PickupService(mock(Service.class));
		PickupService anotherService = new PickupService(mock(Service.class));
		DeliverShipment pickupShipment = new DeliverShipment(shipment);
		
		stateManager.putActivityState(pickupService, StateFactory.LOAD, StateFactory.createState(2));
		assertEquals(ConstraintsStatus.NOT_FULFILLED_BREAK,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}

	@Test
	public void whenDeliveryActivityIsInsertedAndLoadIsSufficient_returnFullFilled(){
		PickupService pickupService = new PickupService(mock(Service.class));
		PickupService anotherService = new PickupService(mock(Service.class));
		DeliverShipment pickupShipment = new DeliverShipment(shipment);
		
		stateManager.putActivityState(pickupService, StateFactory.LOAD, StateFactory.createState(1));
		assertEquals(ConstraintsStatus.FULFILLED,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}
	
	
	
}
