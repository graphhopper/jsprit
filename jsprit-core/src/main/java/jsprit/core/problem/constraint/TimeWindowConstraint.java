package jsprit.core.problem.constraint;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.util.CalculationUtils;


/**
	 * 
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
			double latestVehicleArrival = iFacts.getNewVehicle().getLatestArrival();
			/*
			 * if latest arrival of vehicle (at its end) is smaller than earliest operation start times of activities,
			 * then vehicle can never conduct activities.
			 * 
			 *     |--- vehicle's operation time ---|
			 *                        					|--- prevAct or newAct or nextAct ---|
			 */
			if(latestVehicleArrival < prevAct.getTheoreticalEarliestOperationStartTime() || 
					latestVehicleArrival < newAct.getTheoreticalEarliestOperationStartTime() ||
						latestVehicleArrival < nextAct.getTheoreticalEarliestOperationStartTime()){
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
			/*
			 * if the latest operation start-time of new activity is smaller than the earliest start of prev. activity,
			 * then 
			 * 
			 *                    |--- prevAct ---|
			 *  |--- newAct ---|
			 */
			if(newAct.getTheoreticalLatestOperationStartTime() < prevAct.getTheoreticalEarliestOperationStartTime()){
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
			
			/*
			 *  |--- prevAct ---|
			 *                                          |- earliest arrival of vehicle
			 *                       |--- nextAct ---|
			 */
			double arrTimeAtNextOnDirectRouteWithNewVehicle = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocationId(), nextAct.getLocationId(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
			if(arrTimeAtNextOnDirectRouteWithNewVehicle > nextAct.getTheoreticalLatestOperationStartTime()){
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
			/*
			 *                     |--- newAct ---|
			 *  |--- nextAct ---|
			 */
			if(newAct.getTheoreticalEarliestOperationStartTime() > nextAct.getTheoreticalLatestOperationStartTime()){
				return ConstraintsStatus.NOT_FULFILLED;
			}
			//			log.info("check insertion of " + newAct + " between " + prevAct + " and " + nextAct + ". prevActDepTime=" + prevActDepTime);
			double arrTimeAtNewAct = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
			double latestArrTimeAtNewAct = states.getActivityState(newAct, StateFactory.LATEST_OPERATION_START_TIME, Double.class);
			/*
			 *  |--- prevAct ---|
			 *                       		                 |--- vehicle's arrival @newAct
			 *        latest arrival of vehicle @newAct ---|                     
			 */
			if(arrTimeAtNewAct > latestArrTimeAtNewAct){
				return ConstraintsStatus.NOT_FULFILLED;
			}
//			log.info(newAct + " arrTime=" + arrTimeAtNewAct);
			double endTimeAtNewAct = CalculationUtils.getActivityEndTime(arrTimeAtNewAct, newAct);
			double arrTimeAtNextAct = endTimeAtNewAct + routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), endTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
			double latestArrTimeAtNextAct = states.getActivityState(nextAct, StateFactory.LATEST_OPERATION_START_TIME, Double.class);
			/*
			 *  |--- newAct ---|
			 *                       		                 |--- vehicle's arrival @nextAct
			 *        latest arrival of vehicle @nextAct ---|                     
			 */
			if(arrTimeAtNextAct > latestArrTimeAtNextAct){
				return ConstraintsStatus.NOT_FULFILLED;
			}
			
//			if vehicle cannot even manage direct-route - break
			if(arrTimeAtNextOnDirectRouteWithNewVehicle > latestArrTimeAtNextAct){
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
			return ConstraintsStatus.FULFILLED;
		}
	}