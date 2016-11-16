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

package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.state.*;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.SwitchNotFeasible;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;
import com.graphhopper.jsprit.core.util.ActivityTimeTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by schroeder on 02/08/16.
 */
public class AlgorithmUtil {

    public static void addCoreConstraints(ConstraintManager constraintManager, StateManager stateManager, final VehicleRoutingProblem vrp) {
        constraintManager.addTimeWindowConstraint();
        constraintManager.addLoadConstraint();
        constraintManager.addSkillsConstraint();
        constraintManager.addConstraint(new SwitchNotFeasible(stateManager));
        stateManager.updateLoadStates();
        stateManager.updateTimeWindowStates();
        UpdateVehicleDependentPracticalTimeWindows twUpdater = new UpdateVehicleDependentPracticalTimeWindows(stateManager, vrp.getTransportCosts(), vrp.getActivityCosts());
        twUpdater.setVehiclesToUpdate(new UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate() {

            Map<VehicleTypeKey, Vehicle> uniqueTypes = new HashMap<VehicleTypeKey, Vehicle>();

            @Override
            public Collection<Vehicle> get(VehicleRoute vehicleRoute) {
                if (uniqueTypes.isEmpty()) {
                    for (Vehicle v : vrp.getVehicles()) {
                        if (!uniqueTypes.containsKey(v.getVehicleTypeIdentifier())) {
                            uniqueTypes.put(v.getVehicleTypeIdentifier(), v);
                        }
                    }
                }
                Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
                vehicles.addAll(uniqueTypes.values());
                return vehicles;
            }

        });
        stateManager.addStateUpdater(new UpdateEndLocationIfRouteIsOpen());
        stateManager.addStateUpdater(twUpdater);
        stateManager.updateSkillStates();

        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS, vrp.getActivityCosts()));
        stateManager.addStateUpdater(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));
        stateManager.addStateUpdater(new UpdateFutureWaitingTimes(stateManager, vrp.getTransportCosts()));
    }


}
