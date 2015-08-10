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

import jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.problem.vehicle.VehicleImpl.NoVehicle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


final class VehicleTypeDependentJobInsertionCalculator implements JobInsertionCostsCalculator{

	private Logger logger = LogManager.getLogger(VehicleTypeDependentJobInsertionCalculator.class);
	
	private final VehicleFleetManager fleetManager;
	
	private final JobInsertionCostsCalculator insertionCalculator;
	
	private final VehicleRoutingProblem vrp;
	
	private Set<String> initialVehicleIds = new HashSet<String>();
	
	/**
	 * true if a vehicle(-type) is allowed to take over the whole route that was previously served by another vehicle
	 * 
	 * <p>vehicleSwitch allowed makes sense if fleet consists of vehicles with different capacities such that one
	 * can start with a small vehicle, but as the number of customers grows bigger vehicles can be operated, i.e. 
	 * bigger vehicles can take over the route that was previously served by a small vehicle.
	 * 
	 */
	private boolean vehicleSwitchAllowed = false;

	public VehicleTypeDependentJobInsertionCalculator(final VehicleRoutingProblem vrp, final VehicleFleetManager fleetManager, final JobInsertionCostsCalculator jobInsertionCalc) {
		this.fleetManager = fleetManager;
		this.insertionCalculator = jobInsertionCalc;
		this.vrp = vrp;
		getInitialVehicleIds();
		logger.debug("initialise {}", this);
	}

	private void getInitialVehicleIds() {
        Collection<VehicleRoute> initialVehicleRoutes = vrp.getInitialVehicleRoutes();
        for(VehicleRoute initialRoute : initialVehicleRoutes){
			initialVehicleIds.add(initialRoute.getVehicle().getId());
		}
	}

	@Override
	public String toString() {
		return "[name=vehicleTypeDependentServiceInsertion]";
	}
	
	/**
	 * @return the vehicleSwitchAllowed
	 */
	@SuppressWarnings("UnusedDeclaration")
    public boolean isVehicleSwitchAllowed() {
		return vehicleSwitchAllowed;
	}

	/**
	 * default is true
	 * 
	 * @param vehicleSwitchAllowed the vehicleSwitchAllowed to set
	 */
	public void setVehicleSwitchAllowed(boolean vehicleSwitchAllowed) {
		logger.debug("set vehicleSwitchAllowed to {}", vehicleSwitchAllowed);
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
			if(vehicleSwitchAllowed && !isVehicleWithInitialRoute(selectedVehicle)){
				relevantVehicles.addAll(fleetManager.getAvailableVehicles(selectedVehicle));
			}
		}
		else{ //if no vehicle has been assigned, i.e. it is an empty route
			relevantVehicles.addAll(fleetManager.getAvailableVehicles());		
		}
		for(Vehicle v : relevantVehicles){
			double depTime;
			if(v == selectedVehicle) depTime = currentRoute.getDepartureTime();
			else depTime = v.getEarliestDeparture();
			InsertionData iData = insertionCalculator.getInsertionData(currentRoute, jobToInsert, v, depTime, selectedDriver, bestKnownCost_);
			if(iData instanceof NoInsertionFound) { 
                continue;
			}
			if(iData.getInsertionCost() < bestKnownCost_){
				bestIData = iData;
				bestKnownCost_ = iData.getInsertionCost();
			}
		}
		return bestIData;
	}

	private boolean isVehicleWithInitialRoute(Vehicle selectedVehicle) {
		return initialVehicleIds.contains(selectedVehicle.getId());
	}

}
