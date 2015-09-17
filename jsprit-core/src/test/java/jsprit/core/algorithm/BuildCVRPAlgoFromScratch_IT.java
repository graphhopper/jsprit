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

import jsprit.core.IntegrationTest;
import jsprit.core.algorithm.acceptor.GreedyAcceptance;
import jsprit.core.algorithm.module.RuinAndRecreateModule;
import jsprit.core.algorithm.recreate.BestInsertionBuilder;
import jsprit.core.algorithm.recreate.InsertionStrategy;
import jsprit.core.algorithm.ruin.RadialRuinStrategyFactory;
import jsprit.core.algorithm.ruin.RandomRuinStrategyFactory;
import jsprit.core.algorithm.ruin.RuinStrategy;
import jsprit.core.algorithm.ruin.distance.AvgServiceDistance;
import jsprit.core.algorithm.selector.SelectBest;
import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.InfiniteFleetManagerFactory;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.util.Solutions;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collection;

import static org.junit.Assert.assertEquals;


public class BuildCVRPAlgoFromScratch_IT {

    VehicleRoutingProblem vrp;

    VehicleRoutingAlgorithm vra;

    @Before
    public void setup() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder).read("src/test/resources/vrpnc1-jsprit.xml");
        vrp = builder.build();

        final StateManager stateManager = new StateManager(vrp);
        ConstraintManager cManager = new ConstraintManager(vrp, stateManager);

        VehicleFleetManager fleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        InsertionStrategy bestInsertion = new BestInsertionBuilder(vrp, fleetManager, stateManager, cManager).build();

        RuinStrategy radial = new RadialRuinStrategyFactory(0.15, new AvgServiceDistance(vrp.getTransportCosts())).createStrategy(vrp);
        RuinStrategy random = new RandomRuinStrategyFactory(0.25).createStrategy(vrp);

        SolutionCostCalculator solutionCostCalculator = new SolutionCostCalculator() {

            @Override
            public double getCosts(VehicleRoutingProblemSolution solution) {
                double costs = 0.0;
                for (VehicleRoute route : solution.getRoutes()) {
                    costs += stateManager.getRouteState(route, InternalStates.COSTS, Double.class);
                }
                return costs;
            }
        };

        SearchStrategy randomStrategy = new SearchStrategy("random", new SelectBest(), new GreedyAcceptance(1), solutionCostCalculator);
        RuinAndRecreateModule randomModule = new RuinAndRecreateModule("randomRuin_bestInsertion", bestInsertion, random);
        randomStrategy.addModule(randomModule);

        SearchStrategy radialStrategy = new SearchStrategy("radial", new SelectBest(), new GreedyAcceptance(1), solutionCostCalculator);
        RuinAndRecreateModule radialModule = new RuinAndRecreateModule("radialRuin_bestInsertion", bestInsertion, radial);
        radialStrategy.addModule(radialModule);

        vra = new PrettyAlgorithmBuilder(vrp, fleetManager, stateManager, cManager)
            .withStrategy(randomStrategy, 0.5).withStrategy(radialStrategy, 0.5)
            .addCoreStateAndConstraintStuff()
            .constructInitialSolutionWith(bestInsertion, solutionCostCalculator).build();
        vra.setMaxIterations(2000);

    }

    @Test
    @Category(IntegrationTest.class)
    public void testVRA() {
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        System.out.println("costs=" + Solutions.bestOf(solutions).getCost() + ";#routes=" + Solutions.bestOf(solutions).getRoutes().size());
        assertEquals(530.0, Solutions.bestOf(solutions).getCost(), 50.0);
        assertEquals(5, Solutions.bestOf(solutions).getRoutes().size());
    }

}
