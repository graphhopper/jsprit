package algorithms;

import org.apache.log4j.Logger;

import basics.costs.VehicleRoutingTransportCosts;
import basics.route.TourActivity;

/**
	 * ljsljslfjs
	 * @author stefan
	 *
	 */
	class TimeWindowConstraint implements HardActivityStateLevelConstraint {

		private static Logger log = Logger.getLogger(TimeWindowConstraint.class);
		
		private StateGetter states;
		
		private VehicleRoutingTransportCosts routingCosts;
		
		public TimeWindowConstraint(StateGetter states, VehicleRoutingTransportCosts routingCosts) {
			super();
			this.states = states;
			this.routingCosts = routingCosts;
		}

		@Override
		public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
			if(newAct.getTheoreticalLatestOperationStartTime() < prevAct.getTheoreticalEarliestOperationStartTime()){
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
			if(newAct.getTheoreticalEarliestOperationStartTime() > nextAct.getTheoreticalLatestOperationStartTime()){
				return ConstraintsStatus.NOT_FULFILLED;
			}
			//			log.info("check insertion of " + newAct + " between " + prevAct + " and " + nextAct + ". prevActDepTime=" + prevActDepTime);
			double arrTimeAtNewAct = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
			double latestArrTimeAtNewAct = states.getActivityState(newAct, StateFactory.LATEST_OPERATION_START_TIME).toDouble();
			
			if(arrTimeAtNewAct > latestArrTimeAtNewAct){
				return ConstraintsStatus.NOT_FULFILLED;
			}
//			log.info(newAct + " arrTime=" + arrTimeAtNewAct);
			double endTimeAtNewAct = CalculationUtils.getActivityEndTime(arrTimeAtNewAct, newAct);
			double arrTimeAtNextAct = endTimeAtNewAct + routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), endTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
			double latestArrTimeAtNextAct = states.getActivityState(nextAct, StateFactory.LATEST_OPERATION_START_TIME).toDouble();
			if(arrTimeAtNextAct > latestArrTimeAtNextAct){
				return ConstraintsStatus.NOT_FULFILLED;
			}
			return ConstraintsStatus.FULFILLED;
		}
	}