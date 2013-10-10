package algorithms;

import basics.VehicleRoutingProblem;

public class BestInsertionBuilder implements InsertionStrategyBuilder{

	private VehicleRoutingProblem vrp;
	
	private StateManager stateManager;
	
	private boolean local = true;
	
	private ConstraintManager constraintManager;

	private VehicleFleetManager fleetManager;

	private double weightOfFixedCosts;

	private boolean considerFixedCosts = false;
	
	public BestInsertionBuilder(VehicleRoutingProblem vrp, StateManager stateManager) {
		super();
		this.vrp = vrp;
		this.stateManager = stateManager;
		this.constraintManager = new ConstraintManager();
	}

	public void addConstraint(HardActivityLevelConstraint hardActvitiyLevelConstraint){
		constraintManager.addConstraint(hardActvitiyLevelConstraint);
	};
	
	public void addConstraint(HardRouteLevelConstraint hardRouteLevelConstraint){
		constraintManager.addConstraint(hardRouteLevelConstraint);
	};
	
	//public void setRouteLevel(int forwardLooking, int memory){};
	
	public void setLocalLevel(){
		local = true;
	};
	
	public void considerFixedCosts(double weightOfFixedCosts){
		this.weightOfFixedCosts = weightOfFixedCosts;
		this.considerFixedCosts  = true;
	}
	
	public void setFleetManager(VehicleFleetManager fleetManager){
		this.fleetManager = fleetManager;
	}
	
	//public void setActivityInsertionCostCalculator(ActivityInsertionCostCalculator costCalc){};
	
	@Override
	public InsertionStrategy build() {
		CalculatorBuilder calcBuilder = new CalculatorBuilder(null, null);
		if(local){
			calcBuilder.setLocalLevel();
		}
		calcBuilder.setConstraintManager(constraintManager);
		calcBuilder.setStates(stateManager);
		calcBuilder.setVehicleRoutingProblem(vrp);
		calcBuilder.setVehicleFleetManager(fleetManager);
		if(considerFixedCosts) calcBuilder.considerFixedCosts(weightOfFixedCosts);
		JobInsertionCalculator jobInsertions = calcBuilder.build();
		return new BestInsertion(jobInsertions);
	}

}
