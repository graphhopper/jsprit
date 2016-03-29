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
public class VehicleDependentTimeWindowConstraints implements HardActivityConstraint {

    private RouteAndActivityStateGetter states;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

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
        Double latestArrTimeAtNextAct;
        Location nextActLocation;
        double coef = 1.0;
        if(iFacts.getNewVehicle() != null)
        	coef = iFacts.getNewVehicle().getCoefSetupTime();
        if (nextAct instanceof End) {
            latestArrTimeAtNextAct = latestVehicleArrival;
            nextActLocation = iFacts.getNewVehicle().getEndLocation();
            if (!iFacts.getNewVehicle().isReturnToDepot()) {
                nextActLocation = newAct.getLocation();
            }
        } else {
            latestArrTimeAtNextAct = states.getActivityState(nextAct, iFacts.getNewVehicle(), InternalStates.LATEST_OPERATION_START_TIME, Double.class);
            if (latestArrTimeAtNextAct == null) {//otherwise set it to theoretical_latest_operation_startTime
                latestArrTimeAtNextAct = nextAct.getTheoreticalLatestOperationStartTime();
            }
            
            nextActLocation = nextAct.getLocation();
            setup_time_nextActLocation = nextAct.getSetupTime() * coef;
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
        double setup_time_prevAct_nextActLocation = 0.0;
        if(!prevAct.getLocation().equals(nextActLocation))
        	setup_time_prevAct_nextActLocation = setup_time_nextActLocation;
        double transportTime_prevAct_nextActLocation = setup_time_prevAct_nextActLocation + routingCosts.getTransportTime(prevAct.getLocation(), nextActLocation, prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double arrTimeAtNextOnDirectRouteWithNewVehicle = prevActDepTime + transportTime_prevAct_nextActLocation;
        if (arrTimeAtNextOnDirectRouteWithNewVehicle > latestArrTimeAtNextAct) {
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
        double setup_time_prevAct_newAct = 0.0;
        if(!prevAct.getLocation().equals(newAct.getLocation()))
        	setup_time_prevAct_newAct = newAct.getSetupTime() * coef;
        double transportTime_prevAct_newAct = setup_time_prevAct_newAct + routingCosts.getTransportTime(prevAct.getLocation(), newAct.getLocation(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double arrTimeAtNewAct = prevActDepTime + transportTime_prevAct_newAct;
        double endTimeAtNewAct = Math.max(arrTimeAtNewAct, newAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(newAct, arrTimeAtNewAct,iFacts.getNewDriver(),iFacts.getNewVehicle());
        double backSetupTime = 0.0;
        if(!newAct.getLocation().equals(nextActLocation))
        	backSetupTime = setup_time_nextActLocation;
        double latestArrTimeAtNewAct =
            Math.min(newAct.getTheoreticalLatestOperationStartTime(),
                latestArrTimeAtNextAct - backSetupTime -
                    routingCosts.getBackwardTransportTime(newAct.getLocation(), nextActLocation, latestArrTimeAtNextAct, iFacts.getNewDriver(), iFacts.getNewVehicle())
                    - activityCosts.getActivityDuration(newAct, arrTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle())
            );

			/*
             *  |--- prevAct ---|
			 *                       		                 |--- vehicle's arrival @newAct
			 *        latest arrival of vehicle @newAct ---|
			 */
        if (arrTimeAtNewAct > latestArrTimeAtNewAct) {
            return ConstraintsStatus.NOT_FULFILLED;
        }

        if (nextAct instanceof End) {
            if (!iFacts.getNewVehicle().isReturnToDepot()) {
                return ConstraintsStatus.FULFILLED;
            }
        }
//			log.info(newAct + " arrTime=" + arrTimeAtNewAct);
        double setup_time_newAct_nextActLocation = 0.0;
        if(!newAct.getLocation().equals(nextActLocation))
        	setup_time_newAct_nextActLocation = setup_time_nextActLocation;
        double transportTime_newAct_nextAct = setup_time_newAct_nextActLocation + routingCosts.getTransportTime(newAct.getLocation(), nextActLocation, endTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double arrTimeAtNextAct = endTimeAtNewAct + transportTime_newAct_nextAct;

			/*
             *  |--- newAct ---|
			 *                       		                 |--- vehicle's arrival @nextAct
			 *        latest arrival of vehicle @nextAct ---|
			 */
        if (arrTimeAtNextAct > latestArrTimeAtNextAct) {
            return ConstraintsStatus.NOT_FULFILLED;
        }
        return ConstraintsStatus.FULFILLED;
    }
}

