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

import jsprit.core.problem.vehicle.VehicleImpl.NoVehicle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


class VehicleFleetManagerImpl implements VehicleFleetManager {
	
	public VehicleFleetManagerImpl newInstance(Collection<Vehicle> vehicles){
		return new VehicleFleetManagerImpl(vehicles);
	}
	
	static class TypeContainer {
		
		private ArrayList<Vehicle> vehicleList;
		
		public TypeContainer() {
			super();
			vehicleList = new ArrayList<Vehicle>();
		}
		
		void add(Vehicle vehicle){
			if(vehicleList.contains(vehicle)){
				throw new IllegalStateException("cannot add vehicle twice " + vehicle.getId());
			}
			vehicleList.add(vehicle);
		}
		
		void remove(Vehicle vehicle){
			vehicleList.remove(vehicle);
		}

		public Vehicle getVehicle() {
			return vehicleList.get(0);
//			return vehicleList.getFirst();
		}

		public boolean isEmpty() {
			return vehicleList.isEmpty();
		}
		
	}
	
	private static Logger logger = LogManager.getLogger(VehicleFleetManagerImpl.class);
	
	private Collection<Vehicle> vehicles;
	
	private Set<Vehicle> lockedVehicles;

	private Map<VehicleTypeKey,TypeContainer> typeMapOfAvailableVehicles;
	
	private Map<VehicleTypeKey,Vehicle> penaltyVehicles = new HashMap<VehicleTypeKey, Vehicle>();

	
	public VehicleFleetManagerImpl(Collection<Vehicle> vehicles) {
		super();
		this.vehicles = vehicles;
		this.lockedVehicles = new HashSet<Vehicle>();
		makeMap();
		logger.debug("initialise {}", this);
	}

	@Override
	public String toString() {
		return "[name=finiteVehicles]";
	}

	private void makeMap() {
		typeMapOfAvailableVehicles = new HashMap<VehicleTypeKey, TypeContainer>();
		penaltyVehicles = new HashMap<VehicleTypeKey, Vehicle>();
		for(Vehicle v : vehicles){
			addVehicle(v);
		}
	}

	private void addVehicle(Vehicle v) {
		if(v.getType() == null){
			throw new IllegalStateException("vehicle needs type");
		}
		VehicleTypeKey typeKey = new VehicleTypeKey(v.getType().getTypeId(), v.getStartLocation().getId(), v.getEndLocation().getId(), v.getEarliestDeparture(), v.getLatestArrival(), v.getSkills());
		if(!typeMapOfAvailableVehicles.containsKey(typeKey)){
			typeMapOfAvailableVehicles.put(typeKey, new TypeContainer());
		}
		typeMapOfAvailableVehicles.get(typeKey).add(v);

	}
	
	private void removeVehicle(Vehicle v){
		VehicleTypeKey key = new VehicleTypeKey(v.getType().getTypeId(), v.getStartLocation().getId(), v.getEndLocation().getId(), v.getEarliestDeparture(), v.getLatestArrival(), v.getSkills());
		if(typeMapOfAvailableVehicles.containsKey(key)){
			typeMapOfAvailableVehicles.get(key).remove(v);
		}
	}

	
	/**
	 * Returns a collection of available vehicles.
	 * 
	 *<p>If there is no vehicle with a certain type and location anymore, it looks up whether a penalty vehicle has been specified with 
	 * this type and location. If so, it returns this penalty vehicle. If not, no vehicle with this type and location is returned.
	 */
	@Override
	public Collection<Vehicle> getAvailableVehicles() {
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		for(VehicleTypeKey key : typeMapOfAvailableVehicles.keySet()){
			if(!typeMapOfAvailableVehicles.get(key).isEmpty()){
				vehicles.add(typeMapOfAvailableVehicles.get(key).getVehicle());
			}
			else{
				if(penaltyVehicles.containsKey(key)){
					vehicles.add(penaltyVehicles.get(key));
				}
			}
		}
		return vehicles;
	}
	
	@Override
	public Collection<Vehicle> getAvailableVehicles(Vehicle withoutThisType) {
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		VehicleTypeKey thisKey = new VehicleTypeKey(withoutThisType.getType().getTypeId(), withoutThisType.getStartLocation().getId(), withoutThisType.getEndLocation().getId(), withoutThisType.getEarliestDeparture(), withoutThisType.getLatestArrival(), withoutThisType.getSkills());
		for(VehicleTypeKey key : typeMapOfAvailableVehicles.keySet()){
			if(key.equals(thisKey)) continue;
			if(!typeMapOfAvailableVehicles.get(key).isEmpty()){
				vehicles.add(typeMapOfAvailableVehicles.get(key).getVehicle());
			}
			else{
				if(penaltyVehicles.containsKey(key)){
					vehicles.add(penaltyVehicles.get(key));
				}
			}
		}
		return vehicles;
	}

	/* (non-Javadoc)
	 * @see org.matsim.contrib.freight.vrp.basics.VehicleFleetManager#lock(org.matsim.contrib.freight.vrp.basics.Vehicle)
	 */
	@Override
	public void lock(Vehicle vehicle){
		if(vehicles.isEmpty() || vehicle instanceof NoVehicle){
			return;
		}
		boolean locked = lockedVehicles.add(vehicle);
		removeVehicle(vehicle);
		if(!locked){
			throw new IllegalStateException("cannot lock vehicle twice " + vehicle.getId());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.matsim.contrib.freight.vrp.basics.VehicleFleetManager#unlock(org.matsim.contrib.freight.vrp.basics.Vehicle)
	 */
	@Override
	public void unlock(Vehicle vehicle){
		if(vehicles.isEmpty() || vehicle instanceof NoVehicle){
			return;
		}
		if(vehicle == null) return;
		lockedVehicles.remove(vehicle);
		addVehicle(vehicle);
	}

	/* (non-Javadoc)
	 * @see org.matsim.contrib.freight.vrp.basics.VehicleFleetManager#isLocked(org.matsim.contrib.freight.vrp.basics.Vehicle)
	 */
	@Override
	public boolean isLocked(Vehicle vehicle) {
		return lockedVehicles.contains(vehicle);
	}

	/* (non-Javadoc)
	 * @see org.matsim.contrib.freight.vrp.basics.VehicleFleetManager#unlockAll()
	 */
	@Override
	public void unlockAll() {
		Collection<Vehicle> locked = new ArrayList<Vehicle>(lockedVehicles);
		for(Vehicle v : locked){
			unlock(v);
		}
		if(!lockedVehicles.isEmpty()){
			throw new IllegalStateException("no vehicle must be locked");
		}
	}

	@Deprecated
	public int sizeOfLockedVehicles(){
		return lockedVehicles.size();
	}

}
