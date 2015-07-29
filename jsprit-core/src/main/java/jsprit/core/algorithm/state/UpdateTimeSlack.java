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
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;


/**
 * Updates arrival and end times of activities. 
 * 
 * <p>Note that this modifies arrTime and endTime of each activity in a route.
 * 
 * @author stefan
 *
 */
public class UpdateTimeSlack implements ActivityVisitor, StateUpdater{

	private VehicleRoute route;

	private StateManager stateManager;

	private VehicleRoutingTransportCosts costs;

	private double prevTimeSlack;

	private double prevActDeparture;

	private TourActivity prevAct;

	public UpdateTimeSlack(StateManager stateManager, VehicleRoutingTransportCosts costs) {
		this.stateManager = stateManager;
		this.costs = costs;
	}

	@Override
	public void begin(VehicleRoute route) {
		if(route.isEmpty()) return;
		this.route = route;
		prevActDeparture = route.getVehicle().getEarliestDeparture();
		TourActivity first = route.getActivities().get(0);
		double latestArr = stateManager.getActivityState(first,route.getVehicle(),InternalStates.LATEST_OPERATION_START_TIME,Double.class);
		double latest = latestArr - costs.getBackwardTransportCost(first.getLocation(),route.getStart().getLocation(),latestArr,route.getDriver(),route.getVehicle());
		prevTimeSlack = latest - prevActDeparture;
		prevAct = route.getStart();
	}

	@Override
	public void visit(TourActivity activity) {
		double actArrTime = prevActDeparture + costs.getTransportTime(prevAct.getLocation(),activity.getLocation(),prevActDeparture,route.getDriver(),route.getVehicle());
		double actEarliestStart = Math.max(actArrTime,activity.getTheoreticalEarliestOperationStartTime());
		double actLatestStart = stateManager.getActivityState(activity,route.getVehicle(),InternalStates.LATEST_OPERATION_START_TIME,Double.class);
		double latest_minus_earliest = actLatestStart - actEarliestStart;
		double time_slack_ = Math.max(actArrTime + prevTimeSlack - activity.getTheoreticalEarliestOperationStartTime(),0);
		double time_slack = Math.min(latest_minus_earliest,time_slack_);
		stateManager.putInternalTypedActivityState(activity,route.getVehicle(), InternalStates.TIME_SLACK, time_slack);
		prevTimeSlack = time_slack;
		prevAct = activity;
		prevActDeparture = actEarliestStart + activity.getOperationTime();
	}

	@Override
	public void finish() {}

}
