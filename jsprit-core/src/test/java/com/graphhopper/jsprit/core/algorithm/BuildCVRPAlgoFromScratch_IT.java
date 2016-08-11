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

import com.graphhopper.jsprit.core.algorithm.acceptor.GreedyAcceptance;
import com.graphhopper.jsprit.core.algorithm.module.RuinAndRecreateModule;
import com.graphhopper.jsprit.core.algorithm.recreate.BestInsertionBuilder;
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionStrategy;
import com.graphhopper.jsprit.core.algorithm.ruin.RadialRuinStrategyFactory;
import com.graphhopper.jsprit.core.algorithm.ruin.RandomRuinStrategyFactory;
import com.graphhopper.jsprit.core.algorithm.ruin.RuinStrategy;
import com.graphhopper.jsprit.core.algorithm.ruin.distance.AvgServiceDistance;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.InfiniteFleetManagerFactory;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import com.graphhopper.jsprit.core.util.ChristofidesReader;
import com.graphhopper.jsprit.core.util.Solutions;
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
        new ChristofidesReader(builder).read(getClass().getResourceAsStream("vrpnc1.txt"));
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
    public void testVRA() {
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        assertEquals(530.0, Solutions.bestOf(solutions).getCost(), 50.0);
        assertEquals(5, Solutions.bestOf(solutions).getRoutes().size());
    }

}
