/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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
package jsprit.core.algorithm.ruin;

import jsprit.core.algorithm.ruin.listener.RuinListener;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;





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
    @Deprecated
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
