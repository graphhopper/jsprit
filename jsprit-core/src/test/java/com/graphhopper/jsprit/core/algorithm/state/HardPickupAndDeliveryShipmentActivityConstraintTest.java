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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.constraint.PickupAndDeliverShipmentLoadActivityLevelConstraint;
import com.graphhopper.jsprit.core.problem.job.ServiceJob;
import com.graphhopper.jsprit.core.problem.job.ShipmentJob;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ServiceActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;


public class HardPickupAndDeliveryShipmentActivityConstraintTest {

    VehicleImpl vehicle;

    StateManager stateManager;

    ShipmentJob shipment;

    ServiceJob s1;

    ServiceJob s2;

    PickupAndDeliverShipmentLoadActivityLevelConstraint constraint;

    JobInsertionContext iFacts;

    VehicleRoutingProblem vrp;

    @Before
    public void doBefore() {
        s1 = new ServiceJob.Builder("s1").setLocation(Location.newInstance("loc")).build();
        s2 = new ServiceJob.Builder("s2").setLocation(Location.newInstance("loc")).build();
        shipment = new ShipmentJob.Builder("shipment").setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).setDeliveryLocation(Location.newInstance("delLoc")).addSizeDimension(0, 1).build();


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
        ServiceActivity pickupService = (ServiceActivity) vrp.getActivities(s1).get(0);
        ServiceActivity anotherService = (ServiceActivity) vrp.getActivities(s2).get(0);
        PickupActivity pickupShipment = (PickupActivity) vrp.getActivities(shipment).get(0);

        assertEquals(ConstraintsStatus.FULFILLED, constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
    }

    @Test
    public void whenPickupActivityIsInsertedAndLoadIsNotSufficient_returnNOT_FullFilled() {
        ServiceActivity pickupService = (ServiceActivity) vrp.getActivities(s1).get(0);
        ServiceActivity anotherService = (ServiceActivity) vrp.getActivities(s2).get(0);
        PickupActivity pickupShipment = (PickupActivity) vrp.getActivities(shipment).get(0);

        stateManager.putInternalTypedActivityState(pickupService, InternalStates.LOAD, SizeDimension.Builder.newInstance().addDimension(0, 2).build());
        //		when(stateManager.getActivityState(pickupService, StateFactory.LOAD)).thenReturn(StateFactory.createState(2.0));
        assertEquals(ConstraintsStatus.NOT_FULFILLED, constraint.fulfilled(iFacts, pickupService, pickupShipment, anotherService, 0.0));
    }

    @Test
    public void whenDeliveryActivityIsInsertedAndLoadIsSufficient_returnFullFilled() {
        ServiceActivity pickupService = (ServiceActivity) vrp.getActivities(s1).get(0);
        ServiceActivity anotherService = (ServiceActivity) vrp.getActivities(s2).get(0);

        DeliveryActivity deliverShipment = (DeliveryActivity) vrp.getActivities(shipment)
                        .get(1);

        stateManager.putInternalTypedActivityState(pickupService, InternalStates.LOAD, SizeDimension.Builder.newInstance().addDimension(0, 1).build());
        //		stateManager.putInternalActivityState(pickupService, StateFactory.LOAD, StateFactory.createState(1));
        assertEquals(ConstraintsStatus.FULFILLED, constraint.fulfilled(iFacts, pickupService, deliverShipment, anotherService, 0.0));
    }


}
