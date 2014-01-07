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

import java.util.ArrayList;
import java.util.Collection;

import jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.problem.vehicle.VehicleImpl.NoVehicle;

import org.apache.log4j.Logger;


final class VehicleTypeDependentJobInsertionCalculator implements JobInsertionCostsCalculator{

	private Logger logger = Logger.getLogger(VehicleTypeDependentJobInsertionCalculator.class);
	
	private final VehicleFleetManager fleetManager;
	
	private final JobInsertionCostsCalculator insertionCalculator;
	
	/**
	 * true if a vehicle(-type) is allowed to take over the whole route that was previously served by another vehicle
	 * 
	 * <p>vehicleSwitch allowed makes sense if fleet consists of vehicles with different capacities such that one
	 * can start with a small vehicle, but as the number of customers grows bigger vehicles can be operated, i.e. 
	 * bigger vehicles can take over the route that was previously served by a small vehicle.
	 * 
	 */
	private boolean vehicleSwitchAllowed = false;

	public VehicleTypeDependentJobInsertionCalculator(final VehicleFleetManager fleetManager, final JobInsertionCostsCalculator jobInsertionCalc) {
		this.fleetManager = fleetManager;
		this.insertionCalculator = jobInsertionCalc;
		logger.info("inialise " + this);
	}

	@Override
	public String toString() {
		return "[name=vehicleTypeDependentServiceInsertion]";
	}
	
	/**
	 * @return the vehicleSwitchAllowed
	 */
	public boolean isVehicleSwitchAllowed() {
		return vehicleSwitchAllowed;
	}

	/**
	 * default is true
	 * 
	 * @param vehicleSwitchAllowed the vehicleSwitchAllowed to set
	 */
	public void setVehicleSwitchAllowed(boolean vehicleSwitchAllowed) {
		this.vehicleSwitchAllowed = vehicleSwitchAllowed;
	}

	public InsertionData getInsertionData(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle vehicle, double newVehicleDepartureTime, final Driver driver, final double bestKnownCost) {
		Vehicle selectedVehicle = currentRoute.getVehicle();
		Driver selectedDriver = currentRoute.getDriver();
		InsertionData bestIData = InsertionData.createEmptyInsertionData();
		double bestKnownCost_ = bestKnownCost;
		Collection<Vehicle> relevantVehicles = new ArrayList<Vehicle>();
		if(!(selectedVehicle instanceof NoVehicle)) {
			relevantVehicles.add(selectedVehicle);
			if(vehicleSwitchAllowed){
				relevantVehicles.addAll(fleetManager.getAvailableVehicles(selectedVehicle.getType().getTypeId(),selectedVehicle.getLocationId()));
			}
		}
		else{
			relevantVehicles.addAll(fleetManager.getAvailableVehicles());		
		}
		
		for(Vehicle v : relevantVehicles){
			double depTime = v.getEarliestDeparture();
			InsertionData iData = insertionCalculator.getInsertionData(currentRoute, jobToInsert, v, depTime, selectedDriver, bestKnownCost_);
			if(iData instanceof NoInsertionFound) { 
				if(bestIData instanceof NoInsertionFound) bestIData = iData;
				continue;
			}
			if(iData.getInsertionCost() < bestKnownCost_){
				bestIData = iData;
				bestKnownCost_ = iData.getInsertionCost();
			}
		}
		return bestIData;
	}

}
