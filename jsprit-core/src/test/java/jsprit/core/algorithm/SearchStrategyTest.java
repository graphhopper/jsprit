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

import jsprit.core.algorithm.acceptor.SolutionAcceptor;
import jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import jsprit.core.algorithm.selector.SolutionSelector;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SearchStrategyTest {

    @Test(expected = IllegalStateException.class)
    public void whenANullModule_IsAdded_throwException() {
        SolutionSelector select = mock(SolutionSelector.class);
        SolutionAcceptor accept = mock(SolutionAcceptor.class);
        SolutionCostCalculator calc = mock(SolutionCostCalculator.class);

        SearchStrategy strat = new SearchStrategy("strat", select, accept, calc);
        strat.addModule(null);

    }

    @Test
    public void whenStratRunsWithOneModule_runItOnes() {
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
            public void addModuleListener(
                SearchStrategyModuleListener moduleListener) {

            }
        };
        strat.addModule(mod);
        strat.run(vrp, null);

        assertEquals(runs.size(), 1);
    }

    @Test
    public void whenStratRunsWithTwoModule_runItTwice() {
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
            public void addModuleListener(
                SearchStrategyModuleListener moduleListener) {

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
            public void addModuleListener(
                SearchStrategyModuleListener moduleListener) {

            }
        };
        strat.addModule(mod);
        strat.addModule(mod2);
        strat.run(vrp, null);

        assertEquals(runs.size(), 2);
    }

    @Test
    public void whenStratRunsWithNModule_runItNTimes() {
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
                public void addModuleListener(
                    SearchStrategyModuleListener moduleListener) {

                }
            };
            strat.addModule(mod);
        }
        strat.run(vrp, null);
        assertEquals(runs.size(), N);
    }

    @Test(expected = IllegalStateException.class)
    public void whenSelectorDeliversNullSolution_throwException() {
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
                public void addModuleListener(
                    SearchStrategyModuleListener moduleListener) {

                }
            };
            strat.addModule(mod);
        }
        strat.run(vrp, null);
        assertEquals(runs.size(), N);
    }


}
