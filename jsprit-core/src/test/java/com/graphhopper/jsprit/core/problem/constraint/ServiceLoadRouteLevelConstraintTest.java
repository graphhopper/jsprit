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

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceLoadRouteLevelConstraintTest {

    private Vehicle vehicle;

    private VehicleRoute route;

    RouteAndActivityStateGetter stateGetter;

    ServiceLoadRouteLevelConstraint constraint;

    StateManager stateManager;

    Delivery createDeliveryMock(Capacity size) {
        Delivery d = mock(Delivery.class);
        when(d.getSize()).thenReturn(size);
        when(d.getJobType()).thenReturn(Job.Type.DELIVERY_SERVICE);
        return d;
    }

    Service createServiceMock(Capacity size) {
        Service d = mock(Service.class);
        when(d.getSize()).thenReturn(size);
        when(d.getJobType()).thenReturn(Job.Type.SERVICE);
        return d;
    }

    Pickup createPickupMock(Capacity size) {
        Pickup d = mock(Pickup.class);
        when(d.getSize()).thenReturn(size);
        when(d.getJobType()).thenReturn(Job.Type.PICKUP_SERVICE);
        return d;
    }

    @Before
    public void doBefore() {
        VehicleType type = mock(VehicleType.class);
        when(type.getCapacityDimensions()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).addDimension(2, 3).build());
        vehicle = mock(Vehicle.class);
        when(vehicle.getType()).thenReturn(type);

        route = mock(VehicleRoute.class);

        Capacity currentLoad = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 1).build();
        stateGetter = mock(RouteAndActivityStateGetter.class);
        when(stateGetter.getRouteState(route, InternalStates.LOAD_AT_BEGINNING, Capacity.class)).thenReturn(currentLoad);
        when(stateGetter.getRouteState(route, InternalStates.LOAD_AT_END, Capacity.class)).thenReturn(currentLoad);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(currentLoad);

        constraint = new ServiceLoadRouteLevelConstraint(stateGetter);

        VehicleRoutingProblem vrpMock = mock(VehicleRoutingProblem.class);
        when(vrpMock.getFleetSize()).thenReturn(VehicleRoutingProblem.FleetSize.INFINITE);
        stateManager = new StateManager(vrpMock);
        stateManager.updateLoadStates();
    }

    @Test
    public void whenLoadPlusDeliverySizeDoesNotExceedsVehicleCapacity_itShouldReturnTrue() {
        Service service = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 1).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusDeliverySizeExceedsVehicleCapacityInAllDimension_itShouldReturnFalse() {
        Service service = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusDeliverySizeExceedsVehicleCapacityInOneDimension_itShouldReturnFalse() {
        Service service = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusDeliverySizeJustFitIntoVehicle_itShouldReturnTrue() {
        Service service = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 2).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusPickupSizeDoesNotExceedsVehicleCapacity_itShouldReturnTrue() {
        Service service = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 1).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusPickupSizeExceedsVehicleCapacityInAllDimension_itShouldReturnFalse() {
        Service service = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusPickupSizeExceedsVehicleCapacityInOneDimension_itShouldReturnFalse() {
        Service service = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusPickupSizeJustFitIntoVehicle_itShouldReturnTrue() {
        Service service = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 2).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusServiceSizeDoesNotExceedsVehicleCapacity_itShouldReturnTrue() {
        Service service = createServiceMock(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 1).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusServiceSizeExceedsVehicleCapacityInAllDimension_itShouldReturnFalse() {
        Service service = createServiceMock(Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusServiceSizeExceedsVehicleCapacityInOneDimension_itShouldReturnFalse() {
        Service service = createServiceMock(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusServiceSizeJustFitIntoVehicle_itShouldReturnTrue() {
        Service service = createServiceMock(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 2).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenAddingAServiceAndNewVehicleDoesNotHaveTheCapacity_itShouldReturnFalse() {
        Service service = createServiceMock(Capacity.Builder.newInstance().addDimension(0, 2).build());

        Capacity atBeginning = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 1).build();
        Capacity atEnd = Capacity.Builder.newInstance().addDimension(0, 0).addDimension(1, 0).addDimension(2, 0).build();

        RouteAndActivityStateGetter stateGetter = mock(RouteAndActivityStateGetter.class);
        when(stateGetter.getRouteState(route, InternalStates.LOAD_AT_BEGINNING, Capacity.class)).thenReturn(atBeginning);
        when(stateGetter.getRouteState(route, InternalStates.LOAD_AT_END, Capacity.class)).thenReturn(atEnd);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(atBeginning);

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);

        VehicleType type = mock(VehicleType.class);
        when(type.getCapacityDimensions()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 1).addDimension(2, 2).build());
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getType()).thenReturn(type);

        when(iContext.getNewVehicle()).thenReturn(vehicle);

        ServiceLoadRouteLevelConstraint constraint = new ServiceLoadRouteLevelConstraint(stateGetter);
        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenAddingADeliveryAndNewVehicleDoesNotHaveTheCapacity_itShouldReturnFalse() {
        Service service = createDeliveryMock(Capacity.Builder.newInstance().addDimension(0, 2).build());

        Capacity atBeginning = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 1).build();
        Capacity atEnd = Capacity.Builder.newInstance().addDimension(0, 0).addDimension(1, 0).addDimension(2, 0).build();

        RouteAndActivityStateGetter stateGetter = mock(RouteAndActivityStateGetter.class);
        when(stateGetter.getRouteState(route, InternalStates.LOAD_AT_BEGINNING, Capacity.class)).thenReturn(atBeginning);
        when(stateGetter.getRouteState(route, InternalStates.LOAD_AT_END, Capacity.class)).thenReturn(atEnd);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(atBeginning);

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);

        VehicleType type = mock(VehicleType.class);
        when(type.getCapacityDimensions()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 1).addDimension(2, 2).build());
        vehicle = mock(Vehicle.class);
        when(vehicle.getType()).thenReturn(type);

        when(iContext.getNewVehicle()).thenReturn(vehicle);

        ServiceLoadRouteLevelConstraint constraint = new ServiceLoadRouteLevelConstraint(stateGetter);
        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenAddingAPickupAndNewVehicleDoesNotHaveTheCapacity_itShouldReturnFalse() {
        Pickup service = createPickupMock(Capacity.Builder.newInstance().addDimension(0, 2).build());

        Capacity atBeginning = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 1).build();
        Capacity atEnd = Capacity.Builder.newInstance().addDimension(0, 0).addDimension(1, 0).addDimension(2, 0).build();

        RouteAndActivityStateGetter stateGetter = mock(RouteAndActivityStateGetter.class);
        when(stateGetter.getRouteState(route, InternalStates.LOAD_AT_BEGINNING, Capacity.class)).thenReturn(atBeginning);
        when(stateGetter.getRouteState(route, InternalStates.LOAD_AT_END, Capacity.class)).thenReturn(atEnd);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(atBeginning);

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);

        VehicleType type = mock(VehicleType.class);
        when(type.getCapacityDimensions()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 1).addDimension(2, 2).build());
        vehicle = mock(Vehicle.class);
        when(vehicle.getType()).thenReturn(type);

        when(iContext.getNewVehicle()).thenReturn(vehicle);

        ServiceLoadRouteLevelConstraint constraint = new ServiceLoadRouteLevelConstraint(stateGetter);
        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenNewVehicleCapacityIsNotSufficiant1_returnFalse() {
        final Service pickup = createPickup("pick", 2);
        final Service pickup2 = createPickup("pick2", 3);

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 3).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance("loc")).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addJob(pickup).addJob(pickup2).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(pickup2).build();

        stateManager.informInsertionStarts(Collections.singletonList(route), null);
        JobInsertionContext iContext = new JobInsertionContext(route, pickup, vehicle, null, 0.);
        assertFalse(new ServiceLoadRouteLevelConstraint(stateManager).fulfilled(iContext));
    }

    @Test
    public void whenNewVehicleCapacityIsNotSufficiant2_returnFalse() {
        Pickup service = (Pickup) createPickup("pick", 2);
        Service serviceInRoute = createPickup("pick1", 3);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 3).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance("loc")).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addJob(service).addJob(serviceInRoute).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(serviceInRoute).build();
        stateManager.informInsertionStarts(Collections.singletonList(route), null);
        JobInsertionContext iContext = new JobInsertionContext(route, service, vehicle, null, 0.);

        assertFalse(new ServiceLoadRouteLevelConstraint(stateManager).fulfilled(iContext));
    }


    private Service createPickup(String string, int i) {
        return Pickup.Builder.newInstance(string).addSizeDimension(0, i).setLocation(Location.newInstance("loc")).build();
    }


}
