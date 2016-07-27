/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

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
