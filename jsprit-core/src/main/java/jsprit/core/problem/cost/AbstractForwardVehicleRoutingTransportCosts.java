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
package jsprit.core.problem.cost;

import jsprit.core.problem.Location;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.vehicle.Vehicle;

public abstract class AbstractForwardVehicleRoutingTransportCosts implements VehicleRoutingTransportCosts{

	@Override
	public abstract double getTransportTime(Location from, Location to,double departureTime, Driver driver, Vehicle vehicle);
	
	@Override
	public abstract double getTransportCost(Location from, Location to,double departureTime, Driver driver, Vehicle vehicle);

	@Override
	public double getBackwardTransportTime(Location from, Location to,double arrivalTime, Driver driver, Vehicle vehicle) {
		return getTransportTime(from, to, arrivalTime, driver, vehicle);
	}

	@Override
	public double getBackwardTransportCost(Location from, Location to,double arrivalTime, Driver driver, Vehicle vehicle) {
		return getTransportCost(from, to, arrivalTime, driver, vehicle);
	}

}
