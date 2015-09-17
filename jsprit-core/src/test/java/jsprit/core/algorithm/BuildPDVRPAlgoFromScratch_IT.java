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
import jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.InfiniteFleetManagerFactory;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collection;


public class BuildPDVRPAlgoFromScratch_IT {

    VehicleRoutingProblem vrp;

    VehicleRoutingAlgorithm vra;

    static Logger log = LogManager.getLogger(BuildPDVRPAlgoFromScratch_IT.class);

    @Before
    public void setup() {

        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder).read("src/test/resources/pd_solomon_r101.xml");
        vrp = builder.build();

        final StateManager stateManager = new StateManager(vrp);

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        VehicleFleetManager fleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        InsertionStrategy bestInsertion = new BestInsertionBuilder(vrp, fleetManager, stateManager, constraintManager).build();

        RuinStrategy radial = new RadialRuinStrategyFactory(0.15, new AvgServiceDistance(vrp.getTransportCosts())).createStrategy(vrp);
        RuinStrategy random = new RandomRuinStrategyFactory(0.25).createStrategy(vrp);

        SolutionCostCalculator solutionCostCalculator = new SolutionCostCalculator() {

            @Override
            public double getCosts(VehicleRoutingProblemSolution solution) {
                double costs = 0.0;
                for (VehicleRoute route : solution.getRoutes()) {
                    Double cost_of_route = stateManager.getRouteState(route, InternalStates.COSTS, Double.class);
                    if (cost_of_route == null) cost_of_route = 0.;
                    costs += cost_of_route;
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

        vra = new PrettyAlgorithmBuilder(vrp, fleetManager, stateManager, constraintManager)
            .addCoreStateAndConstraintStuff().constructInitialSolutionWith(bestInsertion, solutionCostCalculator)
            .withStrategy(radialStrategy, 0.5).withStrategy(randomStrategy, 0.5).build();

        vra.setMaxIterations(1000);
        vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));

    }

    @Test
    @Category(IntegrationTest.class)
    public void test() {
        try {
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            System.out.println(Solutions.bestOf(solutions).getCost());
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

}
