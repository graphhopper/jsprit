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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;




class InfiniteVehicles implements VehicleFleetManager{
	
	private static Logger logger = LogManager.getLogger(InfiniteVehicles.class);
	
	private Map<VehicleTypeKey,Vehicle> types = new HashMap<VehicleTypeKey, Vehicle>();
	
//	private List<VehicleTypeKey> sortedTypes = new ArrayList<VehicleTypeKey>();
		
	public InfiniteVehicles(Collection<Vehicle> vehicles){
		extractTypes(vehicles);
		logger.debug("initialise {}", this);
	}
	
	@Override
	public String toString() {
		return "[name=infiniteVehicle]";
	}

	private void extractTypes(Collection<Vehicle> vehicles) {
		for(Vehicle v : vehicles){
			VehicleTypeKey typeKey = new VehicleTypeKey(v.getType().getTypeId(), v.getStartLocation().getId(),v.getEndLocation().getId(), v.getEarliestDeparture(), v.getLatestArrival(), v.getSkills());
			types.put(typeKey,v);
//			sortedTypes.add(typeKey);
		}
	}

	
	@Override
	public void lock(Vehicle vehicle) {
		
	}

	@Override
	public void unlock(Vehicle vehicle) {
		
	}


	@Override
	public boolean isLocked(Vehicle vehicle) {
		return false;
	}

	@Override
	public void unlockAll() {
		
	}

	@Override
	public Collection<Vehicle> getAvailableVehicles() {
		return types.values();
	}

	@Override
	public Collection<Vehicle> getAvailableVehicles(Vehicle withoutThisType) {
		Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
		VehicleTypeKey thisKey = new VehicleTypeKey(withoutThisType.getType().getTypeId(), withoutThisType.getStartLocation().getId(), withoutThisType.getEndLocation().getId(), withoutThisType.getEarliestDeparture(), withoutThisType.getLatestArrival(), withoutThisType.getSkills());
		for(VehicleTypeKey key : types.keySet()){
			if(!key.equals(thisKey)){
				vehicles.add(types.get(key));
			}
		}
		return vehicles;
	}

}
