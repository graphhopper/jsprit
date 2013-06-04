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

import java.util.Iterator;

import org.apache.log4j.Logger;

import algorithms.RouteStates.ActivityState;
import basics.costs.BackwardTransportTime;
import basics.route.Start;
import basics.route.TourActivities;
import basics.route.TourActivity;
import basics.route.VehicleRoute;






/**
 * 
 * @author stefan schroeder
 *
 */

class UpdateTourStatesBackwardInTime implements VehicleRouteUpdater{
	
//	public static Counter counter = new Counter("#updateTWProcesses: ");
	
	private static Logger log = Logger.getLogger(UpdateTourStatesBackwardInTime.class);
	
	public boolean checkFeasibility = true;

	private BackwardTransportTime transportTime;
	
	private RouteStates actStates;
	
	public void setActivityStates(RouteStates actStates){
		this.actStates = actStates;
	}

	public ActivityState state(TourActivity act){
		return actStates.getState(act);
	}
	
	public UpdateTourStatesBackwardInTime(BackwardTransportTime transportTime) {
		super();
		this.transportTime = transportTime;
	}

	/*
	 * 
	 */
	public boolean updateRoute(VehicleRoute vehicleRoute) {
		boolean ok = update(vehicleRoute);
		return ok;
	}



	private boolean update(VehicleRoute vehicleRoute) {
		TourActivities tour = vehicleRoute.getTourActivities();
		int tourSize = tour.getActivities().size();
		Iterator<TourActivity> reverseActIter = vehicleRoute.getTourActivities().reverseActivityIterator();
		TourActivity prevAct;
		boolean feasible = true;
		prevAct = vehicleRoute.getEnd();
		double startAtPrevAct = prevAct.getTheoreticalLatestOperationStartTime();
		int count = 0;
		while(reverseActIter.hasNext()){
			TourActivity currAct = reverseActIter.next();
			
			double latestOperationStartTime = latestOperationStartTime(vehicleRoute, prevAct, currAct, startAtPrevAct);
			ActivityState state = state(currAct);
			state.setLatestOperationStart(latestOperationStartTime);
			prevAct = currAct;
			startAtPrevAct = latestOperationStartTime;
			count++;
		}
//		Start start = vehicleRoute.getStart();
//		double latestOperationStartTime = latestOperationStartTime(vehicleRoute, prevAct, start, startAtPrevAct);
		assert count == tourSize;
		return feasible;
	}

	private double latestOperationStartTime(VehicleRoute vehicleRoute,
			TourActivity prevAct, TourActivity currAct, double startAtPrevAct) {
		double latestDepTimeAtCurrAct = startAtPrevAct - transportTime.getBackwardTransportTime(currAct.getLocationId(), prevAct.getLocationId(), startAtPrevAct, vehicleRoute.getDriver(),vehicleRoute.getVehicle());
		double potentialLatestOperationStartTimeAtCurrAct = latestDepTimeAtCurrAct - currAct.getOperationTime();
		double latestOperationStartTime = Math.min(currAct.getTheoreticalLatestOperationStartTime(), potentialLatestOperationStartTimeAtCurrAct);
		return latestOperationStartTime;
	}

}
