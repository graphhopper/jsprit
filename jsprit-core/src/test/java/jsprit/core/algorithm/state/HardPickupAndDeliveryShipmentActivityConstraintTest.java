package jsprit.core.algorithm.state;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.PickupAndDeliverShipmentLoadActivityLevelConstraint;
import jsprit.core.problem.constraint.HardActivityStateLevelConstraint.ConstraintsStatus;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.DeliverShipment;
import jsprit.core.problem.solution.route.activity.PickupService;
import jsprit.core.problem.solution.route.activity.PickupShipment;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;

import org.junit.Before;
import org.junit.Test;


public class HardPickupAndDeliveryShipmentActivityConstraintTest {

	Vehicle vehicle;
	
	StateManager stateManager;
	
	Shipment shipment;
	
	PickupAndDeliverShipmentLoadActivityLevelConstraint constraint;
	
	JobInsertionContext iFacts;
	
	@Before
	public void doBefore(){
		vehicle = mock(Vehicle.class);
		when(vehicle.getCapacity()).thenReturn(2);
		stateManager = new StateManager(mock(VehicleRoutingProblem.class));
		shipment = mock(Shipment.class);
		when(shipment.getCapacityDemand()).thenReturn(1);
		Capacity capacity = Capacity.Builder.newInstance().addDimension(0, 1).build();
		when(shipment.getCapacity()).thenReturn(capacity);
		iFacts = new JobInsertionContext(null, null, vehicle, null, 0.0);
		constraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
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
		
		stateManager.putInternalActivityState(pickupService, StateFactory.LOAD, StateFactory.createState(2));
//		when(stateManager.getActivityState(pickupService, StateFactory.LOAD)).thenReturn(StateFactory.createState(2.0));
		assertEquals(ConstraintsStatus.NOT_FULFILLED,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}
	
	@Test
	public void whenDeliveryActivityIsInsertedAndLoadIsNotSufficient_returnNOT_FullFilled_BREAK(){
		PickupService pickupService = new PickupService(mock(Service.class));
		PickupService anotherService = new PickupService(mock(Service.class));
		DeliverShipment pickupShipment = new DeliverShipment(shipment);
		
		stateManager.putInternalActivityState(pickupService, StateFactory.LOAD, StateFactory.createState(2));
		assertEquals(ConstraintsStatus.NOT_FULFILLED_BREAK,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}

	@Test
	public void whenDeliveryActivityIsInsertedAndLoadIsSufficient_returnFullFilled(){
		PickupService pickupService = new PickupService(mock(Service.class));
		PickupService anotherService = new PickupService(mock(Service.class));
		DeliverShipment pickupShipment = new DeliverShipment(shipment);
		
		stateManager.putInternalActivityState(pickupService, StateFactory.LOAD, StateFactory.createState(1));
		assertEquals(ConstraintsStatus.FULFILLED,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}
	
	
	
}
