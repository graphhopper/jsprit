package algorithms;

import java.util.ArrayList;
import java.util.List;

import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.Constraint;
import basics.algo.InsertionListener;
import basics.algo.VehicleRoutingAlgorithmListeners.PrioritizedVRAListener;
import basics.route.VehicleFleetManager;

public class BestInsertionBuilder implements InsertionStrategyBuilder{

	private VehicleRoutingProblem vrp;
	
	private StateManager stateManager;
	
	private boolean local = true;
	
	private ConstraintManager constraintManager;

	private VehicleFleetManager fleetManager;

	private double weightOfFixedCosts;

	private boolean considerFixedCosts = false;

	private ActivityInsertionCostsCalculator actInsertionCostsCalculator = null;

	private int forwaredLooking;

	private int memory;
	
	public BestInsertionBuilder(VehicleRoutingProblem vrp, VehicleFleetManager vehicleFleetManager, StateManager stateManager) {
		super();
		this.vrp = vrp;
		this.stateManager = stateManager;
		this.constraintManager = new ConstraintManager();
		this.fleetManager = vehicleFleetManager;
		addCoreStateUpdaters();
	}
	
	private void addCoreStateUpdaters(){
		stateManager.addListener(new UpdateLoadsAtStartAndEndOfRouteWhenInsertionStarts(stateManager));
		stateManager.addListener(new UpdateLoadsAtStartAndEndOfRouteWhenJobHasBeenInserted(stateManager));
		
		stateManager.addActivityVisitor(new UpdateMaxLoad(stateManager));
		stateManager.addActivityVisitor(new UpdateActivityTimes(vrp.getTransportCosts()));
		stateManager.addActivityVisitor(new UpdateCostsAtAllLevels(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));
	}
	
	public BestInsertionBuilder addHardLoadConstraints(){
		constraintManager.addConstraint(new HardPickupAndDeliveryLoadRouteLevelConstraint(stateManager));
		if(vrp.getProblemConstraints().contains(Constraint.DELIVERIES_FIRST)){
			constraintManager.addConstraint(new HardPickupAndDeliveryBackhaulActivityLevelConstraint(stateManager));
			constraintManager.addConstraint(new HardPickupAndDeliveryShipmentActivityLevelConstraint(stateManager,true));
		}
		else{
			constraintManager.addConstraint(new HardPickupAndDeliveryActivityLevelConstraint(stateManager));
			constraintManager.addConstraint(new HardPickupAndDeliveryShipmentActivityLevelConstraint(stateManager,true));
		}
		stateManager.addActivityVisitor(new UpdateOccuredDeliveriesAtActivityLevel(stateManager));
		stateManager.addActivityVisitor(new UpdateFuturePickupsAtActivityLevel(stateManager));
		return this;
	}
	
	public BestInsertionBuilder addHardTimeWindowConstraint(){
		constraintManager.addConstraint(new HardTimeWindowActivityLevelConstraint(stateManager, vrp.getTransportCosts()));
//		stateManager.addActivityVisitor(new UpdateEarliestStartTimeWindowAtActLocations(stateManager, vrp.getTransportCosts()));
		stateManager.addActivityVisitor(new UpdateLatestOperationStartTimeAtActLocations(stateManager, vrp.getTransportCosts()));
		return this;
	}
	

	public BestInsertionBuilder addConstraint(HardActivityLevelConstraint hardActvitiyLevelConstraint){
		constraintManager.addConstraint(hardActvitiyLevelConstraint);
		return this;
	};
	
	public BestInsertionBuilder addConstraint(HardRouteLevelConstraint hardRouteLevelConstraint){
		constraintManager.addConstraint(hardRouteLevelConstraint);
		return this;
	};
	
	public void setRouteLevel(int forwardLooking, int memory){
		local = false;
		this.forwaredLooking = forwardLooking;
		this.memory = memory;
	};
	
	public BestInsertionBuilder setLocalLevel(){
		local = true;
		return this;
	};
	
	public BestInsertionBuilder considerFixedCosts(double weightOfFixedCosts){
		this.weightOfFixedCosts = weightOfFixedCosts;
		this.considerFixedCosts  = true;
		return this;
	}
	
	public void setActivityInsertionCostCalculator(ActivityInsertionCostsCalculator activityInsertionCostsCalculator){
		this.actInsertionCostsCalculator = activityInsertionCostsCalculator;
	};
	
	@Override
	public InsertionStrategy build() {
		List<InsertionListener> iListeners = new ArrayList<InsertionListener>();
		List<PrioritizedVRAListener> algorithmListeners = new ArrayList<PrioritizedVRAListener>();
		CalculatorBuilder calcBuilder = new CalculatorBuilder(iListeners, algorithmListeners);
		if(local){
			calcBuilder.setLocalLevel();
		}
		else {
			calcBuilder.setRouteLevel(forwaredLooking, memory);
		}
		calcBuilder.setConstraintManager(constraintManager);
		calcBuilder.setStates(stateManager);
		calcBuilder.setVehicleRoutingProblem(vrp);
		calcBuilder.setVehicleFleetManager(fleetManager);
		calcBuilder.setActivityInsertionCostsCalculator(actInsertionCostsCalculator);
		if(considerFixedCosts) {
			calcBuilder.considerFixedCosts(weightOfFixedCosts);
		}
		JobInsertionCostsCalculator jobInsertions = calcBuilder.build();
		BestInsertion bestInsertion = new BestInsertion(jobInsertions);
		for(InsertionListener l : iListeners) bestInsertion.addListener(l); 
		return bestInsertion;
	}

}
