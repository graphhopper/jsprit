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
/**
 * 
 */
package util;

import org.apache.log4j.Logger;

import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.Vehicle;
import basics.route.VehicleImpl.VehicleType;


/**
 * @author stefan schroeder
 * 
 */
public class CrowFlyCosts implements VehicleRoutingTransportCosts {

	private static Logger logger = Logger.getLogger(CrowFlyCosts.class);

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
	public double getTransportCost(String fromId, String toId, double time, Driver driver, Vehicle vehicle) {
		double cost;
		try {
			cost = EuclideanDistanceCalculator.calculateDistance(locations.getCoord(fromId), locations.getCoord(toId)) * detourFactor;
		} catch (NullPointerException e) {
			throw new NullPointerException("cannot calculate euclidean distance. coordinates are missing. either add coordinates or use another transport-cost-calculator.");
		}
		return cost;
	}

	@Override
	public double getTransportTime(String fromId, String toId, double time, Driver driver, Vehicle vehicle) {
		double transportTime = getTransportCost(fromId, toId, 0.0, null, null) / speed;
		return transportTime;
	}

	@Override
	public double getBackwardTransportCost(String fromId, String toId, double arrivalTime, Driver driver, Vehicle vehicle) {
		return getTransportCost(fromId, toId, arrivalTime, null, null);
	}

	@Override
	public double getBackwardTransportTime(String fromId, String toId, double arrivalTime, Driver driver, Vehicle vehicle) {
		return getTransportTime(fromId, toId, arrivalTime, null, null);
	}

}
