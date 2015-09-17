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
import jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import jsprit.core.algorithm.selector.SelectBest;
import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.UpdateVariableCosts;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.InfiniteFleetManagerFactory;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collection;

import static org.junit.Assert.assertTrue;


public class BuildPDVRPWithShipmentsAlgoFromScratch_IT {

    @Test
    @Category(IntegrationTest.class)
    public void test() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder).read("src/test/resources/pdp.xml");

        VehicleRoutingProblem vrp = builder.build();

        final StateManager stateManager = new StateManager(vrp);
        stateManager.updateLoadStates();
//        stateManager.updateTimeWindowStates();
        stateManager.addStateUpdater(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
//        constraintManager.addTimeWindowConstraint();
        constraintManager.addLoadConstraint();

        VehicleFleetManager fleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        BestInsertionBuilder bestIBuilder = new BestInsertionBuilder(vrp, fleetManager, stateManager, constraintManager);
        InsertionStrategy bestInsertion = bestIBuilder.build();


        RuinStrategy radial = new RadialRuinStrategyFactory(0.3, new AvgServiceAndShipmentDistance(vrp.getTransportCosts())).createStrategy(vrp);
        RuinStrategy random = new RandomRuinStrategyFactory(0.5).createStrategy(vrp);


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

        SearchStrategyManager strategyManager = new SearchStrategyManager();
        strategyManager.addStrategy(radialStrategy, 0.5);
        strategyManager.addStrategy(randomStrategy, 0.5);

        VehicleRoutingAlgorithm vra = new VehicleRoutingAlgorithm(vrp, strategyManager);
        vra.addListener(stateManager);
        vra.addListener(new RemoveEmptyVehicles(fleetManager));

        VehicleRoutingProblemSolution iniSolution = new InsertionInitialSolutionFactory(bestInsertion, solutionCostCalculator).createSolution(vrp);
        vra.addInitialSolution(iniSolution);

        vra.setMaxIterations(3);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertTrue(!solutions.isEmpty());
    }


}
