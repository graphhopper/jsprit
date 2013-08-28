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
import java.util.List;

import org.apache.log4j.Logger;

import basics.route.TourActivities;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleImpl.NoVehicle;
import basics.route.VehicleRoute;



final class FindCheaperVehicleAlgo {

	private static Logger log = Logger.getLogger(FindCheaperVehicleAlgo.class);
	
	private VehicleFleetManager fleetManager;
	
	private VehicleRouteUpdater tourStateCalculator;
	
	private AuxilliaryCostCalculator auxilliaryCostCalculator;
	
	private double weightFixCosts = 1.0;
	
	private StateManager states;
	
	public void setWeightFixCosts(double weightFixCosts) {
		this.weightFixCosts = weightFixCosts;
	}
	
	public void setStates(StateManager states) {
		this.states = states;
	}

	public FindCheaperVehicleAlgo(VehicleFleetManager fleetManager, VehicleRouteUpdater tourStateCalculator, AuxilliaryCostCalculator auxilliaryCostCalculator) {
		super();
		this.fleetManager = fleetManager;
		this.tourStateCalculator = tourStateCalculator;
		this.auxilliaryCostCalculator = auxilliaryCostCalculator;
	}

	
	public VehicleRoute runAndGetVehicleRoute(VehicleRoute vehicleRoute) {
		if(vehicleRoute.getVehicle() instanceof NoVehicle){
			return vehicleRoute;
		}
		if(vehicleRoute.getTourActivities() == null || vehicleRoute.getVehicle() == null){
			return vehicleRoute;
		}
//		Collection<TypeKey> availableVehicleTypes = fleetManager.getAvailableVehicleTypes(new TypeKey(vehicleRoute.getVehicle().getType(),vehicleRoute.getVehicle().getLocationId()));
		double bestSaving = 0.0;
		Vehicle bestVehicle = null;
		List<TourActivity> path = new ArrayList<TourActivity>();
		path.add(vehicleRoute.getStart());
		path.addAll(vehicleRoute.getTourActivities().getActivities());
		path.add(vehicleRoute.getEnd());
		
		for(Vehicle vehicle : fleetManager.getAvailableVehicles(vehicleRoute.getVehicle().getType().getTypeId(), vehicleRoute.getVehicle().getLocationId())){
//			Vehicle vehicle = fleetManager.getEmptyVehicle(vehicleType);
			if(vehicle.getType().getTypeId().equals(vehicleRoute.getVehicle().getType().getTypeId())){
				continue;
			}
			if(states.getRouteState(vehicleRoute,StateTypes.LOAD).toDouble() <= vehicle.getCapacity()){
				double fixCostSaving = vehicleRoute.getVehicle().getType().getVehicleCostParams().fix - vehicle.getType().getVehicleCostParams().fix;
				double departureTime = vehicleRoute.getStart().getEndTime();
				double newCost = auxilliaryCostCalculator.costOfPath(path, departureTime, vehicleRoute.getDriver(), vehicle);
				double varCostSaving = states.getRouteState(vehicleRoute, StateTypes.COSTS).toDouble() - newCost;
				double totalCostSaving = varCostSaving + weightFixCosts*fixCostSaving;
				if(totalCostSaving > bestSaving){
					bestSaving = totalCostSaving;
					bestVehicle = vehicle;
				}
			}
		}
		if(bestVehicle != null){
			try{
				fleetManager.unlock(vehicleRoute.getVehicle());
				fleetManager.lock(bestVehicle);
			}
			catch(IllegalStateException e){
				throw new IllegalStateException(e);
			}
			TourActivities newTour = TourActivities.copyOf(vehicleRoute.getTourActivities());
			tourStateCalculator.iterate(vehicleRoute);
			return VehicleRoute.newInstance(newTour,vehicleRoute.getDriver(),bestVehicle);
		}
		return vehicleRoute;
	}

}
