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
package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Determines and memorizes the maximum capacity utilization at each activity by looking backward in route,
 * i.e. the maximum capacity utilization at previous activities.
 *
 * @author schroeder
 */
class UpdateMaxCapacityUtilisationAtActivitiesByLookingBackwardInRoute implements ActivityVisitor, StateUpdater {

    private StateManager stateManager;

    private VehicleRoute route;

    private Capacity maxLoad;

    private Capacity defaultValue;

    public UpdateMaxCapacityUtilisationAtActivitiesByLookingBackwardInRoute(StateManager stateManager) {
        this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
        maxLoad = stateManager.getRouteState(route, InternalStates.LOAD_AT_BEGINNING, Capacity.class);
        if (maxLoad == null) maxLoad = defaultValue;
    }

    @Override
    public void visit(TourActivity act) {
        maxLoad = Capacity.max(maxLoad, stateManager.getActivityState(act, InternalStates.LOAD, Capacity.class));
        stateManager.putInternalTypedActivityState(act, InternalStates.PAST_MAXLOAD, maxLoad);
//		assert maxLoad.isGreaterOrEqual(Capacity.Builder.newInstance().build()) : "maxLoad can never be smaller than 0";
//		assert maxLoad.isLessOrEqual(route.getVehicle().getType().getCapacityDimensions()) : "maxLoad can never be bigger than vehicleCap";
    }

    @Override
    public void finish() {
    }
}
