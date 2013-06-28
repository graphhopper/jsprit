package basics.algo;

import java.util.Collection;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;

import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.SearchStrategy.DiscoveredSolution;

public class VariationCoefficientBreaker implements PrematureAlgorithmBreaker, AlgorithmStartsListener, IterationEndsListener{

	private static Logger logger = Logger.getLogger(VariationCoefficientBreaker.class);
	
	private int nuOfIterations;
	
	private double variationCoefficientThreshold;
	
	private int currentIteration;
	
	private double[] solutionValues;
	
	private VehicleRoutingProblemSolution lastAccepted = null;
	
	public VariationCoefficientBreaker(int nuOfIterations, double variationCoefficientThreshold) {
		super();
		this.nuOfIterations = nuOfIterations;
		this.variationCoefficientThreshold = variationCoefficientThreshold;
		solutionValues = new double[nuOfIterations];
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
			else solutionValues[currentIteration]=Double.MAX_VALUE; 
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

	

}
