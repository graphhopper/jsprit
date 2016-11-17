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
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
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

    public ServiceLoadActivityLevelConstraint(RouteAndActivityStateGetter stateManager) {
        super();
        this.stateManager = stateManager;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        SizeDimension futureMaxLoad;
        SizeDimension prevMaxLoad;
        if (prevAct instanceof Start) {
            futureMaxLoad = stateManager.getRouteState(iFacts.getRoute(), InternalStates.MAXLOAD, SizeDimension.class);
            prevMaxLoad = stateManager.getRouteState(iFacts.getRoute(), InternalStates.LOAD_AT_BEGINNING, SizeDimension.class);
        } else {
            futureMaxLoad = stateManager.getActivityState(prevAct, InternalStates.FUTURE_MAXLOAD, SizeDimension.class);
            prevMaxLoad = stateManager.getActivityState(prevAct, InternalStates.PAST_MAXLOAD, SizeDimension.class);
        }
        futureMaxLoad = (futureMaxLoad != null) ? futureMaxLoad : SizeDimension.EMPTY;
        prevMaxLoad = (prevMaxLoad != null) ? prevMaxLoad : SizeDimension.EMPTY;
        SizeDimension capacityOfNewVehicle = iFacts.getNewVehicle().getType().getCapacityDimensions();
        if (!futureMaxLoad.add(newAct.getLoadChange().getPositiveDimensions()).isLessOrEqual(capacityOfNewVehicle)) {
            return ConstraintsStatus.NOT_FULFILLED;
        }
        if (!prevMaxLoad.add(newAct.getLoadChange().getNegativeDimensions().abs()).isLessOrEqual(capacityOfNewVehicle)) {
            return ConstraintsStatus.NOT_FULFILLED_BREAK;
        }
//        if(capacityOfNewVehicle)
//        if (newAct.getLoadChange().sign() != SizeDimensionSign.POSITIVE) {
//            if (!newAct.getLoadChange().abs().add(prevMaxLoad).isLessOrEqual(
//                capacityOfNewVehicle)) {
//                return ConstraintsStatus.NOT_FULFILLED_BREAK;
//            }
//        }
        return ConstraintsStatus.FULFILLED;
    }
}
