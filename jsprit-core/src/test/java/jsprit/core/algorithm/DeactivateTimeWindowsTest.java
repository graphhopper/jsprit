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

package jsprit.core.algorithm;


import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.Solutions;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class DeactivateTimeWindowsTest {

    @Test
    public void activityTimesShouldIgnoreTimeWindows() {
        Service service = Service.Builder.newInstance("s").setLocation(Location.newInstance(20, 0))
            .setTimeWindow(TimeWindow.newInstance(40, 50)).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addVehicle(vehicle).build();
        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(vrp, "src/test/resources/algorithmConfig.xml");
        vraBuilder.addDefaultCostCalculators();
        VehicleRoutingAlgorithm vra = vraBuilder.build(); //this should ignore any constraints
        vra.setMaxIterations(10);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        VehicleRoute route = Solutions.bestOf(solutions).getRoutes().iterator().next();
        Assert.assertEquals(20., route.getActivities().get(0).getEndTime(), 0.01);
    }

    @Test
    public void whenNotActivatingViaStateManager_activityTimesShouldConsiderTimeWindows() {
        Service service = Service.Builder.newInstance("s").setLocation(Location.newInstance(20, 0))
            .setTimeWindow(TimeWindow.newInstance(40, 50)).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addVehicle(vehicle).build();
        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(vrp, "src/test/resources/algorithmConfig.xml");
        vraBuilder.addDefaultCostCalculators();
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        vraBuilder.setStateAndConstraintManager(stateManager, constraintManager);
        VehicleRoutingAlgorithm vra = vraBuilder.build(); //this should ignore any constraints
        vra.setMaxIterations(10);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        VehicleRoute route = Solutions.bestOf(solutions).getRoutes().iterator().next();
        Assert.assertEquals(20., route.getActivities().get(0).getEndTime(), 0.01);
    }

    @Test
    public void activityTimesShouldConsiderTimeWindows() {
        Service service = Service.Builder.newInstance("s").setLocation(Location.newInstance(20, 0))
            .setTimeWindow(TimeWindow.newInstance(40, 50)).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addVehicle(vehicle).build();
        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(vrp, "src/test/resources/algorithmConfig.xml");
        vraBuilder.addCoreConstraints();
        vraBuilder.addDefaultCostCalculators();
        VehicleRoutingAlgorithm vra = vraBuilder.build(); //this should ignore any constraints
        vra.setMaxIterations(10);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        VehicleRoute route = Solutions.bestOf(solutions).getRoutes().iterator().next();
        Assert.assertEquals(40., route.getActivities().get(0).getEndTime(), 0.01);
    }

    @Test
    public void whenActivatingViaStateManager_activityTimesShouldConsiderTimeWindows() {
        Service service = Service.Builder.newInstance("s").setLocation(Location.newInstance(20, 0))
            .setTimeWindow(TimeWindow.newInstance(40, 50)).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addVehicle(vehicle).build();
        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(vrp, "src/test/resources/algorithmConfig.xml");
        vraBuilder.addDefaultCostCalculators();
        StateManager stateManager = new StateManager(vrp);
        stateManager.updateTimeWindowStates();
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addTimeWindowConstraint();
        vraBuilder.setStateAndConstraintManager(stateManager, constraintManager);
        VehicleRoutingAlgorithm vra = vraBuilder.build(); //this should ignore any constraints
        vra.setMaxIterations(10);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        VehicleRoute route = Solutions.bestOf(solutions).getRoutes().iterator().next();
        Assert.assertEquals(40., route.getActivities().get(0).getEndTime(), 0.01);
    }
}
