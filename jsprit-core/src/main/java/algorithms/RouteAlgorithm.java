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

import java.util.Collection;

import basics.Job;
import basics.route.Vehicle;
import basics.route.VehicleRoute;


interface RouteAlgorithm {
	
	interface RouteAlgorithmListener {
		
	}
	
	interface JobRemovedListener extends RouteAlgorithmListener{
		public void removed(VehicleRoute route, Job job);
	}
	
	interface JobInsertedListener extends RouteAlgorithmListener{
		public void inserted(VehicleRoute route, Job job);
	}
	
	interface VehicleSwitchedListener extends RouteAlgorithmListener{
		public void vehicleSwitched(Vehicle oldVehicle, Vehicle newVehicle);
	}
	
	/**
	 * Calculates the best insertion position and the corresponding marginal costs of inserting the job (according to the insertionCostCalculator). 
	 * This does not affect any input parameter, thus the vehicleRoute and its data will not be changed/affected.
	 * 
	 * @param VehicleRoute, Job, double
	 * @return InsertionData
	 */
	public InsertionData calculateBestInsertion(VehicleRoute vehicleRoute, Job job, double bestKnownPrice);
		
	/**
	 * Removes job from vehicleRoute and does not update the resulting tour. Thus the tour state might not be valid anymore. 
	 * Note that this changes vehicleRoute!
	 * 
	 * @return true if job removed successfully, otherwise false
	 */
	public boolean removeJobWithoutTourUpdate(Job job, VehicleRoute vehicleRoute);
	
	/**
	 * Removes job from input parameter vehicleRoute AND updates the state of the resulting tour with tourStateCalc.
	 * Note that this changes para vehicleRoute!
	 */
	public boolean removeJob(Job job, VehicleRoute vehicleRoute);
	
	/**
	 * Inserts job into vehicleRoute.getTour().
	 * Please note, that this changes the parameter vehicleRoute!
	 */
	public void insertJobWithoutTourUpdate(VehicleRoute vehicleRoute, Job job, InsertionData insertionData);
	
	/**
	 * Inserts job into vehicleRoute.getTour().
	 * Please note, that this changes the parameter vehicleRoute! 
	 */
	public void insertJob(Job job, InsertionData insertionData, VehicleRoute vehicleRoute);
	
	/**
	 * Updates vehicleRoute, i.e. uses the tourStateCalculator to update for example timeWindows and loads on vehicleRoute.getTour()
	 * Note that this changes the parameter vehicleRoute!
	 */
	public void updateTour(VehicleRoute vehicleRoute);

	public Collection<RouteAlgorithmListener> getListeners();


}
