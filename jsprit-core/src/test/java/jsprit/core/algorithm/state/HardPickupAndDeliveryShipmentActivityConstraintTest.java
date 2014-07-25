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

import jsprit.core.problem.Capacity;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.HardActivityStateLevelConstraint.ConstraintsStatus;
import jsprit.core.problem.constraint.PickupAndDeliverShipmentLoadActivityLevelConstraint;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.DeliverShipment;
import jsprit.core.problem.solution.route.activity.PickupService;
import jsprit.core.problem.solution.route.activity.PickupShipment;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class HardPickupAndDeliveryShipmentActivityConstraintTest {

	Vehicle vehicle;
	
	StateManager stateManager;
	
	Shipment shipment;

    Service s1;

    Service s2;
	
	PickupAndDeliverShipmentLoadActivityLevelConstraint constraint;
	
	JobInsertionContext iFacts;

    VehicleRoutingProblem vrp;
	
	@Before
	public void doBefore(){
        s1 = Service.Builder.newInstance("s1").setLocationId("loc").build();
        s2 = Service.Builder.newInstance("s2").setLocationId("loc").build();
        shipment = Shipment.Builder.newInstance("shipment").setPickupLocation("pickLoc").setDeliveryLocation("delLoc").addSizeDimension(0,1).build();


//		when(vehicle.getCapacity()).thenReturn(2);
		VehicleType type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0,2).build();
        vehicle = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocationId("start").build();

        vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(shipment).addVehicle(vehicle).build();

		stateManager = new StateManager(vrp);

		iFacts = new JobInsertionContext(null, null, vehicle, null, 0.0);
		constraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
	}
	
	@Test
	public void whenPickupActivityIsInsertedAndLoadIsSufficient_returnFullFilled(){
		PickupService pickupService = (PickupService) vrp.getActivities(s1).get(0);
		PickupService anotherService = (PickupService) vrp.getActivities(s2).get(0);
		PickupShipment pickupShipment = (PickupShipment) vrp.getActivities(shipment).get(0);
		
		assertEquals(ConstraintsStatus.FULFILLED,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}
	
	@Test
	public void whenPickupActivityIsInsertedAndLoadIsNotSufficient_returnNOT_FullFilled(){
        PickupService pickupService = (PickupService) vrp.getActivities(s1).get(0);
        PickupService anotherService = (PickupService) vrp.getActivities(s2).get(0);
        PickupShipment pickupShipment = (PickupShipment) vrp.getActivities(shipment).get(0);

        stateManager.putInternalTypedActivityState(pickupService, StateFactory.LOAD, Capacity.Builder.newInstance().addDimension(0, 2).build());
//		when(stateManager.getActivityState(pickupService, StateFactory.LOAD)).thenReturn(StateFactory.createState(2.0));
		assertEquals(ConstraintsStatus.NOT_FULFILLED,constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
	}

	@Test
	public void whenDeliveryActivityIsInsertedAndLoadIsSufficient_returnFullFilled(){
        PickupService pickupService = (PickupService) vrp.getActivities(s1).get(0);
        PickupService anotherService = (PickupService) vrp.getActivities(s2).get(0);

        DeliverShipment deliverShipment = (DeliverShipment) vrp.getActivities(shipment).get(1);
		
		stateManager.putInternalTypedActivityState(pickupService, StateFactory.LOAD, Capacity.Builder.newInstance().addDimension(0, 1).build());
//		stateManager.putInternalActivityState(pickupService, StateFactory.LOAD, StateFactory.createState(1));
		assertEquals(ConstraintsStatus.FULFILLED,constraint.fulfilled(iFacts, pickupService, deliverShipment, anotherService, 0.0));
	}
	
	
	
}
