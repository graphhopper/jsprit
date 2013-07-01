package basics.algo;

import org.apache.log4j.Logger;

import basics.algo.SearchStrategy.DiscoveredSolution;

public class IterationWithoutImprovementBreaker implements PrematureAlgorithmBreaker{

	private static Logger log = Logger.getLogger(IterationWithoutImprovementBreaker.class);
	
	private int nuOfIterationWithoutImprovement;
	
	private int iterationsWithoutImprovement = 0;
	
	public IterationWithoutImprovementBreaker(int nuOfIterationsWithoutImprovement){
		this.nuOfIterationWithoutImprovement=nuOfIterationsWithoutImprovement;
		log.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[name=IterationWithoutImprovementBreaker][iterationsWithoutImprovement="+nuOfIterationWithoutImprovement+"]";
	}
	
	@Override
	public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
		if(discoveredSolution.isAccepted()) iterationsWithoutImprovement = 0;
		else iterationsWithoutImprovement++;
		if(iterationsWithoutImprovement > nuOfIterationWithoutImprovement){
			return true;
		}
		return false;
	}

	
}
