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

import jsprit.core.algorithm.SearchStrategy.DiscoveredSolution;
import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.algorithm.termination.PrematureAlgorithmTermination;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VehicleRoutingAlgorithmTest {

    @Test
    public void whenSettingIterations_itIsSetCorrectly() {
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class),
            mock(SearchStrategyManager.class));
        algorithm.setMaxIterations(50);
        assertEquals(50, algorithm.getMaxIterations());
    }

    @Test
    public void whenSettingIterationsWithMaxIterations_itIsSetCorrectly() {
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class),
            mock(SearchStrategyManager.class));
        algorithm.setMaxIterations(50);
        assertEquals(50, algorithm.getMaxIterations());
    }

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
    public void whenSettingIterationsWithMaxIterations_iterAreExecutedCorrectly() {
        SearchStrategyManager stratManager = mock(SearchStrategyManager.class);
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class),
            stratManager);
        when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
        when(stratManager.getProbabilities()).thenReturn(Arrays.asList(1.0));
        algorithm.setMaxIterations(1000);
        CountIterations counter = new CountIterations();
        algorithm.addListener(counter);
        algorithm.searchSolutions();
        assertEquals(1000, counter.getCountIterations());
    }

    @Test
    public void whenSettingIterations_iterAreExecutedCorrectly() {
        SearchStrategyManager stratManager = mock(SearchStrategyManager.class);
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class),
            stratManager);
        when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
        when(stratManager.getProbabilities()).thenReturn(Arrays.asList(1.0));
        algorithm.setMaxIterations(1000);
        CountIterations counter = new CountIterations();
        algorithm.addListener(counter);
        algorithm.searchSolutions();
        assertEquals(1000, counter.getCountIterations());
    }

    @Test
    public void whenSettingPrematureTermination_itIsExecutedCorrectly() {
        SearchStrategyManager stratManager = mock(SearchStrategyManager.class);
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class),
            stratManager);
        when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
        when(stratManager.getProbabilities()).thenReturn(Arrays.asList(1.0));
        algorithm.setMaxIterations(1000);
        PrematureAlgorithmTermination termination = new PrematureAlgorithmTermination() {

            private int nuOfIterations = 1;

            @Override
            public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
                if (nuOfIterations == 50) return true;
                nuOfIterations++;
                return false;
            }
        };
        CountIterations counter = new CountIterations();
        algorithm.addListener(counter);
        algorithm.setPrematureAlgorithmTermination(termination);
        algorithm.searchSolutions();
        assertEquals(50, counter.getCountIterations());
    }

    @Test
    public void whenAddingPrematureTermination_itIsExecutedCorrectly() {
        SearchStrategyManager stratManager = mock(SearchStrategyManager.class);
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class), stratManager);
        when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
        when(stratManager.getProbabilities()).thenReturn(Arrays.asList(1.0));
        algorithm.setMaxIterations(1000);
        PrematureAlgorithmTermination termination = new PrematureAlgorithmTermination() {

            private int nuOfIterations = 1;

            @Override
            public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
                if (nuOfIterations == 50) return true;
                nuOfIterations++;
                return false;
            }

        };
        CountIterations counter = new CountIterations();
        algorithm.addListener(counter);
        algorithm.addTerminationCriterion(termination);
        algorithm.searchSolutions();
        assertEquals(50, counter.getCountIterations());
    }

    @Test
    public void whenAddingPrematureTwoTerminationCriteria_itIsExecutedCorrectly() {
        SearchStrategyManager stratManager = mock(SearchStrategyManager.class);
        VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(mock(VehicleRoutingProblem.class), stratManager);
        when(stratManager.getRandomStrategy()).thenReturn(mock(SearchStrategy.class));
        when(stratManager.getProbabilities()).thenReturn(Arrays.asList(1.0));
        algorithm.setMaxIterations(1000);
        PrematureAlgorithmTermination termination = new PrematureAlgorithmTermination() {

            private int nuOfIterations = 1;

            @Override
            public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
                if (nuOfIterations == 50) return true;
                nuOfIterations++;
                return false;
            }

        };
        PrematureAlgorithmTermination termination2 = new PrematureAlgorithmTermination() {

            private int nuOfIterations = 1;

            @Override
            public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
                if (nuOfIterations == 25) return true;
                nuOfIterations++;
                return false;
            }

        };
        CountIterations counter = new CountIterations();
        algorithm.addListener(counter);
        algorithm.addTerminationCriterion(termination);
        algorithm.addTerminationCriterion(termination2);
        algorithm.searchSolutions();
        assertEquals(25, counter.getCountIterations());
    }

}
