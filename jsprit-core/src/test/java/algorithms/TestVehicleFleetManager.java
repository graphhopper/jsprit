/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleImpl.VehicleBuilder;
import basics.route.VehicleImpl.VehicleType;
import basics.route.VehicleImpl.VehicleType.Builder;

import algorithms.VehicleFleetManager;
import algorithms.VehicleFleetManagerImpl;
import algorithms.VehicleFleetManager.TypeKey;

import junit.framework.TestCase;

public class TestVehicleFleetManager extends TestCase{
	
	VehicleFleetManager fleetManager;
	
	Vehicle v1;
	
	Vehicle v2;
	
	public void setUp(){
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		
		v1 = VehicleImpl.VehicleBuilder.newInstance("standard").setLocationId("loc").setType(VehicleImpl.VehicleType.Builder.newInstance("standard", 0).build()).build();
		v2 = VehicleImpl.VehicleBuilder.newInstance("foo").setLocationId("fooLoc").setType(VehicleImpl.VehicleType.Builder.newInstance("foo", 0).build()).build();

		vehicles.add(v1);
		vehicles.add(v2);
		fleetManager = new VehicleFleetManagerImpl(vehicles);	
	}
	
	public void testGetTypes(){
		Collection<TypeKey> types = fleetManager.getAvailableVehicleTypes();
		assertEquals(2, types.size());
	}
	
	public void testGetVehicle(){
		TypeKey typeKey = new TypeKey(v1.getType(),v1.getLocationId());
		Vehicle v = fleetManager.getEmptyVehicle(typeKey);
		assertEquals(v.getId(), v1.getId());
	}
	
	public void testLock(){
		fleetManager.lock(v1);
		Collection<TypeKey> types = fleetManager.getAvailableVehicleTypes();
		assertEquals(1, types.size());
	}
	
	public void testLockTwice(){
		fleetManager.lock(v1);
		Collection<TypeKey> types = fleetManager.getAvailableVehicleTypes();
		assertEquals(1, types.size());
		try{
			fleetManager.lock(v1);
			Collection<TypeKey> types_ = fleetManager.getAvailableVehicleTypes();
			assertFalse(true);
		}
		catch(IllegalStateException e){
			assertTrue(true);
		}
	}
	
	public void testGetTypesWithout(){
		TypeKey typeKey = new TypeKey(v1.getType(),v1.getLocationId());
		Collection<TypeKey> types = fleetManager.getAvailableVehicleTypes(typeKey);
		
		assertEquals(new TypeKey(v2.getType(),v2.getLocationId()), types.iterator().next());
		assertEquals(1, types.size());
	}
	
	public void testUnlock(){
		fleetManager.lock(v1);
		Collection<TypeKey> types = fleetManager.getAvailableVehicleTypes();
		assertEquals(1, types.size());
		fleetManager.unlock(v1);
		Collection<TypeKey> types_ = fleetManager.getAvailableVehicleTypes();
		assertEquals(2, types_.size());
	}

}
