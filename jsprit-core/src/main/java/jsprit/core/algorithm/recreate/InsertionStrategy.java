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
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.recreate.listener.InsertionListener;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;



/**
 * Basic interface for insertion strategies
 *
 * @author stefan schroeder
 * 
 */

public interface InsertionStrategy {

	/**
	 * Inserts unassigned jobs into vehicle routes.
	 *  @param vehicleRoutes existing vehicle routes
	 * @param unassignedJobs jobs to be inserted
     */
	public Collection<Job> insertJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs);

    public void addListener(InsertionListener insertionListener);
	
	public void removeListener(InsertionListener insertionListener);
	
	public Collection<InsertionListener> getListeners();

}
