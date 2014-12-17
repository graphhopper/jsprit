/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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

import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;

import java.util.Iterator;
import java.util.List;



final class AuxilliaryCostCalculator {
	
	private final VehicleRoutingTransportCosts routingCosts;
	
	private final VehicleRoutingActivityCosts activityCosts;

	public AuxilliaryCostCalculator(final VehicleRoutingTransportCosts routingCosts, final VehicleRoutingActivityCosts actCosts) {
		super();
		this.routingCosts = routingCosts;
		this.activityCosts = actCosts;
	}

    /**
     *
     * @param path activity path to get the costs for
     * @param depTime departure time at first activity in path
     * @param driver driver of vehicle
     * @param vehicle vehicle running the path
     * @return cost of path
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
			if(act instanceof End){
				if(!vehicle.isReturnToDepot()){
					return cost;
				}
			}
			double transportCost = routingCosts.getTransportCost(prevAct.getLocation(), act.getLocation(), departureTimePrevAct, driver, vehicle);
			double transportTime = routingCosts.getTransportTime(prevAct.getLocation(), act.getLocation(), departureTimePrevAct, driver, vehicle);
			cost += transportCost;
			double actStartTime = departureTimePrevAct + transportTime;
            departureTimePrevAct = Math.max(actStartTime, act.getTheoreticalEarliestOperationStartTime()) + act.getOperationTime();
			cost += activityCosts.getActivityCost(act, actStartTime, driver, vehicle);
			prevAct = act;
		}
		return cost;
	}
	
	
}
