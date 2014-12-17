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
package jsprit.core.algorithm.ruin.distance;

import jsprit.core.problem.Location;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.util.EuclideanDistanceCalculator;



/**
 * Calculator that calculates average distance between two jobs based on the input-transport costs.
 * 
 * <p>If the distance between two jobs cannot be calculated with input-transport costs, it tries the euclidean distance between these jobs.
 * 
 * @author stefan schroeder
 *
 */
public class AvgServiceAndShipmentDistance implements JobDistance {

	private VehicleRoutingTransportCosts costs;

	public AvgServiceAndShipmentDistance(VehicleRoutingTransportCosts costs) {
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
		if (i.equals(j)) return 0.0;

		if (i instanceof Service && j instanceof Service) {
			return calcDist((Service)i, (Service)j);
		} 
		else if(i instanceof Service && j instanceof Shipment){
			return calcDist((Service)i,(Shipment)j);
		}
		else if(i instanceof Shipment && j instanceof Service){
			return calcDist((Service)j,(Shipment)i);
		}
		else if(i instanceof Shipment && j instanceof Shipment){
			return calcDist((Shipment)i,(Shipment)j);
		}
		else{
			throw new IllegalStateException("this supports only shipments or services");
		}
	}

	private double calcDist(Service i, Service j) {
		return calcDist(i.getLocation(),j.getLocation());
	}

	private double calcDist(Service i, Shipment j) {
		double c_ij1 = calcDist(i.getLocation(),j.getPickupLocation());
		double c_ij2 = calcDist(i.getLocation(),j.getDeliveryLocation());
		return (c_ij1 + c_ij2)/2.0;
	}
	
	private double calcDist(Shipment i, Shipment j) {
		double c_i1j1 = calcDist(i.getPickupLocation(),j.getPickupLocation());
		double c_i1j2 = calcDist(i.getPickupLocation(),j.getDeliveryLocation());
		double c_i2j1 = calcDist(i.getDeliveryLocation(),j.getPickupLocation());
		double c_i2j2 = calcDist(i.getDeliveryLocation(),j.getDeliveryLocation());
		return (c_i1j1 + c_i1j2 + c_i2j1 + c_i2j2)/4.0;
	}

	private double calcDist(Location location_i, Location location_j){
		try{
			return costs.getTransportCost(location_i, location_j, 0.0, null, null);
        }
		catch(IllegalStateException e){
			// now try the euclidean distance between these two services
		}
		return EuclideanDistanceCalculator.calculateDistance(location_i.getCoordinate(), location_j.getCoordinate());
	}
}
