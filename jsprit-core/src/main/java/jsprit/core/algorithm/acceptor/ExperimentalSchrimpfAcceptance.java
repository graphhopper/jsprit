/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package jsprit.core.algorithm.acceptor;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.AlgorithmConfig;
import jsprit.core.algorithm.io.AlgorithmConfigXmlReader;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.listener.AlgorithmStartsListener;
import jsprit.core.algorithm.listener.IterationEndsListener;
import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.Resource;
import jsprit.core.util.Solutions;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Collection;


public class ExperimentalSchrimpfAcceptance implements SolutionAcceptor, IterationStartsListener, AlgorithmStartsListener{

	final static Logger logger = LogManager.getLogger(ExperimentalSchrimpfAcceptance.class.getName());
	
	private final double alpha;
	
	private int nOfTotalIterations = 1000;
	
	private int currentIteration = 0;
	
	private double initialThreshold = 0.0;

	private final int nOfRandomWalks;
	
	private final int solutionMemory;
	
	
	public ExperimentalSchrimpfAcceptance(int solutionMemory, double alpha, int nOfWarmupIterations) {
		super();
		this.alpha = alpha;
		this.nOfRandomWalks = nOfWarmupIterations;
		this.solutionMemory = solutionMemory;
		logger.info("initialise {}", this);
	}
	


	@Override
	public boolean acceptSolution(Collection<VehicleRoutingProblemSolution> solutions, VehicleRoutingProblemSolution newSolution) {
		boolean solutionAccepted = false;
		if (solutions.size() < solutionMemory) {
			solutions.add(newSolution);
			solutionAccepted = true;
		} else {
			VehicleRoutingProblemSolution worst = null;
			double threshold = getThreshold(currentIteration);
			for(VehicleRoutingProblemSolution solutionInMemory : solutions){
				if(worst == null) worst = solutionInMemory;
				else if(solutionInMemory.getCost() > worst.getCost()) worst = solutionInMemory;
			}
			if(newSolution.getRoutes().size() < worst.getRoutes().size()){
				solutions.remove(worst);
				solutions.add(newSolution);
				solutionAccepted = true;
			}
			else if(newSolution.getRoutes().size() == worst.getRoutes().size() && newSolution.getCost() < worst.getCost() + threshold){
				solutions.remove(worst);
				solutions.add(newSolution);
				solutionAccepted = true;
			}
		}
		return solutionAccepted;
	}
	
	@Override
	public String toString() {
		return "[name=schrimpfAcceptanceFunction][alpha="+alpha+"][warmup=" + nOfRandomWalks + "]";
	}
	
	private double getThreshold(int iteration) {
		double scheduleVariable = (double) iteration / (double) nOfTotalIterations;
//		logger.debug("iter={} totalIter={} scheduling={}", iteration, nOfTotalIterations, scheduleVariable);
		double currentThreshold = initialThreshold * Math.exp(-Math.log(2) * scheduleVariable / alpha);
		return currentThreshold;
	}


	@Override
	public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
		reset();
		logger.info("---------------------------------------------------------------------");
		logger.info("prepare schrimpfAcceptanceFunction, i.e. determine initial threshold");
		logger.info("start random-walk (see randomWalk.xml)");
		double now = System.currentTimeMillis();
		this.nOfTotalIterations = algorithm.getMaxIterations();
		
		/*
		 * randomWalk to determine standardDev
		 */
		final double[] results = new double[nOfRandomWalks];
		
		URL resource = Resource.getAsURL("randomWalk.xml");
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		new AlgorithmConfigXmlReader(algorithmConfig).read(resource);
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.createAlgorithm(problem, algorithmConfig);
		vra.setMaxIterations(nOfRandomWalks);
		vra.getAlgorithmListeners().addListener(new IterationEndsListener() {
			
			@Override
			public void informIterationEnds(int iteration, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
				double result = Solutions.bestOf(solutions).getCost();
//				logger.info("result={}", result);
				results[iteration-1] = result;
			}
			
		});
		vra.searchSolutions();
		
		StandardDeviation dev = new StandardDeviation();
		double standardDeviation = dev.evaluate(results);
		initialThreshold = standardDeviation / 2;
		
		logger.info("warmup done");
		logger.info("total time: {}s", ((System.currentTimeMillis()-now)/1000.0));
		logger.info("initial threshold: {}", initialThreshold);
		logger.info("---------------------------------------------------------------------");
		
	}

	private void reset() {
		currentIteration = 0;
	}

	@Override
	public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		currentIteration = i;
	}

}
