/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.algorithm.state;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.constraint.HardActivityStateLevelConstraint.ConstraintsStatus;
import jsprit.core.problem.constraint.PickupAndDeliverShipmentLoadActivityLevelConstraint;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.DeliverShipment;
import jsprit.core.problem.solution.route.activity.PickupService;
import jsprit.core.problem.solution.route.activity.PickupShipment;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;

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
//		when(vehicle.getCapacity()).thenReturn(2);
		VehicleType type = mock(VehicleType.class);
		when(type.getCapacityDimensions()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).build());
		when(vehicle.getType()).thenReturn(type);
//		when(vehicle.getType().getCapacityDimensions()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).build());
		stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		shipment = mock(Shipment.class);
		when(shipment.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).build());
//		when(shipment.getCapacityDemand()).thenReturn(1);
		
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
		
		stateManager.putInternalTypedActivityState(pickupService, StateFactory.LOAD, Capacity.class, Capacity.Builder.newInstance().addDimension(0, 2).build());
//		when(stateManager.getActivityState(pickupService, StateFactory.LOAD)).thenReturn(StateFactory.createState(2.0));
		assertEquals(ConstraintsStatus.NOT_FULFILLED,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}
	
	@Test
	public void whenDeliveryActivityIsInsertedAndLoadIsNotSufficient_returnNOT_FullFilled_BREAK(){
		PickupService pickupService = new PickupService(mock(Service.class));
		PickupService anotherService = new PickupService(mock(Service.class));
		DeliverShipment pickupShipment = new DeliverShipment(shipment);
		
		stateManager.putInternalTypedActivityState(pickupService, StateFactory.LOAD, Capacity.class, Capacity.Builder.newInstance().addDimension(0, 2).build());
		assertEquals(ConstraintsStatus.NOT_FULFILLED_BREAK,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}

	@Test
	public void whenDeliveryActivityIsInsertedAndLoadIsSufficient_returnFullFilled(){
		PickupService pickupService = new PickupService(mock(Service.class));
		PickupService anotherService = new PickupService(mock(Service.class));
		DeliverShipment pickupShipment = new DeliverShipment(shipment);
		
		stateManager.putInternalTypedActivityState(pickupService, StateFactory.LOAD, Capacity.class, Capacity.Builder.newInstance().addDimension(0, 1).build());
//		stateManager.putInternalActivityState(pickupService, StateFactory.LOAD, StateFactory.createState(1));
		assertEquals(ConstraintsStatus.FULFILLED,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}
	
	
	
}
