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

import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.algorithm.termination.PrematureAlgorithmTermination;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Vehicle Routing Algorithm Test")
class VehicleRoutingAlgorithmTest {

    @Test
    @DisplayName("When Setting Iterations _ it Is Set Correctly")
    void whenSettingIterations_itIsSetCorrectly() {
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class), mock(SearchStrategyManager.class));
        algorithm.setMaxIterations(50);
        Assertions.assertEquals(50, algorithm.getMaxIterations());
    }

    @Test
    @DisplayName("When Setting Iterations With Max Iterations _ it Is Set Correctly")
    void whenSettingIterationsWithMaxIterations_itIsSetCorrectly() {
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class), mock(SearchStrategyManager.class));
        algorithm.setMaxIterations(50);
        Assertions.assertEquals(50, algorithm.getMaxIterations());
    }

    @DisplayName("Count Iterations")
    private static class CountIterations implements IterationStartsListener {

        private int countIterations = 0;

        @Override
        public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
            countIterations++;
        }

        public int getCountIterations() {
            return countIterations;
        }
    }

    @Test
    @DisplayName("When Setting Iterations With Max Iterations _ iter Are Executed Correctly")
    void whenSettingIterationsWithMaxIterations_iterAreExecutedCorrectly() {
        SearchStrategyManager stratManager = mock(SearchStrategyManager.class);
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class), stratManager);
        when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
        when(stratManager.getWeights()).thenReturn(Arrays.asList(1.0));
        algorithm.setMaxIterations(1000);
        CountIterations counter = new CountIterations();
        algorithm.addListener(counter);
        algorithm.searchSolutions();
        Assertions.assertEquals(1000, counter.getCountIterations());
    }

    @Test
    @DisplayName("When Setting Iterations _ iter Are Executed Correctly")
    void whenSettingIterations_iterAreExecutedCorrectly() {
        SearchStrategyManager stratManager = mock(SearchStrategyManager.class);
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class), stratManager);
        when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
        when(stratManager.getWeights()).thenReturn(Arrays.asList(1.0));
        algorithm.setMaxIterations(1000);
        CountIterations counter = new CountIterations();
        algorithm.addListener(counter);
        algorithm.searchSolutions();
        Assertions.assertEquals(1000, counter.getCountIterations());
    }

    @Test
    @DisplayName("When Setting Premature Termination _ it Is Executed Correctly")
    void whenSettingPrematureTermination_itIsExecutedCorrectly() {
        SearchStrategyManager stratManager = mock(SearchStrategyManager.class);
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class), stratManager);
        when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
        when(stratManager.getWeights()).thenReturn(Arrays.asList(1.0));
        algorithm.setMaxIterations(1000);
        PrematureAlgorithmTermination termination = new PrematureAlgorithmTermination() {

            private int nuOfIterations = 1;

            @Override
            public boolean isPrematureBreak(SearchStrategy.DiscoveredSolution discoveredSolution) {
                if (nuOfIterations == 50)
                    return true;
                nuOfIterations++;
                return false;
            }
        };
        CountIterations counter = new CountIterations();
        algorithm.addListener(counter);
        algorithm.setPrematureAlgorithmTermination(termination);
        algorithm.searchSolutions();
        Assertions.assertEquals(50, counter.getCountIterations());
    }

    @Test
    @DisplayName("When Adding Premature Termination _ it Is Executed Correctly")
    void whenAddingPrematureTermination_itIsExecutedCorrectly() {
        SearchStrategyManager stratManager = mock(SearchStrategyManager.class);
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class), stratManager);
        when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
        when(stratManager.getWeights()).thenReturn(Arrays.asList(1.0));
        algorithm.setMaxIterations(1000);
        PrematureAlgorithmTermination termination = new PrematureAlgorithmTermination() {

            private int nuOfIterations = 1;

            @Override
            public boolean isPrematureBreak(SearchStrategy.DiscoveredSolution discoveredSolution) {
                if (nuOfIterations == 50)
                    return true;
                nuOfIterations++;
                return false;
            }
        };
        CountIterations counter = new CountIterations();
        algorithm.addListener(counter);
        algorithm.addTerminationCriterion(termination);
        algorithm.searchSolutions();
        Assertions.assertEquals(50, counter.getCountIterations());
    }

    @Test
    @DisplayName("When Adding Premature Two Termination Criteria _ it Is Executed Correctly")
    void whenAddingPrematureTwoTerminationCriteria_itIsExecutedCorrectly() {
        SearchStrategyManager stratManager = mock(SearchStrategyManager.class);
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class), stratManager);
        when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
        when(stratManager.getWeights()).thenReturn(Arrays.asList(1.0));
        algorithm.setMaxIterations(1000);
        PrematureAlgorithmTermination termination = new PrematureAlgorithmTermination() {

            private int nuOfIterations = 1;

            @Override
            public boolean isPrematureBreak(SearchStrategy.DiscoveredSolution discoveredSolution) {
                if (nuOfIterations == 50)
                    return true;
                nuOfIterations++;
                return false;
            }
        };
        PrematureAlgorithmTermination termination2 = new PrematureAlgorithmTermination() {

            private int nuOfIterations = 1;

            @Override
            public boolean isPrematureBreak(SearchStrategy.DiscoveredSolution discoveredSolution) {
                if (nuOfIterations == 25)
                    return true;
                nuOfIterations++;
                return false;
            }
        };
        CountIterations counter = new CountIterations();
        algorithm.addListener(counter);
        algorithm.addTerminationCriterion(termination);
        algorithm.addTerminationCriterion(termination2);
        algorithm.searchSolutions();
        Assertions.assertEquals(25, counter.getCountIterations());
    }
}
