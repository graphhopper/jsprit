package algorithms;

import java.util.ArrayList;
import java.util.List;

import basics.VehicleRoutingProblem;
import basics.algo.InsertionListener;
import basics.algo.VehicleRoutingAlgorithmListeners.PrioritizedVRAListener;

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
		List<InsertionListener> iListeners = new ArrayList<InsertionListener>();
		List<PrioritizedVRAListener> algorithmListeners = new ArrayList<PrioritizedVRAListener>();
		CalculatorBuilder calcBuilder = new CalculatorBuilder(iListeners, algorithmListeners);
		if(local){
			calcBuilder.setLocalLevel();
		}
		calcBuilder.setConstraintManager(constraintManager);
		calcBuilder.setStates(stateManager);
		calcBuilder.setVehicleRoutingProblem(vrp);
		calcBuilder.setVehicleFleetManager(fleetManager);
		if(considerFixedCosts) calcBuilder.considerFixedCosts(weightOfFixedCosts);
		JobInsertionCalculator jobInsertions = calcBuilder.build();
		BestInsertion bestInsertion = new BestInsertion(jobInsertions);
		for(InsertionListener l : iListeners) bestInsertion.addListener(l); 
		return bestInsertion;
	}

}
