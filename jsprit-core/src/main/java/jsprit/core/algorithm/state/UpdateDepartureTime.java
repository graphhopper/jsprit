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
package jsprit.core.algorithm.state;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.solution.route.RouteVisitor;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.Iterator;

/**
 * Updates and memorizes latest operation start times at activities.
 * 
 * @author schroeder
 *
 */
public class UpdateDepartureTime implements RouteVisitor, StateUpdater{



	private VehicleRoute route;

	private VehicleRoutingTransportCosts transportCosts;

	private double arrTimeAtPrevActWithoutWaiting;

	private TourActivity prevAct;

	private boolean hasEarliest = false;

	private boolean hasVariableDepartureTime;

	private StateManager stateManager;

	public UpdateDepartureTime(VehicleRoutingTransportCosts tpCosts, StateManager stateManager) {
		super();
		this.transportCosts = tpCosts;
		this.stateManager = stateManager;
	}

	public void begin(VehicleRoute route) {
		this.route = route;
		hasEarliest = false;
		prevAct = route.getEnd();
		hasVariableDepartureTime = route.getVehicle().hasVariableDepartureTime();
	}


	public void visit(TourActivity activity) {
		if(!hasVariableDepartureTime) return;
		if(hasEarliest){
			double potentialArrivalTimeAtCurrAct = arrTimeAtPrevActWithoutWaiting - transportCosts.getBackwardTransportTime(activity.getLocation(), prevAct.getLocation(), arrTimeAtPrevActWithoutWaiting, route.getDriver(),route.getVehicle()) - activity.getOperationTime();
			if(potentialArrivalTimeAtCurrAct < activity.getTheoreticalEarliestOperationStartTime()){
				arrTimeAtPrevActWithoutWaiting = activity.getTheoreticalEarliestOperationStartTime();
			}
			else if(potentialArrivalTimeAtCurrAct >= activity.getTheoreticalEarliestOperationStartTime() && potentialArrivalTimeAtCurrAct <= activity.getTheoreticalLatestOperationStartTime()){
				arrTimeAtPrevActWithoutWaiting = potentialArrivalTimeAtCurrAct;
			}
			else {
				arrTimeAtPrevActWithoutWaiting = activity.getTheoreticalLatestOperationStartTime();
			}
			stateManager.putInternalTypedActivityState(activity,route.getVehicle(),InternalStates.EARLIEST_WITHOUT_WAITING,arrTimeAtPrevActWithoutWaiting);
		}
		else{
			if(activity.getTheoreticalEarliestOperationStartTime() > 0){
				hasEarliest = true;
				arrTimeAtPrevActWithoutWaiting = activity.getTheoreticalEarliestOperationStartTime();
				stateManager.putInternalTypedActivityState(activity,route.getVehicle(),InternalStates.EARLIEST_WITHOUT_WAITING,arrTimeAtPrevActWithoutWaiting);
			}
		}
		prevAct = activity;
	}


	public void finish() {
		if(!hasVariableDepartureTime) return;
		if(hasEarliest){
			double dep = arrTimeAtPrevActWithoutWaiting - transportCosts.getBackwardTransportTime(route.getStart().getLocation(), prevAct.getLocation(), arrTimeAtPrevActWithoutWaiting, route.getDriver(),route.getVehicle());
			double newDepartureTime = Math.max(route.getVehicle().getEarliestDeparture(), dep);
			route.setVehicleAndDepartureTime(route.getVehicle(),newDepartureTime);
		}
		else route.setVehicleAndDepartureTime(route.getVehicle(),route.getVehicle().getEarliestDeparture());
	}

	@Override
	public void visit(VehicleRoute route) {
		begin(route);
		Iterator<TourActivity> revIterator = route.getTourActivities().reverseActivityIterator();
		while(revIterator.hasNext()){
			visit(revIterator.next());
		}
		finish();
	}
}
