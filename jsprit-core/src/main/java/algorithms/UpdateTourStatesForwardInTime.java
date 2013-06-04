/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms;

import org.apache.log4j.Logger;

import algorithms.RouteStates.ActivityState;
import basics.costs.ForwardTransportCost;
import basics.costs.ForwardTransportTime;
import basics.costs.VehicleRoutingActivityCosts;
import basics.route.Driver;
import basics.route.End;
import basics.route.ServiceActivity;
import basics.route.TourActivities;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;


/**
 * 
 * @author sschroeder
 *
 */

class UpdateTourStatesForwardInTime implements VehicleRouteUpdater{
	
//	public static Counter counter = new Counter("#updateTWProcesses: ");
	private static Logger log = Logger.getLogger(UpdateTourStatesForwardInTime.class);
	
	public boolean checkFeasibility = true;
	
	private VehicleRoutingActivityCosts activityCost;

	private ForwardTransportTime transportTime;

	private ForwardTransportCost transportCost;
	
	private RouteStates routeStates;
	
	private boolean activityStatesSet = false;
	
	public void setActivityStates(RouteStates actStates){
		this.routeStates = actStates;
		activityStatesSet = true;
	}

	public ActivityState state(TourActivity act){
		return routeStates.getState(act);
	}

	public UpdateTourStatesForwardInTime(ForwardTransportTime transportTime, ForwardTransportCost transportCost, VehicleRoutingActivityCosts activityCost) {
		super();
		this.transportTime = transportTime;
		this.transportCost = transportCost;
		this.activityCost = activityCost;
	}

	/**
	 * 
	 * 
	 */
	public boolean updateRoute(VehicleRoute vehicleRoute) {
		vehicleRoute.getVehicleRouteCostCalculator().reset();
		
		Vehicle vehicle = vehicleRoute.getVehicle();
		Driver driver = vehicleRoute.getDriver();
		
		TourActivity prevAct = vehicleRoute.getStart(); 
		
		double startAtPrevAct = vehicleRoute.getStart().getEndTime();
		
		double totalOperationCost = 0.0;
		int totalLoadPicked = 0;
		int currentLoadState = 0;
		
		for(TourActivity currentAct : vehicleRoute.getTourActivities().getActivities()){ 
			totalLoadPicked += getPickedLoad(currentAct); 
			currentLoadState += getCapDemand(currentAct);
			
			double transportTime = this.transportTime.getTransportTime(prevAct.getLocationId(), currentAct.getLocationId(), startAtPrevAct, driver, vehicle);
			
			double arrivalTimeAtCurrAct = startAtPrevAct + transportTime; 
			double operationStartTime = Math.max(currentAct.getTheoreticalEarliestOperationStartTime(), arrivalTimeAtCurrAct);
		
			double operationEndTime = operationStartTime + currentAct.getOperationTime();
			
			currentAct.setArrTime(arrivalTimeAtCurrAct);
			currentAct.setEndTime(operationEndTime);
			
			double transportCost = this.transportCost.getTransportCost(prevAct.getLocationId(), currentAct.getLocationId(), startAtPrevAct, driver, vehicle);
			double actCost = activityCost.getActivityCost(currentAct, arrivalTimeAtCurrAct, driver, vehicle);
			
			vehicleRoute.getVehicleRouteCostCalculator().addTransportCost(transportCost);
			vehicleRoute.getVehicleRouteCostCalculator().addActivityCost(actCost);
			
			totalOperationCost += transportCost;
			totalOperationCost += actCost;
			
			if(activityStatesSet){
				ActivityState currentState = state(currentAct);
				currentState.setEarliestOperationStart(operationStartTime);
				currentState.setCurrentLoad(currentLoadState);
				currentState.setCurrentCost(totalOperationCost);
			}
			
			prevAct = currentAct;
			startAtPrevAct = operationEndTime;
		}
		
		End currentAct = vehicleRoute.getEnd();
		double transportCost = this.transportCost.getTransportCost(prevAct.getLocationId(), currentAct.getLocationId(), startAtPrevAct, driver, vehicle);
		double transportTime = this.transportTime.getTransportTime(prevAct.getLocationId(), currentAct.getLocationId(), startAtPrevAct, driver, vehicle);
		double arrivalTimeAtCurrAct = startAtPrevAct + transportTime; 
		
		currentAct.setArrTime(arrivalTimeAtCurrAct);
		currentAct.setEndTime(arrivalTimeAtCurrAct);
		
		totalOperationCost += transportCost;
			
		routeStates.getRouteState(vehicleRoute).setCosts(totalOperationCost);
		routeStates.getRouteState(vehicleRoute).setLoad(totalLoadPicked);
		
		vehicleRoute.getVehicleRouteCostCalculator().addTransportCost(transportCost);
		
		vehicleRoute.getVehicleRouteCostCalculator().price(vehicleRoute.getDriver());
		vehicleRoute.getVehicleRouteCostCalculator().price(vehicleRoute.getVehicle());
		vehicleRoute.getVehicleRouteCostCalculator().finish();
		return true;
	}

	private int getCapDemand(TourActivity currentAct) {
		return currentAct.getCapacityDemand();
	}

	private double getPickedLoad(TourActivity currentAct) {
		if(currentAct instanceof ServiceActivity){
			return currentAct.getCapacityDemand();
		}
//		else if(currentAct instanceof Pickup){
//			return currentAct.getCapacityDemand();
//		}
		return 0.0;
	}
	
}
