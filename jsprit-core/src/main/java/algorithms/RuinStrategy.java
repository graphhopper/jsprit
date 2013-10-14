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
import basics.route.VehicleRoute;




/**
 * 
 * @author stefan schroeder
 * 
 */

interface RuinStrategy {
	
	/**
	 * Listener that listens to the ruin-process. It informs whoever is interested about start, end and about a removal of a job.
	 * 
	 * @author schroeder
	 *
	 */
	public static interface RuinListener {
		
		/**
		 * informs about ruin-start.
		 * 
		 * @param routes
		 */
		public void ruinStarts(Collection<VehicleRoute> routes);
		
		/**
		 * informs about ruin-end.
		 * 
		 * @param routes
		 * @param unassignedJobs
		 */
		public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs);
		
		/**
		 * informs if a {@link Job} has been removed from a {@link VehicleRoute}.
		 * 
		 * @param job
		 * @param fromRoute
		 */
		public void removed(Job job, VehicleRoute fromRoute);
		
	}

	/**
	 * Ruins a current solution, i.e. a collection of vehicle-routes and 
	 * returns a collection of removed and thus unassigned jobs.
	 * 
	 * @param {@link VehicleRoute}
	 * @return Collection of {@link Job}
	 */
	public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes);

	/**
	 * Removes targetJob as well as its neighbors with a size of (nOfJobs2BeRemoved-1). 
	 */
	public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved);
	
	/**
	 * Adds a ruin-listener.
	 * 
	 * @param {@link RuinListener}
	 */
	public void addListener(RuinListener ruinListener);
	
	public void removeListener(RuinListener ruinListener);
	
	public Collection<RuinListener> getListeners();

}
