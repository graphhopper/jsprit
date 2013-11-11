package algorithms;

import org.apache.log4j.Logger;

import algorithms.HardActivityStateLevelConstraint.ConstraintsStatus;
import basics.route.DeliverShipment;
import basics.route.PickupShipment;
import basics.route.Start;
import basics.route.TourActivity;

public class HardPickupAndDeliveryShipmentActivityLevelConstraint implements HardActivityStateLevelConstraint {
	
	private static Logger logger = Logger.getLogger(HardPickupAndDeliveryShipmentActivityLevelConstraint.class);
	
	private StateManager stateManager;
	
	private boolean backhaul = false;
	
	public HardPickupAndDeliveryShipmentActivityLevelConstraint(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	public HardPickupAndDeliveryShipmentActivityLevelConstraint(StateManager stateManager, boolean backhaul) {
		super();
		this.stateManager = stateManager;
		this.backhaul = backhaul;
	}
	
	@Override
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
//		logger.info(prevAct + " - " + newAct + " - " + nextAct);
		if(!(newAct instanceof PickupShipment) && !(newAct instanceof DeliverShipment)){
			return ConstraintsStatus.FULFILLED;
		}
		if(backhaul){
			if(newAct instanceof PickupShipment && prevAct instanceof DeliverShipment){ 
//				logger.info("NOT_FULFILLED_BREAK");
				return ConstraintsStatus.NOT_FULFILLED_BREAK; }
			if(newAct instanceof DeliverShipment && nextAct instanceof PickupShipment){ 
//				logger.info("NOT_FULFILLED");
				return ConstraintsStatus.NOT_FULFILLED; }
		}
		int loadAtPrevAct;
//		int futurePicks;
//		int pastDeliveries;
		
		if(prevAct instanceof Start){
			loadAtPrevAct = (int)stateManager.getRouteState(iFacts.getRoute(), StateFactory.LOAD_AT_BEGINNING).toDouble();
//			futurePicks = (int)stateManager.getRouteState(iFacts.getRoute(), StateTypes.LOAD).toDouble();
//			pastDeliveries = 0;
		}
		else{
			loadAtPrevAct = (int) stateManager.getActivityState(prevAct, StateFactory.LOAD).toDouble();
//			futurePicks = (int) stateManager.getActivityState(prevAct, StateTypes.FUTURE_PICKS).toDouble();
//			pastDeliveries = (int) stateManager.getActivityState(prevAct, StateTypes.PAST_DELIVERIES).toDouble();
		}
		if(newAct instanceof PickupShipment){
			if(loadAtPrevAct + newAct.getCapacityDemand() > iFacts.getNewVehicle().getCapacity()){
//				logger.info("NOT_FULFILLED");
				return ConstraintsStatus.NOT_FULFILLED;
			}
		}
		if(newAct instanceof DeliverShipment){
			if(loadAtPrevAct + Math.abs(newAct.getCapacityDemand()) > iFacts.getNewVehicle().getCapacity()){
//				logger.info("NOT_FULFILLED_BREAK");
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
			
		}
//		logger.info("FULFILLED");
		return ConstraintsStatus.FULFILLED;
	}
	
		
}