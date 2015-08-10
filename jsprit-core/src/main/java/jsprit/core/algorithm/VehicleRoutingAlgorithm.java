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
import jsprit.core.algorithm.listener.*;
import jsprit.core.algorithm.termination.PrematureAlgorithmTermination;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.util.Solutions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Algorithm that solves a {@link VehicleRoutingProblem}.
 * 
 * @author stefan schroeder
 *
 */
public class VehicleRoutingAlgorithm {

    private static class TerminationManager implements PrematureAlgorithmTermination {

        private Collection<PrematureAlgorithmTermination> terminationCriteria = new ArrayList<PrematureAlgorithmTermination>();

        void addTermination(PrematureAlgorithmTermination termination){
            terminationCriteria.add(termination);
        }

        @Override
        public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
            for(PrematureAlgorithmTermination termination : terminationCriteria){
                if(termination.isPrematureBreak(discoveredSolution)){
                    return true;
                }
            }
            return false;
        }
    }

    private static class Counter {
		private final String name;
		private long counter = 0;
		private long nextCounter = 1;
		private static final Logger log = LogManager.getLogger(Counter.class);

		public Counter(final String name) {
			this.name = name;
		}

		public void incCounter() {
			long i = counter++;
			long n = nextCounter;
			if (i >= n) {
                nextCounter=n*2;
                log.info(this.name + n);
			}
		}

		public void reset() {
			counter=0;
			nextCounter=1;
		}
	}

	private final static Logger logger = LogManager.getLogger();

    private final Counter counter = new Counter("iterations ");

    private final VehicleRoutingProblem problem;

    private final SearchStrategyManager searchStrategyManager;

    private final VehicleRoutingAlgorithmListeners algoListeners = new VehicleRoutingAlgorithmListeners();

    private final Collection<VehicleRoutingProblemSolution> initialSolutions;

    private int maxIterations = 100;

    private TerminationManager terminationManager = new TerminationManager();

    private VehicleRoutingProblemSolution bestEver = null;
	
	public VehicleRoutingAlgorithm(VehicleRoutingProblem problem, SearchStrategyManager searchStrategyManager) {
		super();
		this.problem = problem;
		this.searchStrategyManager = searchStrategyManager;
		initialSolutions = new ArrayList<VehicleRoutingProblemSolution>();
	}

	public VehicleRoutingAlgorithm(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> initialSolutions, SearchStrategyManager searchStrategyManager) {
		super();
		this.problem = problem;
		this.searchStrategyManager = searchStrategyManager;
		this.initialSolutions = initialSolutions;
	}

	/**
	 * Adds solution to the collection of initial solutions.
	 * 
	 * @param solution the solution to be added
	 */
	public void addInitialSolution(VehicleRoutingProblemSolution solution){
        verify(solution);
        initialSolutions.add(solution);
	}

	private void verify(VehicleRoutingProblemSolution solution) {
        int nuJobs = 0;
        for(VehicleRoute route : solution.getRoutes()){
            nuJobs += route.getTourActivities().getJobs().size();
            if(route.getVehicle().getIndex() == 0)
                throw new IllegalStateException("vehicle used in initial solution has no index. probably a vehicle is used that has not been added to the " +
                        " the VehicleRoutingProblem. only use vehicles that have already been added to the problem.");
            for(TourActivity act : route.getActivities()) {
                if (act.getIndex() == 0) {
                    throw new IllegalStateException("act in initial solution has no index. activities are created and associated to their job in VehicleRoutingProblem\n." +
                            " thus if you build vehicle-routes use the jobActivityFactory from vehicle routing problem like that \n" +
                            " VehicleRoute.Builder.newInstance(knownVehicle).setJobActivityFactory(vrp.getJobActivityFactory).addService(..)....build() \n" +
                            " then the activities that are created to build the route are identical to the ones used in VehicleRoutingProblem");
                }
            }
        }
        if(nuJobs != problem.getJobs().values().size()){
            logger.warn("number of jobs in initial solution ({}) is not equal nuJobs in vehicle routing problem ({})" +
                    "\n this might yield unintended effects, e.g. initial solution cannot be improved anymore.", nuJobs, problem.getJobs().values().size() );
        }
    }

    /**
	 * Sets premature termination and overrides existing termination criteria. If existing ones should not be
     * overridden use <code>.addTerminationCriterion(...)</code>.
	 *
	 * @param prematureAlgorithmTermination the termination criterion
	 */
	public void setPrematureAlgorithmTermination(PrematureAlgorithmTermination prematureAlgorithmTermination){
		terminationManager = new TerminationManager();
        terminationManager.addTermination(prematureAlgorithmTermination);
	}

    /**
     * Adds a termination criterion to the collection of already specified termination criteria. If one
     * of the termination criteria is fulfilled, the algorithm terminates prematurely.
     *
     * @param terminationCriterion the termination criterion
     */
    public void addTerminationCriterion(PrematureAlgorithmTermination terminationCriterion){
        terminationManager.addTermination(terminationCriterion);
    }

	/**
	 * Gets the {@link SearchStrategyManager}.
	 * 
	 * @return SearchStrategyManager
	 */
	public SearchStrategyManager getSearchStrategyManager() {
		return searchStrategyManager;
	}

	/**
	 * Runs the vehicle routing algorithm and returns a number of generated solutions.
	 * 
	 * <p>The algorithm runs as long as it is specified in nuOfIterations and prematureBreak. In each iteration it selects a searchStrategy according
	 * to searchStrategyManager and runs the strategy to improve solutions. 
	 * <p>Note that clients are allowed to observe/listen the algorithm. See {@link VehicleRoutingAlgorithmListener} and its according listeners.
	 * 
	 * @return Collection<VehicleRoutingProblemSolution> the solutions 
	 * @see {@link SearchStrategyManager}, {@link VehicleRoutingAlgorithmListener}, {@link AlgorithmStartsListener}, {@link AlgorithmEndsListener}, {@link IterationStartsListener}, {@link IterationEndsListener}
	 */
	public Collection<VehicleRoutingProblemSolution> searchSolutions(){
		logger.info("algorithm starts: [maxIterations={}]", maxIterations);
		double now = System.currentTimeMillis();
		int noIterationsThisAlgoIsRunning = maxIterations;
		counter.reset();
		Collection<VehicleRoutingProblemSolution> solutions = new ArrayList<VehicleRoutingProblemSolution>(initialSolutions);
		algorithmStarts(problem,solutions);
        bestEver = Solutions.bestOf(solutions);
        logger.info("iterations start");
		for(int i=0;i< maxIterations;i++){
			iterationStarts(i+1,problem,solutions);
			logger.debug("start iteration: {}", i);
			counter.incCounter();
			SearchStrategy strategy = searchStrategyManager.getRandomStrategy();
			DiscoveredSolution discoveredSolution = strategy.run(problem, solutions);
			logger.trace("discovered solution: {}", discoveredSolution);
            memorizeIfBestEver(discoveredSolution);
			selectedStrategy(discoveredSolution,problem,solutions);
            if(terminationManager.isPrematureBreak(discoveredSolution)){
				logger.info("premature algorithm termination at iteration {}", (i+1));
				noIterationsThisAlgoIsRunning = (i+1);
				break;
			}
			iterationEnds(i+1,problem,solutions);
		}
		logger.info("iterations end at {} iterations", noIterationsThisAlgoIsRunning);
		addBestEver(solutions);
        algorithmEnds(problem, solutions);
		logger.info("took {} seconds", ((System.currentTimeMillis()-now)/1000.0));
		return solutions;
	}

    private void addBestEver(Collection<VehicleRoutingProblemSolution> solutions) {
        if(bestEver != null) solutions.add(bestEver);
    }

    private void memorizeIfBestEver(DiscoveredSolution discoveredSolution) {
        if(discoveredSolution == null) return;
        if(bestEver == null) bestEver = discoveredSolution.getSolution();
        else if(discoveredSolution.getSolution().getCost() < bestEver.getCost()) bestEver = discoveredSolution.getSolution();
    }


    private void selectedStrategy(DiscoveredSolution discoveredSolution, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		algoListeners.selectedStrategy(discoveredSolution,problem,solutions);
	}

	private void algorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		algoListeners.algorithmEnds(problem, solutions);
	}

	public VehicleRoutingAlgorithmListeners getAlgorithmListeners() {
		return algoListeners;
	}
	
	public void addListener(VehicleRoutingAlgorithmListener l){
		algoListeners.addListener(l);
		if(l instanceof SearchStrategyListener) searchStrategyManager.addSearchStrategyListener((SearchStrategyListener) l);
		if(l instanceof SearchStrategyModuleListener) searchStrategyManager.addSearchStrategyModuleListener((SearchStrategyModuleListener) l);
	}

	private void iterationEnds(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		algoListeners.iterationEnds(i,problem, solutions);
	}

	private void iterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		algoListeners.iterationStarts(i, problem, solutions);
	}

	private void algorithmStarts(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		algoListeners.algorithmStarts(problem, this, solutions);
	}

    /**
     * Sets max number of iterations.
     *
     * @param maxIterations max number of iteration the algorithm runs
     */
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
        logger.debug("set maxIterations to {}", this.maxIterations);
    }

    /**
     * Gets max number of iterations.
     *
     * @return max number of iterations
     */
    public int getMaxIterations() {
        return maxIterations;
    }


}
