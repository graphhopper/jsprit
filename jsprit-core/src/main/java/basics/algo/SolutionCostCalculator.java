package basics.algo;

import basics.VehicleRoutingProblemSolution;

public interface SolutionCostCalculator {
	
	/**
	 * This assumes that the solution is modified by setting its costs <br>
	 * <code>solution.setCost(costs);</code>
	 * @param solution
	 */
	public void calculateCosts(VehicleRoutingProblemSolution solution);

}
