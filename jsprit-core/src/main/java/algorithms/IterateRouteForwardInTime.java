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

import algorithms.ForwardInTimeListeners.ForwardInTimeListener;
import basics.costs.ForwardTransportTime;
import basics.route.Driver;
import basics.route.End;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;


/**
 * 
 * @author sschroeder
 *
 */

class IterateRouteForwardInTime implements VehicleRouteUpdater{
	
	private static Logger log = Logger.getLogger(IterateRouteForwardInTime.class);
	
	private ForwardTransportTime transportTime;

	private ForwardInTimeListeners listeners;
	
	public IterateRouteForwardInTime(ForwardTransportTime transportTime) {
		super();
		this.transportTime = transportTime;
		listeners = new ForwardInTimeListeners();
	}

	/**
	 * 
	 * 
	 */
	public void iterate(VehicleRoute vehicleRoute) {
		if(listeners.isEmpty()) return;
		listeners.start(vehicleRoute, vehicleRoute.getStart(), vehicleRoute.getStart().getEndTime());
		
		Vehicle vehicle = vehicleRoute.getVehicle();
		Driver driver = vehicleRoute.getDriver();
		TourActivity prevAct = vehicleRoute.getStart(); 
		double startAtPrevAct = prevAct.getEndTime();		
		
		for(TourActivity currentAct : vehicleRoute.getTourActivities().getActivities()){ 
			double transportTime = this.transportTime.getTransportTime(prevAct.getLocationId(), currentAct.getLocationId(), startAtPrevAct, driver, vehicle);
			double arrivalTimeAtCurrAct = startAtPrevAct + transportTime; 
			double operationStartTime = Math.max(currentAct.getTheoreticalEarliestOperationStartTime(), arrivalTimeAtCurrAct);
			double operationEndTime = operationStartTime + currentAct.getOperationTime();
			
			listeners.nextActivity(currentAct,arrivalTimeAtCurrAct,operationEndTime);
			
			prevAct = currentAct;
			startAtPrevAct = operationEndTime;
		}
		
		End currentAct = vehicleRoute.getEnd();
		double transportTime = this.transportTime.getTransportTime(prevAct.getLocationId(), currentAct.getLocationId(), startAtPrevAct, driver, vehicle);
		double arrivalTimeAtCurrAct = startAtPrevAct + transportTime; 
		
		listeners.end(vehicleRoute.getEnd(), arrivalTimeAtCurrAct);
	}
	
	public void addListener(ForwardInTimeListener l){
		listeners.addListener(l);
	}

}
