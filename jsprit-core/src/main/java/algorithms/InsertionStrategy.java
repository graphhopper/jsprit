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

import java.util.Collection;

import basics.Job;
import basics.algo.InsertionListener;
import basics.route.VehicleRoute;




/**
 * 
 * @author stefan schroeder
 * 
 */

interface InsertionStrategy {

	class Insertion {
		
		private final VehicleRoute route;
		
		private final InsertionData insertionData;

		public Insertion(VehicleRoute vehicleRoute, InsertionData insertionData) {
			super();
			this.route = vehicleRoute;
			this.insertionData = insertionData;
		}

		public VehicleRoute getRoute() {
			return route;
		}
		
		public InsertionData getInsertionData() {
			return insertionData;
		}
		
	}

	
	


	/**
	 * Assigns the unassigned jobs to service-providers
	 * 
	 * @param vehicleRoutes
	 * @param unassignedJobs
	 */
	public void insertJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs);
	
	public void addListener(InsertionListener insertionListener);
	
	public void removeListener(InsertionListener insertionListener);
	
	public Collection<InsertionListener> getListeners();

}
