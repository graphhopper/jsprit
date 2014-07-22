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

/**
 * Factory that creates a finite fleetmanager.
 *
 * @author schroeder
 */
public class FiniteFleetManagerFactory implements VehicleFleetManagerFactory{

	private Collection<Vehicle> vehicles;
	
	/**
	 * Constucts the factory.
	 *
	 * @param vehicles vehicles to be added to the fleetManager
	 */
	public FiniteFleetManagerFactory(Collection<Vehicle> vehicles) {
		super();
		this.vehicles = vehicles;
	}

	/**
	 * Creates the finite fleetmanager.
	 * 
	 * @return VehicleFleetManager
	 * @throws java.lang.IllegalStateException if vehicles == null or vehicles.isEmpty()
	 */
	@Override
	public VehicleFleetManager createFleetManager() {
		if(vehicles == null) throw new IllegalStateException("vehicles is null. this must not be.");
		if(vehicles.isEmpty()) throw new IllegalStateException("vehicle-collection is empty. this must not be");
		return new VehicleFleetManagerImpl(vehicles);
	}

}
