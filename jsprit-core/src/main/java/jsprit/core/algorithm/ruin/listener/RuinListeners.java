
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

package jsprit.core.algorithm.ruin.listener;

import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class RuinListeners {
	
	private Collection<RuinListener> ruinListeners = new ArrayList<RuinListener>();

	public void ruinStarts(Collection<VehicleRoute> routes){
		for(RuinListener l : ruinListeners) l.ruinStarts(routes);
	}
	
	public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs){
		for(RuinListener l : ruinListeners) l.ruinEnds(routes, unassignedJobs);
	}
	
	public void removed(Job job, VehicleRoute fromRoute){
		for(RuinListener l : ruinListeners) l.removed(job, fromRoute);
	}
	
	public void addListener(RuinListener ruinListener){
		ruinListeners.add(ruinListener);
	}
	
	public void removeListener(RuinListener ruinListener){
		ruinListeners.remove(ruinListener);
	}
	
	public Collection<RuinListener> getListeners(){
		return Collections.unmodifiableCollection(ruinListeners);
	}
}
