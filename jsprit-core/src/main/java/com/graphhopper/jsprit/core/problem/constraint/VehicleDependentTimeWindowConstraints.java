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
import com.graphhopper.jsprit.core.problem.cost.SetupTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;


/**
 * @author stefan
 */
public class VehicleDependentTimeWindowConstraints implements HardActivityConstraint {

    private RouteAndActivityStateGetter states;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private SetupTime setupCosts = new SetupTime();

    public VehicleDependentTimeWindowConstraints(RouteAndActivityStateGetter states, VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts) {
        super();
        this.states = states;
        this.routingCosts = routingCosts;
        this.activityCosts = activityCosts;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        double latestVehicleArrival = iFacts.getNewVehicle().getLatestArrival();
        double setup_time_nextActLocation = 0.0;
        Double latestReadyTimeAtNextAct;
        Location nextActLocation;
        if (nextAct instanceof End) {
            latestReadyTimeAtNextAct = latestVehicleArrival;
            nextActLocation = iFacts.getNewVehicle().getEndLocation();
            if (!iFacts.getNewVehicle().isReturnToDepot()) {
                nextActLocation = newAct.getLocation();
            }
        } else {
            latestReadyTimeAtNextAct = states.getActivityState(nextAct, iFacts.getNewVehicle(), InternalStates.LATEST_OPERATION_START_TIME, Double.class);
            if (latestReadyTimeAtNextAct == null) {//otherwise set it to theoretical_latest_operation_startTime
                latestReadyTimeAtNextAct = nextAct.getTheoreticalLatestOperationStartTime();
            }
            
            nextActLocation = nextAct.getLocation();
            setup_time_nextActLocation = setupCosts.getSetupTime(nextAct, iFacts.getNewVehicle());
        }

			/*
             * if latest arrival of vehicle (at its end) is smaller than earliest operation start times of activities,
			 * then vehicle can never conduct activities.
			 *
			 *     |--- vehicle's operation time ---|
			 *                        					|--- prevAct or newAct or nextAct ---|
			 */
        double newAct_theoreticalEarliestOperationStartTime = newAct.getTheoreticalEarliestOperationStartTime();

        if (latestVehicleArrival < prevAct.getTheoreticalEarliestOperationStartTime() ||
            latestVehicleArrival < newAct_theoreticalEarliestOperationStartTime ||
            latestVehicleArrival < nextAct.getTheoreticalEarliestOperationStartTime()) {
            return ConstraintsStatus.NOT_FULFILLED_BREAK;
        }
            /*
             * if the latest operation start-time of new activity is smaller than the earliest start of prev. activity,
			 * then
			 *
			 *                    |--- prevAct ---|
			 *  |--- newAct ---|
			 */
        if (newAct.getTheoreticalLatestOperationStartTime() < prevAct.getTheoreticalEarliestOperationStartTime()) {
            return ConstraintsStatus.NOT_FULFILLED_BREAK;
        }

			/*
             *  |--- prevAct ---|
			 *                                          |- earliest arrival of vehicle
			 *                       |--- nextAct ---|
			 */
        double transportTime_prevAct_nextActLocation = routingCosts.getTransportTime(prevAct.getLocation(), nextActLocation, prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double setupTime_prevAct_nextActLocation = 0.;
        if(!prevAct.getLocation().equals(nextActLocation))
            setupTime_prevAct_nextActLocation = setup_time_nextActLocation;
        double arrTimeAtNextOnDirectRouteWithNewVehicle = prevActDepTime + transportTime_prevAct_nextActLocation;
        double readyTimeAtNextOnDirectRouteWithVehicle = arrTimeAtNextOnDirectRouteWithNewVehicle + setupTime_prevAct_nextActLocation;
        if (readyTimeAtNextOnDirectRouteWithVehicle > latestReadyTimeAtNextAct) {
            return ConstraintsStatus.NOT_FULFILLED_BREAK;
        }

			/*
             *                     |--- newAct ---|
			 *  |--- nextAct ---|
			 */
        if (newAct.getTheoreticalEarliestOperationStartTime() > nextAct.getTheoreticalLatestOperationStartTime()) {
            return ConstraintsStatus.NOT_FULFILLED;
        }
        //			log.info("check insertion of " + newAct + " between " + prevAct + " and " + nextAct + ". prevActDepTime=" + prevActDepTime);
        double setup_time_prevAct_newAct = setupCosts.getSetupTime(prevAct, newAct, iFacts.getNewVehicle());
        double transportTime_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocation(), newAct.getLocation(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double arrTimeAtNewAct = prevActDepTime + transportTime_prevAct_newAct;
        double readyTimeAtNewAct = arrTimeAtNewAct + setup_time_prevAct_newAct;
        double endTimeAtNewAct = Math.max(readyTimeAtNewAct, newAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(newAct,readyTimeAtNewAct,iFacts.getNewDriver(),iFacts.getNewVehicle());
        double setup_time_newAct_nextActLocation = 0.0;
        if(!newAct.getLocation().equals(nextActLocation))
            setup_time_newAct_nextActLocation = setup_time_nextActLocation;
        double latestReadyTimeAtNewAct =
            Math.min(newAct.getTheoreticalLatestOperationStartTime(),
                latestReadyTimeAtNextAct - setup_time_newAct_nextActLocation -
                    routingCosts.getBackwardTransportTime(newAct.getLocation(), nextActLocation, latestReadyTimeAtNextAct, iFacts.getNewDriver(), iFacts.getNewVehicle())
                    - activityCosts.getActivityDuration(newAct, readyTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle())
            );

			/*
             *  |--- prevAct ---|
			 *                       		                 |--- vehicle's arrival @newAct
			 *        latest arrival of vehicle @newAct ---|
			 */
        if (readyTimeAtNewAct > latestReadyTimeAtNewAct) {
            return ConstraintsStatus.NOT_FULFILLED;
        }

        if (nextAct instanceof End) {
            if (!iFacts.getNewVehicle().isReturnToDepot()) {
                return ConstraintsStatus.FULFILLED;
            }
        }
//			log.info(newAct + " arrTime=" + arrTimeAtNewAct);
        double transportTime_newAct_nextAct = routingCosts.getTransportTime(newAct.getLocation(), nextActLocation, endTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double arrTimeAtNextAct = endTimeAtNewAct + transportTime_newAct_nextAct;
        double readyTimeAtNextAct = arrTimeAtNextAct + setup_time_newAct_nextActLocation;

			/*
             *  |--- newAct ---|
			 *                       		                 |--- vehicle's arrival @nextAct
			 *        latest arrival of vehicle @nextAct ---|
			 */
        if (readyTimeAtNextAct > latestReadyTimeAtNextAct) {
            return ConstraintsStatus.NOT_FULFILLED;
        }
        return ConstraintsStatus.FULFILLED;
    }
}

