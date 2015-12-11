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
package jsprit.core.algorithm.state;

import jsprit.core.problem.*;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ServiceActivity;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StateManagerTest {

    static class ActFac implements JobActivityFactory {

        @Override
        public List<AbstractActivity> createActivities(Job job) {
            ServiceActivity act = mock(ServiceActivity.class);
            when(act.getIndex()).thenReturn(1);
            List<AbstractActivity> acts = new ArrayList<AbstractActivity>();
            acts.add(act);
            return acts;
        }
    }

    private VehicleRoute getRoute(Vehicle vehicle) {
        return VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(new ActFac()).addService(Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build()).build();
    }

    private VehicleRoutingProblem vrpMock;

    @Before
    public void doBefore(){
        vrpMock = mock(VehicleRoutingProblem.class);
        when(vrpMock.getFleetSize()).thenReturn(VehicleRoutingProblem.FleetSize.INFINITE);
    }

    @Test
    public void whenInternalRouteStateIsSet_itMustBeSetCorrectly() {
        VehicleRoute route = getRoute(mock(Vehicle.class));
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = InternalStates.COSTS;
        stateManager.putTypedInternalRouteState(route, id, 10.);
        assertEquals(10., stateManager.getRouteState(route, id, Double.class), 0.01);
    }

    @Test
    public void whenInternalRouteStateIsNotSet_itShouldReturnNull() {
        VehicleRoute route = getRoute(mock(Vehicle.class));
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = InternalStates.COSTS;
        Double costs = stateManager.getRouteState(route, id, Double.class);
        assertTrue(costs == null);
    }

    @Test
    public void whenVehicleDependentInternalRouteStateIsSet_itMustBeSetCorrectly() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        //noinspection UnusedDeclaration
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).build();

        VehicleRoute route = getRoute(vehicle);
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = InternalStates.COSTS;
        stateManager.putTypedInternalRouteState(route, vehicle, id, 10.);
        assertEquals(10., stateManager.getRouteState(route, vehicle, id, Double.class), 0.01);
    }

    @Test
    public void whenVehicleDependentInternalRouteStateIsNotSet_itMustBeSetCorrectly() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        //noinspection UnusedDeclaration
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).build();

        VehicleRoute route = getRoute(vehicle);
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = InternalStates.COSTS;
        Double costs = stateManager.getRouteState(route, vehicle, id, Double.class);
        assertTrue(costs == null);
    }

    @Test
    public void whenRouteStateIsSetWithGenericMethodAndBoolean_itMustBeSetCorrectly() {
        VehicleRoute route = getRoute(mock(Vehicle.class));
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        stateManager.putRouteState(route, id, true);
        assertTrue(stateManager.getRouteState(route, id, Boolean.class));
    }

    @Test
    public void whenRouteStateIsSetWithGenericMethodAndInteger_itMustBeSetCorrectly() {
        VehicleRoute route = getRoute(mock(Vehicle.class));
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        int load = 3;
        stateManager.putRouteState(route, id, load);
        int getLoad = stateManager.getRouteState(route, id, Integer.class);
        assertEquals(3, getLoad);
    }

    @Test
    public void whenRouteStateIsSetWithGenericMethodAndCapacity_itMustBeSetCorrectly() {
        VehicleRoute route = getRoute(mock(Vehicle.class));
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        Capacity capacity = Capacity.Builder.newInstance().addDimension(0, 500).build();
        stateManager.putRouteState(route, id, capacity);
        Capacity getCap = stateManager.getRouteState(route, id, Capacity.class);
        assertEquals(500, getCap.get(0));
    }


    @Test
    public void whenActivityStateIsSetWithGenericMethodAndBoolean_itMustBeSetCorrectly() {
        TourActivity activity = mock(TourActivity.class);
        when(activity.getIndex()).thenReturn(1);
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        stateManager.putActivityState(activity, id, true);
        assertTrue(stateManager.getActivityState(activity, id, Boolean.class));
    }

    @Test
    public void whenActivityStateIsSetWithGenericMethodAndInteger_itMustBeSetCorrectly() {
        TourActivity activity = mock(TourActivity.class);
        when(activity.getIndex()).thenReturn(1);
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        int load = 3;
        stateManager.putActivityState(activity, id, load);
        int getLoad = stateManager.getActivityState(activity, id, Integer.class);
        assertEquals(3, getLoad);
    }

    @Test
    public void whenActivityStateIsSetWithGenericMethodAndCapacity_itMustBeSetCorrectly() {
        TourActivity activity = mock(TourActivity.class);
        when(activity.getIndex()).thenReturn(1);
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        Capacity capacity = Capacity.Builder.newInstance().addDimension(0, 500).build();
        stateManager.putActivityState(activity, id, capacity);
        Capacity getCap = stateManager.getActivityState(activity, id, Capacity.class);
        assertEquals(500, getCap.get(0));
    }

    @Test
    public void whenProblemStateIsSet_itMustBeSetCorrectly() {
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("problemState");
        stateManager.putProblemState(id, Boolean.class, true);
        boolean problemState = stateManager.getProblemState(id, Boolean.class);
        assertTrue(problemState);
    }

    @Test(expected = NullPointerException.class)
    public void whenProblemStateIsSetAndStateManagerClearedAfterwards_itThrowsException() {
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("problemState");
        stateManager.putProblemState(id, Boolean.class, true);
        stateManager.clear();
        @SuppressWarnings("unused")
        boolean problemState = stateManager.getProblemState(id, Boolean.class);
    }

    @Test
    public void whenProblemStateIsSetAndStateManagerClearedAfterwards_itReturnsNull() {
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("problemState");
        stateManager.putProblemState(id, Boolean.class, true);
        stateManager.clear();
        Boolean problemState = stateManager.getProblemState(id, Boolean.class);
        assertNull(problemState);
    }

    @Test
    public void whenCreatingNewState_itShouldHaveAnIndex() {
        StateManager stateManager = new StateManager(vrpMock);
        StateId stateId = stateManager.createStateId("foo-state");
        assertEquals(21, stateId.getIndex());
    }

    @Test
    public void whenCreatingNewStates_theyShouldHaveAnIndex() {
        StateManager stateManager = new StateManager(vrpMock);
        StateId fooState = stateManager.createStateId("foo-state");
        StateId foofooState = stateManager.createStateId("foo-foo-state");
        assertEquals(21, fooState.getIndex());
        assertEquals(22, foofooState.getIndex());
    }

    @Test
    public void whenCreatingTwoStatesWithTheSameName_theyShouldHaveTheSameIndex() {
        StateManager stateManager = new StateManager(vrpMock);
        StateId fooState = stateManager.createStateId("foo-state");
        StateId foofooState = stateManager.createStateId("foo-state");
        assertEquals(21, fooState.getIndex());
        assertEquals(21, foofooState.getIndex());
    }

    @Test
    public void whenCreatingAVehicleDependentRouteState_itShouldBeMemorized() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        //noinspection UnusedDeclaration
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).build();
        VehicleRoute route = getRoute(vehicle);
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        Capacity capacity = Capacity.Builder.newInstance().addDimension(0, 500).build();
        stateManager.putRouteState(route, vehicle, id, capacity);
        Capacity getCap = stateManager.getRouteState(route, vehicle, id, Capacity.class);
        assertEquals(500, getCap.get(0));
    }

    @Test
    public void whenCreatingAVehicleDependentActivityState_itShouldBeMemorized() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        //noinspection UnusedDeclaration
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).build();
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        Capacity capacity = Capacity.Builder.newInstance().addDimension(0, 500).build();
        TourActivity act = mock(TourActivity.class);
        when(act.getIndex()).thenReturn(1);
        stateManager.putActivityState(act, vehicle, id, capacity);
        Capacity getCap = stateManager.getActivityState(act, vehicle, id, Capacity.class);
        assertEquals(500, getCap.get(0));
    }

    @Test
    public void whenMemorizingVehicleInfo_itShouldBeMemorized() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        //noinspection UnusedDeclaration
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).build();
        VehicleRoute route = getRoute(vehicle);
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("vehicleParam");
        double distanceParam = vehicle.getType().getVehicleCostParams().perDistanceUnit;
        stateManager.putRouteState(route, vehicle, id, distanceParam);
        assertEquals(1., stateManager.getRouteState(route, vehicle, id, Double.class), 0.01);
    }

    @Test
    public void whenMemorizingTwoVehicleInfoForRoute_itShouldBeMemorized() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(4.).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).setType(type).build();
        VehicleRoute route = getRoute(vehicle);

        //getting the indices created in vrpBuilder
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        VehicleRoutingProblem vrp = vrpBuilder.addVehicle(vehicle).addVehicle(vehicle2).build();

        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("vehicleParam");
        double distanceParam = vehicle.getType().getVehicleCostParams().perDistanceUnit;
        stateManager.putRouteState(route, vehicle, id, distanceParam);
        stateManager.putRouteState(route, vehicle2, id, vehicle2.getType().getVehicleCostParams().perDistanceUnit);
        assertEquals(1., stateManager.getRouteState(route, vehicle, id, Double.class), 0.01);
        assertEquals(4., stateManager.getRouteState(route, vehicle2, id, Double.class), 0.01);
    }

    @Test
    public void whenMemorizingTwoVehicleInfoForAct_itShouldBeMemorized() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(4.).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).setType(type).build();

        //getting the indices created in vrpBuilder
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        VehicleRoutingProblem vrp = vrpBuilder.addVehicle(vehicle).addVehicle(vehicle2).build();

        TourActivity act = mock(TourActivity.class);
        when(act.getIndex()).thenReturn(1);
        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("vehicleParam");
        double distanceParam = vehicle.getType().getVehicleCostParams().perDistanceUnit;
        stateManager.putActivityState(act, vehicle, id, distanceParam);
        stateManager.putActivityState(act, vehicle2, id, vehicle2.getType().getVehicleCostParams().perDistanceUnit);

        assertEquals(1., stateManager.getActivityState(act, vehicle, id, Double.class), 0.01);
        assertEquals(4., stateManager.getActivityState(act, vehicle2, id, Double.class), 0.01);
    }

    @Test
    public void whenClearing_arrElementsShouldBeNull() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(4.).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).setType(type).build();

        //getting the indices created in vrpBuilder
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        VehicleRoutingProblem vrp = vrpBuilder.addVehicle(vehicle).addVehicle(vehicle2).build();

        TourActivity act = mock(TourActivity.class);
        when(act.getIndex()).thenReturn(1);
        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("vehicleParam");
        double distanceParam = vehicle.getType().getVehicleCostParams().perDistanceUnit;
        stateManager.putActivityState(act, vehicle, id, distanceParam);
        stateManager.putActivityState(act, vehicle2, id, vehicle2.getType().getVehicleCostParams().perDistanceUnit);

        stateManager.clear();

        assertNull(stateManager.getActivityState(act, vehicle, id, Double.class));
        assertNull(stateManager.getActivityState(act, vehicle2, id, Double.class));
    }
}
