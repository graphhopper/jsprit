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
package jsprit.core.algorithm.termination;

import java.util.Collection;

import jsprit.core.algorithm.SearchStrategy.DiscoveredSolution;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.listener.AlgorithmStartsListener;
import jsprit.core.algorithm.listener.IterationEndsListener;
import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.Solutions;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;


/**
 * Breaks algorithm prematurely based on variationCoefficient.
 * 
 * <p>Note that this must be registered in algorithm<br>
 * <code>algorithm.getAlgorithmListeners().addListener(this);</code>
 * 
 * 
 * @author stefan
 *
 */
public class VariationCoefficientTermination implements PrematureAlgorithmTermination, IterationStartsListener, AlgorithmStartsListener, IterationEndsListener{

	private static Logger logger = Logger.getLogger(VariationCoefficientTermination.class);
	
	private int nuOfIterations;
	
	private double variationCoefficientThreshold;
	
	private int currentIteration;
	
	private double[] solutionValues;
	
	private VehicleRoutingProblemSolution lastAccepted = null;
	
	/**
	 * Breaks algorithm prematurely based on variationCoefficient.
	 * 
	 * <p>Note that this must be registered in algorithm<br>
	 * <code>algorithm.getAlgorithmListeners().addListener(this);</code>
	 * 
	 * 
	 * @author stefan
	 *
	 */
	public VariationCoefficientTermination(int nuOfIterations, double variationCoefficientThreshold) {
		super();
		this.nuOfIterations = nuOfIterations;
		this.variationCoefficientThreshold = variationCoefficientThreshold;
		solutionValues = new double[nuOfIterations];
		logger.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[name=VariationCoefficientBreaker][variationCoefficientThreshold="+variationCoefficientThreshold+"][iterations="+nuOfIterations+"]";
	}

	@Override
	public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
		if(discoveredSolution.isAccepted()){
			lastAccepted = discoveredSolution.getSolution();
			solutionValues[currentIteration]=discoveredSolution.getSolution().getCost();
		}
		else{
			if(lastAccepted != null){
				solutionValues[currentIteration]=lastAccepted.getCost();
			} 
			else solutionValues[currentIteration]=Integer.MAX_VALUE; 
		}
		if(lastAccepted !=null) {
//			logger.info(lastAccepted.getCost());
//			logger.info("inArr,"+(currentIteration)+","+solutionValues[currentIteration]);
		}
		if(currentIteration == (nuOfIterations-1)){
			double mean = StatUtils.mean(solutionValues);
			double stdDev = new StandardDeviation(true).evaluate(solutionValues, mean);
			double variationCoefficient = stdDev/mean;
//			logger.info("[mean="+mean+"][stdDev="+stdDev+"][variationCoefficient="+variationCoefficient+"]");
			if(variationCoefficient < variationCoefficientThreshold){
				return true;
			}
		}
		return false;
	}
	
	private void reset(){
		currentIteration=0;
	}

	@Override
	public void informAlgorithmStarts(VehicleRoutingProblem problem,VehicleRoutingAlgorithm algorithm,Collection<VehicleRoutingProblemSolution> solutions) {
		reset();
	}

	@Override
	public void informIterationEnds(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		if(currentIteration == (nuOfIterations-1)){
			reset();
		}
		else{
			currentIteration++;
		}
	}

	@Override
	public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		if(lastAccepted == null) lastAccepted = Solutions.bestOf(solutions);
	}

	

}
