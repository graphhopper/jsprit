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

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateMaxTimeInVehicle;
import com.graphhopper.jsprit.core.algorithm.state.UpdateVehicleDependentPracticalTimeWindows;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by schroeder on 15/09/16.
 */
public class MaxTimeInVehicleConstraint implements HardActivityConstraint {

    private final VehicleRoutingProblem vrp;

    private final TransportTime transportTime;

    private final VehicleRoutingActivityCosts activityCosts;

    private final StateId latestStartId;

    private final StateManager stateManager;

    public MaxTimeInVehicleConstraint(TransportTime transportTime, VehicleRoutingActivityCosts activityCosts, StateId latestStartId, StateManager stateManager, VehicleRoutingProblem vrp) {
        this.transportTime = transportTime;
        this.latestStartId = latestStartId;
        this.stateManager = stateManager;
        this.activityCosts = activityCosts;
        this.vrp = vrp;
    }

    @Override
    public ConstraintsStatus fulfilled(final JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        boolean newActIsPickup = newAct instanceof PickupActivity;
        boolean newActIsDelivery = newAct instanceof DeliveryActivity;

        /*
        1. check whether insertion of new shipment satisfies own max-in-vehicle-constraint
        2. check whether insertion of new shipment satisfies all other max-in-vehicle-constraints
         */
        //************ 1. check whether insertion of new shipment satisfies own max-in-vehicle-constraint
        double newActArrival = prevActDepTime + transportTime.getTransportTime(prevAct.getLocation(),newAct.getLocation(),prevActDepTime,iFacts.getNewDriver(),iFacts.getNewVehicle());
        double newActStart = Math.max(newActArrival, newAct.getTheoreticalEarliestOperationStartTime());
        double newActDeparture = newActStart + activityCosts.getActivityDuration(prevAct, newAct, newActArrival, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double nextActArrival = newActDeparture + transportTime.getTransportTime(newAct.getLocation(),nextAct.getLocation(),newActDeparture,iFacts.getNewDriver(),iFacts.getNewVehicle());
        double nextActStart = Math.max(nextActArrival,nextAct.getTheoreticalEarliestOperationStartTime());
        if(newAct instanceof DeliveryActivity){
            double pickupEnd;
            if(iFacts.getAssociatedActivities().size() == 1){
                pickupEnd = iFacts.getNewDepTime();
            }
            else {
                pickupEnd = iFacts.getRelatedActivityContext().getEndTime();
            }
            double timeInVehicle = newActStart - pickupEnd;
            double maxTimeInVehicle = ((TourActivity.JobActivity)newAct).getJob().getMaxTimeInVehicle();
            if(timeInVehicle > maxTimeInVehicle) return ConstraintsStatus.NOT_FULFILLED;

        }
        else if(newActIsPickup){
            if(iFacts.getAssociatedActivities().size() == 1){
                double maxTimeInVehicle = ((TourActivity.JobActivity)newAct).getJob().getMaxTimeInVehicle();
                //ToDo - estimate in vehicle time of pickups here - This seems to trickier than I thought
                double nextActDeparture = nextActStart + activityCosts.getActivityDuration(prevAct, nextAct, nextActArrival, iFacts.getNewDriver(), iFacts.getNewVehicle());
//                if(!nextAct instanceof End)
                double timeToEnd = 0; //newAct.end + tt(newAct,nextAct) + t@nextAct + t_to_end
                if(timeToEnd > maxTimeInVehicle) return ConstraintsStatus.NOT_FULFILLED;
            }
        }

        //************ 2. check whether insertion of new shipment satisfies all other max-in-vehicle-constraints

        if(newActIsPickup || iFacts.getAssociatedActivities().size() == 1) {
            double latest;
            if (iFacts.getRoute().isEmpty()) latest = Double.MAX_VALUE;
            else if (nextAct instanceof End) {
                latest = stateManager.getRouteState(iFacts.getRoute(), iFacts.getNewVehicle(), latestStartId, Double.class);
            } else latest = stateManager.getActivityState(nextAct, iFacts.getNewVehicle(), latestStartId, Double.class);

            if (nextActStart > latest) {
                return ConstraintsStatus.NOT_FULFILLED;
            }

        } else {
            boolean isShipment = iFacts.getAssociatedActivities().size() == 2;
            if (newActIsDelivery && isShipment) {
                StateManager localStateManager = new StateManager(vrp);
                StateId stateId = localStateManager.createStateId("local-slack");
                UpdateMaxTimeInVehicle updateMaxTimeInVehicle = new UpdateMaxTimeInVehicle(localStateManager, stateId, transportTime, activityCosts);
                updateMaxTimeInVehicle.setVehiclesToUpdate(new UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate() {
                    @Override
                    public Collection<Vehicle> get(VehicleRoute route) {
                        return Arrays.asList(iFacts.getNewVehicle());
                    }
                });
                updateMaxTimeInVehicle.begin(iFacts.getRoute());
                List<TourActivity> tourActivities = new ArrayList<>(iFacts.getRoute().getActivities());
                tourActivities.add(iFacts.getRelatedActivityContext().getInsertionIndex(), iFacts.getAssociatedActivities().get(0));
                for (TourActivity act : tourActivities) {
                    updateMaxTimeInVehicle.visit(act);
                }
                updateMaxTimeInVehicle.finish(tourActivities, iFacts.getJob());

                double latest;
                if (iFacts.getRoute().isEmpty()) latest = Double.MAX_VALUE;
                else if (nextAct instanceof End) {
                    latest = localStateManager.getRouteState(iFacts.getRoute(), iFacts.getNewVehicle(), stateId, Double.class);
                } else
                    latest = localStateManager.getActivityState(nextAct, iFacts.getNewVehicle(), stateId, Double.class);

                if (nextActStart > latest) {
                    return ConstraintsStatus.NOT_FULFILLED;
                }

            }
        }
        return ConstraintsStatus.FULFILLED;
    }

//    private double getMaxTime(String jobId) {
//        if(maxTimes.containsKey(jobId)) return maxTimes.get(jobId);
//        else return Double.MAX_VALUE;
//    }
}
