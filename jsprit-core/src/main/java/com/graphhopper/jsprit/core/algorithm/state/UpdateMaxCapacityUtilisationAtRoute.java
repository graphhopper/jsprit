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
 * Updates load at activity level.
 * <p>
 * <p>Note that this assumes that StateTypes.LOAD_AT_DEPOT is already updated, i.e. it starts by setting loadAtDepot to StateTypes.LOAD_AT_DEPOT.
 * If StateTypes.LOAD_AT_DEPOT is not set, it starts with 0 load at depot.
 * <p>
 * <p>Thus it DEPENDS on StateTypes.LOAD_AT_DEPOT
 *
 * @author stefan
 */
class UpdateMaxCapacityUtilisationAtRoute implements ActivityVisitor, StateUpdater {

    private StateManager stateManager;

    private Capacity currentLoad = Capacity.Builder.newInstance().build();

    private VehicleRoute route;

    private Capacity maxLoad;

    private Capacity defaultValue;

    public UpdateMaxCapacityUtilisationAtRoute(StateManager stateManager) {
        super();
        this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
    }

    @Override
    public void begin(VehicleRoute route) {
        currentLoad = stateManager.getRouteState(route, InternalStates.LOAD_AT_BEGINNING, Capacity.class);
        if (currentLoad == null) currentLoad = defaultValue;
        maxLoad = currentLoad;
        this.route = route;
    }

    @Override
    public void visit(TourActivity act) {
        currentLoad = Capacity.addup(currentLoad, act.getSize());
        maxLoad = Capacity.max(maxLoad, currentLoad);
    }

    @Override
    public void finish() {
        stateManager.putTypedInternalRouteState(route, InternalStates.MAXLOAD, maxLoad);
    }
}
