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

import com.graphhopper.jsprit.core.algorithm.box.GreedySchrimpfFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.UnassignedJobReasonTracker;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UnassignedJobListTest {


    @Test
    public void job2ShouldBeInBadJobList_dueToTimeWindow() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.addVehicle(VehicleImpl.Builder.newInstance("v1").setEarliestStart(0).setLatestArrival(12).setStartLocation(Location.newInstance(1, 1)).build());
        Service job1 = Service.Builder.newInstance("job1").setLocation(Location.newInstance(0, 0)).setTimeWindow(TimeWindow.newInstance(0, 12)).setServiceTime(1).build();
        builder.addJob(job1);
        Service job2 = Service.Builder.newInstance("job2").setLocation(Location.newInstance(2, 2)).setTimeWindow(TimeWindow.newInstance(12, 24)).setServiceTime(1).build();
        builder.addJob(job2);

        VehicleRoutingProblem vrp = builder.build();
        VehicleRoutingAlgorithm algorithm = new GreedySchrimpfFactory().createAlgorithm(vrp);
        algorithm.setMaxIterations(10);

        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        algorithm.addListener(reasonTracker);

        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        assertTrue(!solution.getUnassignedJobs().contains(job1));
        assertTrue(solution.getUnassignedJobs().contains(job2));
        assertEquals(2, reasonTracker.getMostLikelyReasonCode("job2"));
    }

    @Test
    public void job2ShouldBeInBadJobList_dueToSize() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.addVehicle(VehicleImpl.Builder.newInstance("v1").setEarliestStart(0).setLatestArrival(12).setStartLocation(Location.newInstance(1, 1)).build());
        Service job1 = Service.Builder.newInstance("job1").setLocation(Location.newInstance(0, 0)).setTimeWindow(TimeWindow.newInstance(0, 12)).setServiceTime(1).build();
        builder.addJob(job1);
        Service job2 = Service.Builder.newInstance("job2").setLocation(Location.newInstance(2, 2)).addSizeDimension(0, 10).setTimeWindow(TimeWindow.newInstance(0, 12)).setServiceTime(1).build();
        builder.addJob(job2);

        VehicleRoutingProblem vrp = builder.build();

        VehicleRoutingAlgorithm algorithm = new GreedySchrimpfFactory().createAlgorithm(vrp);
        algorithm.setMaxIterations(10);

        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        algorithm.addListener(reasonTracker);

        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        assertTrue(!solution.getUnassignedJobs().contains(job1));
        assertTrue(solution.getUnassignedJobs().contains(job2));
        assertEquals(3, reasonTracker.getMostLikelyReasonCode("job2"));
    }

}
