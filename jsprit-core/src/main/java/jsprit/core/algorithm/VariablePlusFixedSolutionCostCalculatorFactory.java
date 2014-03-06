package jsprit.core.algorithm;

import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.solution.route.state.StateFactory;

public class VariablePlusFixedSolutionCostCalculatorFactory {
	
	private RouteAndActivityStateGetter stateManager;
	
	public VariablePlusFixedSolutionCostCalculatorFactory(RouteAndActivityStateGetter stateManager) {
		super();
		this.stateManager = stateManager;
	}

	public SolutionCostCalculator createCalculator(){
		return new SolutionCostCalculator() {
			
			@Override
			public double getCosts(VehicleRoutingProblemSolution solution) {
				double c = 0.0;
				for(VehicleRoute r : solution.getRoutes()){
					c += stateManager.getRouteState(r, StateFactory.COSTS,Double.class);
					c += r.getVehicle().getType().getVehicleCostParams().fix;
				}
				return c;
			}
		};
	}

}
