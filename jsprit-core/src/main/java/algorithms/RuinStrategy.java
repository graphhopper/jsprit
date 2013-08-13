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

import java.util.Collection;
import java.util.List;

import basics.Job;
import basics.route.VehicleRoute;




/**
 * 
 * @author stefan schroeder
 * 
 */

interface RuinStrategy {
	
	public static interface RuinListener {
		
		public void ruinStarts(Collection<VehicleRoute> routes);
		
		public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs);
		
		public void removed(Job job, VehicleRoute fromRoute);
		
	}

	/**
	 * Ruins a current solution, i.e. removes jobs from service providers and
	 * returns a collection of these removed, and thus unassigned, jobs.
	 * 
	 * @param vehicleRoutes
	 * @return
	 */
	public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes);

	public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved);
	
	public void addListener(RuinListener ruinListener);

}
