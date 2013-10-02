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
package algorithms;

import org.apache.log4j.Logger;

import basics.Job;
import basics.Service;
import basics.costs.VehicleRoutingTransportCosts;


/**
 * Calculator that calculates average distance between two jobs based on the input-transport costs.
 * 
 * <p>If the distance between two jobs cannot be calculated with input-transport costs, it tries the euclidean distance between these jobs.
 * 
 * @author stefan schroeder
 *
 */
class JobDistanceAvgCosts implements JobDistance {

	private static Logger log = Logger.getLogger(JobDistanceAvgCosts.class);
	
	private VehicleRoutingTransportCosts costs;

	public JobDistanceAvgCosts(VehicleRoutingTransportCosts costs) {
		super();
		this.costs = costs;

	}

	/**
	 * Calculates and returns the average distance between two jobs based on the input-transport costs.
	 * 
	 * <p>If the distance between two jobs cannot be calculated with input-transport costs, it tries the euclidean distance between these jobs.
	 */ 
	@Override
	public double getDistance(Job i, Job j) {
		double avgCost = 0.0;
		if (i instanceof Service && j instanceof Service) {
			if (i.equals(j)) {
				avgCost = 0.0;
			} else {
				Service s_i = (Service) i;
				Service s_j = (Service) j;
				avgCost = calcDist(s_i, s_j);
			}
		} else {
			throw new UnsupportedOperationException(
					"currently, this class just works with shipments and services.");
		}
		return avgCost;
	}

	private double calcDist(Service s_i, Service s_j) {
		double distance;
		try{
			distance = costs.getTransportCost(s_i.getLocationId(), s_j.getLocationId(), 0.0, null, null);
			return distance;
		}
		catch(IllegalStateException e){
			// now try the euclidean distance between these two services
		}
		EuclideanServiceDistance euclidean = new EuclideanServiceDistance();
		distance = euclidean.getDistance(s_i, s_j);
		return distance;
	}

}
