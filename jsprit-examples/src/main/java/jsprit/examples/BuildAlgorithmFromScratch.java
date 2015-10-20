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

package jsprit.examples;


import jsprit.analysis.toolbox.AlgorithmEventsRecorder;
import jsprit.analysis.toolbox.AlgorithmEventsViewer;
import jsprit.core.algorithm.PrettyAlgorithmBuilder;
import jsprit.core.algorithm.SearchStrategy;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.acceptor.GreedyAcceptance;
import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.algorithm.module.RuinAndRecreateModule;
import jsprit.core.algorithm.recreate.*;
import jsprit.core.algorithm.ruin.RadialRuinStrategyFactory;
import jsprit.core.algorithm.ruin.RandomRuinStrategyFactory;
import jsprit.core.algorithm.ruin.RuinStrategy;
import jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import jsprit.core.algorithm.selector.SelectBest;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.analysis.SolutionAnalyser;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.FiniteFleetManagerFactory;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import jsprit.instance.reader.CordeauReader;
import jsprit.util.Examples;

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
        vra.setMaxIterations(100);
        AlgorithmEventsRecorder eventsRecorder = new AlgorithmEventsRecorder(vrp, "output/events.dgs.gz");
        eventsRecorder.setRecordingRange(90, 100);
        vra.addListener(eventsRecorder);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);
        AlgorithmEventsViewer viewer = new AlgorithmEventsViewer();
        viewer.setRuinDelay(3);
        viewer.setRecreationDelay(1);
        viewer.display("output/events.dgs.gz");

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
        RegretInsertionFast regret = (RegretInsertionFast) iBuilder.build();
        DefaultScorer scoringFunction = new DefaultScorer(vrp);
        scoringFunction.setDepotDistanceParam(0.2);
        scoringFunction.setTimeWindowParam(-.2);
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
                SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, new SolutionAnalyser.DistanceCalculator() {
                    @Override
                    public double getDistance(Location from, Location to) {
                        return vrp.getTransportCosts().getTransportCost(from, to, 0., null, null);
                    }
                });
                return analyser.getVariableTransportCosts() + solution.getUnassignedJobs().size() * 500.;
            }

        };
    }


}
