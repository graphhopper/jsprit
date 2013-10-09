package algorithms;

import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.FleetSize;

public class BestInsertionBuilder implements InsertionStrategyBuilder{

	private VehicleRoutingProblem vrp;
	
	private StateManager stateManager;
	
	private boolean local = true;
	
	private ConstraintManager constraintManager;

	private VehicleFleetManager fleetManager;
	
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
		JobInsertionCalculator jobInsertions = calcBuilder.build();
		return new BestInsertion(jobInsertions);
	}

	private VehicleFleetManager createFleetManager(VehicleRoutingProblem vrp) {
		if(vrp.getFleetSize().equals(FleetSize.INFINITE)){
			return new InfiniteVehicles(vrp.getVehicles());
		}
		else return new VehicleFleetManagerImpl(vrp.getVehicles());
	}

}
