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
package jsprit.core.algorithm.recreate.listener;

import jsprit.core.algorithm.recreate.InsertionData;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Collection;


public class InsertionListeners {
	
	private Collection<InsertionListener> listeners = new ArrayList<InsertionListener>();
	
	public Collection<InsertionListener> getListeners(){
		return listeners;
	}
	
	public void informJobInserted(Job insertedJob, VehicleRoute inRoute, double additionalCosts, double additionalTime){
		for(InsertionListener l : listeners){
			if(l instanceof JobInsertedListener){
				((JobInsertedListener)l).informJobInserted(insertedJob, inRoute, additionalCosts, additionalTime);
			}
		}
	}
	
	public void informVehicleSwitched(VehicleRoute route, Vehicle oldVehicle, Vehicle newVehicle){
		for(InsertionListener l : listeners){
			if(l instanceof VehicleSwitchedListener){
				((VehicleSwitchedListener) l).vehicleSwitched(route, oldVehicle, newVehicle);
			}
		}
	}
	
	public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route){
		for(InsertionListener l : listeners){
			if(l instanceof BeforeJobInsertionListener){
				((BeforeJobInsertionListener)l).informBeforeJobInsertion(job, data, route);
			}
		}
	}
	
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs){
		for(InsertionListener l : listeners){
			if(l instanceof InsertionStartsListener){
				((InsertionStartsListener)l).informInsertionStarts(vehicleRoutes, unassignedJobs);
			}
		}
	}
	
	public void informInsertionEndsListeners(Collection<VehicleRoute> vehicleRoutes) {
		for(InsertionListener l : listeners){
			if(l instanceof InsertionEndsListener){
				((InsertionEndsListener)l).informInsertionEnds(vehicleRoutes);
			}
		}
	}
	
	public void addListener(InsertionListener insertionListener){
        listeners.add(insertionListener);
	}
	
	public void removeListener(InsertionListener insertionListener){
		listeners.remove(insertionListener);
	}

	public void addAllListeners(Collection<InsertionListener> listeners) {
		for(InsertionListener l : listeners) addListener(l);
	}

}
