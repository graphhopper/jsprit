package algorithms;

import basics.route.DeliveryActivity;
import basics.route.PickupActivity;
import basics.route.ServiceActivity;
import basics.route.Start;
import basics.route.TourActivity;

public class HardPickupAndDeliveryActivityLevelConstraint implements HardActivityLevelConstraint {
	
	private StateManager stateManager;
	
	public HardPickupAndDeliveryActivityLevelConstraint(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		int loadAtPrevAct;
		int futurePicks;
		int pastDeliveries;
		if(prevAct instanceof Start){
			loadAtPrevAct = (int)stateManager.getRouteState(iFacts.getRoute(), StateIdFactory.LOAD_AT_DEPOT).toDouble();
			futurePicks = (int)stateManager.getRouteState(iFacts.getRoute(), StateIdFactory.LOAD).toDouble();
			pastDeliveries = 0;
		}
		else{
			loadAtPrevAct = (int) stateManager.getActivityState(prevAct, StateIdFactory.LOAD).toDouble();
			futurePicks = (int) stateManager.getActivityState(prevAct, StateIdFactory.FUTURE_PICKS).toDouble();
			pastDeliveries = (int) stateManager.getActivityState(prevAct, StateIdFactory.PAST_DELIVERIES).toDouble();
		}
		if(newAct instanceof PickupActivity || newAct instanceof ServiceActivity){
			if(loadAtPrevAct + newAct.getCapacityDemand() + futurePicks > iFacts.getNewVehicle().getCapacity()){
				return false;
			}
		}
		if(newAct instanceof DeliveryActivity){
			if(loadAtPrevAct + Math.abs(newAct.getCapacityDemand()) + pastDeliveries > iFacts.getNewVehicle().getCapacity()){
				return false;
			}
			
		}
		return true;
	}
		
}