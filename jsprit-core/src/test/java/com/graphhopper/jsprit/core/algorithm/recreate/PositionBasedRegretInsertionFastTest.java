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
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.FiniteFleetManagerFactory;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Position Based Regret Insertion Fast Test")
class PositionBasedRegretInsertionFastTest {

    @Test
    @DisplayName("Should insert all services into single route")
    void shouldInsertAllServicesIntoSingleRoute() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addVehicle(v).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        ArrayList<com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener> iListeners = new ArrayList<>();
        JobInsertionCostsCalculator calculator = new JobInsertionCostsCalculatorBuilder(
                iListeners, new ArrayList<>())
                .setLocalLevel(true)
                .setConstraintManager(constraintManager)
                .setStateManager(stateManager)
                .setVehicleRoutingProblem(vrp)
                .setVehicleFleetManager(fm)
                .setAllowVehicleSwitch(true)
                .build();

        PositionBasedRegretInsertionFast insertion = new PositionBasedRegretInsertionFast(calculator, vrp, fm);
        for (var l : iListeners) insertion.addListener(l);

        Collection<VehicleRoute> routes = new ArrayList<>();
        stateManager.informInsertionStarts(routes, null);
        insertion.insertJobs(routes, vrp.getJobs().values());

        assertEquals(1, routes.size());
        assertEquals(2, routes.iterator().next().getActivities().size());
    }

    @Test
    @DisplayName("Should insert all shipments into single route")
    void shouldInsertAllShipmentsIntoSingleRoute() {
        Shipment s1 = Shipment.Builder.newInstance("s1")
                .setPickupLocation(Location.Builder.newInstance().setId("p1").setCoordinate(Coordinate.newInstance(0, 5)).build())
                .setDeliveryLocation(Location.Builder.newInstance().setId("d1").setCoordinate(Coordinate.newInstance(0, 10)).build())
                .build();
        Shipment s2 = Shipment.Builder.newInstance("s2")
                .setPickupLocation(Location.Builder.newInstance().setId("p2").setCoordinate(Coordinate.newInstance(0, 15)).build())
                .setDeliveryLocation(Location.Builder.newInstance().setId("d2").setCoordinate(Coordinate.newInstance(0, 20)).build())
                .build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addVehicle(v).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        ArrayList<com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener> iListeners = new ArrayList<>();
        JobInsertionCostsCalculator calculator = new JobInsertionCostsCalculatorBuilder(
                iListeners, new ArrayList<>())
                .setLocalLevel(true)
                .setConstraintManager(constraintManager)
                .setStateManager(stateManager)
                .setVehicleRoutingProblem(vrp)
                .setVehicleFleetManager(fm)
                .setAllowVehicleSwitch(true)
                .build();

        PositionBasedRegretInsertionFast insertion = new PositionBasedRegretInsertionFast(calculator, vrp, fm);
        for (var l : iListeners) insertion.addListener(l);

        Collection<VehicleRoute> routes = new ArrayList<>();
        stateManager.informInsertionStarts(routes, null);
        insertion.insertJobs(routes, vrp.getJobs().values());

        assertEquals(1, routes.size());
        assertEquals(4, routes.iterator().next().getActivities().size());
    }

    @Test
    @DisplayName("Should work with regret-3")
    void shouldWorkWithRegret3() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(0, 15)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3).addVehicle(v).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        ArrayList<com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener> iListeners = new ArrayList<>();
        JobInsertionCostsCalculator calculator = new JobInsertionCostsCalculatorBuilder(
                iListeners, new ArrayList<>())
                .setLocalLevel(true)
                .setConstraintManager(constraintManager)
                .setStateManager(stateManager)
                .setVehicleRoutingProblem(vrp)
                .setVehicleFleetManager(fm)
                .setAllowVehicleSwitch(true)
                .build();

        PositionBasedRegretInsertionFast insertion = new PositionBasedRegretInsertionFast(calculator, vrp, fm);
        insertion.setRegretK(3);
        for (var l : iListeners) insertion.addListener(l);

        Collection<VehicleRoute> routes = new ArrayList<>();
        stateManager.informInsertionStarts(routes, null);
        insertion.insertJobs(routes, vrp.getJobs().values());

        assertEquals(1, routes.size());
        assertEquals(3, routes.iterator().next().getActivities().size());
    }

    @Test
    @DisplayName("Should work with top routes to expand setting")
    void shouldWorkWithTopRoutesToExpandSetting() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addVehicle(v).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        ArrayList<com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener> iListeners = new ArrayList<>();
        JobInsertionCostsCalculator calculator = new JobInsertionCostsCalculatorBuilder(
                iListeners, new ArrayList<>())
                .setLocalLevel(true)
                .setConstraintManager(constraintManager)
                .setStateManager(stateManager)
                .setVehicleRoutingProblem(vrp)
                .setVehicleFleetManager(fm)
                .setAllowVehicleSwitch(true)
                .build();

        PositionBasedRegretInsertionFast insertion = new PositionBasedRegretInsertionFast(calculator, vrp, fm);
        insertion.setTopRoutesToExpand(5);
        for (var l : iListeners) insertion.addListener(l);

        Collection<VehicleRoute> routes = new ArrayList<>();
        stateManager.informInsertionStarts(routes, null);
        insertion.insertJobs(routes, vrp.getJobs().values());

        assertEquals(1, routes.size());
        assertEquals(2, routes.iterator().next().getActivities().size());
    }

    @Test
    @DisplayName("Should be creatable via InsertionStrategyBuilder")
    void shouldBeCreatableViaInsertionStrategyBuilder() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addVehicle(v).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        InsertionStrategy strategy = new InsertionStrategyBuilder(vrp, fm, stateManager, constraintManager)
                .setInsertionStrategy(InsertionStrategyBuilder.Strategy.POSITION_BASED_REGRET_FAST)
                .setPositionBasedRegretK(3)
                .setTopRoutesToExpand(5)
                .build();

        assertNotNull(strategy);
        assertTrue(strategy instanceof PositionBasedRegretInsertionFast);

        Collection<VehicleRoute> routes = new ArrayList<>();
        stateManager.informInsertionStarts(routes, null);
        strategy.insertJobs(routes, vrp.getJobs().values());

        assertEquals(1, routes.size());
    }

    @Test
    @DisplayName("Should handle empty job list")
    void shouldHandleEmptyJobList() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(v).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        ArrayList<com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener> iListeners = new ArrayList<>();
        JobInsertionCostsCalculator calculator = new JobInsertionCostsCalculatorBuilder(
                iListeners, new ArrayList<>())
                .setLocalLevel(true)
                .setConstraintManager(constraintManager)
                .setStateManager(stateManager)
                .setVehicleRoutingProblem(vrp)
                .setVehicleFleetManager(fm)
                .setAllowVehicleSwitch(true)
                .build();

        PositionBasedRegretInsertionFast insertion = new PositionBasedRegretInsertionFast(calculator, vrp, fm);

        Collection<VehicleRoute> routes = new ArrayList<>();
        Collection<?> badJobs = insertion.insertJobs(routes, new ArrayList<>());

        assertTrue(routes.isEmpty());
        assertTrue(badJobs.isEmpty());
    }

    @Test
    @DisplayName("Should assign services to multiple vehicles")
    void shouldAssignServicesToMultipleVehicles() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2")
                .setLocation(Location.newInstance(0, -10)).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 10)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                .setStartLocation(Location.newInstance(0, -10)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addVehicle(v1).addVehicle(v2)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        ArrayList<com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener> iListeners = new ArrayList<>();
        JobInsertionCostsCalculator calculator = new JobInsertionCostsCalculatorBuilder(
                iListeners, new ArrayList<>())
                .setLocalLevel(true)
                .setConstraintManager(constraintManager)
                .setStateManager(stateManager)
                .setVehicleRoutingProblem(vrp)
                .setVehicleFleetManager(fm)
                .setAllowVehicleSwitch(true)
                .build();

        PositionBasedRegretInsertionFast insertion = new PositionBasedRegretInsertionFast(calculator, vrp, fm);
        for (var l : iListeners) insertion.addListener(l);

        Collection<VehicleRoute> routes = new ArrayList<>();
        stateManager.informInsertionStarts(routes, null);
        insertion.insertJobs(routes, vrp.getJobs().values());

        // Total jobs inserted should be 2
        int totalJobs = 0;
        for (VehicleRoute route : routes) {
            totalJobs += route.getTourActivities().getJobs().size();
        }
        assertEquals(2, totalJobs);
    }
}
