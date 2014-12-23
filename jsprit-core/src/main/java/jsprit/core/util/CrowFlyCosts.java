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
/**
 * 
 */
package jsprit.core.util;

import jsprit.core.problem.Location;
import jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import jsprit.core.problem.cost.TransportDistance;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.vehicle.Vehicle;



/**
 * @author stefan schroeder
 * 
 */
public class CrowFlyCosts extends AbstractForwardVehicleRoutingTransportCosts implements TransportDistance{

	public int speed = 1;

	public double detourFactor = 1.0;

	private Locations locations;

	public CrowFlyCosts(Locations locations) {
		super();
		this.locations = locations;
	}
	
	@Override
	public String toString() {
		return "[name=crowFlyCosts]";
	}

	@Override
	public double getTransportCost(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
		double distance;
		try {
			distance = calculateDistance(from, to);
		} catch (NullPointerException e) {
			throw new NullPointerException("cannot calculate euclidean distance. coordinates are missing. either add coordinates or use another transport-cost-calculator.");
		}
		double costs = distance;
		if(vehicle != null){
			if(vehicle.getType() != null){
				costs = distance * vehicle.getType().getVehicleCostParams().perDistanceUnit;
			}
		}
		return costs;
	}

	private double calculateDistance(Location fromLocation, Location toLocation) {
		Coordinate from = null;
		Coordinate to = null;
		if(fromLocation.getCoordinate() != null & toLocation.getCoordinate() != null){
			from = fromLocation.getCoordinate();
			to = toLocation.getCoordinate();
		}
		else if(locations != null){
			from = locations.getCoord(fromLocation.getId());
			to = locations.getCoord(toLocation.getId());
		}
		if(from == null || to == null) throw new NullPointerException();
		return calculateDistance(from,to);
	}

	private double calculateDistance(Coordinate from, Coordinate to) {
		return EuclideanDistanceCalculator.calculateDistance(from,to) * detourFactor;
	}

	@Override
	public double getTransportTime(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
		double distance;
		try {
			distance = calculateDistance(from, to);
		} catch (NullPointerException e) {
			throw new NullPointerException("cannot calculate euclidean distance. coordinates are missing. either add coordinates or use another transport-cost-calculator.");
		}
		return distance / speed;
	}

	@Override
	public double getDistance(Location from, Location to) {
		return calculateDistance(from,to);
	}
}
