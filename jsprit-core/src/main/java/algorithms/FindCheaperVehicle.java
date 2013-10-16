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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import basics.Job;
import basics.algo.InsertionStartsListener;
import basics.route.VehicleRoute;

class FindCheaperVehicle implements InsertionStartsListener{

	FindCheaperVehicleAlgo findCheaperVehicle;
	
	public FindCheaperVehicle(FindCheaperVehicleAlgo findCheaperVehicle) {
		super();
		this.findCheaperVehicle = findCheaperVehicle;
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
		List<VehicleRoute> newRoutes = new ArrayList<VehicleRoute>();
		for(VehicleRoute route : vehicleRoutes){
			if(route.isEmpty()) continue;
			VehicleRoute cheaperRoute = findCheaperVehicle.runAndGetVehicleRoute(route);
			newRoutes.add(cheaperRoute);
		}
		vehicleRoutes.clear();
		vehicleRoutes.addAll(newRoutes);
	}
	
	@Override
	public String toString() {
		return "[name=findCheaperVehicle]";
	}

}
