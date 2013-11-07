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
package algorithms;

import basics.Job;
import basics.route.Driver;
import basics.route.Vehicle;
import basics.route.VehicleRoute;

public class InsertionContext {
	
	private VehicleRoute route;
	private Job job;
	private Vehicle newVehicle;
	private Driver newDriver;
	private double newDepTime;
	
	/**
	 * @return the route
	 */
	public VehicleRoute getRoute() {
		return route;
	}

	/**
	 * @return the job
	 */
	public Job getJob() {
		return job;
	}

	/**
	 * @return the newVehicle
	 */
	public Vehicle getNewVehicle() {
		return newVehicle;
	}

	/**
	 * @return the newDriver
	 */
	public Driver getNewDriver() {
		return newDriver;
	}

	/**
	 * @return the newDepTime
	 */
	public double getNewDepTime() {
		return newDepTime;
	}

	public InsertionContext(VehicleRoute route, Job job, Vehicle newVehicle,
			Driver newDriver, double newDepTime) {
		super();
		this.route = route;
		this.job = job;
		this.newVehicle = newVehicle;
		this.newDriver = newDriver;
		this.newDepTime = newDepTime;
	}
	
	

}
