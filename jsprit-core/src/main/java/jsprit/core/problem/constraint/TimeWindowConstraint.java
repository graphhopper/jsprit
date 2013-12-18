package jsprit.core.problem.constraint;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.util.CalculationUtils;


/**
	 * ljsljslfjs
	 * @author stefan
	 *
	 */
	class TimeWindowConstraint implements HardActivityStateLevelConstraint {

		private RouteAndActivityStateGetter states;
		
		private VehicleRoutingTransportCosts routingCosts;
		
		public TimeWindowConstraint(RouteAndActivityStateGetter states, VehicleRoutingTransportCosts routingCosts) {
			super();
			this.states = states;
			this.routingCosts = routingCosts;
		}

		@Override
		public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
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
			double arrTimeAtNextOnDirectRouteWithNewVehicle = prevActDepTime + routingCosts.getTransportCost(prevAct.getLocationId(), nextAct.getLocationId(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle()); 
			//if vehicle cannot even manage direct-route - break
			if(arrTimeAtNextOnDirectRouteWithNewVehicle > latestArrTimeAtNextAct){
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
			return ConstraintsStatus.FULFILLED;
		}
	}