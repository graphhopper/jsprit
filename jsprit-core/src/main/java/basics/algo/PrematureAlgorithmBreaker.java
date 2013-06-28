package basics.algo;

import basics.algo.SearchStrategy.DiscoveredSolution;

public interface PrematureAlgorithmBreaker {
	
	public boolean isPrematureBreak(DiscoveredSolution discoveredSolution);

}
