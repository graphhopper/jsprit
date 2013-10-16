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
import java.util.List;

import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.TourActivity;
import basics.route.Vehicle;


final class AuxilliaryCostCalculator {
	
	private final VehicleRoutingTransportCosts routingCosts;
	
	private final VehicleRoutingActivityCosts activityCosts;

	public AuxilliaryCostCalculator(final VehicleRoutingTransportCosts routingCosts, final VehicleRoutingActivityCosts costFunction) {
		super();
		this.routingCosts = routingCosts;
		this.activityCosts = costFunction;
	}
	
	/**
	 * 
	 * @param path
	 * @param depTime
	 * @param driver
	 * @param vehicle
	 * @return
	 */
	public double costOfPath(final List<TourActivity> path, final double depTime, final Driver driver, final Vehicle vehicle){
		if(path.isEmpty()){
			return 0.0;
		}
		double cost = 0.0;
		Iterator<TourActivity> actIter = path.iterator();
		TourActivity prevAct = actIter.next();
		double startCost = 0.0;
		cost += startCost;
		double departureTimePrevAct = depTime;
		while(actIter.hasNext()){
			TourActivity act = actIter.next();
			double transportCost = routingCosts.getTransportCost(prevAct.getLocationId(), act.getLocationId(), departureTimePrevAct, driver, vehicle);
			double transportTime = routingCosts.getTransportTime(prevAct.getLocationId(), act.getLocationId(), departureTimePrevAct, driver, vehicle);
			cost += transportCost;
			double actStartTime = departureTimePrevAct + transportTime;
			double earliestOperationStartTime = Math.max(actStartTime, act.getTheoreticalEarliestOperationStartTime());
			double actEndTime = earliestOperationStartTime + act.getOperationTime();
			departureTimePrevAct = actEndTime;
			cost += activityCosts.getActivityCost(act, actStartTime, driver, vehicle);
			prevAct = act;
		}
		return cost;
	}
	
	public double costOfPath(String startLocationId, final double startTime, final List<TourActivity> path, String endLocationId, final Driver driver, final Vehicle vehicle){
		if(path.isEmpty()){
			return 0.0;
		}
		double cost = 0.0;
//		Iterator<TourActivity> actIter = path.iterator();
		String prevActLocation = startLocationId;
//		TourActivity prevAct = actIter.next();
		double startCost = 0.0;
		cost += startCost;
		double departureTimePrevAct = startTime;
		for(TourActivity act : path){
//			TourActivity act = actIter.next();
			double transportCost = routingCosts.getTransportCost(prevActLocation, act.getLocationId(), departureTimePrevAct, driver, vehicle);
			double transportTime = routingCosts.getTransportTime(prevActLocation, act.getLocationId(), departureTimePrevAct, driver, vehicle);
			cost += transportCost;
			double actStartTime = departureTimePrevAct + transportTime;
			double earliestOperationStartTime = Math.max(actStartTime, act.getTheoreticalEarliestOperationStartTime());
			double actEndTime = earliestOperationStartTime + act.getOperationTime();
			departureTimePrevAct = actEndTime;
			cost += activityCosts.getActivityCost(act, actStartTime, driver, vehicle);
			prevActLocation = act.getLocationId();
		}
		
		/*
		 *!!! ENDLOCATION
		=> Start u. End können primitiv sein. 
		 
		 */
		
		return cost;
	}
	
}
