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
	
	public abstract void lock(Vehicle vehicle);

	public abstract void unlock(Vehicle vehicle);

	public abstract boolean isLocked(Vehicle vehicle);

	public abstract void unlockAll();

	public abstract Collection<Vehicle> getAvailableVehicles();

	/**
	 * 
	 * @param withoutThisType
	 * @param locationId
	 * @return
	 * @deprecated use .getAvailableVehicles(Vehicle without) instead. this might ignore withoutType and returns all available vehicles
	 */
	@Deprecated
	public Collection<Vehicle> getAvailableVehicles(String withoutThisType, String locationId);
	
	public Collection<Vehicle> getAvailableVehicles(Vehicle withoutThisType);

}
