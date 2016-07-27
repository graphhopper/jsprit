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
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

/**
 * Ensures that capacity constraint is met, i.e. that current load plus
 * new job size does not exceeds capacity of new vehicle.
 * <p>
 * <p>If job is neither Pickup, Delivery nor Service, it returns true.
 *
 * @author stefan
 */
public class ServiceLoadRouteLevelConstraint implements HardRouteConstraint {

    private RouteAndActivityStateGetter stateManager;

    private Capacity defaultValue;

    public ServiceLoadRouteLevelConstraint(RouteAndActivityStateGetter stateManager) {
        super();
        this.stateManager = stateManager;
        this.defaultValue = Capacity.Builder.newInstance().build();
    }

    @Override
    public boolean fulfilled(JobInsertionContext insertionContext) {
        Capacity maxLoadAtRoute = stateManager.getRouteState(insertionContext.getRoute(), InternalStates.MAXLOAD, Capacity.class);
        if (maxLoadAtRoute == null) maxLoadAtRoute = defaultValue;
        Capacity capacityDimensions = insertionContext.getNewVehicle().getType().getCapacityDimensions();
        if (!maxLoadAtRoute.isLessOrEqual(capacityDimensions)) {
            return false;
        }
        if (insertionContext.getJob() instanceof Delivery) {
            Capacity loadAtDepot = stateManager.getRouteState(insertionContext.getRoute(), InternalStates.LOAD_AT_BEGINNING, Capacity.class);
            if (loadAtDepot == null) loadAtDepot = defaultValue;
            if (!Capacity.addup(loadAtDepot, insertionContext.getJob().getSize()).isLessOrEqual(capacityDimensions)) {
                return false;
            }
        } else if (insertionContext.getJob() instanceof Pickup || insertionContext.getJob() instanceof Service) {
            Capacity loadAtEnd = stateManager.getRouteState(insertionContext.getRoute(), InternalStates.LOAD_AT_END, Capacity.class);
            if (loadAtEnd == null) loadAtEnd = defaultValue;
            if (!Capacity.addup(loadAtEnd, insertionContext.getJob().getSize()).isLessOrEqual(capacityDimensions)) {
                return false;
            }
        }
        return true;
    }

}
