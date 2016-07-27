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
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;


/**
 * Ensures load constraint for inserting ServiceActivity.
 * <p>
 * <p>When using this, you need to use<br>
 *
 * @author schroeder
 */
public class ServiceLoadActivityLevelConstraint implements HardActivityConstraint {

    private RouteAndActivityStateGetter stateManager;

    private Capacity defaultValue;

    public ServiceLoadActivityLevelConstraint(RouteAndActivityStateGetter stateManager) {
        super();
        this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        Capacity futureMaxLoad;
        Capacity prevMaxLoad;
        if (prevAct instanceof Start) {
            futureMaxLoad = stateManager.getRouteState(iFacts.getRoute(), InternalStates.MAXLOAD, Capacity.class);
            if (futureMaxLoad == null) futureMaxLoad = defaultValue;
            prevMaxLoad = stateManager.getRouteState(iFacts.getRoute(), InternalStates.LOAD_AT_BEGINNING, Capacity.class);
            if (prevMaxLoad == null) prevMaxLoad = defaultValue;
        } else {
            futureMaxLoad = stateManager.getActivityState(prevAct, InternalStates.FUTURE_MAXLOAD, Capacity.class);
            if (futureMaxLoad == null) futureMaxLoad = defaultValue;
            prevMaxLoad = stateManager.getActivityState(prevAct, InternalStates.PAST_MAXLOAD, Capacity.class);
            if (prevMaxLoad == null) prevMaxLoad = defaultValue;

        }
        if (newAct instanceof PickupService || newAct instanceof ServiceActivity) {
            if (!Capacity.addup(newAct.getSize(), futureMaxLoad).isLessOrEqual(iFacts.getNewVehicle().getType().getCapacityDimensions())) {
                return ConstraintsStatus.NOT_FULFILLED;
            }
        }
        if (newAct instanceof DeliverService) {
            if (!Capacity.addup(Capacity.invert(newAct.getSize()), prevMaxLoad).isLessOrEqual(iFacts.getNewVehicle().getType().getCapacityDimensions())) {
                return ConstraintsStatus.NOT_FULFILLED_BREAK;
            }
        }
        return ConstraintsStatus.FULFILLED;
    }
}
