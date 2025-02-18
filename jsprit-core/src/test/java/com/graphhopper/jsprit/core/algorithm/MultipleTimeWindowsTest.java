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
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by schroeder on 26/05/15.
 */
@DisplayName("Multiple Time Windows Test")
class MultipleTimeWindowsTest {

    @Test
    @DisplayName("Service 2 Should Not Be Inserted")
    void service2ShouldNotBeInserted() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2").addTimeWindow(50., 60.).setLocation(Location.newInstance(20, 0)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 0)).setEarliestStart(0.).setLatestArrival(40).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s).addJob(s2).addVehicle(v).build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(100);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());
        assertEquals(1, solution.getUnassignedJobs().size());
    }

    @Test
    @DisplayName("Service 2 Should Be Inserted Into New Vehicle")
    void service2ShouldBeInsertedIntoNewVehicle() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10, 0)).addTimeWindow(5., 15.).build();
        Service s2 = Service.Builder.newInstance("s2").addTimeWindow(50., 60.).setLocation(Location.newInstance(20, 0)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 0)).setEarliestStart(0.).setLatestArrival(40).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(0, 0)).setEarliestStart(40.).setLatestArrival(80).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s).addJob(s2).addVehicle(v).addVehicle(v2).build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(100);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());
        assertEquals(0, solution.getUnassignedJobs().size());
        assertEquals(2, solution.getRoutes().size());
    }

    @Test
    @DisplayName("Service 2 Should Be Inserted")
    void service2ShouldBeInserted() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2").addTimeWindow(50., 60.).addTimeWindow(15., 25).setLocation(Location.newInstance(20, 0)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 0)).setEarliestStart(0.).setLatestArrival(40).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s).addJob(s2).addVehicle(v).build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(100);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());
        assertEquals(0, solution.getUnassignedJobs().size());
    }
}
