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

import algorithms.BackwardInTimeListeners.BackwardInTimeListener;
import basics.costs.BackwardTransportTime;
import basics.route.TourActivity;
import basics.route.VehicleRoute;


/**
 * 
 * @author stefan schroeder
 *
 */

class IterateRouteBackwardInTime implements VehicleRouteUpdater{
	
	private static Logger log = Logger.getLogger(IterateRouteBackwardInTime.class);

	private BackwardTransportTime transportTime;
	
	private BackwardInTimeListeners listeners;
	
	public IterateRouteBackwardInTime(BackwardTransportTime transportTime) {
		super();
		this.transportTime = transportTime;
		listeners = new BackwardInTimeListeners();
	}

	/*
	 * 
	 */
	public void iterate(VehicleRoute vehicleRoute) {
		listeners.start(vehicleRoute);
		Iterator<TourActivity> reverseActIter = vehicleRoute.getTourActivities().reverseActivityIterator();
		TourActivity prevAct;
		prevAct = vehicleRoute.getEnd();
		double startAtPrevAct = prevAct.getTheoreticalLatestOperationStartTime();
		listeners.prevActivity(prevAct, startAtPrevAct, startAtPrevAct);
		while(reverseActIter.hasNext()){
			TourActivity currAct = reverseActIter.next();	
			double latestDepTimeAtCurrAct = startAtPrevAct - transportTime.getBackwardTransportTime(currAct.getLocationId(), prevAct.getLocationId(), startAtPrevAct, vehicleRoute.getDriver(),vehicleRoute.getVehicle());
			double potentialLatestOperationStartTimeAtCurrAct = latestDepTimeAtCurrAct - currAct.getOperationTime();
			double latestOperationStartTime = Math.min(currAct.getTheoreticalLatestOperationStartTime(), potentialLatestOperationStartTimeAtCurrAct);
			listeners.prevActivity(currAct, latestDepTimeAtCurrAct, latestOperationStartTime);
			prevAct = currAct;
			startAtPrevAct = latestOperationStartTime;
		}
		listeners.finnish();
	}
	
	public void addListener(BackwardInTimeListener l){ listeners.addListener(l); }

}
