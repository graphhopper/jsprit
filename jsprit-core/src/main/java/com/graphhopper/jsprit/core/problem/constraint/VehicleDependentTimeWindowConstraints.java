/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActWithoutStaticLocation;
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
        double earliestVehicleDeparture = iFacts.getNewVehicle().getEarliestDeparture();
        /**
         * fail to take different time windows of different vehicle types into account.
         * for example, original vehicle's time window is 07:00-12:00
         * obviously it cannot delivery the first order with 13:00-21:00 in current route,
         * but currently it omits this kind of checking logic and only does for time windows among preAct, newAct, nextAct and newVehicle
         */
        Location startPoint;
        if(prevAct instanceof Start){
            startPoint = prevAct.getLocation();
        }
        else{
            startPoint = iFacts.getRoute().getStart().getLocation();
        }
        TourActivity tmpFirstAct,tmpEndAct;
        if(iFacts.getRoute().getActivities().size() == 0) {
            tmpFirstAct = newAct;
            tmpEndAct = newAct;
        }
        else{
            tmpFirstAct = iFacts.getRoute().getActivities().get(0);
            tmpEndAct = iFacts.getRoute().getActivities().get(iFacts.getRoute().getActivities().size()-1);
        }
        double eta = earliestVehicleDeparture + routingCosts.getTransportTime(startPoint, tmpFirstAct.getLocation(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        if (eta > tmpFirstAct.getTheoreticalLatestOperationStartTime()
                || latestVehicleArrival < tmpFirstAct.getArrTime()
                || latestVehicleArrival < tmpEndAct.getArrTime()) {
            return ConstraintsStatus.NOT_FULFILLED_BREAK;
        }
        if(iFacts.getNewVehicle().isReturnToDepot()){
            eta = tmpEndAct.getEndTime() + routingCosts.getTransportTime(tmpEndAct.getLocation(), iFacts.getNewVehicle().getEndLocation(), tmpEndAct.getEndTime(), iFacts.getNewDriver(), iFacts.getNewVehicle());
            if(latestVehicleArrival < eta){
                return ConstraintsStatus.NOT_FULFILLED_BREAK;
            }
        }
        
        
        Double latestArrTimeAtNextAct;
        Location prevLocation = prevAct.getLocation();
        if (prevAct instanceof ActWithoutStaticLocation) ((ActWithoutStaticLocation) prevAct).getPreviousLocation();
        Location newLocation = newAct.getLocation();
        if (newAct instanceof ActWithoutStaticLocation) newLocation = prevLocation;
        Location nextLocation = nextAct.getLocation();
        if (nextAct instanceof ActWithoutStaticLocation) ((ActWithoutStaticLocation) nextAct).getNextLocation();
        if (nextAct instanceof End) {
            latestArrTimeAtNextAct = latestVehicleArrival;
            nextLocation = iFacts.getNewVehicle().getEndLocation();
            if (!iFacts.getNewVehicle().isReturnToDepot()) {
                nextLocation = newLocation;
            }
        } else {
            latestArrTimeAtNextAct = states.getActivityState(nextAct, iFacts.getNewVehicle(), InternalStates.LATEST_OPERATION_START_TIME, Double.class);
            if (latestArrTimeAtNextAct == null) {//otherwise set it to theoretical_latest_operation_startTime
                latestArrTimeAtNextAct = nextAct.getTheoreticalLatestOperationStartTime();
            }
//            nextLocation = nextAct.getLocation();
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

        double arrTimeAtNextOnDirectRouteWithNewVehicle = prevActDepTime + routingCosts.getTransportTime(prevLocation, nextLocation, prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
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
        double arrTimeAtNewAct = prevActDepTime + routingCosts.getTransportTime(prevLocation, newLocation, prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double endTimeAtNewAct = Math.max(arrTimeAtNewAct, newAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(newAct, arrTimeAtNewAct,iFacts.getNewDriver(),iFacts.getNewVehicle());
        double latestArrTimeAtNewAct =
            Math.min(newAct.getTheoreticalLatestOperationStartTime(),
                latestArrTimeAtNextAct -
                    routingCosts.getBackwardTransportTime(newLocation, nextLocation, latestArrTimeAtNextAct, iFacts.getNewDriver(), iFacts.getNewVehicle())
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

        double arrTimeAtNextAct = endTimeAtNewAct + routingCosts.getTransportTime(newLocation, nextLocation, endTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());

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

