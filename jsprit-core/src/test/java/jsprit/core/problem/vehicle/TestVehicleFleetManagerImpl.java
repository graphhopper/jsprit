/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.vehicle;

import jsprit.core.problem.Location;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class TestVehicleFleetManagerImpl {

    VehicleFleetManager fleetManager;

    Vehicle v1;

    Vehicle v2;

    @Before
    public void setUp() {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();

        v1 = VehicleImpl.Builder.newInstance("standard").setStartLocation(Location.newInstance("loc")).setType(VehicleTypeImpl.Builder.newInstance("standard").build()).build();
        v2 = VehicleImpl.Builder.newInstance("foo").setStartLocation(Location.newInstance("fooLoc")).setType(VehicleTypeImpl.Builder.newInstance("foo").build()).build();

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
    public void testWithPenalty_whenHavingOneRegularVehicleAvailable_noPenaltyVehicleIsReturn() {
        Vehicle penalty4standard = VehicleImpl.Builder.newInstance("standard_penalty").setStartLocation(Location.newInstance("loc")).
            setType(VehicleTypeImpl.Builder.newInstance("standard").build()).build();

        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        vehicles.add(v1);
        vehicles.add(v2);
        vehicles.add(penalty4standard);
        VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(vehicles).createFleetManager();

        Collection<Vehicle> availableVehicles = fleetManager.getAvailableVehicles();
        assertEquals(2, availableVehicles.size());
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
