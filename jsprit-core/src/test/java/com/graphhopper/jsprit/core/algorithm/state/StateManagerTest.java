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
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ServiceActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("State Manager Test")
class StateManagerTest {

    @DisplayName("Act Fac")
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

    @BeforeEach
    void doBefore() {
        vrpMock = mock(VehicleRoutingProblem.class);
        when(vrpMock.getFleetSize()).thenReturn(VehicleRoutingProblem.FleetSize.INFINITE);
    }

    @Test
    @DisplayName("When Internal Route State Is Set _ it Must Be Set Correctly")
    void whenInternalRouteStateIsSet_itMustBeSetCorrectly() {
        VehicleRoute route = getRoute(mock(Vehicle.class));
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = InternalStates.COSTS;
        stateManager.putTypedInternalRouteState(route, id, 10.);
        assertEquals(10., stateManager.getRouteState(route, id, Double.class), 0.01);
    }

    @Test
    @DisplayName("When Internal Route State Is Not Set _ it Should Return Null")
    void whenInternalRouteStateIsNotSet_itShouldReturnNull() {
        VehicleRoute route = getRoute(mock(Vehicle.class));
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = InternalStates.COSTS;
        Double costs = stateManager.getRouteState(route, id, Double.class);
        assertTrue(costs == null);
    }

    @Test
    @DisplayName("When Vehicle Dependent Internal Route State Is Set _ it Must Be Set Correctly")
    void whenVehicleDependentInternalRouteStateIsSet_itMustBeSetCorrectly() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        // noinspection UnusedDeclaration
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).build();
        VehicleRoute route = getRoute(vehicle);
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = InternalStates.COSTS;
        stateManager.putTypedInternalRouteState(route, vehicle, id, 10.);
        assertEquals(10., stateManager.getRouteState(route, vehicle, id, Double.class), 0.01);
    }

    @Test
    @DisplayName("When Vehicle Dependent Internal Route State Is Not Set _ it Must Be Set Correctly")
    void whenVehicleDependentInternalRouteStateIsNotSet_itMustBeSetCorrectly() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        // noinspection UnusedDeclaration
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).build();
        VehicleRoute route = getRoute(vehicle);
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = InternalStates.COSTS;
        Double costs = stateManager.getRouteState(route, vehicle, id, Double.class);
        assertTrue(costs == null);
    }

    @Test
    @DisplayName("When Route State Is Set With Generic Method And Boolean _ it Must Be Set Correctly")
    void whenRouteStateIsSetWithGenericMethodAndBoolean_itMustBeSetCorrectly() {
        VehicleRoute route = getRoute(mock(Vehicle.class));
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        stateManager.putRouteState(route, id, true);
        assertTrue(stateManager.getRouteState(route, id, Boolean.class));
    }

    @Test
    @DisplayName("When Route State Is Set With Generic Method And Integer _ it Must Be Set Correctly")
    void whenRouteStateIsSetWithGenericMethodAndInteger_itMustBeSetCorrectly() {
        VehicleRoute route = getRoute(mock(Vehicle.class));
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        int load = 3;
        stateManager.putRouteState(route, id, load);
        int getLoad = stateManager.getRouteState(route, id, Integer.class);
        assertEquals(3, getLoad);
    }

    @Test
    @DisplayName("When Route State Is Set With Generic Method And Capacity _ it Must Be Set Correctly")
    void whenRouteStateIsSetWithGenericMethodAndCapacity_itMustBeSetCorrectly() {
        VehicleRoute route = getRoute(mock(Vehicle.class));
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        Capacity capacity = Capacity.Builder.newInstance().addDimension(0, 500).build();
        stateManager.putRouteState(route, id, capacity);
        Capacity getCap = stateManager.getRouteState(route, id, Capacity.class);
        assertEquals(500, getCap.get(0));
    }

    @Test
    @DisplayName("When Activity State Is Set With Generic Method And Boolean _ it Must Be Set Correctly")
    void whenActivityStateIsSetWithGenericMethodAndBoolean_itMustBeSetCorrectly() {
        TourActivity activity = mock(TourActivity.class);
        when(activity.getIndex()).thenReturn(1);
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("myState");
        stateManager.putActivityState(activity, id, true);
        assertTrue(stateManager.getActivityState(activity, id, Boolean.class));
    }

    @Test
    @DisplayName("When Activity State Is Set With Generic Method And Integer _ it Must Be Set Correctly")
    void whenActivityStateIsSetWithGenericMethodAndInteger_itMustBeSetCorrectly() {
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
    @DisplayName("When Activity State Is Set With Generic Method And Capacity _ it Must Be Set Correctly")
    void whenActivityStateIsSetWithGenericMethodAndCapacity_itMustBeSetCorrectly() {
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
    @DisplayName("When Problem State Is Set _ it Must Be Set Correctly")
    void whenProblemStateIsSet_itMustBeSetCorrectly() {
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("problemState");
        stateManager.putProblemState(id, Boolean.class, true);
        boolean problemState = stateManager.getProblemState(id, Boolean.class);
        assertTrue(problemState);
    }

    @Test
    @DisplayName("When Problem State Is Set And State Manager Cleared Afterwards _ it Throws Exception")
    void whenProblemStateIsSetAndStateManagerClearedAfterwards_itThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            StateManager stateManager = new StateManager(vrpMock);
            StateId id = stateManager.createStateId("problemState");
            stateManager.putProblemState(id, Boolean.class, true);
            stateManager.clear();
            @SuppressWarnings("unused")
            boolean problemState = stateManager.getProblemState(id, Boolean.class);
        });
    }

    @Test
    @DisplayName("When Problem State Is Set And State Manager Cleared Afterwards _ it Returns Null")
    void whenProblemStateIsSetAndStateManagerClearedAfterwards_itReturnsNull() {
        StateManager stateManager = new StateManager(vrpMock);
        StateId id = stateManager.createStateId("problemState");
        stateManager.putProblemState(id, Boolean.class, true);
        stateManager.clear();
        Boolean problemState = stateManager.getProblemState(id, Boolean.class);
        assertNull(problemState);
    }

    @Test
    @DisplayName("When Creating New State _ it Should Have An Index")
    void whenCreatingNewState_itShouldHaveAnIndex() {
        StateManager stateManager = new StateManager(vrpMock);
        StateId stateId = stateManager.createStateId("foo-state");
        assertEquals(21, stateId.getIndex());
    }

    @Test
    @DisplayName("When Creating New States _ they Should Have An Index")
    void whenCreatingNewStates_theyShouldHaveAnIndex() {
        StateManager stateManager = new StateManager(vrpMock);
        StateId fooState = stateManager.createStateId("foo-state");
        StateId foofooState = stateManager.createStateId("foo-foo-state");
        assertEquals(21, fooState.getIndex());
        assertEquals(22, foofooState.getIndex());
    }

    @Test
    @DisplayName("When Creating Two States With The Same Name _ they Should Have The Same Index")
    void whenCreatingTwoStatesWithTheSameName_theyShouldHaveTheSameIndex() {
        StateManager stateManager = new StateManager(vrpMock);
        StateId fooState = stateManager.createStateId("foo-state");
        StateId foofooState = stateManager.createStateId("foo-state");
        assertEquals(21, fooState.getIndex());
        assertEquals(21, foofooState.getIndex());
    }

    @Test
    @DisplayName("When Creating A Vehicle Dependent Route State _ it Should Be Memorized")
    void whenCreatingAVehicleDependentRouteState_itShouldBeMemorized() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).build();
        VehicleRoute route = getRoute(vehicle);
        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("myState");
        Capacity capacity = Capacity.Builder.newInstance().addDimension(0, 500).build();
        stateManager.putRouteState(route, vehicle, id, capacity);
        Capacity getCap = stateManager.getRouteState(route, vehicle, id, Capacity.class);
        assertEquals(500, getCap.get(0));
    }

    @Test
    @DisplayName("When Creating A Vehicle Dependent Activity State _ it Should Be Memorized")
    void whenCreatingAVehicleDependentActivityState_itShouldBeMemorized() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        // noinspection UnusedDeclaration
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
    @DisplayName("When Memorizing Vehicle Info _ it Should Be Memorized")
    void whenMemorizingVehicleInfo_itShouldBeMemorized() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).build();
        VehicleRoute route = getRoute(vehicle);
        StateManager stateManager = new StateManager(vrp);
        StateId id = stateManager.createStateId("vehicleParam");
        double distanceParam = vehicle.getType().getVehicleCostParams().perDistanceUnit;
        stateManager.putRouteState(route, vehicle, id, distanceParam);
        assertEquals(1., stateManager.getRouteState(route, vehicle, id, Double.class), 0.01);
    }

    @Test
    @DisplayName("When Memorizing Two Vehicle Info For Route _ it Should Be Memorized")
    void whenMemorizingTwoVehicleInfoForRoute_itShouldBeMemorized() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(4.).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("loc")).setType(type).build();
        VehicleRoute route = getRoute(vehicle);
        // getting the indices created in vrpBuilder
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
    @DisplayName("When Memorizing Two Vehicle Info For Act _ it Should Be Memorized")
    void whenMemorizingTwoVehicleInfoForAct_itShouldBeMemorized() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(4.).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("loc")).setType(type).build();
        // getting the indices created in vrpBuilder
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
    @DisplayName("When Clearing _ arr Elements Should Be Null")
    void whenClearing_arrElementsShouldBeNull() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(4.).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("loc")).setType(type).build();
        // getting the indices created in vrpBuilder
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

    @Test
    @DisplayName("Array Ini Should Work")
    void arrayIniShouldWork() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(4.).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("loc")).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("loc")).setType(type).build();
        // getting the indices created in vrpBuilder
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        VehicleRoutingProblem vrp = vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addVehicle(vehicle).addVehicle(vehicle2).build();
        VehicleRoute route = mock(VehicleRoute.class);
        when(route.getVehicle()).thenReturn(vehicle2);
        StateManager stateManager = new StateManager(vrp);
        StateId myState = null;
        for (int i = 0; i < 10; i++) {
            myState = stateManager.createStateId("myState" + i);
        }
        stateManager.putTypedInternalRouteState(route, myState, 1.);
    }
}
