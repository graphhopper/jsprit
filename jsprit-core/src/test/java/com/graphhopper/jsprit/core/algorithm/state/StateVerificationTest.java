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

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.RouteVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The verification mode for vehicle-dependent states (a state written for some vehicle types but read for
 * another one throws) and the route-change diagnostics.
 */
class StateVerificationTest {

    private VehicleImpl v1;

    private VehicleImpl v2;

    private VehicleRoutingProblem vrp;

    private VehicleRoute route;

    private TourActivity act;

    @BeforeEach
    void setUp() {
        // different start locations: two vehicle type keys
        v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 0)).build();
        v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(10, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(5, 5)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(6, 5)).build();
        vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addVehicle(v2).addJob(s1).addJob(s2)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();
        route = VehicleRoute.Builder.newInstance(v1).setJobActivityFactory(vrp.getJobActivityFactory()).addService(s1).addService(s2).build();
        act = route.getActivities().get(0);
    }

    @Test
    void activityStateMissingForOneVehicleTypeOnlyThrows() {
        StateManager stateManager = new StateManager(vrp);
        stateManager.setFailOnMissingVehicleState(true);
        StateId distance = stateManager.createDoubleStateId("distance");
        StateId object = stateManager.createStateId("object");
        stateManager.putActivityDouble(act, v1, distance, 12.);
        stateManager.putActivityState(act, v1, object, "x");

        assertEquals(12., stateManager.getActivityDouble(act, v1, distance));
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> stateManager.getActivityDouble(act, v2, distance));
        assertTrue(e.getMessage().contains("distance") && e.getMessage().contains("v2"), e.getMessage());
        assertThrows(IllegalStateException.class, () -> stateManager.getActivityState(act, v2, distance, Double.class));
        assertThrows(IllegalStateException.class, () -> stateManager.getActivityState(act, v2, object, String.class));
        // asking whether a state exists never throws
        assertFalse(stateManager.hasActivityState(act, v2, distance));
    }

    @Test
    void routeStateMissingForOneVehicleTypeOnlyThrows() {
        StateManager stateManager = new StateManager(vrp);
        stateManager.setFailOnMissingVehicleState(true);
        StateId distance = stateManager.createDoubleStateId("distance");
        StateId object = stateManager.createStateId("object");
        stateManager.putRouteDouble(route, v1, distance, 12.);
        stateManager.putRouteState(route, v1, object, "x");

        assertThrows(IllegalStateException.class, () -> stateManager.getRouteDouble(route, v2, distance));
        assertThrows(IllegalStateException.class, () -> stateManager.getRouteState(route, v2, distance, Double.class));
        assertThrows(IllegalStateException.class, () -> stateManager.getRouteState(route, v2, object, String.class));
        assertFalse(stateManager.hasRouteState(route, v2, distance));
    }

    @Test
    void stateThatNoVehicleTypeHasReadsAsNoState() {
        StateManager stateManager = new StateManager(vrp);
        stateManager.setFailOnMissingVehicleState(true);
        StateId distance = stateManager.createDoubleStateId("distance");
        StateId other = stateManager.createDoubleStateId("other");
        stateManager.putActivityDouble(act, v1, other, 1.);
        stateManager.putRouteDouble(route, v1, other, 1.);

        assertTrue(Double.isNaN(stateManager.getActivityDouble(act, v2, distance)));
        assertNull(stateManager.getActivityState(act, v2, distance, Double.class));
        assertTrue(Double.isNaN(stateManager.getRouteDouble(route, v2, distance)));
        assertNull(stateManager.getRouteState(route, v2, distance, Double.class));
    }

    @Test
    void sparseStateMayBeMissingForSomeVehicleTypes() {
        StateManager stateManager = new StateManager(vrp);
        stateManager.setFailOnMissingVehicleState(true);
        StateId flag = stateManager.createStateId("flag");
        stateManager.declareSparse(flag);
        stateManager.putActivityState(act, v1, flag, true);
        stateManager.putRouteState(route, v1, flag, true);

        assertNull(stateManager.getActivityState(act, v2, flag, Boolean.class));
        assertNull(stateManager.getRouteState(route, v2, flag, Boolean.class));
    }

    @Test
    void withoutVerificationAMissingStateReadsAsNoState() {
        StateManager stateManager = new StateManager(vrp);
        stateManager.setFailOnMissingVehicleState(false);
        StateId distance = stateManager.createDoubleStateId("distance");
        stateManager.putActivityDouble(act, v1, distance, 12.);
        stateManager.putRouteDouble(route, v1, distance, 12.);

        assertTrue(Double.isNaN(stateManager.getActivityDouble(act, v2, distance)));
        assertTrue(Double.isNaN(stateManager.getRouteDouble(route, v2, distance)));
    }

    @Test
    void routeIsUnchangedUntilItsSequenceVehicleOrDepartureChanges() {
        StateManager stateManager = new StateManager(vrp);
        StateUpdateStats stats = stateManager.trackRouteChanges(true);

        stateManager.informInsertionStarts(Collections.singletonList(route), Collections.emptyList());
        assertEquals(1, stats.getRoutes());
        assertEquals(0, stats.getUnchangedRoutes());

        // a new iteration works on a copy of the route: same activity indices, other objects
        stateManager.informIterationStarts(1, vrp, Collections.emptyList());
        VehicleRoute copy = VehicleRoute.copyOf(route);
        stateManager.informInsertionStarts(Collections.singletonList(copy), Collections.emptyList());
        assertEquals(2, stats.getRoutes());
        assertEquals(1, stats.getUnchangedRoutes());
        assertEquals(2, stats.getUnchangedActivities());
        assertEquals(1, stats.getComparedRoutes());
        assertEquals(0, stats.getDifferingRoutes());

        copy.setVehicleAndDepartureTime(v1, 100.);
        stateManager.informInsertionStarts(Collections.singletonList(copy), Collections.emptyList());
        assertEquals(1, stats.getUnchangedRoutes());

        copy.setVehicleAndDepartureTime(v2, 100.);
        stateManager.informInsertionStarts(Collections.singletonList(copy), Collections.emptyList());
        assertEquals(1, stats.getUnchangedRoutes());

        stateManager.informInsertionStarts(Collections.singletonList(copy), Collections.emptyList());
        assertEquals(2, stats.getUnchangedRoutes());

        copy.getTourActivities().removeJob(vrp.getJobs().get("s2"));
        stateManager.informInsertionStarts(Collections.singletonList(copy), Collections.emptyList());
        assertEquals(2, stats.getUnchangedRoutes());
        assertEquals(6, stats.getInsertionStarts());
    }

    @Test
    void stateThatDependsOnMoreThanTheRouteIsReported() {
        StateManager stateManager = new StateManager(vrp);
        StateUpdateStats stats = stateManager.trackRouteChanges(true);
        StateId calls = stateManager.createStateId("calls");
        int[] counter = {0};
        class CountingUpdater implements StateUpdater, RouteVisitor {
            @Override
            public void visit(VehicleRoute r) {
                stateManager.putRouteState(r, calls, ++counter[0]);
            }
        }
        stateManager.addStateUpdater(new CountingUpdater());

        stateManager.informInsertionStarts(Collections.singletonList(route), Collections.emptyList());
        stateManager.informIterationStarts(1, vrp, Collections.emptyList());
        stateManager.informInsertionStarts(Collections.singletonList(VehicleRoute.copyOf(route)), Collections.emptyList());

        assertEquals(1, stats.getDifferingRoutes());
        assertArrayEquals(new long[]{1, 0, 0, 1}, stats.getDifferences().get("route:calls"));
    }

    @Test
    void theStatesOfTheBoxAlgorithmAreAFunctionOfTheRoute() {
        Random random = new Random(3);
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        for (int i = 0; i < 6; i++) {
            builder.addVehicle(VehicleImpl.Builder.newInstance("v" + i).setStartLocation(Location.newInstance(i * 20, 0))
                .setEarliestStart(i * 10).setLatestArrival(600 + i * 50).build());
        }
        for (int i = 0; i < 60; i++) {
            Service.Builder service = Service.Builder.newInstance("s" + i).setServiceTime(5)
                .setLocation(Location.newInstance(random.nextInt(100), random.nextInt(100)));
            if (i % 3 == 0) service.addTimeWindow(TimeWindow.newInstance(100, 400));
            builder.addJob(service.build());
        }
        VehicleRoutingProblem problem = builder.build();
        StateManager stateManager = new StateManager(problem);
        stateManager.setFailOnMissingVehicleState(true);
        StateUpdateStats stats = stateManager.trackRouteChanges(true);
        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(problem)
            .setStateAndConstraintManager(stateManager, new ConstraintManager(problem, stateManager))
            .setProperty(Jsprit.Parameter.THREADS, "1").buildAlgorithm();
        algorithm.setMaxIterations(200);
        algorithm.searchSolutions();

        assertTrue(stats.getUnchangedRoutes() > 0, stats.toString());
        assertTrue(stats.getComparedRoutes() > 0, stats.toString());
        assertEquals(0, stats.getDifferingRoutes(), stats.toString());
        System.out.println(stats);
    }
}
