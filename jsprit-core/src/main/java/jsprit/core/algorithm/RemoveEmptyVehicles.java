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

import jsprit.core.algorithm.recreate.listener.InsertionEndsListener;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.VehicleFleetManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class RemoveEmptyVehicles implements InsertionEndsListener{

	private VehicleFleetManager fleetManager;
	
	public RemoveEmptyVehicles(VehicleFleetManager fleetManager) {
		super();
		this.fleetManager = fleetManager;
	}

	@Override
	public String toString() {
		return "[name=removeEmptyVehicles]";
	}

	@Override
	public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes) {
		List<VehicleRoute> routes = new ArrayList<VehicleRoute>(vehicleRoutes);
		for(VehicleRoute route : routes){
			if(route.isEmpty()) { 
				fleetManager.unlock(route.getVehicle());
				vehicleRoutes.remove(route); 
			}
		}
	}
}
