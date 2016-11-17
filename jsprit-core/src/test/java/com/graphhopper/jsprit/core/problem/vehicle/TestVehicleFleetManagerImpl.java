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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class TestVehicleFleetManagerImpl {

    VehicleFleetManager fleetManager;

    VehicleImpl v1;

    VehicleImpl v2;

    @Before
    public void setUp() {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();

        v1 = VehicleImpl.Builder.newInstance("standard").setStartLocation(Location.newInstance("loc")).setType(VehicleTypeImpl.Builder.newInstance("standard").build()).build();
        v2 = VehicleImpl.Builder.newInstance("foo").setStartLocation(Location.newInstance("fooLoc")).setType(VehicleTypeImpl.Builder.newInstance("foo").build()).build();

        VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addVehicle(v2).build();
//		v1.
        vehicles.add(v1);
        vehicles.add(v2);
        fleetManager = new FiniteFleetManagerFactory(vehicles).createFleetManager();
    }

    @Test
    public void testGetVehicles() {
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        assertEquals(2, vehicles.size());
    }

    @Test
    public void testLock() {
        fleetManager.lock(v1);
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        assertEquals(1, vehicles.size());
    }

    @Test
    public void testLock2() {
        fleetManager.lock(v1);
        fleetManager.lock(v2);
        fleetManager.unlock(v2);
        assertTrue(fleetManager.isLocked(v1));
    }

    @Test
    public void testIsLocked() {
        fleetManager.lock(v1);
        assertTrue(fleetManager.isLocked(v1));
    }

    @Test
    public void testLockTwice() {
        fleetManager.lock(v1);
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        assertEquals(1, vehicles.size());
        try {
            fleetManager.lock(v1);
            @SuppressWarnings("unused")
            Collection<Vehicle> vehicles_ = fleetManager.getAvailableVehicles();
            assertFalse(true);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testGetVehiclesWithout() {
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles(v1);

        assertEquals(v2, vehicles.iterator().next());
        assertEquals(1, vehicles.size());
    }

    @Test
    public void testUnlock() {
        fleetManager.lock(v1);
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        assertEquals(1, vehicles.size());
        fleetManager.unlock(v1);
        Collection<Vehicle> vehicles_ = fleetManager.getAvailableVehicles();
        assertEquals(2, vehicles_.size());
    }

    @Test
    public void whenAddingTwoVehiclesWithSameTypeIdAndLocation_getAvailableVehicleShouldReturnOnlyOneOfThem() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("standard").build();
        Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("loc")).setType(type).build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("loc")).setType(type).build();
        VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(Arrays.asList(v1, v2)).createFleetManager();
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        assertEquals(1, vehicles.size());
    }

    @Test
    public void whenAddingTwoVehiclesWithSameTypeIdStartAndEndLocationAndWorkingShift_getAvailableVehicleShouldReturnOnlyOneOfThem() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("standard").build();
        Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("loc")).setEndLocation(Location.newInstance("endLoc"))
            .setType(type).setEarliestStart(0.).setLatestArrival(10.).build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("loc")).setEndLocation(Location.newInstance("endLoc"))
            .setType(type).setEarliestStart(0.).setLatestArrival(10.).build();
        VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(Arrays.asList(v1, v2)).createFleetManager();
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        assertEquals(1, vehicles.size());
    }

    @Test
    public void whenAddingTwoVehiclesWithDifferentType_getAvailableVehicleShouldReturnBoth() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("standard").build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("type2").build();
        Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("loc")).setEndLocation(Location.newInstance("endLoc"))
            .setType(type).setEarliestStart(0.).setLatestArrival(10.).build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("loc")).setEndLocation(Location.newInstance("endLoc"))
            .setType(type2).setEarliestStart(0.).setLatestArrival(10.).build();
        VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addVehicle(v2).build();
        VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(Arrays.asList(v1, v2)).createFleetManager();
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        assertEquals(2, vehicles.size());
        assertTrue(vehicleInCollection(v1, vehicles));
        assertTrue(vehicleInCollection(v2, vehicles));
    }

    @Test
    public void whenAddingTwoVehiclesWithDifferentStartLocation_getAvailableVehicleShouldReturnBoth() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("standard").build();
        Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("startLoc")).setEndLocation(Location.newInstance("endLoc"))
            .setType(type).setEarliestStart(0.).setLatestArrival(10.).build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("loc")).setEndLocation(Location.newInstance("endLoc"))
            .setType(type).setEarliestStart(0.).setLatestArrival(10.).build();
        VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addVehicle(v2).build();
        VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(Arrays.asList(v1, v2)).createFleetManager();
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        assertEquals(2, vehicles.size());
        assertTrue(vehicleInCollection(v1, vehicles));
        assertTrue(vehicleInCollection(v2, vehicles));
    }

    @Test
    public void whenAddingTwoVehiclesWithDifferentEndLocation_getAvailableVehicleShouldReturnBoth() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("standard").build();
        Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("loc")).setEndLocation(Location.newInstance("endLocation"))
            .setType(type).setEarliestStart(0.).setLatestArrival(10.).build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("loc")).setEndLocation(Location.newInstance("endLoc"))
            .setType(type).setEarliestStart(0.).setLatestArrival(10.).build();
        VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addVehicle(v2).build();
        VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(Arrays.asList(v1, v2)).createFleetManager();
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        assertEquals(2, vehicles.size());
        assertTrue(vehicleInCollection(v1, vehicles));
        assertTrue(vehicleInCollection(v2, vehicles));
    }

    @Test
    public void whenAddingTwoVehiclesWithDifferentEarliestStart_getAvailableVehicleShouldReturnBoth() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("standard").build();
        Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("loc")).setEndLocation(Location.newInstance("endLoc"))
            .setType(type).setEarliestStart(5.).setLatestArrival(10.).build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("loc")).setEndLocation(Location.newInstance("endLoc"))
            .setType(type).setEarliestStart(0.).setLatestArrival(10.).build();
        VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addVehicle(v2).build();
        VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(Arrays.asList(v1, v2)).createFleetManager();
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        assertEquals(2, vehicles.size());
        assertTrue(vehicleInCollection(v1, vehicles));
        assertTrue(vehicleInCollection(v2, vehicles));
    }

    @Test
    public void whenAddingTwoVehiclesWithDifferentLatestArr_getAvailableVehicleShouldReturnBoth() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("standard").build();
        Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("loc")).setEndLocation(Location.newInstance("endLoc"))
            .setType(type).setEarliestStart(0.).setLatestArrival(20.).build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("loc")).setEndLocation(Location.newInstance("endLoc"))
            .setType(type).setEarliestStart(0.).setLatestArrival(10.).build();
        VehicleRoutingProblem.Builder.newInstance().addVehicle(v1).addVehicle(v2).build();
        VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(Arrays.asList(v1, v2)).createFleetManager();
        Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        assertEquals(2, vehicles.size());
        assertTrue(vehicleInCollection(v1, vehicles));
        assertTrue(vehicleInCollection(v2, vehicles));
    }

    private boolean vehicleInCollection(Vehicle v, Collection<Vehicle> vehicles) {
        for (Vehicle veh : vehicles) {
            if (veh == v) return true;
        }
        return false;
    }


}
