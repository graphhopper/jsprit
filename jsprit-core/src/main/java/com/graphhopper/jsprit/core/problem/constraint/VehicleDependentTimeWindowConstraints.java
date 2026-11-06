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
import com.graphhopper.jsprit.core.problem.misc.ActivityContext;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActWithoutStaticLocation;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.List;


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
        double latestArrTimeAtNextAct;
        Location prevLocation = prevAct.getLocation();
        if (prevAct instanceof ActWithoutStaticLocation)
            prevLocation = ((ActWithoutStaticLocation) prevAct).getPreviousLocation();
        Location newLocation = newAct.getLocation();
        if (newAct instanceof ActWithoutStaticLocation) newLocation = prevLocation;
        Location nextLocation = nextAct.getLocation();
        // A floating next activity (no location of its own) is performed where the vehicle is, so once the new
        // activity precedes it, it takes place at the new location with no travel in between. Its stored latest
        // start was computed with the leg from its old predecessor to the stop after it, which no longer applies;
        // the activity's own window is checked here, the stop after it in legAfterFloatingActivities.
        boolean nextFloats = nextAct instanceof ActWithoutStaticLocation;
        if (nextFloats) nextLocation = newLocation;
        if (nextAct instanceof End) {
            latestArrTimeAtNextAct = latestVehicleArrival;
            nextLocation = iFacts.getNewVehicle().getEndLocation();
            if (!iFacts.getNewVehicle().isReturnToDepot()) {
                nextLocation = newLocation;
            }
        } else if (nextFloats) {
            latestArrTimeAtNextAct = nextAct.getTheoreticalLatestOperationStartTime();
        } else {
            latestArrTimeAtNextAct = states.getActivityDouble(nextAct, iFacts.getNewVehicle(), InternalStates.LATEST_OPERATION_START_TIME);
            if (Double.isNaN(latestArrTimeAtNextAct)) {//otherwise set it to theoretical_latest_operation_startTime
                latestArrTimeAtNextAct = nextAct.getTheoreticalLatestOperationStartTime();
            }
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

        // without the new activity a floating next activity happens at the previous activity, with no travel
        double arrTimeAtNextOnDirectRouteWithNewVehicle = nextFloats ? prevActDepTime
            : prevActDepTime + routingCosts.getTransportTime(prevLocation, nextLocation, prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
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
        double endTimeAtNewAct = Math.max(arrTimeAtNewAct, newAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, newAct, arrTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double latestArrTimeAtNewAct =
            Math.min(newAct.getTheoreticalLatestOperationStartTime(),
                latestArrTimeAtNextAct -
                    routingCosts.getBackwardTransportTime(newLocation, nextLocation, latestArrTimeAtNextAct, iFacts.getNewDriver(), iFacts.getNewVehicle())
                        - activityCosts.getActivityDuration(prevAct, newAct, arrTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle())
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
        if (nextFloats) {
            return legAfterFloatingActivities(iFacts, newAct, newLocation, nextAct, arrTimeAtNextAct);
        }
        return ConstraintsStatus.FULFILLED;
    }

    /**
     * The new activity is followed by one or more floating activities, all performed at the new location: walks
     * them with their waiting and duration and checks the arrival at the first located activity after them (or
     * at the route end) against its latest start, which is the leg the floating activities' stored states do
     * not know about. A pickup of the same shipment that is being inserted at the same time is not in the route
     * yet and is not seen; the check then misses that detour, it never rejects a feasible position for it.
     */
    private ConstraintsStatus legAfterFloatingActivities(JobInsertionContext iFacts, TourActivity newAct, Location newLocation, TourActivity nextAct, double arrTimeAtNextAct) {
        List<TourActivity> acts = iFacts.getRoute().getActivities();
        int i = indexOf(acts, nextAct, iFacts.getActivityContext());
        if (i < 0) return ConstraintsStatus.FULFILLED; // the next activity is not in the route: nothing after it to check
        Vehicle vehicle = iFacts.getNewVehicle();
        TourActivity prev = newAct;
        TourActivity act = nextAct;
        double time = arrTimeAtNextAct;
        while (true) {
            double start = Math.max(time, act.getTheoreticalEarliestOperationStartTime());
            if (start > act.getTheoreticalLatestOperationStartTime()) return ConstraintsStatus.NOT_FULFILLED;
            time = start + activityCosts.getActivityDuration(prev, act, time, iFacts.getNewDriver(), vehicle);
            prev = act;
            i++;
            if (i >= acts.size()) {
                if (!vehicle.isReturnToDepot()) return ConstraintsStatus.FULFILLED;
                double arrTimeAtEnd = time + routingCosts.getTransportTime(newLocation, vehicle.getEndLocation(), time, iFacts.getNewDriver(), vehicle);
                return arrTimeAtEnd > vehicle.getLatestArrival() ? ConstraintsStatus.NOT_FULFILLED : ConstraintsStatus.FULFILLED;
            }
            act = acts.get(i);
            if (!(act instanceof ActWithoutStaticLocation)) break;
        }
        double latestArrTimeAtAct = states.getActivityDouble(act, vehicle, InternalStates.LATEST_OPERATION_START_TIME);
        if (Double.isNaN(latestArrTimeAtAct)) latestArrTimeAtAct = act.getTheoreticalLatestOperationStartTime();
        double arrTimeAtAct = time + routingCosts.getTransportTime(newLocation, act.getLocation(), time, iFacts.getNewDriver(), vehicle);
        return arrTimeAtAct > latestArrTimeAtAct ? ConstraintsStatus.NOT_FULFILLED : ConstraintsStatus.FULFILLED;
    }

    /** position of nextAct in the route: the insertion index of the context when it points at it, otherwise a scan by identity */
    private static int indexOf(List<TourActivity> acts, TourActivity nextAct, ActivityContext context) {
        if (context != null) {
            int hint = context.getInsertionIndex();
            if (hint >= 0 && hint < acts.size() && acts.get(hint) == nextAct) return hint;
        }
        for (int i = 0; i < acts.size(); i++) {
            if (acts.get(i) == nextAct) return i;
        }
        return -1;
    }
}

