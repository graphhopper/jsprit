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

package com.graphhopper.jsprit.examples;


import com.graphhopper.jsprit.analysis.toolbox.AlgorithmEventsRecorder;
import com.graphhopper.jsprit.core.algorithm.PrettyAlgorithmBuilder;
import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.acceptor.GreedyAcceptance;
import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.algorithm.module.RuinAndRecreateModule;
import com.graphhopper.jsprit.core.algorithm.recreate.*;
import com.graphhopper.jsprit.core.algorithm.ruin.RadialRuinStrategyFactory;
import com.graphhopper.jsprit.core.algorithm.ruin.RandomRuinStrategyFactory;
import com.graphhopper.jsprit.core.algorithm.ruin.RuinStrategy;
import com.graphhopper.jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.FiniteFleetManagerFactory;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.instance.reader.CordeauReader;
import com.graphhopper.jsprit.util.Examples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BuildAlgorithmFromScratch {


    public static class MyBestStrategy extends AbstractInsertionStrategy {

        private JobInsertionCostsCalculatorLight insertionCalculator;


        public MyBestStrategy(VehicleRoutingProblem vrp, VehicleFleetManager fleetManager, StateManager stateManager, ConstraintManager constraintManager) {
            super(vrp);
            insertionCalculator = JobInsertionCostsCalculatorLightFactory.createStandardCalculator(vrp, fleetManager, stateManager, constraintManager);
        }

        @Override
        public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
            List<Job> badJobs = new ArrayList<Job>();
            List<Job> unassigned = new ArrayList<Job>(unassignedJobs);
            Collections.shuffle(unassigned, random);

            for (Job j : unassigned) {

                InsertionData bestInsertionData = InsertionData.createEmptyInsertionData();
                VehicleRoute bestRoute = null;
                //look for inserting unassigned job into existing route
                for (VehicleRoute r : vehicleRoutes) {
                    InsertionData insertionData = insertionCalculator.getInsertionData(j, r, bestInsertionData.getInsertionCost());
                    if (insertionData instanceof InsertionData.NoInsertionFound) continue;
                    if (insertionData.getInsertionCost() < bestInsertionData.getInsertionCost()) {
                        bestInsertionData = insertionData;
                        bestRoute = r;
                    }
                }
                //try whole new route
                VehicleRoute empty = VehicleRoute.emptyRoute();
                InsertionData insertionData = insertionCalculator.getInsertionData(j, empty, bestInsertionData.getInsertionCost());
                if (!(insertionData instanceof InsertionData.NoInsertionFound)) {
                    if (insertionData.getInsertionCost() < bestInsertionData.getInsertionCost()) {
                        vehicleRoutes.add(empty);
                        insertJob(j, insertionData, empty);
                    }
                } else {
                    if (bestRoute != null) insertJob(j, bestInsertionData, bestRoute);
                    else badJobs.add(j);
                }
            }
            return badJobs;
        }


    }


    public static void main(String[] args) {
        Examples.createOutputFolder();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read("input/p08");
        final VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = createAlgorithm(vrp);
        vra.setMaxIterations(2000);
        AlgorithmEventsRecorder eventsRecorder = new AlgorithmEventsRecorder(vrp, "output/events.dgs.gz");
        eventsRecorder.setRecordingRange(90, 100);
        vra.addListener(eventsRecorder);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);

//        AlgorithmEventsViewer viewer = new AlgorithmEventsViewer();
//        viewer.setRuinDelay(3);
//        viewer.setRecreationDelay(1);
//        viewer.display("output/events.dgs.gz");

    }


    public static VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp) {

        VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        /*
         * insertion strategies
         */
        //my custom best insertion
        MyBestStrategy best = new MyBestStrategy(vrp, fleetManager, stateManager, constraintManager);

        //regret insertion
        InsertionBuilder iBuilder = new InsertionBuilder(vrp, fleetManager, stateManager, constraintManager);
        iBuilder.setInsertionStrategy(InsertionBuilder.Strategy.REGRET);
        iBuilder.setFastRegret(true);
        RegretInsertionFast regret = (RegretInsertionFast) iBuilder.build();
        DefaultScorer scoringFunction = new DefaultScorer(vrp);
        scoringFunction.setDepotDistanceParam(0.0);
        scoringFunction.setTimeWindowParam(0.0);
        regret.setScoringFunction(scoringFunction);

		/*
         * ruin strategies
		 */
        RuinStrategy randomRuin = new RandomRuinStrategyFactory(0.5).createStrategy(vrp);
        RuinStrategy radialRuin = new RadialRuinStrategyFactory(0.3, new AvgServiceAndShipmentDistance(vrp.getTransportCosts())).createStrategy(vrp);

		/*
         * objective function
		 */
        SolutionCostCalculator objectiveFunction = getObjectiveFunction(vrp);

        SearchStrategy firstStrategy = new SearchStrategy("firstStrategy", new SelectBest(), new GreedyAcceptance(1), objectiveFunction);
        firstStrategy.addModule(new RuinAndRecreateModule("randRuinRegretIns", regret, randomRuin));

        SearchStrategy secondStrategy = new SearchStrategy("secondStrategy", new SelectBest(), new GreedyAcceptance(1), objectiveFunction);
        secondStrategy.addModule(new RuinAndRecreateModule("radRuinRegretIns", regret, radialRuin));

        SearchStrategy thirdStrategy = new SearchStrategy("thirdStrategy", new SelectBest(), new GreedyAcceptance(1), objectiveFunction);
        secondStrategy.addModule(new RuinAndRecreateModule("radRuinBestIns", regret, radialRuin));

        PrettyAlgorithmBuilder prettyAlgorithmBuilder = PrettyAlgorithmBuilder.newInstance(vrp, fleetManager, stateManager, constraintManager);
        final VehicleRoutingAlgorithm vra = prettyAlgorithmBuilder
            .withStrategy(firstStrategy, 0.5).withStrategy(secondStrategy, 0.5).withStrategy(thirdStrategy, 0.2)
            .addCoreStateAndConstraintStuff()
            .constructInitialSolutionWith(regret, objectiveFunction)
            .build();

        //if you want to switch on/off strategies or adapt their weight within the search, you can do the following
        //e.g. from iteration 50 on, switch off first strategy
        //switch on again at iteration 90 with slightly higher weight
        IterationStartsListener strategyAdaptor = new IterationStartsListener() {
            @Override
            public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
                if (i == 50) {
                    vra.getSearchStrategyManager().informStrategyWeightChanged("firstStrategy", 0.0);
                    System.out.println("switched off firstStrategy");
                }
                if (i == 90) {
                    vra.getSearchStrategyManager().informStrategyWeightChanged("firstStrategy", 0.7);
                    System.out.println("switched on firstStrategy again with higher weight");
                }
            }
        };
        vra.addListener(strategyAdaptor);
        return vra;

    }

    private static SolutionCostCalculator getObjectiveFunction(final VehicleRoutingProblem vrp) {
        return new SolutionCostCalculator() {


            @Override
            public double getCosts(VehicleRoutingProblemSolution solution) {
                SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
                return analyser.getVariableTransportCosts() + solution.getUnassignedJobs().size() * 500.;
            }

        };
    }


}
