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
		listeners.start(vehicleRoute, vehicleRoute.getEnd(), vehicleRoute.getEnd().getTheoreticalLatestOperationStartTime());
		
		Iterator<TourActivity> reverseActIter = vehicleRoute.getTourActivities().reverseActivityIterator();
		TourActivity prevAct;
		prevAct = vehicleRoute.getEnd();
		double latestArrivalTimeAtPrevAct = prevAct.getTheoreticalLatestOperationStartTime();
		
		while(reverseActIter.hasNext()){
			TourActivity currAct = reverseActIter.next();	
			double latestDepTimeAtCurrAct = latestArrivalTimeAtPrevAct - transportTime.getBackwardTransportTime(currAct.getLocationId(), prevAct.getLocationId(), latestArrivalTimeAtPrevAct, vehicleRoute.getDriver(),vehicleRoute.getVehicle());
			double potentialLatestArrivalTimeAtCurrAct = latestDepTimeAtCurrAct - currAct.getOperationTime();
			double latestArrivalTime = Math.min(currAct.getTheoreticalLatestOperationStartTime(), potentialLatestArrivalTimeAtCurrAct);
			
			listeners.prevActivity(currAct, latestDepTimeAtCurrAct, latestArrivalTime);
			
			prevAct = currAct;
			latestArrivalTimeAtPrevAct = latestArrivalTime;
		}
		
		TourActivity currAct = vehicleRoute.getStart();
		double latestDepTimeAtCurrAct = latestArrivalTimeAtPrevAct - transportTime.getBackwardTransportTime(currAct.getLocationId(), prevAct.getLocationId(), latestArrivalTimeAtPrevAct, vehicleRoute.getDriver(),vehicleRoute.getVehicle());
		
		listeners.end(vehicleRoute.getStart(), latestDepTimeAtCurrAct);
	}
	
	public void addListener(BackwardInTimeListener l){ listeners.addListener(l); }

}
