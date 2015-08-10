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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;


/**
 * Terminates algorithm prematurely based on variationCoefficient (http://en.wikipedia.org/wiki/Coefficient_of_variation).
 * 
 * <p>Note, that this must be registered as AlgorithmListener <br>
 * It will be activated by:<br>
 *
 * <code>algorithm.setPrematureAlgorithmTermination(this);</code><br>
 * <code>algorithm.addListener(this);</code>
 *
 * 
 * @author stefan schroeder
 *
 */
public class VariationCoefficientTermination implements PrematureAlgorithmTermination, IterationStartsListener, AlgorithmStartsListener, IterationEndsListener{

	private final static Logger logger = LogManager.getLogger(VariationCoefficientTermination.class);
	
	private final int noIterations;
	
	private final double variationCoefficientThreshold;
	
	private int currentIteration;
	
	private double[] solutionValues;
	
	private VehicleRoutingProblemSolution lastAccepted = null;

    /**
     * Constructs termination.
     *
     * @param noIterations size of the sample, i.e. number previous solutions values to take into account. If for example
     *                     noIterations = 10 then every 10th iteration the variationCoefficient will be calculated with the
     *                     last 10 solution values.
     * @param variationCoefficientThreshold the threshold used to terminate the algorithm. If the calculated variationCoefficient
     *                                      is smaller than the specified threshold, the algorithm terminates.
     */
	public VariationCoefficientTermination(int noIterations, double variationCoefficientThreshold) {
		super();
		this.noIterations = noIterations;
		this.variationCoefficientThreshold = variationCoefficientThreshold;
		solutionValues = new double[noIterations];
		logger.debug("initialise {}", this);
	}
	
	@Override
	public String toString() {
		return "[name=VariationCoefficientBreaker][variationCoefficientThreshold="+variationCoefficientThreshold+"][iterations="+ noIterations +"]";
	}

	@Override
	public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
		if(discoveredSolution.isAccepted()){
			lastAccepted = discoveredSolution.getSolution();
			solutionValues[currentIteration] = discoveredSolution.getSolution().getCost();
		}
		else{
			if(lastAccepted != null){
				solutionValues[currentIteration] = lastAccepted.getCost();
			} 
			else solutionValues[currentIteration] = Integer.MAX_VALUE;
		}
		if(currentIteration == (noIterations - 1)){
			double mean = StatUtils.mean(solutionValues);
			double stdDev = new StandardDeviation(true).evaluate(solutionValues, mean);
			double variationCoefficient = stdDev / mean;
			if(variationCoefficient < variationCoefficientThreshold){
				return true;
			}
		}
		return false;
	}
	
	private void reset(){
		currentIteration = 0;
	}

	@Override
	public void informAlgorithmStarts(VehicleRoutingProblem problem,VehicleRoutingAlgorithm algorithm,Collection<VehicleRoutingProblemSolution> solutions) {
		reset();
	}

	@Override
	public void informIterationEnds(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		if(currentIteration == (noIterations - 1)){
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
