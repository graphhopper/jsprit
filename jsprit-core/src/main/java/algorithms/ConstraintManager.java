package algorithms;

import basics.VehicleRoutingProblem;
import basics.route.TourActivity;

public class ConstraintManager implements HardActivityStateLevelConstraint, HardRouteStateLevelConstraint{

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
			addConstraint(new TimeWindowConstraint(stateManager, vrp.getTransportCosts()));
			stateManager.addActivityVisitor(new TimeWindowUpdater(stateManager, vrp.getTransportCosts()));
			timeWindowConstraintsSet = true;
		}
	}

	public void addLoadConstraint(){
		if(!loadConstraintsSet){
			addConstraint(new ServiceLoadRouteLevelConstraint(stateManager));
			addConstraint(new ServiceLoadActivityLevelConstraint(stateManager));
			UpdateLoads updateLoads = new UpdateLoads(stateManager);
			stateManager.addActivityVisitor(updateLoads);
			stateManager.addListener(updateLoads);
			stateManager.addActivityVisitor(new UpdateFuturePickups(stateManager));
			stateManager.addActivityVisitor(new UpdateOccuredDeliveries(stateManager));
			loadConstraintsSet=true;
		}
	}
	
	public void addConstraint(HardActivityStateLevelConstraint actLevelConstraint){
		actLevelConstraintManager.addConstraint(actLevelConstraint);
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