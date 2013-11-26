package jsprit.examples;

import java.io.File;
import java.util.Collection;

import jsprit.analysis.toolbox.SolutionPrinter;
import jsprit.core.algorithm.InsertionInitialSolutionFactory;
import jsprit.core.algorithm.RemoveEmptyVehicles;
import jsprit.core.algorithm.SearchStrategy;
import jsprit.core.algorithm.SearchStrategyManager;
import jsprit.core.algorithm.VariablePlusFixedSolutionCostCalculatorFactory;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.acceptor.GreedyAcceptance;
import jsprit.core.algorithm.module.RuinAndRecreateModule;
import jsprit.core.algorithm.recreate.BestInsertionBuilder;
import jsprit.core.algorithm.recreate.InsertionStrategy;
import jsprit.core.algorithm.ruin.RadialRuinStrategyFactory;
import jsprit.core.algorithm.ruin.RandomRuinStrategyFactory;
import jsprit.core.algorithm.ruin.RuinStrategy;
import jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import jsprit.core.algorithm.selector.SelectBest;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.UpdateVariableCosts;
import jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.InfiniteFleetManagerFactory;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.util.Solutions;
import jsprit.instance.reader.SolomonReader;

public class BuildAlgorithmFromScratch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * some preparation - create output folder
		 */
		File dir = new File("output");
		// if the directory does not exist, create it
		if (!dir.exists()){
			System.out.println("creating directory ./output");
			boolean result = dir.mkdir();  
			if(result) System.out.println("./output created");  
		}
		
		/*
		 * Build the problem.
		 * 
		 * But define a problem-builder first.
		 */
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		
		/*
		 * A solomonReader reads solomon-instance files, and stores the required information in the builder.
		 */
		new SolomonReader(vrpBuilder).read("input/C101_solomon.txt");
		
		/*
		 * Finally, the problem can be built. By default, transportCosts are crowFlyDistances (as usually used for vrp-instances).
		 */
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		/*
		 * Build algorithm
		 */
		VehicleRoutingAlgorithm vra = buildAlgorithmFromScratch(vrp);
		
		/*
		 * search solution
		 */
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		/*
		 * print result
		 */
		SolutionPrinter.print(Solutions.bestOf(solutions));

	}

	private static VehicleRoutingAlgorithm buildAlgorithmFromScratch(VehicleRoutingProblem vrp) {
		
		/*
		 * manages route and activity states.
		 */
		StateManager stateManager = new StateManager(vrp);
		/*
		 * tells stateManager to update load states
		 */
		stateManager.updateLoadStates();
		/*
		 * tells stateManager to update time-window states
		 */
		stateManager.updateTimeWindowStates();
		/*
		 * stateManager.addStateUpdater(updater);
		 * lets you register your own stateUpdater
		 */
		
		/*
		 * updates variable costs once a vehicleRoute has changed (by removing or adding a customer)
		 */
		stateManager.addStateUpdater(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));
		
		/*
		 * constructs a constraintManager that manages the various hardConstraints (and soon also softConstraints)
		 */
		ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
		/*
		 * tells constraintManager to add timeWindowConstraints
		 */
		constraintManager.addTimeWindowConstraint();
		/*
		 * tells constraintManager to add loadConstraints
		 */
		constraintManager.addLoadConstraint();
		/*
		 * add an arbitrary number of hardConstraints by
		 * constraintManager.addConstraint(...)
		 */
		
		/*
		 * define a fleetManager, here infinite vehicles can be used 
		 */
		VehicleFleetManager fleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
		
		/*
		 * define ruin-and-recreate strategies
		 * 
		 */
		/*
		 * first, define an insertion-strategy, i.e. bestInsertion
		 */
		BestInsertionBuilder iBuilder = new BestInsertionBuilder(vrp, fleetManager, stateManager, constraintManager);
		/*
		 * no need to set further options
		 */
		InsertionStrategy iStrategy = iBuilder.build();
		
		/*
		 * second, define random-ruin that ruins 50-percent of the selected solution
		 */
		RuinStrategy randomRuin = new RandomRuinStrategyFactory(0.5).createStrategy(vrp);
		
		/*
		 * third, define radial-ruin that ruins 30-percent of the selected solution
		 * the second para defines the distance between two jobs.
		 */
		RuinStrategy radialRuin = new RadialRuinStrategyFactory(0.3, new AvgServiceAndShipmentDistance(vrp.getTransportCosts())).createStrategy(vrp);
		
		/*
		 * now define a strategy
		 */
		/*
		 * but before define how a generated solution is evaluated
		 * here: the VariablePlusFixed.... comes out of the box and it does what its name suggests
		 */
		SolutionCostCalculator solutionCostCalculator = new VariablePlusFixedSolutionCostCalculatorFactory(stateManager).createCalculator();
		
		SearchStrategy firstStrategy = new SearchStrategy(new SelectBest(), new GreedyAcceptance(1), solutionCostCalculator);
		firstStrategy.addModule(new RuinAndRecreateModule("randomRuinAndBestInsertion", iStrategy, randomRuin));
		
		SearchStrategy secondStrategy = new SearchStrategy(new SelectBest(), new GreedyAcceptance(1), solutionCostCalculator);
		secondStrategy.addModule(new RuinAndRecreateModule("radialRuinAndBestInsertion", iStrategy, radialRuin));
		
		/*
		 * put both strategies together, each with the prob of 0.5 to be selected
		 */
		SearchStrategyManager searchStrategyManager = new SearchStrategyManager();
		searchStrategyManager.addStrategy(firstStrategy, 0.5);
		searchStrategyManager.addStrategy(secondStrategy, 0.5);
		
		/*
		 * construct the algorithm
		 */
		VehicleRoutingAlgorithm vra = new VehicleRoutingAlgorithm(vrp, searchStrategyManager);
		//do not forgett to add the stateManager listening to the algorithm-stages
		vra.addListener(stateManager);
		//remove empty vehicles after insertion has finished
		vra.addListener(new RemoveEmptyVehicles(fleetManager));
		
		/*
		 * Do not forget to add an initial solution by vra.addInitialSolution(solution);
		 * or
		 */
		vra.addInitialSolution(new InsertionInitialSolutionFactory(iStrategy, solutionCostCalculator).createSolution(vrp));
		
		/*
		 * define the nIterations (by default nIteration=100)
		 */
		vra.setNuOfIterations(1000);
		
		/*
		 * optionally define a premature termination criterion (by default: not criterion is set)
		 */
		vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));
		
		return vra;
	}

}
