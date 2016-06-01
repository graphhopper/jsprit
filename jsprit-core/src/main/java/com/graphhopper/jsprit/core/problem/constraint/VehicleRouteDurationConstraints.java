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

package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;


/**
 * @author stefan
 */
public class VehicleRouteDurationConstraints implements HardActivityConstraint {

    private RouteAndActivityStateGetter states;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

    public VehicleRouteDurationConstraints(RouteAndActivityStateGetter states, VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts) {
        super();
        this.states = states;
        this.routingCosts = routingCosts;
        this.activityCosts = activityCosts;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        double maximumVehicleDuration = iFacts.getNewVehicle().getMaximumRouteDuration();
        
        double routeDurationIncrease;
        double oldDuration = iFacts.getRoute().getEnd().getArrTime() - iFacts.getRoute().getStart().getEndTime();
        
        if (oldDuration > maximumVehicleDuration)
            return ConstraintsStatus.NOT_FULFILLED_BREAK;

        double tp_time_prevAct_newAct = this.routingCosts.getTransportTime(prevAct.getLocation(), newAct.getLocation(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double newAct_arrTime = prevActDepTime + tp_time_prevAct_newAct;
        double newAct_endTime = Math.max(newAct_arrTime, newAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(newAct,newAct_arrTime,iFacts.getNewDriver(),iFacts.getNewVehicle());
        
        if (nextAct instanceof End && !iFacts.getNewVehicle().isReturnToDepot()) {
            routeDurationIncrease = newAct_endTime - prevActDepTime;
        }
        else {
            double tp_time_newAct_nextAct = this.routingCosts.getTransportTime(newAct.getLocation(), nextAct.getLocation(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
            double nextAct_arrTime = newAct_endTime + tp_time_newAct_nextAct;
            double nextAct_endTime = Math.max(nextAct_arrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(nextAct,nextAct_arrTime,iFacts.getNewDriver(),iFacts.getNewVehicle());
            
            double arrTime_nextAct = prevActDepTime + this.routingCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
            double endTime_nextAct_old = Math.max(arrTime_nextAct, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(nextAct,arrTime_nextAct,iFacts.getNewDriver(),iFacts.getNewVehicle());
            
            double endTimeDelay_nextAct = Math.max(0., nextAct_endTime - endTime_nextAct_old);
            Double futureWaiting = states.getActivityState(nextAct, iFacts.getRoute().getVehicle(), InternalStates.FUTURE_WAITING, Double.class);
            if(futureWaiting == null) {
                futureWaiting = 0.;
            }
            routeDurationIncrease = Math.max(0, endTimeDelay_nextAct - futureWaiting);
        }

        double newDuration = oldDuration + routeDurationIncrease;
        if (newDuration > iFacts.getNewVehicle().getMaximumRouteDuration())
            return ConstraintsStatus.NOT_FULFILLED;
        return ConstraintsStatus.FULFILLED;
    }
}

