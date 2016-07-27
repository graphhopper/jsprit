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
import com.graphhopper.jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * A {@link com.graphhopper.jsprit.core.problem.solution.route.activity.ReverseActivityVisitor} that looks forward in the vehicle route and determines
 * the maximum capacity utilization (in terms of loads) at subsequent activities.
 * <p>
 * <p>Assume a vehicle route with the following activity sequence {start,pickup(1,4),delivery(2,3),pickup(3,2),end} where
 * pickup(1,2) = pickup(id,cap-demand).<br>
 * Future maxLoad for each activity are calculated as follows:<br>
 * loadAt(end)=6 (since two pickups need to be delivered to depot)<br>
 * pickup(3)=max(loadAt(pickup(3)), futureMaxLoad(end))=max(6,6)=6
 * delivery(2)=max(loadAt(delivery(2),futureMaxLoad(pickup(3))=max(4,6)=6
 * pickup(1)=max(7,6)=7
 * start=max(7,7)=7
 * activity (apart from start and end), the maximum capacity is determined when forward looking into the route.
 * That is at each activity we know how much capacity is available whithout breaking future capacity constraints.
 *
 * @author schroeder
 */
class UpdateMaxCapacityUtilisationAtActivitiesByLookingForwardInRoute implements ReverseActivityVisitor, StateUpdater {

    private StateManager stateManager;

    private VehicleRoute route;

    private Capacity maxLoad;

    private Capacity defaultValue;

    public UpdateMaxCapacityUtilisationAtActivitiesByLookingForwardInRoute(StateManager stateManager) {
        super();
        this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
        maxLoad = stateManager.getRouteState(route, InternalStates.LOAD_AT_END, Capacity.class);
        if (maxLoad == null) maxLoad = defaultValue;
    }

    @Override
    public void visit(TourActivity act) {
        maxLoad = Capacity.max(maxLoad, stateManager.getActivityState(act, InternalStates.LOAD, Capacity.class));
        stateManager.putInternalTypedActivityState(act, InternalStates.FUTURE_MAXLOAD, maxLoad);
//		assert maxLoad.isLessOrEqual(route.getVehicle().getType().getCapacityDimensions()) : "maxLoad can in every capacity dimension never be bigger than vehicleCap";
//		assert maxLoad.isGreaterOrEqual(Capacity.Builder.newInstance().build()) : "maxLoad can never be smaller than 0";
    }

    @Override
    public void finish() {
    }
}
