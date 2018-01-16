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

package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.MaxDistanceConstraint;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.apache.commons.math3.stat.Frequency;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by schroeder on 06/02/17.
 */
public class UnassignedJobReasonTrackerTest {

    Vehicle vehicle;

    @Before
    public void doBefore() {
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0, 1);
        VehicleType vehicleType = vehicleTypeBuilder.build();
        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder.setType(vehicleType);
        vehicleBuilder.setEarliestStart(0).setLatestArrival(100);
        vehicle = vehicleBuilder.build();
    }

    @Test
    public void shouldReturnCorrectCapacityReasonCode() {
        Service service = Service.Builder.newInstance("1").addSizeDimension(0, 5).setLocation(Location.newInstance(5, 7)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addVehicle(vehicle).addJob(service)
            .build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        vra.addListener(reasonTracker);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(1, solution.getUnassignedJobs().size());
        Assert.assertEquals(3, reasonTracker.getMostLikelyReasonCode(solution.getUnassignedJobs().iterator().next().getId()));
    }

    @Test
    public void shouldReturnCorrectSkillReasonCode() {
        Service service = Service.Builder.newInstance("1").addSizeDimension(0, 1).addRequiredSkill("ice").setLocation(Location.newInstance(5, 7)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addVehicle(vehicle).addJob(service)
            .build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        vra.addListener(reasonTracker);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(1, solution.getUnassignedJobs().size());
        Assert.assertEquals(1, reasonTracker.getMostLikelyReasonCode(solution.getUnassignedJobs().iterator().next().getId()));
    }

    @Test
    public void shouldReturnCorrectTWReasonCode() {
        Service service = Service.Builder.newInstance("1").addSizeDimension(0, 1).setTimeWindow(TimeWindow.newInstance(110, 200)).setLocation(Location.newInstance(5, 7)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addVehicle(vehicle).addJob(service)
            .build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        vra.addListener(reasonTracker);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(1, solution.getUnassignedJobs().size());
        Assert.assertEquals(2, reasonTracker.getMostLikelyReasonCode(solution.getUnassignedJobs().iterator().next().getId()));
    }

    @Test
    public void shouldReturnCorrectMaxDistanceReasonCode() {
        Service service = Service.Builder.newInstance("1").setLocation(Location.newInstance(51, 0)).build();

        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();

        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addVehicle(vehicle).addJob(service).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        StateId maxDistance = stateManager.createStateId("max-distance");
        Map<Vehicle, Double> distMap = new HashMap<>();
        distMap.put(vehicle, 100d);
        MaxDistanceConstraint distanceConstraint = new MaxDistanceConstraint(stateManager, maxDistance, vrp.getTransportCosts(), distMap);
        constraintManager.addConstraint(distanceConstraint, ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager, constraintManager)
            .buildAlgorithm();
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        vra.addListener(reasonTracker);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(1, solution.getUnassignedJobs().size());
        Assert.assertEquals(4, reasonTracker.getMostLikelyReasonCode(solution.getUnassignedJobs().iterator().next().getId()));
    }

    @Test
    public void getMostLikelyTest() {
        Frequency frequency = new Frequency();
        frequency.addValue("a");
        frequency.addValue("b");
        frequency.addValue("a");
        frequency.addValue("a");
        frequency.addValue("a");
        frequency.addValue("a");
        frequency.addValue("a");
        Assert.assertEquals("a", UnassignedJobReasonTracker.getMostLikelyFailedConstraintName(frequency));
    }

    @Test
    public void testFreq() {
        Frequency frequency = new Frequency();
        frequency.addValue("VehicleDependentTimeWindowHardActivityConstraint");
        frequency.addValue("b");
        frequency.addValue("VehicleDependentTimeWindowHardActivityConstraint");

        Iterator<Map.Entry<Comparable<?>, Long>> entryIterator = frequency.entrySetIterator();
        while (entryIterator.hasNext()) {
            Map.Entry<Comparable<?>, Long> e = entryIterator.next();
            System.out.println(e.getKey().toString() + " " + e.getValue());
        }
    }
}
