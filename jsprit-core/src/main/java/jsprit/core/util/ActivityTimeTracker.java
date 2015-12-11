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
package jsprit.core.util;

import jsprit.core.algorithm.state.ActivityStartAsSoonAsArrived;
import jsprit.core.algorithm.state.ActivityStartStrategy;
import jsprit.core.algorithm.state.ActivityStartsAsSoonAsNextTimeWindowOpens;
import jsprit.core.problem.cost.ForwardTransportTime;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;

public class ActivityTimeTracker implements ActivityVisitor {

    public static enum ActivityPolicy {

        AS_SOON_AS_TIME_WINDOW_OPENS, AS_SOON_AS_ARRIVED

    }

    private ForwardTransportTime transportTime;

    private TourActivity prevAct = null;

    private double startAtPrevAct;

    private VehicleRoute route;

    private boolean beginFirst = false;

    private double actArrTime;

    private double actEndTime;

    private ActivityStartStrategy startStrategy;

	public ActivityTimeTracker(ForwardTransportTime transportTime) {
		super();
		this.transportTime = transportTime;
		this.startStrategy = new ActivityStartsAsSoonAsNextTimeWindowOpens();
	}

    public ActivityTimeTracker(ForwardTransportTime transportTime, ActivityPolicy activityPolicy) {
        super();
        this.transportTime = transportTime;
		if(activityPolicy.equals(ActivityPolicy.AS_SOON_AS_ARRIVED)){
			this.startStrategy = new ActivityStartAsSoonAsArrived();
		}
		else this.startStrategy = new ActivityStartsAsSoonAsNextTimeWindowOpens();
    }

    public ActivityTimeTracker(ForwardTransportTime transportTime, ActivityStartStrategy startStrategy) {
        super();
        this.transportTime = transportTime;
        this.startStrategy = startStrategy;
    }

    public double getActArrTime() {
        return actArrTime;
    }

    public double getActEndTime() {
        return actEndTime;
    }

    @Override
    public void begin(VehicleRoute route) {
        prevAct = route.getStart();
        startAtPrevAct = prevAct.getEndTime();
        actEndTime = startAtPrevAct;
        this.route = route;
        beginFirst = true;
    }

    @Override
    public void visit(TourActivity activity) {
        if(!beginFirst) throw new IllegalStateException("never called begin. this however is essential here");
        double transportTime = this.transportTime.getTransportTime(prevAct.getLocation(), activity.getLocation(), startAtPrevAct, route.getDriver(), route.getVehicle());
        double arrivalTimeAtCurrAct = startAtPrevAct + transportTime;
        actArrTime = arrivalTimeAtCurrAct;
        assert actArrTime <= activity.getTimeWindows().get(activity.getTimeWindows().size()-1).getEnd() : "that should not be";
        double operationEndTime = startStrategy.getActivityStartTime(activity,arrivalTimeAtCurrAct) + activity.getOperationTime();
        actEndTime = operationEndTime;
        prevAct = activity;
        startAtPrevAct = operationEndTime;
    }

    @Override
    public void finish() {
        double transportTime = this.transportTime.getTransportTime(prevAct.getLocation(), route.getEnd().getLocation(), startAtPrevAct, route.getDriver(), route.getVehicle());
        double arrivalTimeAtCurrAct = startAtPrevAct + transportTime;
        actArrTime = arrivalTimeAtCurrAct;
        assert actArrTime <= route.getVehicle().getLatestArrival() : "oohh. this should not be";
        actEndTime = arrivalTimeAtCurrAct;
        beginFirst = false;
    }


}
