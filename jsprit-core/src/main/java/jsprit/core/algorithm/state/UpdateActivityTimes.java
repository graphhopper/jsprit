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

import jsprit.core.problem.cost.ForwardTransportTime;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.util.ActivityTimeTracker;


/**
 * Updates arrival and end times of activities. 
 * 
 * <p>Note that this modifies arrTime and endTime of each activity in a route.
 * 
 * @author stefan
 *
 */
public class UpdateActivityTimes implements ActivityVisitor, StateUpdater{

    private ActivityTimeTracker timeTracker;
	
	private VehicleRoute route;

	/**
	 * Updates arrival and end times of activities. 
	 * 
	 * <p>Note that this modifies arrTime and endTime of each activity in a route.
	 * 
	 * <p>ArrTimes and EndTimes can be retrieved by <br>
	 * <code>activity.getArrTime()</code> and
	 * <code>activity.getEndTime()</code>
	 * 
	 *
	 *
	 */
	public UpdateActivityTimes(ForwardTransportTime transportTime) {
		super();
		timeTracker = new ActivityTimeTracker(transportTime);
	}

    public UpdateActivityTimes(ForwardTransportTime transportTime, ActivityTimeTracker.ActivityPolicy activityPolicy){
        timeTracker = new ActivityTimeTracker(transportTime,activityPolicy);
    }

	@Override
	public void begin(VehicleRoute route) {
		timeTracker.begin(route);
		this.route = route;
		route.getStart().setEndTime(timeTracker.getActEndTime());
	}

	@Override
	public void visit(TourActivity activity) {
		timeTracker.visit(activity);
		activity.setArrTime(timeTracker.getActArrTime());
		activity.setEndTime(timeTracker.getActEndTime());
	}

	@Override
	public void finish() {
		timeTracker.finish();
		route.getEnd().setArrTime(timeTracker.getActArrTime());
	}

}
