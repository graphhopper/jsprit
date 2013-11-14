package algorithms;

import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.Constraint;
import basics.route.TourActivity;

public class ConstraintManager implements HardActivityStateLevelConstraint, HardRouteStateLevelConstraint{

	public static enum Priority {
		CRITICAL, HIGH, LOW
	}
	
	private HardActivityLevelConstraintManager actLevelConstraintManager = new HardActivityLevelConstraintManager();
	
	private HardRouteLevelConstraintManager routeLevelConstraintManager = new HardRouteLevelConstraintManager();
	
	private VehicleRoutingProblem vrp;
	
	private StateManager stateManager;
	
	private boolean loadConstraintsSet = false;
	
	private boolean timeWindowConstraintsSet = false;
	
	public ConstraintManager(VehicleRoutingProblem vrp, StateManager stateManager) {
		this.vrp = vrp;
		this.stateManager = stateManager;
	}
	
	public void addTimeWindowConstraint(){
		if(!timeWindowConstraintsSet){
			addConstraint(new TimeWindowConstraint(stateManager, vrp.getTransportCosts()),Priority.HIGH);
			stateManager.addActivityVisitor(new TimeWindowUpdater(stateManager, vrp.getTransportCosts()));
			timeWindowConstraintsSet = true;
		}
	}

	public void addLoadConstraint(){
		if(!loadConstraintsSet){
			if(vrp.getProblemConstraints().contains(Constraint.DELIVERIES_FIRST)){
				addConstraint(new ServiceBackhaulConstraint(),Priority.HIGH);
			}
			addConstraint(new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager),Priority.CRITICAL);
			addConstraint(new ServiceLoadRouteLevelConstraint(stateManager));
			addConstraint(new ServiceLoadActivityLevelConstraint(stateManager),Priority.LOW);
			UpdateLoads updateLoads = new UpdateLoads(stateManager);
			stateManager.addActivityVisitor(updateLoads);
			stateManager.addListener(updateLoads);
			stateManager.addActivityVisitor(new UpdateFuturePickups(stateManager));
			stateManager.addActivityVisitor(new UpdateOccuredDeliveries(stateManager));
			loadConstraintsSet=true;
		}
	}
	
	public void addConstraint(HardActivityStateLevelConstraint actLevelConstraint, Priority priority){
		actLevelConstraintManager.addConstraint(actLevelConstraint,priority);
	}
	
	public void addConstraint(HardRouteStateLevelConstraint routeLevelConstraint){
		routeLevelConstraintManager.addConstraint(routeLevelConstraint);
	}
	
	@Override
	public boolean fulfilled(InsertionContext insertionContext) {
		return routeLevelConstraintManager.fulfilled(insertionContext);
	}

	@Override
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct,TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		return actLevelConstraintManager.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
	}
	
}