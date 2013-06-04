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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import basics.Job;
import basics.algo.InsertionEndsListener;
import basics.algo.InsertionListener;
import basics.algo.InsertionStartsListener;
import basics.algo.JobInsertedListener;
import basics.route.VehicleRoute;




abstract class AbstractInsertionStrategy implements InsertionStrategy{
	
	private static Logger log = Logger.getLogger(AbstractInsertionStrategy.class);
	
	private Collection<InsertionListener> listener = new ArrayList<InsertionListener>();
	
	public abstract RouteAlgorithm getRouteAlgorithm();
	
	public void informJobInserted(int nOfJobs2Recreate, Job insertedJob, VehicleRoute insertedIn){
		for(InsertionListener l : listener){
			if(l instanceof JobInsertedListener){
				((JobInsertedListener)l).informJobInserted(nOfJobs2Recreate, insertedJob, insertedIn);
			}
		}
	}
	
	public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route){
		for(InsertionListener l : listener){
			if(l instanceof BeforeJobInsertionListener){
				((BeforeJobInsertionListener)l).informBeforeJobInsertion(job, data, route);
			}
		}
	}
	
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, int nOfJobs2Recreate){
		for(InsertionListener l : listener){
			if(l instanceof InsertionStartsListener){
				((InsertionStartsListener)l).informInsertionStarts(vehicleRoutes,nOfJobs2Recreate);
			}
		}
	}
	
	public void informInsertionEndsListeners(Collection<VehicleRoute> vehicleRoutes) {
		for(InsertionListener l : listener){
			if(l instanceof InsertionEndsListener){
				((InsertionEndsListener)l).informInsertionEnds(vehicleRoutes);
			}
		}
	}

	public Collection<InsertionListener> getListener() {
		return Collections.unmodifiableCollection(listener);
	}
	
	public void addListener(InsertionListener l){
		log.info("add insertion-listener " + l);
		listener.add(l);
	}

	public void addAllListener(List<InsertionListener> list) {
		for(InsertionListener l : list) addListener(l);
	}
	
	

}
