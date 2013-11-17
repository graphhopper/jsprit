package algorithms;

import org.apache.log4j.Logger;

import basics.route.DeliverShipment;
import basics.route.PickupShipment;
import basics.route.Start;
import basics.route.TourActivity;

/**
 * Constraint that ensures capacity constraint at each activity.
 * 
 * <p>This is critical to consistently calculate pd-problems with capacity constraints. Critical means
 * that is MUST be visited. It also assumes that pd-activities are visited in the order they occur in a tour.
 * 
 * @author schroeder
 *
 */
public class PickupAndDeliverShipmentLoadActivityLevelConstraint implements HardActivityStateLevelConstraint {
	
	private static Logger logger = Logger.getLogger(PickupAndDeliverShipmentLoadActivityLevelConstraint.class);
	
	private StateManager stateManager;
	
	/**
	 * Constructs the constraint ensuring capacity constraint at each activity.
	 * 
	 * <p>This is critical to consistently calculate pd-problems with capacity constraints. Critical means
	 * that is MUST be visited. It also assumes that pd-activities are visited in the order they occur in a tour.
	 * 
	 * 
	 * @param stateManager
	 */
	public PickupAndDeliverShipmentLoadActivityLevelConstraint(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}
	
	@Override
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		if(!(newAct instanceof PickupShipment) && !(newAct instanceof DeliverShipment)){
			return ConstraintsStatus.FULFILLED;
		}
		int loadAtPrevAct;
		if(prevAct instanceof Start){
			loadAtPrevAct = (int)stateManager.getRouteState(iFacts.getRoute(), StateFactory.LOAD_AT_BEGINNING).toDouble();
		}
		else{
			loadAtPrevAct = (int) stateManager.getActivityState(prevAct, StateFactory.LOAD).toDouble();
		}
		if(newAct instanceof PickupShipment){
			if(loadAtPrevAct + newAct.getCapacityDemand() > iFacts.getNewVehicle().getCapacity()){
				return ConstraintsStatus.NOT_FULFILLED;
			}
		}
		if(newAct instanceof DeliverShipment){
			if(loadAtPrevAct + Math.abs(newAct.getCapacityDemand()) > iFacts.getNewVehicle().getCapacity()){
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
		}
		return ConstraintsStatus.FULFILLED;
	}
	
		
}