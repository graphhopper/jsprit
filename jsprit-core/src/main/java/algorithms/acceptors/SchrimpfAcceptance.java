/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms.acceptors;

import java.net.URL;
import java.util.Collection;

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;

import util.Resource;
import util.Solutions;
import algorithms.VehicleRoutingAlgorithms;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.AlgorithmStartsListener;
import basics.algo.IterationEndsListener;
import basics.algo.IterationStartsListener;
import basics.io.AlgorithmConfig;
import basics.io.AlgorithmConfigXmlReader;



public class SchrimpfAcceptance implements SolutionAcceptor, IterationStartsListener, AlgorithmStartsListener{

	private static Logger logger = Logger.getLogger(SchrimpfAcceptance.class);
	
	private final double alpha;
	
	private int nOfTotalIterations = 1000;
	
	private int currentIteration = 0;
	
	private double initialThreshold = 0.0;

	private final int nOfRandomWalks;
	
	private final int solutionMemory;
	
	
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
			if(newSolution.getCost() < worst.getCost() + threshold){
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
//		logger.debug("iter="+iteration+" totalIter="+nOfTotalIterations+" scheduling="+scheduleVariable);
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
		this.nOfTotalIterations = algorithm.getNuOfIterations();
		
		/*
		 * randomWalk to determine standardDev
		 */
		final double[] results = new double[nOfRandomWalks];
		
		URL resource = Resource.getAsURL("randomWalk.xml");
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		new AlgorithmConfigXmlReader(algorithmConfig).read(resource);
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.createAlgorithm(problem, algorithmConfig);
		vra.setNuOfIterations(nOfRandomWalks);
		vra.getAlgorithmListeners().addListener(new IterationEndsListener() {
			
			@Override
			public void informIterationEnds(int iteration, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
				double result = Solutions.getBest(solutions).getCost();
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
