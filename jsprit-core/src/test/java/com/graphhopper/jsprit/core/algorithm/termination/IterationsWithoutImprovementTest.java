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

package com.graphhopper.jsprit.core.algorithm.termination;


import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IterationsWithoutImprovementTest {

    @Test
    public void itShouldTerminateAfter100() {
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(100);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        when(discoveredSolution.isAccepted()).thenReturn(false);
        int terminatedAfter = 0;
        for (int i = 0; i < 200; i++) {
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(100, terminatedAfter);
    }

    @Test
    public void itShouldTerminateAfter1() {
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(1);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        when(discoveredSolution.isAccepted()).thenReturn(false);
        int terminatedAfter = 0;
        for (int i = 0; i < 200; i++) {
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(1, terminatedAfter);
    }

    @Test
    public void itShouldTerminateAfter150() {
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(100);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        int terminatedAfter = 0;
        for (int i = 0; i < 200; i++) {
            when(discoveredSolution.isAccepted()).thenReturn(false);
            if (i == 49) when(discoveredSolution.isAccepted()).thenReturn(true);
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(150, terminatedAfter);
    }

    @Test
    public void isPrematureBreakZeroPercentage() {
        int maxIterations = 200;
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(100, 0);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);

        int terminatedAfter = maxIterations;
        for (int i = 0; i < maxIterations; i++) {
            when(discoveredSolution.isAccepted()).thenReturn(i< 50 ? true : false);
            if (termination.isPrematureBreak(discoveredSolution)) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(150, terminatedAfter);
    }

    @Test
    public void isPrematureBreakWithPercentageShouldBreak() {
        int maxIterations = 200;
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(10, 1.0);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        VehicleRoutingProblemSolution solution = mock(VehicleRoutingProblemSolution.class);
        when(discoveredSolution.getSolution()).thenReturn(solution);
        when(solution.getUnassignedJobs()).thenReturn(new ArrayList<Job>());

        int terminatedAfter = maxIterations;
        for (int i = 0; i < maxIterations; i++) {

            when(solution.getCost()).thenReturn(i < 100 ? 100.0 - (0.1*i) : 40-((i-100)*0.01));
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(110, terminatedAfter);
    }

    @Test
    public void isPrematureBreakWithPercentageCostNotImprovedButUnassignedImproved() {
        int maxIterations = 200;
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(10, 1.0);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        VehicleRoutingProblemSolution solution = mock(VehicleRoutingProblemSolution.class);
        Job job = mock(Job.class);
        when(discoveredSolution.getSolution()).thenReturn(solution);
        List<Job> unassignedJobs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            unassignedJobs.add(job);
        }

        int terminatedAfter = maxIterations;
        for (int i = 0; i < maxIterations; i++) {
            when(solution.getCost()).thenReturn(100.0);
            if (i <= 50){
                unassignedJobs.remove(0);
            }
            when(solution.getUnassignedJobs()).thenReturn(unassignedJobs);
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                terminatedAfter = i;
                break;
            }
        }
        Assert.assertEquals(60, terminatedAfter);
    }

    @Test
    public void isPrematureBreakLastCostIsWorstShouldNotBreak() {
        int maxIterations = 7;
        IterationWithoutImprovementTermination termination = new IterationWithoutImprovementTermination(5, 1.0);
        SearchStrategy.DiscoveredSolution discoveredSolution = mock(SearchStrategy.DiscoveredSolution.class);
        VehicleRoutingProblemSolution solution = mock(VehicleRoutingProblemSolution.class);
        when(discoveredSolution.getSolution()).thenReturn(solution);
        when(solution.getUnassignedJobs()).thenReturn(new ArrayList<Job>());

        boolean isTerminate = false;
        for (int i = 0; i < maxIterations; i++) {

            when(solution.getCost()).thenReturn(i < 6.0 ? 10.0-i : 12);
            boolean terminate = termination.isPrematureBreak(discoveredSolution);
            if (terminate) {
                isTerminate= true;
                break;
            }
        }
        Assert.assertFalse(isTerminate);
    }
}
