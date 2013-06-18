/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;


import basics.algo.InsertionEndsListener;
import basics.algo.InsertionStartsListener;
import basics.route.VehicleRoute;

class RemoveEmptyVehicles implements InsertionStartsListener, InsertionEndsListener{

	private static Logger log = Logger.getLogger(RemoveEmptyVehicles.class); 
	
	private VehicleFleetManager fleetManager;
	
	RemoveEmptyVehicles(VehicleFleetManager fleetManager) {
		super();
		this.fleetManager = fleetManager;
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, int nOfJobs2Recreate) {
//		List<VehicleRoute> routes = new ArrayList<VehicleRoute>(vehicleRoutes);
//		for(VehicleRoute route : routes){
//			if(route.isEmpty()) { vehicleRoutes.remove(route); }
//		}
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
