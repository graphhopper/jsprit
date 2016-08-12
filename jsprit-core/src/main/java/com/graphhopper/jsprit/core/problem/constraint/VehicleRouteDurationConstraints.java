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
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;


/**
 * @author stefan
 */
public class VehicleRouteDurationConstraints implements HardActivityConstraint {

    private RouteAndActivityStateGetter stateManager;

    private VehicleRoutingTransportCosts routingCosts;

    public VehicleRouteDurationConstraints(RouteAndActivityStateGetter stateManager, VehicleRoutingTransportCosts routingCosts) {
        super();
        this.stateManager = stateManager;
        this.routingCosts = routingCosts;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if(iFacts.getNewVehicle().getMaximumRouteDuration() == null)
            return ConstraintsStatus.FULFILLED;

        Double oldDuration = 0.0;
        
        if (!iFacts.getRoute().isEmpty()) {
            double routeEndTime = iFacts.getRoute().getEnd().getArrTime();
            double routeRealStartTime = this.stateManager.getRouteState(iFacts.getRoute(), InternalStates.MAXIMUM_ROUTE_DURATION, Double.class);
            oldDuration = routeEndTime - routeRealStartTime;
        }
        
        double maximumVehicleDuration = iFacts.getNewVehicle().getMaximumRouteDuration();

        if (oldDuration > maximumVehicleDuration && !(prevAct instanceof Start))
            return ConstraintsStatus.NOT_FULFILLED_BREAK;

        double tp_time_prevAct_newAct = this.routingCosts.getTransportTime(prevAct.getLocation(), newAct.getLocation(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double newAct_arrTime = prevActDepTime + tp_time_prevAct_newAct;
        double newAct_startTime = Math.max(newAct_arrTime, newAct.getTheoreticalEarliestOperationStartTime());

        double newAct_endTime = newAct_startTime + newAct.getOperationTime();
        double routeDurationIncrease = 0.0;
        
        if (prevAct instanceof Start) {
            double tp_time_start_newAct_backward = this.routingCosts.getBackwardTransportTime(prevAct.getLocation(), newAct.getLocation(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
            double newRouteRealStartTime = newAct_startTime - tp_time_start_newAct_backward;
            if (iFacts.getRoute().isEmpty())
                routeDurationIncrease += prevActDepTime - newRouteRealStartTime;
            else
                routeDurationIncrease += this.stateManager.getRouteState(iFacts.getRoute(), InternalStates.MAXIMUM_ROUTE_DURATION, Double.class) - newRouteRealStartTime;
        }
        if (nextAct instanceof End && !iFacts.getNewVehicle().isReturnToDepot()) {
            routeDurationIncrease += newAct_endTime - prevActDepTime;
        }
        else {
            double tp_time_newAct_nextAct = this.routingCosts.getTransportTime(newAct.getLocation(), nextAct.getLocation(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
            double nextAct_arrTime = newAct_endTime + tp_time_newAct_nextAct;
            double endTime_nextAct_new = Math.max(nextAct_arrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + nextAct.getOperationTime();

            double arrTime_nextAct = prevActDepTime + this.routingCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
            double endTime_nextAct_old = Math.max(arrTime_nextAct, nextAct.getTheoreticalEarliestOperationStartTime()) + nextAct.getOperationTime();

            double endTimeDelay_nextAct = Math.max(0.0D, endTime_nextAct_new - endTime_nextAct_old);
            Double futureWaiting = this.stateManager.getActivityState(nextAct, iFacts.getRoute().getVehicle(), InternalStates.FUTURE_WAITING, Double.class);
            if(futureWaiting == null) {
                futureWaiting = Double.valueOf(0.0D);
            }

            routeDurationIncrease += Math.max(0, endTimeDelay_nextAct - futureWaiting);
        }

        Double newDuration = oldDuration + routeDurationIncrease;

        if (newDuration > maximumVehicleDuration)
            return ConstraintsStatus.NOT_FULFILLED;
        else
            return ConstraintsStatus.FULFILLED;

    }
}

