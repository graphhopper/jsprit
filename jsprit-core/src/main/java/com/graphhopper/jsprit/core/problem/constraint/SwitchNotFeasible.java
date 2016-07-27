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
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;

/**
 * Created by schroeder on 19/09/15.
 */
public class SwitchNotFeasible implements HardRouteConstraint {

    private StateManager stateManager;

    public SwitchNotFeasible(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public boolean fulfilled(JobInsertionContext insertionContext) {
        Boolean notFeasible = stateManager.getRouteState(insertionContext.getRoute(), insertionContext.getNewVehicle(), InternalStates.SWITCH_NOT_FEASIBLE, Boolean.class);
        if (notFeasible == null || insertionContext.getRoute().getVehicle().getVehicleTypeIdentifier().equals(insertionContext.getNewVehicle().getVehicleTypeIdentifier()))
            return true;
        else return !notFeasible;
    }

}
