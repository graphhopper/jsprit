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
		if(listeners.isEmpty()) return;
		if(vehicleRoute.isEmpty()) return;
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
