package algorithms;

import basics.route.DeliveryActivity;
import basics.route.PickupActivity;
import basics.route.ServiceActivity;
import basics.route.Start;
import basics.route.TourActivity;

class HardPickupAndDeliveryActivityLevelConstraint implements HardActivityLevelConstraint {
	
	private StateGetter stateManager;
	
	public HardPickupAndDeliveryActivityLevelConstraint(StateGetter stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		int loadAtPrevAct;
		int futurePicks;
		int pastDeliveries;
		if(prevAct instanceof Start){
			loadAtPrevAct = (int)stateManager.getRouteState(iFacts.getRoute(), StateFactory.LOAD_AT_BEGINNING).toDouble();
			futurePicks = (int)stateManager.getRouteState(iFacts.getRoute(), StateFactory.LOAD_AT_END).toDouble();
			pastDeliveries = 0;
		}
		else{
			loadAtPrevAct = (int) stateManager.getActivityState(prevAct, StateFactory.LOAD).toDouble();
			futurePicks = (int) stateManager.getActivityState(prevAct, StateFactory.FUTURE_PICKS).toDouble();
			pastDeliveries = (int) stateManager.getActivityState(prevAct, StateFactory.PAST_DELIVERIES).toDouble();
		}
		if(newAct instanceof PickupActivity || newAct instanceof ServiceActivity){
			if(loadAtPrevAct + newAct.getCapacityDemand() + futurePicks > iFacts.getNewVehicle().getCapacity()){
				return ConstraintsStatus.NOT_FULFILLED;
			}
		}
		if(newAct instanceof DeliveryActivity){
			if(loadAtPrevAct + Math.abs(newAct.getCapacityDemand()) + pastDeliveries > iFacts.getNewVehicle().getCapacity()){
				return ConstraintsStatus.NOT_FULFILLED;
			}
			
		}
		return ConstraintsStatus.FULFILLED;
	}
		
}