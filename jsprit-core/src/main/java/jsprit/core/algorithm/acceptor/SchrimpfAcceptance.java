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


/**
 * ThresholdAcceptance-Function defined by Schrimpf et al. (2000).
 * 
 * <p>The <b>idea</b> can be described as follows: Most problems do not only have one unique minimum (maximum) but 
 * a number of local minima (maxima). To avoid to get stuck in a local minimum at the beginning of a search
 * this threshold-acceptance function accepts also worse solution at the beginning (in contrary to a greedy
 * approach which only accepts better solutions), and converges to a greedy approach at the end. <br>
 * The difficulty is to define (i) an appropriate initial threshold and (ii) a corresponding function describing
 * how the threshold converges to zero, i.e. the greedy threshold.
 * 
 * <p>ad i) The initial threshold is determined by a random walk through the search space. 
 * The random walk currently runs with the following algorithm: src/main/resources/randomWalk.xml. It runs 
 * as long as it is specified in nuOfWarmupIterations. In the first iteration or walk respectively the algorithm generates a solution. 
 * This solution in turn is the basis of the next walk yielding to another solution value ... and so on. 
 * Each solution value is memorized since the initial threshold is essentially a function of the standard deviation of these solution values. 
 * To be more precise: initial threshold = stddev(solution values) / 2.     
 * 
 * <p>ad ii) The threshold of iteration i is determined as follows: 
 * threshold(i) = initialThreshold * Math.exp(-Math.log(2) * (i / nuOfTotalIterations) / alpha)
 * To get a better understanding of the threshold-function go to Wolfram Alpha and plot the following line 
 * (just copy and paste it into Wolfram's console: <a href="https://www.wolframalpha.com/">www.wolframalpha.com</a>):
 * <p>100. * exp(-log(2)* (x/1000) / 0.1) (x from 0 to 1000) (y from 0 to 100)
 * <p>with <br>
 * initialThreshold = 100<br>
 * nuOfTotalIter = 1000<br>
 * alpha = 0.1<br>
 * x corresponds to i iterations and<br>
 * y to the threshold(i)
 * 
 * <p>Gerhard Schrimpf, Johannes Schneider, Hermann Stamm- Wilbrandt, and Gunter Dueck (2000). 
 * Record breaking optimization results using the ruin and recreate principle. 
 * Journal of Computational Physics, 159(2):139 – 171, 2000. ISSN 0021-9991. doi: 10.1006/jcph.1999. 6413. 
 * URL http://www.sciencedirect.com/science/article/ pii/S0021999199964136
 * 
 * <p>Probably the determination of the initial threshold will be separated from the threshold-acceptance
 * function (25.02.14 stefan)
 * 
 * @author schroeder
 *
 */
public class SchrimpfAcceptance implements SolutionAcceptor, IterationStartsListener, AlgorithmStartsListener{

	private static Logger logger = LogManager.getLogger(SchrimpfAcceptance.class.getName());
	
	private final double alpha;
	
	private int nOfTotalIterations = 1000;
	
	private int currentIteration = 0;
	
	private double initialThreshold = 0.0;

	private int nOfRandomWalks;
	
	private final int solutionMemory;
	
	private boolean determineInitialThreshold = true;
	
	public SchrimpfAcceptance(int solutionMemory, double alpha){
		this.alpha = alpha;
		this.solutionMemory = solutionMemory;
		determineInitialThreshold = false;
		logger.info("initialise " + this);
	}
	
	/**
	 * @deprecated use <code>new SchrimpfAcceptance(solutionMemory,alpha)</code> instead. if you want to determine ini-threshold with a
	 * random walk and the algorithm 'randomWalk.xml' use SchrimpfInitialThresholdGenerator.class instead.
	 */
	@Deprecated
	public SchrimpfAcceptance(int solutionMemory, double alpha, int nOfWarmupIterations) {
		super();
		this.alpha = alpha;
		this.nOfRandomWalks = nOfWarmupIterations;
		this.solutionMemory = solutionMemory;
		logger.info("initialise " + this);
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
            if(worst == null){
                solutions.add(newSolution);
                solutionAccepted = true;
            }
			else if(newSolution.getCost() < worst.getCost() + threshold){
				solutions.remove(worst);
				solutions.add(newSolution);
				solutionAccepted = true;
			}
		}
		return solutionAccepted;
	}
	
	@Override
	public String toString() {
		return "[name=SchrimpfAcceptance][alpha="+alpha+"][warmup=" + nOfRandomWalks + "]";
	}
	
	private double getThreshold(int iteration) {
		double scheduleVariable = (double) iteration / (double) nOfTotalIterations;
		return initialThreshold * Math.exp(-1. * Math.log(2) * scheduleVariable / alpha);
	}


	@SuppressWarnings("UnusedDeclaration")
    public double getInitialThreshold(){
		return initialThreshold;
	}
	
	/**
	 * Sets initial threshold.
	 * <p>Note that if initial threshold has been set, automatic generation of initial threshold is disabled.
	 *  
	 * @param initialThreshold the initialThreshold to set
	 */
	public void setInitialThreshold(double initialThreshold) {
		this.initialThreshold = initialThreshold;
		determineInitialThreshold=false;
	}

	@Override
	public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
		if(!determineInitialThreshold){
			logger.info("skip threshold initialization from here");
			return;
		}
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
//				logger.info("result="+result);
				results[iteration-1] = result;
			}

		});
		vra.searchSolutions();

		StandardDeviation dev = new StandardDeviation();
		double standardDeviation = dev.evaluate(results);
		initialThreshold = standardDeviation / 2;

		logger.info("warmup done");
		logger.info("total time: " + ((System.currentTimeMillis()-now)/1000.0) + "s");
		logger.info("initial threshold: " + initialThreshold);
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
