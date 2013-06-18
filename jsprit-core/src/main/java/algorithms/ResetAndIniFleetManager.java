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

import java.util.Collection;

import org.apache.log4j.Logger;

import basics.algo.InsertionStartsListener;
import basics.route.VehicleRoute;

class ResetAndIniFleetManager implements InsertionStartsListener{

	private static Logger log = Logger.getLogger(ResetAndIniFleetManager.class);
	
	private VehicleFleetManager vehicleFleetManager;
	
	ResetAndIniFleetManager(VehicleFleetManager vehicleFleetManager) {
		super();
		this.vehicleFleetManager = vehicleFleetManager;
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, int nOfJobs2Recreate) {
		vehicleFleetManager.unlockAll();
		for(VehicleRoute route : vehicleRoutes){
//			if(!route.isEmpty()){
				vehicleFleetManager.lock(route.getVehicle());
//			}
		}
	}

	@Override
	public String toString() {
		return "[name=resetAndIniFleetManager]";
	}
}
