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
package jsprit.core.util;



import jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.vehicle.Vehicle;

/**
 * 
 * @author stefan schroeder
 * 
 */

public class ManhattanCosts extends AbstractForwardVehicleRoutingTransportCosts {

	public double speed = 1;

	private Locations locations;

	public ManhattanCosts(Locations locations) {
		super();
		this.locations = locations;
	}

	@Override
	public double getTransportCost(String fromId, String toId, double time,Driver driver, Vehicle vehicle) {
        double distance;
        try {
            distance = calculateDistance(fromId, toId);
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

	@Override
	public double getTransportTime(String fromId, String toId, double time,Driver driver, Vehicle vehicle) {
		return calculateDistance(fromId, toId) / speed;
	}

	private double calculateDistance(String fromId, String toId) {
		return Math.abs(locations.getCoord(fromId).getX() - locations.getCoord(toId).getX())
				+ Math.abs(locations.getCoord(fromId).getY() - locations.getCoord(toId).getY());
	}

}
