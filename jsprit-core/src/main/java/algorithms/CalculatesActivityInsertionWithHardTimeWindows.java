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
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.End;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;



final class CalculatesActivityInsertionWithHardTimeWindows {

	private static Logger logger = Logger.getLogger(CalculatesActivityInsertionWithHardTimeWindows.class);
	
	private RouteStates routeStates;
		
	private VehicleRoutingTransportCosts routingCosts;
	
	private VehicleRoutingActivityCosts activityCosts;
	
	public CalculatesActivityInsertionWithHardTimeWindows(RouteStates activityStates, VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts){
		this.routeStates = activityStates;
		this.routingCosts = routingCosts;
		this.activityCosts = activityCosts;
		logger.info("initialise " + this);
	}
	
	public double calculate(VehicleRoute vehicleRoute, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, Driver driver, Vehicle vehicle) {	
		boolean prevIsStart = false;
		
		if(prevAct instanceof Start){
			prevIsStart = true;
		}
		
		double tp_costs_prevAct_newAct = routingCosts.getTransportCost(prevAct.getLocationId(), newAct.getLocationId(), prevAct.getEndTime(), driver, vehicle);
		double tp_time_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevAct.getEndTime(), driver, vehicle);
		
		double newAct_arrTime = prevAct.getEndTime() + tp_time_prevAct_newAct;
		double newAct_operationStartTime = Math.max(newAct_arrTime, newAct.getTheoreticalEarliestOperationStartTime());
		
		double newAct_endTime = newAct_operationStartTime + newAct.getOperationTime();
		
		double act_costs_newAct = activityCosts.getActivityCost(newAct, newAct_arrTime, driver, vehicle);
		
		double tp_costs_newAct_nextAct = routingCosts.getTransportCost(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, driver, vehicle);
		double tp_time_newAct_nextAct = routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, driver, vehicle);
		
		double nextAct_arrTime = newAct_endTime + tp_time_newAct_nextAct;
		double act_costs_nextAct = activityCosts.getActivityCost(nextAct, nextAct_arrTime, driver, vehicle);
		
		double activityInsertionCosts;
		
		double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct + act_costs_newAct + act_costs_nextAct; 
		
		if(nextAct_arrTime > getLatestOperationStart(nextAct)){
			activityInsertionCosts = Double.MAX_VALUE;
		}
		else if(vehicleRoute.isEmpty()){ 
			activityInsertionCosts = totalCosts; 
		}
		else{
			double oldCostOfPrevAct;
			if(prevIsStart) oldCostOfPrevAct = 0.0;
			else oldCostOfPrevAct = state(prevAct).getCurrentCost();
			double oldCostOfNextAct;
			if(nextAct instanceof End) oldCostOfNextAct = routeStates.getRouteState(vehicleRoute).getCosts();
			else oldCostOfNextAct = state(nextAct).getCurrentCost();
			activityInsertionCosts = (totalCosts) - (oldCostOfNextAct-oldCostOfPrevAct);
		} 
		return activityInsertionCosts;
	}
	
	private ActivityState state(TourActivity act) {
		return routeStates.getState(act);
	}

	private double getLatestOperationStart(TourActivity act) {
		if(state(act) != null){
			return state(act).getLatestOperationStart();
		}
		return act.getTheoreticalLatestOperationStartTime();
	}
	
	@Override
	public String toString() {
		return "[name=calculatesHardTimeWindowActivityInsertion]";
	}

}
