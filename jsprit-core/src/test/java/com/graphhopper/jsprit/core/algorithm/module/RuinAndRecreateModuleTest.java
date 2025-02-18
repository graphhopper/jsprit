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
package com.graphhopper.jsprit.core.algorithm.module;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionStrategy;
import com.graphhopper.jsprit.core.algorithm.ruin.RuinStrategy;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Ruin And Recreate Module Test")
class RuinAndRecreateModuleTest {

    @Test
    @DisplayName("Initial Num Of Unassigned Should Work Correctly")
    void initialNumOfUnassignedShouldWorkCorrectly() {
        InsertionStrategy insertionStrategy = mock(InsertionStrategy.class);
        RuinStrategy ruinStrategy = mock(RuinStrategy.class);
        RuinAndRecreateModule module = new RuinAndRecreateModule("name", insertionStrategy, ruinStrategy);
        Collection<VehicleRoute> routes = new ArrayList<>();
        List<Job> unassigned = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Job mockedJob = mock(Job.class);
            when(mockedJob.getId()).thenReturn(String.valueOf(i));
            unassigned.add(mockedJob);
        }
        VehicleRoutingProblemSolution previousSolution = new VehicleRoutingProblemSolution(routes, unassigned, 0);
        VehicleRoutingProblemSolution newSolution = module.runAndGetSolution(previousSolution);
        Assertions.assertEquals(0, newSolution.getUnassignedJobs().size());
    }

    @Test
    @DisplayName("Proportion Of Unassigned Should Work Correctly")
    void proportionOfUnassignedShouldWorkCorrectly() {
        InsertionStrategy insertionStrategy = mock(InsertionStrategy.class);
        RuinStrategy ruinStrategy = mock(RuinStrategy.class);
        RuinAndRecreateModule module = new RuinAndRecreateModule("name", insertionStrategy, ruinStrategy);
        module.setMinUnassignedJobsToBeReinserted(5);
        module.setProportionOfUnassignedJobsToBeReinserted(0.01);
        Collection<VehicleRoute> routes = new ArrayList<>();
        List<Job> unassigned = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Job mockedJob = mock(Job.class);
            when(mockedJob.getId()).thenReturn(String.valueOf(i));
            unassigned.add(mockedJob);
        }
        VehicleRoutingProblemSolution previousSolution = new VehicleRoutingProblemSolution(routes, unassigned, 0);
        VehicleRoutingProblemSolution newSolution = module.runAndGetSolution(previousSolution);
        Assertions.assertEquals(15, newSolution.getUnassignedJobs().size());
    }

    @Test
    @DisplayName("Proportion Of Unassigned Should Work Correctly 2")
    void proportionOfUnassignedShouldWorkCorrectly2() {
        InsertionStrategy insertionStrategy = mock(InsertionStrategy.class);
        RuinStrategy ruinStrategy = mock(RuinStrategy.class);
        RuinAndRecreateModule module = new RuinAndRecreateModule("name", insertionStrategy, ruinStrategy);
        module.setMinUnassignedJobsToBeReinserted(5);
        module.setProportionOfUnassignedJobsToBeReinserted(0.5);
        Collection<VehicleRoute> routes = new ArrayList<>();
        List<Job> unassigned = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Job mockedJob = mock(Job.class);
            when(mockedJob.getId()).thenReturn(String.valueOf(i));
            unassigned.add(mockedJob);
        }
        VehicleRoutingProblemSolution previousSolution = new VehicleRoutingProblemSolution(routes, unassigned, 0);
        VehicleRoutingProblemSolution newSolution = module.runAndGetSolution(previousSolution);
        Assertions.assertEquals(10, newSolution.getUnassignedJobs().size());
    }
}
