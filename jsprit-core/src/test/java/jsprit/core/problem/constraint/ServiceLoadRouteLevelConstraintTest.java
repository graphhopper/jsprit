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

import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

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
        Service service = mock(Delivery.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 1).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusDeliverySizeExceedsVehicleCapacityInAllDimension_itShouldReturnFalse() {
        Service service = mock(Delivery.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusDeliverySizeExceedsVehicleCapacityInOneDimension_itShouldReturnFalse() {
        Service service = mock(Delivery.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusDeliverySizeJustFitIntoVehicle_itShouldReturnTrue() {
        Service service = mock(Delivery.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 2).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusPickupSizeDoesNotExceedsVehicleCapacity_itShouldReturnTrue() {
        Service service = mock(Pickup.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 1).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusPickupSizeExceedsVehicleCapacityInAllDimension_itShouldReturnFalse() {
        Service service = mock(Pickup.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusPickupSizeExceedsVehicleCapacityInOneDimension_itShouldReturnFalse() {
        Service service = mock(Pickup.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusPickupSizeJustFitIntoVehicle_itShouldReturnTrue() {
        Service service = mock(Pickup.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 2).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusServiceSizeDoesNotExceedsVehicleCapacity_itShouldReturnTrue() {
        Service service = mock(Service.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 1).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusServiceSizeExceedsVehicleCapacityInAllDimension_itShouldReturnFalse() {
        Service service = mock(Service.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusServiceSizeExceedsVehicleCapacityInOneDimension_itShouldReturnFalse() {
        Service service = mock(Service.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 3).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertFalse(constraint.fulfilled(iContext));
    }

    @Test
    public void whenLoadPlusServiceSizeJustFitIntoVehicle_itShouldReturnTrue() {
        Service service = mock(Service.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 2).build());

        JobInsertionContext iContext = mock(JobInsertionContext.class);
        when(iContext.getJob()).thenReturn(service);
        when(iContext.getRoute()).thenReturn(route);
        when(iContext.getNewVehicle()).thenReturn(vehicle);

        assertTrue(constraint.fulfilled(iContext));
    }

    @Test
    public void whenAddingAServiceAndNewVehicleDoesNotHaveTheCapacity_itShouldReturnFalse() {
        Service service = mock(Service.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).build());

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
        Service service = mock(Delivery.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).build());

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
        Pickup service = mock(Pickup.class);
        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).build());

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

        stateManager.informInsertionStarts(Arrays.asList(route), null);
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
        stateManager.informInsertionStarts(Arrays.asList(route), null);
        JobInsertionContext iContext = new JobInsertionContext(route, service, vehicle, null, 0.);

        assertFalse(new ServiceLoadRouteLevelConstraint(stateManager).fulfilled(iContext));
    }


    private Service createPickup(String string, int i) {
        return Pickup.Builder.newInstance(string).addSizeDimension(0, i).setLocation(Location.newInstance("loc")).build();
    }


}
