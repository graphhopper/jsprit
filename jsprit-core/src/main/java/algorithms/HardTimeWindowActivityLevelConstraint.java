package algorithms;

import org.apache.log4j.Logger;

import basics.costs.VehicleRoutingTransportCosts;
import basics.route.TourActivity;

/**
	 * ljsljslfjs
	 * @author stefan
	 *
	 */
	class HardTimeWindowActivityLevelConstraint implements HardActivityLevelConstraint {

		private static Logger log = Logger.getLogger(HardTimeWindowActivityLevelConstraint.class);
		
		private StateGetter states;
		
		private VehicleRoutingTransportCosts routingCosts;
		
		public HardTimeWindowActivityLevelConstraint(StateGetter states, VehicleRoutingTransportCosts routingCosts) {
			super();
			this.states = states;
			this.routingCosts = routingCosts;
		}

		@Override
		public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
//			log.info("check insertion of " + newAct + " between " + prevAct + " and " + nextAct + ". prevActDepTime=" + prevActDepTime);
			double arrTimeAtNewAct = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
			double latestArrTimeAtNewAct = states.getActivityState(newAct, StateIdFactory.LATEST_OPERATION_START_TIME).toDouble();
			if(arrTimeAtNewAct > latestArrTimeAtNewAct){
				return false;
			}
//			log.info(newAct + " arrTime=" + arrTimeAtNewAct);
			double endTimeAtNewAct = CalculationUtils.getActivityEndTime(arrTimeAtNewAct, newAct);
			double arrTimeAtNextAct = endTimeAtNewAct + routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), endTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
			double latestArrTimeAtNextAct = states.getActivityState(nextAct, StateIdFactory.LATEST_OPERATION_START_TIME).toDouble();
			if(arrTimeAtNextAct > latestArrTimeAtNextAct){
				return false;
			}
//			log.info(nextAct + " arrTime=" + arrTimeAtNextAct);
			return true;
		}
	}