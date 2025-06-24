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

import com.graphhopper.jsprit.core.algorithm.acceptor.SolutionAcceptor;
import com.graphhopper.jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import com.graphhopper.jsprit.core.algorithm.selector.SolutionSelector;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Search Strategy Test")
class SearchStrategyTest {

    @Test
    @DisplayName("When A Null Module _ Is Added _ throw Exception")
    void whenANullModule_IsAdded_throwException() {
        assertThrows(IllegalStateException.class, () -> {
            SolutionSelector select = mock(SolutionSelector.class);
            SolutionAcceptor accept = mock(SolutionAcceptor.class);
            SolutionCostCalculator calc = mock(SolutionCostCalculator.class);
            SearchStrategy strat = new SearchStrategy("strat", select, accept, calc);
            strat.addModule(null);
        });
    }

    @Test
    @DisplayName("When Strat Runs With One Module _ run It Ones")
    void whenStratRunsWithOneModule_runItOnes() {
        SolutionSelector select = mock(SolutionSelector.class);
        SolutionAcceptor accept = mock(SolutionAcceptor.class);
        SolutionCostCalculator calc = mock(SolutionCostCalculator.class);
        final VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
        final VehicleRoutingProblemSolution newSol = mock(VehicleRoutingProblemSolution.class);
        when(select.selectSolution(null)).thenReturn(newSol);
        final Collection<Integer> runs = new ArrayList<Integer>();
        SearchStrategy strat = new SearchStrategy("strat", select, accept, calc);
        SearchStrategyModule mod = new SearchStrategyModule() {

            @Override
            public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
                runs.add(1);
                return vrpSolution;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public void addModuleListener(SearchStrategyModuleListener moduleListener) {
            }
        };
        strat.addModule(mod);
        strat.run(vrp, null);
        Assertions.assertEquals(runs.size(), 1);
    }

    @Test
    @DisplayName("When Strat Runs With Two Module _ run It Twice")
    void whenStratRunsWithTwoModule_runItTwice() {
        SolutionSelector select = mock(SolutionSelector.class);
        SolutionAcceptor accept = mock(SolutionAcceptor.class);
        SolutionCostCalculator calc = mock(SolutionCostCalculator.class);
        final VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
        final VehicleRoutingProblemSolution newSol = mock(VehicleRoutingProblemSolution.class);
        when(select.selectSolution(null)).thenReturn(newSol);
        final Collection<Integer> runs = new ArrayList<Integer>();
        SearchStrategy strat = new SearchStrategy("strat", select, accept, calc);
        SearchStrategyModule mod = new SearchStrategyModule() {

            @Override
            public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
                runs.add(1);
                return vrpSolution;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public void addModuleListener(SearchStrategyModuleListener moduleListener) {
            }
        };
        SearchStrategyModule mod2 = new SearchStrategyModule() {

            @Override
            public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
                runs.add(1);
                return vrpSolution;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public void addModuleListener(SearchStrategyModuleListener moduleListener) {
            }
        };
        strat.addModule(mod);
        strat.addModule(mod2);
        strat.run(vrp, null);
        Assertions.assertEquals(runs.size(), 2);
    }

    @Test
    @DisplayName("When Strat Runs With N Module _ run It N Times")
    void whenStratRunsWithNModule_runItNTimes() {
        SolutionSelector select = mock(SolutionSelector.class);
        SolutionAcceptor accept = mock(SolutionAcceptor.class);
        SolutionCostCalculator calc = mock(SolutionCostCalculator.class);
        final VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
        final VehicleRoutingProblemSolution newSol = mock(VehicleRoutingProblemSolution.class);
        when(select.selectSolution(null)).thenReturn(newSol);
        int N = new Random().nextInt(1000);
        final Collection<Integer> runs = new ArrayList<Integer>();
        SearchStrategy strat = new SearchStrategy("strat", select, accept, calc);
        for (int i = 0; i < N; i++) {
            SearchStrategyModule mod = new SearchStrategyModule() {

                @Override
                public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
                    runs.add(1);
                    return vrpSolution;
                }

                @Override
                public String getName() {
                    return null;
                }

                @Override
                public void addModuleListener(SearchStrategyModuleListener moduleListener) {
                }
            };
            strat.addModule(mod);
        }
        strat.run(vrp, null);
        Assertions.assertEquals(runs.size(), N);
    }

    @Test
    @DisplayName("When Selector Delivers Null Solution _ throw Exception")
    void whenSelectorDeliversNullSolution_throwException() {
        assertThrows(IllegalStateException.class, () -> {
            SolutionSelector select = mock(SolutionSelector.class);
            SolutionAcceptor accept = mock(SolutionAcceptor.class);
            SolutionCostCalculator calc = mock(SolutionCostCalculator.class);
            final VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
            when(select.selectSolution(null)).thenReturn(null);
            int N = new Random().nextInt(1000);
            final Collection<Integer> runs = new ArrayList<Integer>();
            SearchStrategy strat = new SearchStrategy("strat", select, accept, calc);
            for (int i = 0; i < N; i++) {
                SearchStrategyModule mod = new SearchStrategyModule() {

                    @Override
                    public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
                        runs.add(1);
                        return vrpSolution;
                    }

                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void addModuleListener(SearchStrategyModuleListener moduleListener) {
                    }
                };
                strat.addModule(mod);
            }
            strat.run(vrp, null);
            Assertions.assertEquals(runs.size(), N);
        });
    }
}
