/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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

import jsprit.core.problem.HasId;
import jsprit.core.problem.HasIndex;
import jsprit.core.problem.Location;
import jsprit.core.problem.Skills;

/**
 * Basic interface for vehicle-data.
 * 
 * @author schroeder
 *
 */
public interface Vehicle extends HasId, HasIndex {

	/**
	 * Returns the earliest departure of vehicle which should be the lower bound of this vehicle's departure times. 
	 * 
	 * @return earliest departure time
	 */
	public abstract double getEarliestDeparture();

	/**
	 * Returns the latest arrival time at this vehicle's end-location which should be the upper bound of this vehicle's arrival times at end-location.
	 * 
	 * @return latest arrival time of this vehicle
	 * 
	 */
	public abstract double getLatestArrival();

	/**
	 * Returns the {@link VehicleType} of this vehicle.
	 * 
	 * @return {@link VehicleType} of this vehicle
	 */
	public abstract VehicleType getType();

	/**
	 * Returns the id of this vehicle.
	 * 
	 * @return id
	 */
	public abstract String getId();

	/**
	 * Returns true if vehicle returns to depot, false otherwise.
	 * 
	 * @return true if isReturnToDepot
	 */
	public abstract boolean isReturnToDepot();

    public abstract Location getStartLocation();
	
	public abstract Location getEndLocation();
	
	public abstract VehicleTypeKey getVehicleTypeIdentifier();

    public abstract Skills getSkills();
}
