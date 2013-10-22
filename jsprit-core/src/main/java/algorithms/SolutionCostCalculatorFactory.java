package algorithms;

import basics.VehicleRoutingProblemSolution;
import basics.algo.SolutionCostCalculator;
import basics.route.VehicleRoute;

public class SolutionCostCalculatorFactory {
	
	private StateManager stateManager;
	
	public SolutionCostCalculatorFactory(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	public SolutionCostCalculator createCalculator(){
		return new SolutionCostCalculator() {
			
			@Override
			public void calculateCosts(VehicleRoutingProblemSolution solution) {
				double c = 0.0;
				for(VehicleRoute r : solution.getRoutes()){
					c += stateManager.getRouteState(r, StateFactory.COSTS).toDouble();
					c += r.getVehicle().getType().getVehicleCostParams().fix;
				}
				solution.setCost(c);
			}
		};
	}

}
