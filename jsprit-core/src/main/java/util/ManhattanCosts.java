/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package util;



import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.Vehicle;

/**
 * 
 * @author stefan schroeder
 * 
 */

public class ManhattanCosts implements VehicleRoutingTransportCosts {

	public double speed = 1;

	private Locations locations;

	public ManhattanCosts(Locations locations) {
		super();
		this.locations = locations;
	}

	@Override
	public double getTransportCost(String fromId, String toId, double time,
			Driver driver, Vehicle vehicle) {
		return calculateDistance(fromId, toId);
	}

	@Override
	public double getTransportTime(String fromId, String toId, double time,
			Driver driver, Vehicle vehicle) {
		double transportTime = calculateDistance(fromId, toId) / speed;
		return transportTime;
	}

	private double calculateDistance(String fromId, String toId) {
		double distance = Math.abs(locations.getCoord(fromId).getX()
				- locations.getCoord(toId).getX())
				+ Math.abs(locations.getCoord(fromId).getY()
						- locations.getCoord(toId).getY());
		return distance;
	}

	@Override
	public double getBackwardTransportCost(String fromId, String toId,
			double arrivalTime, Driver driver, Vehicle vehicle) {
		return getTransportCost(fromId, toId, arrivalTime, null, null);
	}

	@Override
	public double getBackwardTransportTime(String fromId, String toId,
			double arrivalTime, Driver driver, Vehicle vehicle) {
		return getTransportTime(fromId, toId, arrivalTime, null, null);
	}

}
