package basics.algo;

import basics.algo.SearchStrategy.DiscoveredSolution;

public class IterationWithoutImprovementBreaker implements PrematureAlgorithmBreaker{

	private int nuOfIterationWithoutImprovement;
	
	private int iterationsWithoutImprovement = 0;
	
	public IterationWithoutImprovementBreaker(int nuOfIterationsWithoutImprovement){
		this.nuOfIterationWithoutImprovement=nuOfIterationsWithoutImprovement;
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
