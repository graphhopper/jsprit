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

import java.util.Collection;


public interface VehicleFleetManager {
	
	/**
	 * Locks vehicle.
	 * 
	 * <p>This indicates that this vehicle is being used. Thus it is not in list of available vehicles.
	 * @param vehicle
	 */
	public abstract void lock(Vehicle vehicle);

	/**
	 * Unlocks vehicle.
	 * 
	 * <p>This indicates that this vehicle is not being used anymore. Thus it is in list of available vehicles.
	 * @param vehicle
	 */
	public abstract void unlock(Vehicle vehicle);

	/**
	 * Returns true if locked.
	 * 
	 * @param vehicle
	 * @return
	 */
	public abstract boolean isLocked(Vehicle vehicle);

	/**
	 * Unlocks all locked vehicles.
	 * 
	 */
	public abstract void unlockAll();

	/**
	 * Returns a collection of available vehicles.
	 * 
	 * <p>Note that this does not return ALL available vehicles that were added to the fleetmanager. Vehicles are clustered according
	 * to {@link VehicleTypeKey}. If there are two unlocked vehicle with the same VehicleTypeKey then only one of them will be returned.
	 * This is to avoid returning too many vehicles that are basically equal.
	 * <p>Look at {@link VehicleTypeKey} to figure out whether two vehicles are equal or not.
	 * 
	 * @return
	 */
	public abstract Collection<Vehicle> getAvailableVehicles();
	
	public Collection<Vehicle> getAvailableVehicles(Vehicle withoutThisType);

}
