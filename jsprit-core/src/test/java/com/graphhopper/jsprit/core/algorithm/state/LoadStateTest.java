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

import com.graphhopper.jsprit.core.problem.*;
import com.graphhopper.jsprit.core.problem.job.*;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests to test correct calc of load states
 */
public class LoadStateTest {

    private VehicleRoute serviceRoute;

    private VehicleRoute pickup_delivery_route;

    private VehicleRoute shipment_route;

    private StateManager stateManager;

    @Before
    public void doBefore() {
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
        Shipment shipment1 = Shipment.Builder.newInstance("s1").addSizeDimension(0, 10).setPickupLocation(Location.Builder.newInstance().setId("pick").build()).setDeliveryLocation(Location.newInstance("del")).build();
        Shipment shipment2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 5).setPickupLocation(Location.Builder.newInstance().setId("pick").build()).setDeliveryLocation(Location.newInstance("del")).build();
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


    @Test
    public void loadAtEndShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Capacity routeState = stateManager.getRouteState(serviceRoute, InternalStates.LOAD_AT_END, Capacity.class);
        assertEquals(15, routeState.get(0));
    }

    @Test
    public void loadAtBeginningShouldBe0() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Capacity routeState = stateManager.getRouteState(serviceRoute, InternalStates.LOAD_AT_BEGINNING, Capacity.class);
        assertEquals(0, routeState.get(0));
    }

    @Test
    public void loadAtAct1ShouldBe10() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Capacity atAct1 = stateManager.getActivityState(serviceRoute.getActivities().get(0), InternalStates.LOAD, Capacity.class);
        assertEquals(10, atAct1.get(0));
    }

    @Test
    public void loadAtAct2ShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Capacity atAct2 = stateManager.getActivityState(serviceRoute.getActivities().get(1), InternalStates.LOAD, Capacity.class);
        assertEquals(15, atAct2.get(0));
    }

    @Test
    public void futureMaxLoatAtAct1ShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Capacity atAct1 = stateManager.getActivityState(serviceRoute.getActivities().get(0), InternalStates.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(15, atAct1.get(0));
    }

    @Test
    public void futureMaxLoatAtAct2ShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Capacity atAct2 = stateManager.getActivityState(serviceRoute.getActivities().get(1), InternalStates.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(15, atAct2.get(0));
    }

    @Test
    public void pastMaxLoatAtAct1ShouldBe0() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Capacity atAct1 = stateManager.getActivityState(serviceRoute.getActivities().get(0), InternalStates.PAST_MAXLOAD, Capacity.class);
        assertEquals(10, atAct1.get(0));
    }

    @Test
    public void pastMaxLoatAtAct2ShouldBe10() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Capacity atAct2 = stateManager.getActivityState(serviceRoute.getActivities().get(1), InternalStates.PAST_MAXLOAD, Capacity.class);
        assertEquals(15, atAct2.get(0));
    }

    /*
    test pickup_delivery_route
    pickup 10 and deliver 5
     */
    @Test
    public void when_pdroute_loadAtEndShouldBe10() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Capacity routeState = stateManager.getRouteState(pickup_delivery_route, InternalStates.LOAD_AT_END, Capacity.class);
        assertEquals(10, routeState.get(0));
    }

    @Test
    public void when_pdroute_loadAtBeginningShouldBe5() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Capacity routeState = stateManager.getRouteState(pickup_delivery_route, InternalStates.LOAD_AT_BEGINNING, Capacity.class);
        assertEquals(5, routeState.get(0));
    }

    @Test
    public void when_pdroute_loadAtAct1ShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Capacity atAct1 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(0), InternalStates.LOAD, Capacity.class);
        assertEquals(15, atAct1.get(0));
    }

    @Test
    public void when_pdroute_loadAtAct2ShouldBe10() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Capacity atAct2 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(1), InternalStates.LOAD, Capacity.class);
        assertEquals(10, atAct2.get(0));
    }

    @Test
    public void when_pdroute_futureMaxLoatAtAct1ShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Capacity atAct1 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(0), InternalStates.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(15, atAct1.get(0));
    }

    @Test
    public void when_pdroute_futureMaxLoatAtAct2ShouldBe10() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Capacity atAct2 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(1), InternalStates.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(10, atAct2.get(0));
    }

    @Test
    public void when_pdroute_pastMaxLoatAtAct1ShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Capacity atAct1 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(0), InternalStates.PAST_MAXLOAD, Capacity.class);
        assertEquals(15, atAct1.get(0));
    }

    @Test
    public void when_pdroute_pastMaxLoatAtAct2ShouldBe10() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Capacity atAct2 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(1), InternalStates.PAST_MAXLOAD, Capacity.class);
        assertEquals(15, atAct2.get(0));
    }

    /*
    shipment_route
    shipment1 10
    shipment2 15
    pick1_pick2_deliver2_deliver1

     */
    @Test
    public void when_shipmentroute_loadAtEndShouldBe0() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity routeState = stateManager.getRouteState(shipment_route, InternalStates.LOAD_AT_END, Capacity.class);
        assertEquals(0, routeState.get(0));
    }

    @Test
    public void when_shipmentroute_loadAtBeginningShouldBe0() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity routeState = stateManager.getRouteState(shipment_route, InternalStates.LOAD_AT_BEGINNING, Capacity.class);
        assertEquals(0, routeState.get(0));
    }

    @Test
    public void when_shipmentroute_loadAtAct1ShouldBe10() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct1 = stateManager.getActivityState(shipment_route.getActivities().get(0), InternalStates.LOAD, Capacity.class);
        assertEquals(10, atAct1.get(0));
    }

    @Test
    public void when_shipmentroute_loadAtAct2ShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct2 = stateManager.getActivityState(shipment_route.getActivities().get(1), InternalStates.LOAD, Capacity.class);
        assertEquals(15, atAct2.get(0));
    }

    @Test
    public void when_shipmentroute_loadAtAct3ShouldBe10() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(2), InternalStates.LOAD, Capacity.class);
        assertEquals(10, atAct.get(0));
    }

    @Test
    public void when_shipmentroute_loadAtAct4ShouldBe0() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(3), InternalStates.LOAD, Capacity.class);
        assertEquals(0, atAct.get(0));
    }

    @Test
    public void when_shipmentroute_futureMaxLoatAtAct1ShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct1 = stateManager.getActivityState(shipment_route.getActivities().get(0), InternalStates.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(15, atAct1.get(0));
    }

    @Test
    public void when_shipmentroute_futureMaxLoatAtAct2ShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct2 = stateManager.getActivityState(shipment_route.getActivities().get(1), InternalStates.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(15, atAct2.get(0));
    }

    @Test
    public void when_shipmentroute_futureMaxLoatAtAct3ShouldBe10() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(2), InternalStates.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(10, atAct.get(0));
    }

    @Test
    public void when_shipmentroute_futureMaxLoatAtAct4ShouldBe0() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(3), InternalStates.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(0, atAct.get(0));
    }

    @Test
    public void when_shipmentroute_pastMaxLoatAtAct1ShouldBe10() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct1 = stateManager.getActivityState(shipment_route.getActivities().get(0), InternalStates.PAST_MAXLOAD, Capacity.class);
        assertEquals(10, atAct1.get(0));
    }

    @Test
    public void when_shipmentroute_pastMaxLoatAtAct2ShouldBe10() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct2 = stateManager.getActivityState(shipment_route.getActivities().get(1), InternalStates.PAST_MAXLOAD, Capacity.class);
        assertEquals(15, atAct2.get(0));
    }

    @Test
    public void when_shipmentroute_pastMaxLoatAtAct3ShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(2), InternalStates.PAST_MAXLOAD, Capacity.class);
        assertEquals(15, atAct.get(0));
    }

    @Test
    public void when_shipmentroute_pastMaxLoatAtAct4ShouldBe15() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(3), InternalStates.PAST_MAXLOAD, Capacity.class);
        assertEquals(15, atAct.get(0));
    }
}
