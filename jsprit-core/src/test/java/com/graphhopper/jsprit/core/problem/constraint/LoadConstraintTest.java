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

package com.graphhopper.jsprit.core.problem.constraint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.CopyJobActivityFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.CustomJob;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ServiceActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindows;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;

/**
 * unit tests to test load constraints
 */
public class LoadConstraintTest {

    private VehicleRoute serviceRoute;

    private VehicleRoute pickupDeliveryRoute;

    private VehicleRoute shipmentRoute;

    private StateManager stateManager;

    @Before
    public void doBefore() {
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = mock(VehicleType.class);
        when(type.getCapacityDimensions()).thenReturn(SizeDimension.Builder.newInstance().addDimension(0, 20).build());
        when(vehicle.getType()).thenReturn(type);

        VehicleRoutingProblem.Builder serviceProblemBuilder = VehicleRoutingProblem.Builder.newInstance();
        Service s1 = new Service.Builder("s").addSizeDimension(0, 10).setLocation(Location.newInstance("loc")).build();
        Service s2 = new Service.Builder("s2").addSizeDimension(0, 5).setLocation(Location.newInstance("loc")).build();
        serviceProblemBuilder.addJob(s1).addJob(s2);
        final VehicleRoutingProblem serviceProblem = serviceProblemBuilder.build();

        final VehicleRoutingProblem.Builder pdProblemBuilder = VehicleRoutingProblem.Builder.newInstance();
        Pickup pickup = new Pickup.Builder("pick").addSizeDimension(0, 10).setLocation(Location.newInstance("loc")).build();
        Delivery delivery = new Delivery.Builder("del").addSizeDimension(0, 5).setLocation(Location.newInstance("loc")).build();
        pdProblemBuilder.addJob(pickup).addJob(delivery);
        final VehicleRoutingProblem pdProblem = pdProblemBuilder.build();

        final VehicleRoutingProblem.Builder shipmentProblemBuilder = VehicleRoutingProblem.Builder.newInstance();
        Shipment shipment1 = Shipment.Builder.newInstance("s1").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("pick").build()).setDeliveryLocation(Location.newInstance("del")).build();
        Shipment shipment2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 5).setPickupLocation(Location.Builder.newInstance().setId("pick").build()).setDeliveryLocation(Location.newInstance("del")).build();
        shipmentProblemBuilder.addJob(shipment1).addJob(shipment2).build();
        final VehicleRoutingProblem shipmentProblem = shipmentProblemBuilder.build();

        VehicleRoute.Builder serviceRouteBuilder = VehicleRoute.Builder.newInstance(vehicle);
        serviceRouteBuilder.setJobActivityFactory(new CopyJobActivityFactory());
        serviceRoute = serviceRouteBuilder.addService(s1).addService(s2).build();

        VehicleRoute.Builder pdRouteBuilder = VehicleRoute.Builder.newInstance(vehicle);
        pdRouteBuilder.setJobActivityFactory(new CopyJobActivityFactory());
        pickupDeliveryRoute = pdRouteBuilder.addService(pickup).addService(delivery).build();

        VehicleRoute.Builder shipmentRouteBuilder = VehicleRoute.Builder.newInstance(vehicle);
        shipmentRouteBuilder.setJobActivityFactory(new CopyJobActivityFactory());
        shipmentRoute = shipmentRouteBuilder.addPickup(shipment1).addPickup(shipment2).addDelivery(shipment2).addDelivery(shipment1).build();

        VehicleRoutingProblem vrpMock = mock(VehicleRoutingProblem.class);
        when(vrpMock.getFleetSize()).thenReturn(VehicleRoutingProblem.FleetSize.FINITE);
        stateManager = new StateManager(vrpMock);
        stateManager.updateLoadStates();
    }

    @Test
    public void whenCustomJob_itShouldNotIgnoreCapacity() {
        CustomJob cj = CustomJob.Builder.newInstance("job")
                        .addPickup(Location.newInstance(10, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).build())
                        .addPickup(Location.newInstance(5, 0), SizeDimension.Builder.newInstance().addDimension(0, 2).build())
                        .addPickup(Location.newInstance(20, 0), SizeDimension.Builder.newInstance().addDimension(0, 1).build())
                        .build();
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 2).build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem.Builder.newInstance().addJob(cj).addVehicle(v).build();
        VehicleRoute route = VehicleRoute.emptyRoute();
        route.setVehicleAndDepartureTime(v, 0);
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.emptyList());
        ServiceLoadRouteLevelConstraint loadConstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(route, cj, v, null, 0.);
        Assert.assertFalse(loadConstraint.fulfilled(context));
    }

    /*
    serviceroute
     */
    @Test
    public void whenServiceRouteAndNewServiceFitsIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.emptyList());
        Service s = Service.Builder.newInstance("service").setLocation(Location.newInstance(0))
                        .addSizeDimension(0, 5).build();
        ServiceLoadRouteLevelConstraint loadConstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        assertTrue(loadConstraint.fulfilled(context));
    }

    @Test
    public void whenServiceRouteAndNewServiceFitsInBetweenStartAndAct1_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 5).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getLoadChange()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenServiceRouteAndNewServiceFitsInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 5).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getLoadChange()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenServiceRouteAndNewServiceFitsInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 5).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getLoadChange()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    /*
    service does not fit in at act level
     */
    @Test
    public void whenServiceRouteAndNewServiceDoesNotFitInBetweenStartAndAct1_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getLoadChange()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenServiceRouteAndNewServiceDoesNotFitInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getLoadChange()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenServiceRouteAndNewServiceDoesNotFitInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getLoadChange()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }


    @Test
    public void whenServiceRouteAndNewServiceDoesNotFitIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.emptyList());
        Service s = Service.Builder.newInstance("service").setLocation(Location.newInstance(0)).addSizeDimension(0, 6).build();
        ServiceLoadRouteLevelConstraint loadConstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        assertFalse(loadConstraint.fulfilled(context));
    }

    /*
    pickupDeliveryRoute
    pickup 10
    delivery 5
     */
    @Test
    public void whenPDRouteRouteAndNewPickupFitsIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Pickup s = Pickup.Builder.newInstance("pick").addSizeDimension(0, 10).setLocation(Location.newInstance(0)).build();
        ServiceLoadRouteLevelConstraint loadConstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, serviceRoute.getVehicle(), null, 0.);
        assertTrue(loadConstraint.fulfilled(context));
    }

    @Test
    public void whenPDRouteRouteAndNewDeliveryFitsIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.emptyList());
        Delivery s = Delivery.Builder.newInstance("del").addSizeDimension(0, 15).setLocation(Location.newInstance(0)).build();
        ServiceLoadRouteLevelConstraint loadConstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, serviceRoute.getVehicle(), null, 0.);
        assertTrue(loadConstraint.fulfilled(context));
    }

    @Test
    public void whenPDRouteRouteAndNewPickupDoesNotFitIn_itShouldReturnNotFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Pickup s = Pickup.Builder.newInstance("pickup")
                        .setLocation(Location.newInstance(0))
                        .addSizeDimension(0, 11).build();
        ServiceLoadRouteLevelConstraint loadConstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, serviceRoute.getVehicle(), null, 0.);
        assertFalse(loadConstraint.fulfilled(context));
    }

    @Test
    public void whenPDRouteRouteAndNewDeliveryDoesNotFitIn_itShouldReturnNotFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Delivery s = Delivery.Builder.newInstance("del").setLocation(Location.newInstance(0))
                        .addSizeDimension(0, 16).build();
        ServiceLoadRouteLevelConstraint loadConstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, serviceRoute.getVehicle(), null, 0.);
        assertFalse(loadConstraint.fulfilled(context));
    }

    /*
    pick fits in between activities
     */
    @Test
    public void whenPDRoute_newPickupShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 5).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getStart(), newAct, pickupDeliveryRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newPickupShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 5).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getActivities().get(0), newAct, pickupDeliveryRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newPickupShouldFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 10).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getActivities().get(1), newAct, pickupDeliveryRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    /*
    pickup does not fit in between activities
     */
    @Test
    public void whenPDRoute_newPickupShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getStart(), newAct, pickupDeliveryRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newPickupShouldNotFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getActivities().get(0), newAct, pickupDeliveryRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newPickupShouldNotFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 11).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getActivities().get(1), newAct, pickupDeliveryRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }


    /*
    pick fits in between activities
     */
    @Test
    public void whenPDRoute_newDeliveryShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 15).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        DeliveryActivity newAct = new DeliveryActivity(s, "del", null, 0,
                        newSize.invert(),
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getStart(), newAct, pickupDeliveryRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 16).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        DeliveryActivity newAct = new DeliveryActivity(s, "del", null, 0,
                        newSize.invert(),
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getStart(), newAct, pickupDeliveryRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 5).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        DeliveryActivity newAct = new DeliveryActivity(s, "del", null, 0,
                        newSize.invert(),
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getActivities().get(0), newAct, pickupDeliveryRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newDeliveryNotShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        DeliveryActivity newAct = new DeliveryActivity(s, "del", null, 0,
                        newSize.invert(),
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getActivities().get(0), newAct, pickupDeliveryRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 5).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        DeliveryActivity newAct = new DeliveryActivity(s, "del", null, 0,
                        newSize.invert(),
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getActivities().get(1), newAct, pickupDeliveryRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldNotFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickupDeliveryRoute, s, pickupDeliveryRoute.getVehicle(), null, 0.);
        DeliveryActivity newAct = new DeliveryActivity(s, "del", null, 0,
                        newSize.invert(),
                        TimeWindows.ANY_TIME.getTimeWindows());

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickupDeliveryRoute.getActivities().get(1), newAct, pickupDeliveryRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    public void whenPDRouteAndNewServiceFitsInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 5).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getLoadChange()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRouteAndNewServiceFitsInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 5).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getLoadChange()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    /*
    service does not fit in at act level
     */
    @Test
    public void whenPDRouteAndNewServiceDoesNotFitInBetweenStartAndAct1_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getLoadChange()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenPDRouteAndNewServiceDoesNotFitInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickupDeliveryRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getLoadChange()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenPDRouteAndNewServiceDoesNotFitInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();

        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getLoadChange()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }


    @Test
    public void whenPDRouteAndNewServiceDoesNotFitIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = Service.Builder.newInstance("service").addSizeDimension(0, 6).setLocation(Location.newInstance(0)).build();
        ServiceLoadRouteLevelConstraint loadConstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        assertFalse(loadConstraint.fulfilled(context));
    }

    /*
shipment route
shipment1 10
shipment2 5

pickup(s1) pickup(s2) delivery(s2) deliver(s1)
     */

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 20).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getStart(), newAct, shipmentRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 21).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getStart(), newAct, shipmentRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 10).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(0), newAct, shipmentRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 11).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(0), newAct, shipmentRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 5).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(1), newAct, shipmentRoute.getActivities().get(2), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(1), newAct, shipmentRoute.getActivities().get(2), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 10).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(2), newAct, shipmentRoute.getActivities().get(3), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 11).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(2), newAct, shipmentRoute.getActivities().get(3), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 20).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(3), newAct, shipmentRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 21).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        PickupActivity newAct = new PickupActivity(s, "pick", null, 0, newSize,
                        TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(3), newAct, shipmentRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);

    }

    /*
    deliverShipment
     */

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 20).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        DeliveryActivity newAct = new DeliveryActivity(s, "pick", null, 0,
                        newSize.invert(), TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getStart(), newAct, shipmentRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 21).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        DeliveryActivity newAct = new DeliveryActivity(s, "pick", null, 0,
                        newSize.invert(), TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getStart(), newAct, shipmentRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 10).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        DeliveryActivity newAct = new DeliveryActivity(s, "pick", null, 0,
                        newSize.invert(), TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(0), newAct, shipmentRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 11).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        DeliveryActivity newAct = new DeliveryActivity(s, "pick", null, 0,
                        newSize.invert(), TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(0), newAct, shipmentRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 5).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        DeliveryActivity newAct = new DeliveryActivity(s, "pick", null, 0,
                        newSize.invert(), TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(1), newAct, shipmentRoute.getActivities().get(2), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 6).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        DeliveryActivity newAct = new DeliveryActivity(s, "pick", null, 0,
                        newSize.invert(), TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(1), newAct, shipmentRoute.getActivities().get(2), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 10).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        DeliveryActivity newAct = new DeliveryActivity(s, "pick", null, 0,
                        newSize.invert(), TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(2), newAct, shipmentRoute.getActivities().get(3), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 11).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        DeliveryActivity newAct = new DeliveryActivity(s, "pick", null, 0,
                        newSize.invert(), TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(2), newAct, shipmentRoute.getActivities().get(3), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 20).build();


        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        DeliveryActivity newAct = new DeliveryActivity(s, "pick", null, 0,
                        newSize.invert(), TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(3), newAct, shipmentRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(shipmentRoute), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        SizeDimension newSize = SizeDimension.Builder.newInstance().addDimension(0, 21).build();

        JobInsertionContext context = new JobInsertionContext(shipmentRoute, s, shipmentRoute.getVehicle(), null, 0.);

        DeliveryActivity newAct = new DeliveryActivity(s, "pick", null, 0,
                        newSize.invert(), TimeWindows.ANY_TIME.getTimeWindows());
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipmentRoute.getActivities().get(3), newAct, shipmentRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);

    }

}
