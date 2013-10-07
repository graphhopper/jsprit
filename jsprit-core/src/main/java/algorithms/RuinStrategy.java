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

import basics.Job;
import basics.algo.RuinListener;
import basics.route.VehicleRoute;




/**
 * 
 * @author stefan schroeder
 * 
 */

public interface RuinStrategy {
	
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
