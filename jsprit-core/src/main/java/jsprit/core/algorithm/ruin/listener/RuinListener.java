/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.algorithm.ruin.listener;

import jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;


/**
 * Listener that listens to the ruin-process. It informs whoever is interested about start, end and about a removal of a job.
 * 
 * @author schroeder
 *
 */
public interface RuinListener extends SearchStrategyModuleListener{
	
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
