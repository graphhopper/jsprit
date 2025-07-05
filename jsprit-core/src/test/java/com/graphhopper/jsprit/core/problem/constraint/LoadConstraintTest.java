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

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.*;
import com.graphhopper.jsprit.core.problem.job.*;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * unit tests to test load constraints
 */
@DisplayName("Load Constraint Test")
class LoadConstraintTest {

    private VehicleRoute serviceRoute;

    private VehicleRoute pickup_delivery_route;

    private VehicleRoute shipment_route;

    private StateManager stateManager;

    @BeforeEach
    void doBefore() {
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = mock(VehicleType.class);
        when(type.getCapacityDimensions()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 20).build());
        when(vehicle.getType()).thenReturn(type);
        VehicleRoutingProblem.Builder serviceProblemBuilder = VehicleRoutingProblem.Builder.newInstance();
        Service s1 = Service.Builder.newInstance("s").addSizeDimension(0, 10).setLocation(Location.newInstance("loc")).build();
        Service s2 = Service.Builder.newInstance("s2").addSizeDimension(0, 5).setLocation(Location.newInstance("loc")).build();
        serviceProblemBuilder.addJob(s1).addJob(s2);
        final VehicleRoutingProblem serviceProblem = serviceProblemBuilder.build();
        final VehicleRoutingProblem.Builder pdProblemBuilder = VehicleRoutingProblem.Builder.newInstance();
        Pickup pickup = (Pickup) Pickup.Builder.newInstance("pick").addSizeDimension(0, 10).setLocation(Location.newInstance("loc")).build();
        Delivery delivery = (Delivery) Delivery.Builder.newInstance("del").addSizeDimension(0, 5).setLocation(Location.newInstance("loc")).build();
        pdProblemBuilder.addJob(pickup).addJob(delivery);
        final VehicleRoutingProblem pdProblem = pdProblemBuilder.build();
        final VehicleRoutingProblem.Builder shipmentProblemBuilder = VehicleRoutingProblem.Builder.newInstance();
        Shipment shipment1 = Shipment.Builder.newInstance("s1").addSizeDimension(0, 10).setPickupLocation(PickupLocation.newInstance(Location.Builder.newInstance().setId("pick").build())).setDeliveryLocation(Location.newInstance("del")).build();
        Shipment shipment2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 5).setPickupLocation(PickupLocation.newInstance(Location.Builder.newInstance().setId("pick").build())).setDeliveryLocation(Location.newInstance("del")).build();
        shipmentProblemBuilder.addJob(shipment1).addJob(shipment2).build();
        final VehicleRoutingProblem shipmentProblem = shipmentProblemBuilder.build();
        VehicleRoute.Builder serviceRouteBuilder = VehicleRoute.Builder.newInstance(vehicle);
        serviceRouteBuilder.setJobActivityFactory(new JobActivityFactory() {

            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return serviceProblem.copyAndGetActivities(job);
            }
        });
        serviceRoute = serviceRouteBuilder.addService(s1).addService(s2).build();
        VehicleRoute.Builder pdRouteBuilder = VehicleRoute.Builder.newInstance(vehicle);
        pdRouteBuilder.setJobActivityFactory(new JobActivityFactory() {

            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return pdProblem.copyAndGetActivities(job);
            }
        });
        pickup_delivery_route = pdRouteBuilder.addService(pickup).addService(delivery).build();
        VehicleRoute.Builder shipmentRouteBuilder = VehicleRoute.Builder.newInstance(vehicle);
        shipmentRouteBuilder.setJobActivityFactory(new JobActivityFactory() {

            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return shipmentProblem.copyAndGetActivities(job);
            }
        });
        shipment_route = shipmentRouteBuilder.addPickup(shipment1).addPickup(shipment2).addDelivery(shipment2).addDelivery(shipment1).build();
        VehicleRoutingProblem vrpMock = mock(VehicleRoutingProblem.class);
        when(vrpMock.getFleetSize()).thenReturn(VehicleRoutingProblem.FleetSize.FINITE);
        stateManager = new StateManager(vrpMock);
        stateManager.updateLoadStates();
    }

    /*
    serviceroute
     */
    @Test
    @DisplayName("When Service Route And New Service Fits In _ it Should Return Fulfilled")
    void whenServiceRouteAndNewServiceFitsIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(serviceRoute), Collections.emptyList());
        Service s = createMock(Capacity.Builder.newInstance().addDimension(0, 5).build());
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 5).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        assertTrue(loadconstraint.fulfilled(context));
    }

    private static Service createMock(Capacity size) {
        Service mock = mock(Service.class);
        when(mock.getJobType()).thenReturn(Job.Type.SERVICE);
        when(mock.getSize()).thenReturn(size);
        when(mock.isDeliveredToVehicleEnd()).thenReturn(true);
        when(mock.isPickedUpAtVehicleStart()).thenReturn(false);
        return mock;
    }

    private static Delivery createDeliveryMock(Capacity size) {
        Delivery mock = mock(Delivery.class);
        when(mock.getJobType()).thenReturn(Job.Type.DELIVERY_SERVICE);
        when(mock.getSize()).thenReturn(size);
        when(mock.isDeliveredToVehicleEnd()).thenReturn(false);
        when(mock.isPickedUpAtVehicleStart()).thenReturn(true);
        return mock;
    }

    private static Pickup createPickupMock(Capacity size) {
        Pickup mock = mock(Pickup.class);
        when(mock.getJobType()).thenReturn(Job.Type.PICKUP_SERVICE);
        when(mock.getSize()).thenReturn(size);
        when(mock.isDeliveredToVehicleEnd()).thenReturn(true);
        when(mock.isPickedUpAtVehicleStart()).thenReturn(false);
        return mock;
    }

    private Shipment createShipmentMock(Capacity size) {
        Shipment shipment = mock(Shipment.class);
        when(shipment.getJobType()).thenReturn(Job.Type.SHIPMENT);
        when(shipment.getSize()).thenReturn(size);
        when(shipment.isDeliveredToVehicleEnd()).thenReturn(false);
        when(shipment.isPickedUpAtVehicleStart()).thenReturn(false);
        return shipment;
    }

    @Test
    @DisplayName("When Service Route And New Service Fits In Between Start And Act 1 _ it Should Return Fulfilled")
    void whenServiceRouteAndNewServiceFitsInBetweenStartAndAct1_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(serviceRoute), Collections.emptyList());
        Capacity size = Capacity.Builder.newInstance().addDimension(0, 5).build();
        Service s = createMock(size);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(size);
        when(newAct.getJob()).thenReturn(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Service Route And New Service Fits In Between Ac 1 And Act 2 _ it Should Return Fulfilled")
    void whenServiceRouteAndNewServiceFitsInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(serviceRoute), Collections.emptyList());
        Capacity size = Capacity.Builder.newInstance().addDimension(0, 5).build();
        Service s = createMock(size);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(size);
        when(newAct.getJob()).thenReturn(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Service Route And New Service Fits In Between Ac 2 And End _ it Should Return Fulfilled")
    void whenServiceRouteAndNewServiceFitsInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(serviceRoute), Collections.emptyList());
        Capacity size = Capacity.Builder.newInstance().addDimension(0, 5).build();
        Service s = createMock(size);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(size);
        when(newAct.getJob()).thenReturn(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    /*
    service does not fit in at act level
     */
    @Test
    @DisplayName("When Service Route And New Service Does Not Fit In Between Start And Act 1 _ it Should Return Fulfilled")
    void whenServiceRouteAndNewServiceDoesNotFitInBetweenStartAndAct1_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(serviceRoute), Collections.emptyList());
        Capacity size = Capacity.Builder.newInstance().addDimension(0, 6).build();
        Service s = createMock(size);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(size);
        when(newAct.getJob()).thenReturn(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When Service Route And New Service Does Not Fit In Between Ac 1 And Act 2 _ it Should Return Fulfilled")
    void whenServiceRouteAndNewServiceDoesNotFitInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(serviceRoute), Collections.emptyList());
        Capacity size = Capacity.Builder.newInstance().addDimension(0, 6).build();
        Service s = createMock(size);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(size);
        when(newAct.getJob()).thenReturn(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When Service Route And New Service Does Not Fit In Between Ac 2 And End _ it Should Return Fulfilled")
    void whenServiceRouteAndNewServiceDoesNotFitInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(serviceRoute), Collections.emptyList());
        Capacity size = Capacity.Builder.newInstance().addDimension(0, 6).build();
        Service s = createMock(size);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(size);
        when(newAct.getJob()).thenReturn(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When Service Route And New Service Does Not Fit In _ it Should Return Fulfilled")
    void whenServiceRouteAndNewServiceDoesNotFitIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(serviceRoute), Collections.emptyList());
        Service s = createMock(Capacity.Builder.newInstance().addDimension(0, 6).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        assertFalse(loadconstraint.fulfilled(context));
    }

    /*
    pickup_delivery_route
    pickup 10
    delivery 5
     */
    @Test
    @DisplayName("When PD Route Route And New Pickup Fits In _ it Should Return Fulfilled")
    void whenPDRouteRouteAndNewPickupFitsIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Pickup s = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 10).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, serviceRoute.getVehicle(), null, 0.);
        assertTrue(loadconstraint.fulfilled(context));
    }

    @Test
    @DisplayName("When PD Route Route And New Delivery Fits In _ it Should Return Fulfilled")
    void whenPDRouteRouteAndNewDeliveryFitsIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Delivery s = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 15).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, serviceRoute.getVehicle(), null, 0.);
        assertTrue(loadconstraint.fulfilled(context));
    }

    @Test
    @DisplayName("When PD Route Route And New Pickup Does Not Fit In _ it Should Return Not Fulfilled")
    void whenPDRouteRouteAndNewPickupDoesNotFitIn_itShouldReturnNotFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Pickup s = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 11).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, serviceRoute.getVehicle(), null, 0.);
        assertFalse(loadconstraint.fulfilled(context));
    }

    @Test
    @DisplayName("When PD Route Route And New Delivery Does Not Fit In _ it Should Return Not Fulfilled")
    void whenPDRouteRouteAndNewDeliveryDoesNotFitIn_itShouldReturnNotFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Delivery s = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 16).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, serviceRoute.getVehicle(), null, 0.);
        assertFalse(loadconstraint.fulfilled(context));
    }

    /*
    pick fits in between activities
     */
    @Test
    @DisplayName("When PD Route _ new Pickup Should Fit In Between Start And Act 1")
    void whenPDRoute_newPickupShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Pickup s = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 5).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        PickupService newAct = new PickupService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When PD Route _ new Pickup Should Fit In Between Act 1 And Act 2")
    void whenPDRoute_newPickupShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Pickup s = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 5).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        PickupService newAct = new PickupService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When PD Route _ new Pickup Should Fit In Between Act 2 And End")
    void whenPDRoute_newPickupShouldFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Pickup s = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 10).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        PickupService newAct = new PickupService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(1), newAct, pickup_delivery_route.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    /*
    pickup does not fit in between activities
     */
    @Test
    @DisplayName("When PD Route _ new Pickup Should Not Fit In Between Start And Act 1")
    void whenPDRoute_newPickupShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Pickup s = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 6).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        PickupService newAct = new PickupService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When PD Route _ new Pickup Should Not Fit In Between Act 1 And Act 2")
    void whenPDRoute_newPickupShouldNotFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Pickup s = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 6).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        PickupService newAct = new PickupService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When PD Route _ new Pickup Should Not Fit In Between Act 2 And End")
    void whenPDRoute_newPickupShouldNotFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Pickup s = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 11).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        PickupService newAct = new PickupService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(1), newAct, pickup_delivery_route.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    /*
    pick fits in between activities
     */
    @Test
    @DisplayName("When PD Route _ new Delivery Should Fit In Between Start And Act 1")
    void whenPDRoute_newDeliveryShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Delivery s = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 15).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When PD Route _ new Delivery Should Not Fit In Between Start And Act 1")
    void whenPDRoute_newDeliveryShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Delivery s = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 16).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    @DisplayName("When PD Route _ new Delivery Should Fit In Between Act 1 And Act 2")
    void whenPDRoute_newDeliveryShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Delivery s = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 5).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When PD Route _ new Delivery Not Should Fit In Between Act 1 And Act 2")
    void whenPDRoute_newDeliveryNotShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Delivery s = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 6).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    @DisplayName("When PD Route _ new Delivery Should Fit In Between Act 2 And End")
    void whenPDRoute_newDeliveryShouldFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Delivery s = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 5).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(1), newAct, pickup_delivery_route.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When PD Route _ new Delivery Should Not Fit In Between Act 2 And End")
    void whenPDRoute_newDeliveryShouldNotFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Delivery s = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 6).build());
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(1), newAct, pickup_delivery_route.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    @DisplayName("When PD Route And New Service Fits In Between Ac 1 And Act 2 _ it Should Return Fulfilled")
    void whenPDRouteAndNewServiceFitsInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Capacity size = Capacity.Builder.newInstance().addDimension(0, 5).build();
        Service s = createMock(size);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(size);
        when(newAct.getJob()).thenReturn(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When PD Route And New Service Fits In Between Ac 2 And End _ it Should Return Fulfilled")
    void whenPDRouteAndNewServiceFitsInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Capacity size = Capacity.Builder.newInstance().addDimension(0, 5).build();
        Service s = createMock(size);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(size);
        when(newAct.getJob()).thenReturn(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    /*
    service does not fit in at act level
     */
    @Test
    @DisplayName("When PD Route And New Service Does Not Fit In Between Start And Act 1 _ it Should Return Fulfilled")
    void whenPDRouteAndNewServiceDoesNotFitInBetweenStartAndAct1_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(serviceRoute), Collections.emptyList());
        Capacity size = Capacity.Builder.newInstance().addDimension(0, 6).build();
        Service s = createMock(size);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(size);
        when(newAct.getJob()).thenReturn(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When PD Route And New Service Does Not Fit In Between Ac 1 And Act 2 _ it Should Return Fulfilled")
    void whenPDRouteAndNewServiceDoesNotFitInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(pickup_delivery_route), Collections.emptyList());
        Capacity size = Capacity.Builder.newInstance().addDimension(0, 6).build();
        Service s = createMock(size);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(size);
        when(newAct.getJob()).thenReturn(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When PD Route And New Service Does Not Fit In Between Ac 2 And End _ it Should Return Fulfilled")
    void whenPDRouteAndNewServiceDoesNotFitInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(serviceRoute), Collections.emptyList());
        Capacity size = Capacity.Builder.newInstance().addDimension(0, 6).build();
        Service s = createMock(size);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(size);
        when(newAct.getJob()).thenReturn(s);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When PD Route And New Service Does Not Fit In _ it Should Return Fulfilled")
    void whenPDRouteAndNewServiceDoesNotFitIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Collections.singletonList(serviceRoute), Collections.emptyList());
        Service s = createMock(Capacity.Builder.newInstance().addDimension(0, 6).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);
        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        assertFalse(loadconstraint.fulfilled(context));
    }

    /*
shipment route
shipment1 10
shipment2 5

pickup(s1) pickup(s2) delivery(s2) deliver(s1)
 */
    @Test
    @DisplayName("When Shipment Route And Pickup Of New Shipment Should Fit In Between Start And Act 1")
    void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getStart(), newAct, shipment_route.getActivities().get(0), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Pickup Of New Shipment Should Not Fit In Between Start And Act 1")
    void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 21).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getStart(), newAct, shipment_route.getActivities().get(0), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Pickup Of New Shipment Should Fit In Between Act 1 And Act 2")
    void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(0), newAct, shipment_route.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Pickup Of New Shipment Should Not Fit In Between Act 1 And Act 2")
    void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(0), newAct, shipment_route.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Pickup Of New Shipment Should Fit In Between Act 2 And Act 3")
    void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(1), newAct, shipment_route.getActivities().get(2), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Pickup Of New Shipment Should Not Fit In Between Act 2 And Act 3")
    void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(1), newAct, shipment_route.getActivities().get(2), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Pickup Of New Shipment Should Fit In Between Act 3 And Act 4")
    void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(2), newAct, shipment_route.getActivities().get(3), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Pickup Of New Shipment Should Not Fit In Between Act 3 And Act 4")
    void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(2), newAct, shipment_route.getActivities().get(3), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Pickup Of New Shipment Should Fit In Between Act 4 And End")
    void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(3), newAct, shipment_route.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Pickup Of New Shipment Should Not Fit In Between Act 4 And End")
    void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 21).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(3), newAct, shipment_route.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    /*
    deliverShipment
     */
    @Test
    @DisplayName("When Shipment Route And Delivery Of New Shipment Should Fit In Between Start And Act 1")
    void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getStart(), newAct, shipment_route.getActivities().get(0), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Delivery Of New Shipment Should Not Fit In Between Start And Act 1")
    void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 21).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getStart(), newAct, shipment_route.getActivities().get(0), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    @DisplayName("When Shipment Route And Delivery Of New Shipment Should Fit In Between Act 1 And Act 2")
    void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(0), newAct, shipment_route.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Delivery Of New Shipment Should Not Fit In Between Act 1 And Act 2")
    void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(0), newAct, shipment_route.getActivities().get(1), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    @DisplayName("When Shipment Route And Delivery Of New Shipment Should Fit In Between Act 2 And Act 3")
    void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(1), newAct, shipment_route.getActivities().get(2), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Delivery Of New Shipment Should Not Fit In Between Act 2 And Act 3")
    void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(1), newAct, shipment_route.getActivities().get(2), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    @DisplayName("When Shipment Route And Delivery Of New Shipment Should Fit In Between Act 3 And Act 4")
    void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(2), newAct, shipment_route.getActivities().get(3), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Delivery Of New Shipment Should Not Fit In Between Act 3 And Act 4")
    void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(2), newAct, shipment_route.getActivities().get(3), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    @DisplayName("When Shipment Route And Delivery Of New Shipment Should Fit In Between Act 4 And End")
    void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(3), newAct, shipment_route.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    @DisplayName("When Shipment Route And Delivery Of New Shipment Should Not Fit In Between Act 4 And End")
    void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Collections.singletonList(shipment_route), Collections.emptyList());
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 21).build();
        Shipment s = createShipmentMock(newSize);
        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);
        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(3), newAct, shipment_route.getEnd(), 0.);
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }
}
