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
package com.graphhopper.jsprit.core.problem.vehicle;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * The fleet manager offers one free vehicle per class and reads the vehicles in use from the routes.
 */
public class VehicleFleetManagerTest {

    private final VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 2).build();

    private VehicleImpl vehicle(String id) {
        return VehicleImpl.Builder.newInstance(id).setStartLocation(Location.newInstance(0, 0)).setType(type).build();
    }

    private VehicleImpl vehicleWithBreak(String id, double start, double end) {
        return VehicleImpl.Builder.newInstance(id).setStartLocation(Location.newInstance(0, 0)).setType(type)
            .setBreak(Break.Builder.newInstance("break-" + id).setTimeWindow(TimeWindow.newInstance(start, end)).setServiceTime(10).build()).build();
    }

    private VehicleFleetManagerImpl fleet(Vehicle... vehicles) {
        VehicleFleetManagerImpl fleet = (VehicleFleetManagerImpl) new FiniteFleetManagerFactory(Arrays.asList(vehicles)).createFleetManager(null);
        fleet.setVerify(true);
        return fleet;
    }

    private static List<String> ids(Collection<Vehicle> vehicles) {
        List<String> ids = new ArrayList<>();
        for (Vehicle v : vehicles) ids.add(v.getId());
        return ids;
    }

    @Test
    public void identicalVehiclesFormOneClassAndTheFirstFreeOneIsOffered() {
        Vehicle a = vehicle("a"), b = vehicle("b"), c = vehicle("c");
        VehicleFleetManagerImpl fleet = fleet(a, b, c);
        assertEquals(List.of("a"), ids(fleet.getAvailableVehicles()));
        VehicleRoute route = VehicleRoute.Builder.newInstance(a).build();
        fleet.sync(route);
        assertEquals(List.of("b"), ids(fleet.getAvailableVehicles()));
        assertSame(b, fleet.getAvailableVehicle(a));
        assertTrue(fleet.getAvailableVehicles(a).isEmpty());
    }

    @Test
    public void theOfferedVehicleDoesNotRotate() {
        Vehicle a = vehicle("a"), b = vehicle("b");
        VehicleFleetManagerImpl fleet = fleet(a, b);
        for (int i = 0; i < 5; i++) {
            fleet.reset(Collections.emptyList());
            assertEquals(List.of("a"), ids(fleet.getAvailableVehicles()));
        }
    }

    @Test
    public void vehiclesThatDifferInTheirBreakAreBothOffered() {
        Vehicle a = vehicleWithBreak("a", 0, 100), b = vehicleWithBreak("b", 200, 300), c = vehicleWithBreak("c", 0, 100);
        VehicleFleetManagerImpl fleet = fleet(a, b, c);
        assertEquals(List.of("a", "b"), ids(fleet.getAvailableVehicles()));
    }

    @Test
    public void aSwitchFreesTheOldVehicleWithoutAnyEvent() {
        Vehicle a = vehicle("a"), b = vehicleWithBreak("b", 0, 100);
        VehicleFleetManagerImpl fleet = fleet(a, b);
        VehicleRoute route = VehicleRoute.Builder.newInstance(a).build();
        fleet.reset(List.of(route));
        assertTrue(fleet.isInUse(a));
        route.setVehicleAndDepartureTime(b, 0);
        fleet.sync(route);
        assertFalse(fleet.isInUse(a));
        assertTrue(fleet.isInUse(b));
    }

    @Test
    public void syncIsIdempotentAndIndependentOfTheOrder() {
        Vehicle a = vehicle("a"), b = vehicleWithBreak("b", 0, 100), c = vehicleWithBreak("c", 200, 300);
        VehicleRoute r1 = VehicleRoute.Builder.newInstance(a).build();
        VehicleRoute r2 = VehicleRoute.Builder.newInstance(b).build();
        VehicleFleetManagerImpl one = fleet(a, b, c), other = fleet(a, b, c);
        one.sync(r1);
        one.sync(r2);
        one.sync(r1);
        other.sync(r2);
        other.sync(r1);
        other.sync(r2);
        other.sync(r2);
        assertEquals(List.of("c"), ids(one.getAvailableVehicles()));
        assertEquals(ids(one.getAvailableVehicles()), ids(other.getAvailableVehicles()));
    }

    @Test
    public void aReleasedRouteFreesItsVehicle() {
        Vehicle a = vehicle("a");
        VehicleFleetManagerImpl fleet = fleet(a);
        VehicleRoute route = VehicleRoute.Builder.newInstance(a).build();
        fleet.sync(route);
        assertTrue(fleet.getAvailableVehicles().isEmpty());
        fleet.release(route);
        fleet.release(route);
        assertEquals(List.of("a"), ids(fleet.getAvailableVehicles()));
    }

    @Test
    public void resetForgetsRoutesThatAreNotPartOfTheSolutionAnymore() {
        Vehicle a = vehicle("a"), b = vehicleWithBreak("b", 0, 100);
        VehicleFleetManagerImpl fleet = fleet(a, b);
        fleet.sync(VehicleRoute.Builder.newInstance(a).build());
        fleet.reset(List.of(VehicleRoute.Builder.newInstance(b).build()));
        assertEquals(List.of("a"), ids(fleet.getAvailableVehicles()));
    }

    @Test(expected = IllegalStateException.class)
    public void verificationThrowsWhenARouteChangedItsVehicleWithoutTheMandatoryCall() {
        Vehicle a = vehicle("a"), b = vehicleWithBreak("b", 0, 100);
        VehicleFleetManagerImpl fleet = fleet(a, b);
        VehicleRoute route = VehicleRoute.Builder.newInstance(a).build();
        fleet.reset(List.of(route));
        route.setVehicleAndDepartureTime(b, 0);
        fleet.getAvailableVehicles();
    }

    @Test(expected = IllegalStateException.class)
    public void verificationThrowsForARouteThatGotItsVehicleWithoutTheMandatoryCall() {
        Vehicle a = vehicle("a");
        VehicleFleetManagerImpl fleet = fleet(a);
        fleet.verify(VehicleRoute.Builder.newInstance(a).build());
    }

    @Test
    public void anInfiniteFleetOffersOneVehiclePerClassAndNeverRunsOut() {
        Vehicle a = vehicle("a"), b = vehicle("b"), c = vehicleWithBreak("c", 0, 100);
        VehicleFleetManager fleet = new InfiniteFleetManagerFactory(List.of(a, b, c)).createFleetManager();
        fleet.sync(VehicleRoute.Builder.newInstance(a).build());
        assertEquals(List.of("a", "c"), ids(fleet.getAvailableVehicles()));
        assertEquals(List.of("c"), ids(fleet.getAvailableVehicles(b)));
        assertSame(a, fleet.getAvailableVehicle(b));
    }

    @Test
    public void aSearchUsesEveryVehicleAtMostOnce() {
        boolean before = Boolean.getBoolean(VehicleFleetManagerImpl.VERIFY);
        System.setProperty(VehicleFleetManagerImpl.VERIFY, "true");
        try {
            VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
            for (int i = 0; i < 6; i++) builder.addVehicle(vehicle("v" + i));
            Random random = new Random(4711);
            for (int i = 0; i < 12; i++)
                builder.addJob(Service.Builder.newInstance("s" + i).addSizeDimension(0, 1)
                    .setLocation(Location.newInstance(random.nextInt(100), random.nextInt(100))).build());
            VehicleRoutingProblem vrp = builder.build();
            VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS, "1").buildAlgorithm();
            algorithm.setMaxIterations(50);
            VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());
            assertTrue(solution.getUnassignedJobs().isEmpty());
            Set<String> used = new HashSet<>();
            for (VehicleRoute route : solution.getRoutes())
                assertTrue("vehicle used twice: " + route.getVehicle().getId(), used.add(route.getVehicle().getId()));
            assertEquals(6, used.size());
        } finally {
            System.setProperty(VehicleFleetManagerImpl.VERIFY, String.valueOf(before));
        }
    }
}
