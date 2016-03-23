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
package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public abstract class AbstractForwardVehicleRoutingTransportCosts implements VehicleRoutingTransportCosts {

    @Override
    public abstract double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle);

    @Override
    public abstract double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle);

    @Override
    public double getBackwardTransportTime(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
    	double timeShift = 0;
    	if(from != to && to != vehicle.getStartLocation() && from != vehicle.getEndLocation()){
    		timeShift = -to.getSetupTime();
    	}
        return timeShift + getTransportTime(from, to, arrivalTime, driver, vehicle);
    }

    @Override
    public double getBackwardTransportCost(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
        return getTransportCost(from, to, arrivalTime, driver, vehicle);
    }

}
