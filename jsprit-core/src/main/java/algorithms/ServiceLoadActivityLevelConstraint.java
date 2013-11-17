package algorithms;

import org.apache.log4j.Logger;

import basics.route.DeliverService;
import basics.route.PickupService;
import basics.route.ServiceActivity;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

/**
 * Ensures load constraint for inserting ServiceActivity.
 * 
 * <p>When using this, you need to use<br>
 * 
 * 
 * @author schroeder
 *
 */
class ServiceLoadActivityLevelConstraint implements HardActivityStateLevelConstraint {
	
	private static Logger log = Logger.getLogger(ServiceLoadActivityLevelConstraint.class);
	
	private StateGetter stateManager;
	
	public ServiceLoadActivityLevelConstraint(StateGetter stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		int futureMaxLoad;
		int prevMaxLoad;
		if(prevAct instanceof Start){
			futureMaxLoad = (int)stateManager.getRouteState(iFacts.getRoute(), StateFactory.MAXLOAD).toDouble();
			prevMaxLoad = (int)stateManager.getRouteState(iFacts.getRoute(), StateFactory.LOAD_AT_BEGINNING).toDouble();
		}
		else{
			futureMaxLoad = (int) stateManager.getActivityState(prevAct, StateFactory.FUTURE_PICKS).toDouble();
			prevMaxLoad = (int) stateManager.getActivityState(prevAct, StateFactory.PAST_DELIVERIES).toDouble();
			
		}
		if(newAct instanceof PickupService || newAct instanceof ServiceActivity){
			if(newAct.getCapacityDemand() + futureMaxLoad > iFacts.getNewVehicle().getCapacity()){
//				log.debug("insertionOf("+newAct+").BETWEEN("+prevAct+").AND("+nextAct+")=NOT_POSSIBLE");
				return ConstraintsStatus.NOT_FULFILLED;
			}
		}
		if(newAct instanceof DeliverService){
			if(Math.abs(newAct.getCapacityDemand()) + prevMaxLoad > iFacts.getNewVehicle().getCapacity()){
//				log.debug("insertionOf("+newAct+").BETWEEN("+prevAct+").AND("+nextAct+")=NOT_POSSIBLE[break=neverBePossibleAnymore]");
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
			
		}
//		log.debug("insertionOf("+newAct+").BETWEEN("+prevAct+").AND("+nextAct+")=POSSIBLE");
		return ConstraintsStatus.FULFILLED;
	}		
}