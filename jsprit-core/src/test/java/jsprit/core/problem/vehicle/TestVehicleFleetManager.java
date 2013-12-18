/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package jsprit.core.problem.vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jsprit.core.problem.vehicle.FiniteFleetManagerFactory;
import jsprit.core.problem.vehicle.PenaltyVehicleType;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import junit.framework.TestCase;

public class TestVehicleFleetManager extends TestCase{
	
	VehicleFleetManager fleetManager;
	
	Vehicle v1;
	
	Vehicle v2;
	
	public void setUp(){
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		
		v1 = VehicleImpl.Builder.newInstance("standard").setLocationId("loc").setType(VehicleTypeImpl.Builder.newInstance("standard", 0).build()).build();
		v2 = VehicleImpl.Builder.newInstance("foo").setLocationId("fooLoc").setType(VehicleTypeImpl.Builder.newInstance("foo", 0).build()).build();

		vehicles.add(v1);
		vehicles.add(v2);
		fleetManager = new FiniteFleetManagerFactory(vehicles).createFleetManager();	
	}
	
	public void testGetVehicles(){
		Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
		assertEquals(2, vehicles.size());
	}
	
	public void testLock(){
		fleetManager.lock(v1);
		Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
		assertEquals(1, vehicles.size());
	}
	
	public void testLockTwice(){
		fleetManager.lock(v1);
		Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
		assertEquals(1, vehicles.size());
		try{
			fleetManager.lock(v1);
			@SuppressWarnings("unused")
			Collection<Vehicle> vehicles_ = fleetManager.getAvailableVehicles();
			assertFalse(true);
		}
		catch(IllegalStateException e){
			assertTrue(true);
		}
	}
	
	public void testGetVehiclesWithout(){
		Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles(v1.getType().getTypeId(),v1.getLocationId());
		
		assertEquals(v2, vehicles.iterator().next());
		assertEquals(1, vehicles.size());
	}
	
	public void testUnlock(){
		fleetManager.lock(v1);
		Collection<Vehicle> vehicles = fleetManager.getAvailableVehicles();
		assertEquals(1, vehicles.size());
		fleetManager.unlock(v1);
		Collection<Vehicle> vehicles_ = fleetManager.getAvailableVehicles();
		assertEquals(2, vehicles_.size());
	}
	
	public void testWithPenalty_whenHavingOneRegularVehicleAvailable_noPenaltyVehicleIsReturn(){
		Vehicle penalty4standard = VehicleImpl.Builder.newInstance("standard_penalty").setLocationId("loc").
					setType(VehicleTypeImpl.Builder.newInstance("standard", 0).build()).build();
		
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		vehicles.add(v1);
		vehicles.add(v2);
		vehicles.add(penalty4standard);
		VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(vehicles).createFleetManager();
		
		Collection<Vehicle> availableVehicles = fleetManager.getAvailableVehicles();
		assertEquals(2, availableVehicles.size());
	}

	public void testWithPenalty_whenHavingTwoRegularVehicleAvailablePlusOnePenaltyVehicle_andOneIsLocked_returnTheOtherRegularVehicle(){
		VehicleTypeImpl penaltyType = VehicleTypeImpl.Builder.newInstance("standard", 0).build();
		PenaltyVehicleType penaltyVehicleType = new PenaltyVehicleType(penaltyType);
		
		Vehicle penalty4standard = VehicleImpl.Builder.newInstance("standard_penalty").setLocationId("loc").
					setType(penaltyVehicleType).build();
		
		Vehicle v3 = VehicleImpl.Builder.newInstance("standard_v3").setLocationId("loc").
				setType(penaltyType).build();
		
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		vehicles.add(v1);
		vehicles.add(v2);
		vehicles.add(penalty4standard);
		vehicles.add(v3);
		VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(vehicles).createFleetManager();
		fleetManager.lock(v1);
		fleetManager.lock(v2);
		Collection<Vehicle> availableVehicles = fleetManager.getAvailableVehicles();
		assertEquals(1, availableVehicles.size());
		assertEquals(v3, availableVehicles.iterator().next());
	}
	
	public void testWithPenalty_whenHavingNoRegularVehicleAvailable_penaltyVehicleIsReturned(){
		VehicleTypeImpl penaltyType = VehicleTypeImpl.Builder.newInstance("standard", 0).build();
		
		Vehicle penalty4standard = VehicleImpl.Builder.newInstance("standard_penalty").setLocationId("loc").
					setType(penaltyType).build();
		
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		vehicles.add(v1);
		vehicles.add(v2);
		vehicles.add(penalty4standard);
		VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(vehicles).createFleetManager();
		fleetManager.lock(v1);
		fleetManager.lock(v2);
		Collection<Vehicle> availableVehicles = fleetManager.getAvailableVehicles();
		assertEquals(penalty4standard, availableVehicles.iterator().next());
	}
}
