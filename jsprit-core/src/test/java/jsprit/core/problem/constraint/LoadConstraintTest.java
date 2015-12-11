/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jsprit.core.problem.constraint;

import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.*;
import jsprit.core.problem.job.*;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.*;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * unit tests to test load constraints
 */
public class LoadConstraintTest {

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


    /*
    serviceroute
     */
    @Test
    public void whenServiceRouteAndNewServiceFitsIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 5).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        assertTrue(loadconstraint.fulfilled(context));
    }

    @Test
    public void whenServiceRouteAndNewServiceFitsInBetweenStartAndAct1_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenServiceRouteAndNewServiceFitsInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenServiceRouteAndNewServiceFitsInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

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
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenServiceRouteAndNewServiceDoesNotFitInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenServiceRouteAndNewServiceDoesNotFitInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }


    @Test
    public void whenServiceRouteAndNewServiceDoesNotFitIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 6).build());
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
    public void whenPDRouteRouteAndNewPickupFitsIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 10).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, serviceRoute.getVehicle(), null, 0.);
        assertTrue(loadconstraint.fulfilled(context));
    }

    @Test
    public void whenPDRouteRouteAndNewDeliveryFitsIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 15).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, serviceRoute.getVehicle(), null, 0.);
        assertTrue(loadconstraint.fulfilled(context));
    }

    @Test
    public void whenPDRouteRouteAndNewPickupDoesNotFitIn_itShouldReturnNotFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 11).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, serviceRoute.getVehicle(), null, 0.);
        assertFalse(loadconstraint.fulfilled(context));
    }

    @Test
    public void whenPDRouteRouteAndNewDeliveryDoesNotFitIn_itShouldReturnNotFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 16).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, serviceRoute.getVehicle(), null, 0.);
        assertFalse(loadconstraint.fulfilled(context));
    }

    /*
    pick fits in between activities
     */
    @Test
    public void whenPDRoute_newPickupShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        PickupService newAct = new PickupService(s);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newPickupShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        PickupService newAct = new PickupService(s);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newPickupShouldFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        when(s.getSize()).thenReturn(newSize);
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
    public void whenPDRoute_newPickupShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        PickupService newAct = new PickupService(s);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newPickupShouldNotFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        PickupService newAct = new PickupService(s);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newPickupShouldNotFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        when(s.getSize()).thenReturn(newSize);
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
    public void whenPDRoute_newDeliveryShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 15).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 16).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newDeliveryNotShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(1), newAct, pickup_delivery_route.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldNotFitInBetweenAct2AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route, s, pickup_delivery_route.getVehicle(), null, 0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(1), newAct, pickup_delivery_route.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);
    }

    @Test
    public void whenPDRouteAndNewServiceFitsInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRouteAndNewServiceFitsInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

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
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenPDRouteAndNewServiceDoesNotFitInBetweenAc1AndAct2_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(pickup_delivery_route), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenPDRouteAndNewServiceDoesNotFitInBetweenAc2AndEnd_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute, s, serviceRoute.getVehicle(), null, 0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }


    @Test
    public void whenPDRouteAndNewServiceDoesNotFitIn_itShouldReturnFulfilled() {
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
        Service s = mock(Service.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 6).build());
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
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getStart(), newAct, shipment_route.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 21).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getStart(), newAct, shipment_route.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(0), newAct, shipment_route.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(0), newAct, shipment_route.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(1), newAct, shipment_route.getActivities().get(2), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(1), newAct, shipment_route.getActivities().get(2), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(2), newAct, shipment_route.getActivities().get(3), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(2), newAct, shipment_route.getActivities().get(3), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(3), newAct, shipment_route.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 21).build();
        when(s.getSize()).thenReturn(newSize);

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
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getStart(), newAct, shipment_route.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenStartAndAct1() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 21).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getStart(), newAct, shipment_route.getActivities().get(0), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(0), newAct, shipment_route.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct1AndAct2() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(0), newAct, shipment_route.getActivities().get(1), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(1), newAct, shipment_route.getActivities().get(2), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct2AndAct3() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(1), newAct, shipment_route.getActivities().get(2), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(2), newAct, shipment_route.getActivities().get(3), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct3AndAct4() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(2), newAct, shipment_route.getActivities().get(3), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(3), newAct, shipment_route.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct4AndEnd() {
        stateManager.informInsertionStarts(Arrays.asList(shipment_route), Collections.<Job>emptyList());
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 21).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route, s, shipment_route.getVehicle(), null, 0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, shipment_route.getActivities().get(3), newAct, shipment_route.getEnd(), 0.);

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK, status);

    }

}
