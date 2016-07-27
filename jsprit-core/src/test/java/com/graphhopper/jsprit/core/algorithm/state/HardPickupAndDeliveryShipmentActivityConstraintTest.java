/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.constraint.PickupAndDeliverShipmentLoadActivityLevelConstraint;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupService;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipment;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class HardPickupAndDeliveryShipmentActivityConstraintTest {

    VehicleImpl vehicle;

    StateManager stateManager;

    Shipment shipment;

    Service s1;

    Service s2;

    PickupAndDeliverShipmentLoadActivityLevelConstraint constraint;

    JobInsertionContext iFacts;

    VehicleRoutingProblem vrp;

    @Before
    public void doBefore() {
        s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance("loc")).build();
        s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance("loc")).build();
        shipment = Shipment.Builder.newInstance("shipment").setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).setDeliveryLocation(Location.newInstance("delLoc")).addSizeDimension(0, 1).build();


//		when(vehicle.getCapacity()).thenReturn(2);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, 2).build();
        vehicle = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance("start")).build();

        vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(shipment).addVehicle(vehicle).build();

        stateManager = new StateManager(vrp);

        iFacts = new JobInsertionContext(null, null, vehicle, null, 0.0);
        constraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
    }

    @Test
    public void whenPickupActivityIsInsertedAndLoadIsSufficient_returnFullFilled() {
        PickupService pickupService = (PickupService) vrp.getActivities(s1).get(0);
        PickupService anotherService = (PickupService) vrp.getActivities(s2).get(0);
        PickupShipment pickupShipment = (PickupShipment) vrp.getActivities(shipment).get(0);

        assertEquals(ConstraintsStatus.FULFILLED, constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
    }

    @Test
    public void whenPickupActivityIsInsertedAndLoadIsNotSufficient_returnNOT_FullFilled() {
        PickupService pickupService = (PickupService) vrp.getActivities(s1).get(0);
        PickupService anotherService = (PickupService) vrp.getActivities(s2).get(0);
        PickupShipment pickupShipment = (PickupShipment) vrp.getActivities(shipment).get(0);

        stateManager.putInternalTypedActivityState(pickupService, InternalStates.LOAD, Capacity.Builder.newInstance().addDimension(0, 2).build());
//		when(stateManager.getActivityState(pickupService, StateFactory.LOAD)).thenReturn(StateFactory.createState(2.0));
        assertEquals(ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
    }

    @Test
    public void whenDeliveryActivityIsInsertedAndLoadIsSufficient_returnFullFilled() {
        PickupService pickupService = (PickupService) vrp.getActivities(s1).get(0);
        PickupService anotherService = (PickupService) vrp.getActivities(s2).get(0);

        DeliverShipment deliverShipment = (DeliverShipment) vrp.getActivities(shipment).get(1);

        stateManager.putInternalTypedActivityState(pickupService, InternalStates.LOAD, Capacity.Builder.newInstance().addDimension(0, 1).build());
//		stateManager.putInternalActivityState(pickupService, StateFactory.LOAD, StateFactory.createState(1));
        assertEquals(ConstraintsStatus.FULFILLED, constraint.fulfilled(iFacts, pickupService, deliverShipment, anotherService, 0.0));
    }


}
