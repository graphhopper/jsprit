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
package jsprit.core.algorithm;

import jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.VehicleFleetManager;

import java.util.ArrayList;
import java.util.Collection;


public class ResetAndIniFleetManager implements InsertionStartsListener{

	private VehicleFleetManager vehicleFleetManager;
	
	public ResetAndIniFleetManager(VehicleFleetManager vehicleFleetManager) {
		super();
		this.vehicleFleetManager = vehicleFleetManager;
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
		vehicleFleetManager.unlockAll();
		Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>(vehicleRoutes);
		for(VehicleRoute route : routes){
			vehicleFleetManager.lock(route.getVehicle());
		}
	}

	@Override
	public String toString() {
		return "[name=resetAndIniFleetManager]";
	}
}
