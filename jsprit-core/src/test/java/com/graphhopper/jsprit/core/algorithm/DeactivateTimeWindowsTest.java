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


import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

public class DeactivateTimeWindowsTest {

    VehicleRoutingProblem vrp;

    @Before
    public void doBefore(){
        Service service = Service.Builder.newInstance("s").setLocation(Location.newInstance(20, 0))
            .setTimeWindow(TimeWindow.newInstance(40, 50)).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addVehicle(vehicle).build();

    }

    @Test
    public void activityTimesShouldConsiderTimeWindows() {
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp); //this should ignore any constraints
        vra.setMaxIterations(10);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        VehicleRoute route = Solutions.bestOf(solutions).getRoutes().iterator().next();
        Assert.assertEquals(40., route.getActivities().get(0).getEndTime(), 0.01);
    }

    @Test
    public void whenActivatingViaStateManager_activityTimesShouldConsiderTimeWindows() {
        StateManager stateManager = new StateManager(vrp);
        stateManager.updateTimeWindowStates();
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addTimeWindowConstraint();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).addCoreStateAndConstraintStuff(true)
            .setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        vra.setMaxIterations(10);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        VehicleRoute route = Solutions.bestOf(solutions).getRoutes().iterator().next();
        Assert.assertEquals(40., route.getActivities().get(0).getEndTime(), 0.01);
    }
}
