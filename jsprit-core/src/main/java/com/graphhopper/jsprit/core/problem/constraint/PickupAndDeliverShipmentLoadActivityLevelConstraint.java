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
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;


/**
 * Constraint that ensures capacity constraint at each activity.
 * <p>
 * <p>This is critical to consistently calculate pd-problems with capacity constraints. Critical means
 * that is MUST be visited. It also assumes that pd-activities are visited in the order they occur in a tour.
 *
 * @author schroeder
 */
public class PickupAndDeliverShipmentLoadActivityLevelConstraint implements HardActivityConstraint {

    private RouteAndActivityStateGetter stateManager;

    private Capacity defaultValue;

    /**
     * Constructs the constraint ensuring capacity constraint at each activity.
     * <p>
     * <p>This is critical to consistently calculate pd-problems with capacity constraints. Critical means
     * that is MUST be visited. It also assumes that pd-activities are visited in the order they occur in a tour.
     *
     * @param stateManager the stateManager
     */
    public PickupAndDeliverShipmentLoadActivityLevelConstraint(RouteAndActivityStateGetter stateManager) {
        super();
        this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
    }

    /**
     * Checks whether there is enough capacity to insert newAct between prevAct and nextAct.
     */
    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if (!(newAct instanceof PickupShipment) && !(newAct instanceof DeliverShipment)) {
            return ConstraintsStatus.FULFILLED;
        }
        Capacity loadAtPrevAct;
        if (prevAct instanceof Start) {
            loadAtPrevAct = stateManager.getRouteState(iFacts.getRoute(), InternalStates.LOAD_AT_BEGINNING, Capacity.class);
            if (loadAtPrevAct == null) loadAtPrevAct = defaultValue;
        } else {
            loadAtPrevAct = stateManager.getActivityState(prevAct, InternalStates.LOAD, Capacity.class);
            if (loadAtPrevAct == null) loadAtPrevAct = defaultValue;
        }
        if (newAct instanceof PickupShipment) {
            if (!Capacity.addup(loadAtPrevAct, newAct.getSize()).isLessOrEqual(iFacts.getNewVehicle().getType().getCapacityDimensions())) {
                return ConstraintsStatus.NOT_FULFILLED;
            }
        }
        if (newAct instanceof DeliverShipment) {
            if (!Capacity.addup(loadAtPrevAct, Capacity.invert(newAct.getSize())).isLessOrEqual(iFacts.getNewVehicle().getType().getCapacityDimensions()))
                return ConstraintsStatus.NOT_FULFILLED_BREAK;
        }
        return ConstraintsStatus.FULFILLED;
    }


}
