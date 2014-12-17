/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.constraint;

import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.problem.Location;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.util.CalculationUtils;


/**
	 * 
	 * @author stefan
	 *
	 */
	class TimeWindowConstraint implements HardActivityConstraint {

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
            Double latestArrTimeAtNextAct;
            Location nextActLocation;
            if(nextAct instanceof End) {
                latestArrTimeAtNextAct = latestVehicleArrival;
                nextActLocation = iFacts.getNewVehicle().getEndLocation();
                if(!iFacts.getNewVehicle().isReturnToDepot()){
                    nextActLocation = newAct.getLocation();
                }
            }
            else{
                latestArrTimeAtNextAct = states.getActivityState(nextAct, InternalStates.LATEST_OPERATION_START_TIME, Double.class);
                if(latestArrTimeAtNextAct==null) latestArrTimeAtNextAct=nextAct.getTheoreticalLatestOperationStartTime();
                nextActLocation = nextAct.getLocation();
            }

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
			double arrTimeAtNextOnDirectRouteWithNewVehicle = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
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
//            double latestArrTimeAtNextAct = states.getActivityState(nextAct, StateFactory.LATEST_OPERATION_START_TIME, Double.class);
            double arrTimeAtNewAct = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocation(), newAct.getLocation(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());


            double latestArrTimeAtNewAct = Math.min(newAct.getTheoreticalLatestOperationStartTime(),latestArrTimeAtNextAct -
                    routingCosts.getBackwardTransportTime(nextActLocation, newAct.getLocation(), latestArrTimeAtNextAct, iFacts.getNewDriver(),
                            iFacts.getNewVehicle()) - newAct.getOperationTime());
			/*
			 *  |--- prevAct ---|
			 *                       		                 |--- vehicle's arrival @newAct
			 *        latest arrival of vehicle @newAct ---|                     
			 */
			if(arrTimeAtNewAct > latestArrTimeAtNewAct){
				return ConstraintsStatus.NOT_FULFILLED;
			}
			
			if(nextAct instanceof End){
				if(!iFacts.getNewVehicle().isReturnToDepot()){
					return ConstraintsStatus.FULFILLED;
				}
			}
//			log.info(newAct + " arrTime=" + arrTimeAtNewAct);
			double endTimeAtNewAct = CalculationUtils.getActivityEndTime(arrTimeAtNewAct, newAct);
			double arrTimeAtNextAct = endTimeAtNewAct + routingCosts.getTransportTime(newAct.getLocation(), nextAct.getLocation(), endTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());

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
