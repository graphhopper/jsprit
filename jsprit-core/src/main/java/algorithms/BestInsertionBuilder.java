package algorithms;

import basics.VehicleRoutingProblem;
import algorithms.HardConstraints.HardActivityLevelConstraint;
import algorithms.HardConstraints.HardRouteLevelConstraint;

public class BestInsertionBuilder implements InsertionStrategyBuilder{

	private VehicleRoutingProblem vrp;
	
	private StateManager stateManager;
	
	public BestInsertionBuilder(VehicleRoutingProblem vrp, StateManager stateManager) {
		super();
		this.vrp = vrp;
		this.stateManager = stateManager;
	}

	public void addConstraint(HardActivityLevelConstraint hardActvitiyLevelConstraint){};
	
	public void addConstraint(HardRouteLevelConstraint hardRouteLevelConstraint){};
	
	public void setRouteLevel(int forwardLooking, int memory){};
	
	public void setLocalLevel(){};
	
	public void setActivityInsertionCostCalculator(ActivityInsertionCostCalculator costCalc){};
	
	@Override
	public InsertionStrategy build() {
		return null;
	}

}
