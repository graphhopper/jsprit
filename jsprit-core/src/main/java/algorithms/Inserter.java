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

import algorithms.InsertionData.NoInsertionFound;
import basics.Job;
import basics.Service;
import basics.route.ServiceActivity;
import basics.route.TourActivityFactory;
import basics.route.DefaultTourActivityFactory;
import basics.route.VehicleRoute;

class Inserter {

	private InsertionListeners insertionListeners;
	
	private TourActivityFactory activityFactory;
	
	public Inserter(InsertionListeners insertionListeners) {
		this.insertionListeners = insertionListeners;
		activityFactory = new DefaultTourActivityFactory();
	}

	public void insertJob(Job job, InsertionData insertionData, VehicleRoute vehicleRoute){
		insertionListeners.informBeforeJobInsertion(job, insertionData, vehicleRoute);
		
		if(insertionData == null || (insertionData instanceof NoInsertionFound)) throw new IllegalStateException("insertionData null. cannot insert job.");
		if(job == null) throw new IllegalStateException("cannot insert null-job");
		if(!(vehicleRoute.getVehicle().getId().toString().equals(insertionData.getSelectedVehicle().getId().toString()))){
			insertionListeners.informVehicleSwitched(vehicleRoute, vehicleRoute.getVehicle(), insertionData.getSelectedVehicle());
			vehicleRoute.setVehicle(insertionData.getSelectedVehicle(), insertionData.getVehicleDepartureTime());
		}
//		if(vehicleRoute.getDepartureTime() != vehicleRoute.g)
		if(job instanceof Service) {
			vehicleRoute.getTourActivities().addActivity(insertionData.getDeliveryInsertionIndex(), activityFactory.createActivity((Service)job));
			vehicleRoute.setDepartureTime(insertionData.getVehicleDepartureTime());
		}
		else throw new IllegalStateException("neither service nor shipment. this is not supported.");
		
		insertionListeners.informJobInserted(job, vehicleRoute, insertionData.getInsertionCost(), insertionData.getAdditionalTime());
//		updateTour(vehicleRoute);
	}
}
