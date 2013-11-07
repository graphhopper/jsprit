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
package basics.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import basics.Job;
import basics.route.VehicleRoute;

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
