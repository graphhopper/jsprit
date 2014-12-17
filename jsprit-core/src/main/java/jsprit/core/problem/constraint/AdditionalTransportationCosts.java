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

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.util.CalculationUtils;

/**
 * Calculates additional transportation costs induced by inserting newAct.
 * 
 * @author schroeder
 *
 */
class AdditionalTransportationCosts implements SoftActivityConstraint{

	private VehicleRoutingTransportCosts routingCosts;
	
	/**
	 * Constructs the calculator that calculates additional transportation costs induced by inserting new activity.
	 *
	 * <p>It is calculated at local level, i.e. the additional costs of inserting act_new between act_i and act_j is c(act_i,act_new,newVehicle)+c(act_new,act_j,newVehicle)-c(act_i,act_j,oldVehicle)
	 * <p>If newVehicle.isReturnToDepot == false then the additional costs of inserting act_new between act_i and end is c(act_i,act_new) [since act_new is then the new end-of-route]
	 *
	 * @param routingCosts
	 */
	public AdditionalTransportationCosts(VehicleRoutingTransportCosts routingCosts) {
		super();
		this.routingCosts = routingCosts;
	}

	/**
	 * Returns additional transportation costs induced by inserting newAct.
	 * 
	 * <p>It is calculated at local level, i.e. the additional costs of inserting act_new between act_i and act_j is c(act_i,act_new,newVehicle)+c(act_new,act_j,newVehicle)-c(act_i,act_j,oldVehicle)
	 * <p>If newVehicle.isReturnToDepot == false then the additional costs of inserting act_new between act_i and end is c(act_i,act_new) [since act_new is then the new end-of-route]
	 */
	@Override
	public double getCosts(JobInsertionContext iFacts, TourActivity prevAct,TourActivity newAct, TourActivity nextAct, double depTimeAtPrevAct) {
		double tp_costs_prevAct_newAct = routingCosts.getTransportCost(prevAct.getLocation(), newAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
		double tp_time_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocation(), newAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double newAct_arrTime = depTimeAtPrevAct + tp_time_prevAct_newAct;
		double newAct_endTime = CalculationUtils.getActivityEndTime(newAct_arrTime, newAct);
		
		//open routes
		if(nextAct instanceof End){
			if(!iFacts.getNewVehicle().isReturnToDepot()){
				return tp_costs_prevAct_newAct;
			}
		}
		
		double tp_costs_newAct_nextAct = routingCosts.getTransportCost(newAct.getLocation(), nextAct.getLocation(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct; 
		
		double oldCosts;
		if(iFacts.getRoute().isEmpty()){
			double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocation(), nextAct.getLocation(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
			oldCosts = tp_costs_prevAct_nextAct;
		}
		else{
			double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocation(), nextAct.getLocation(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
			oldCosts = tp_costs_prevAct_nextAct;
		}
		
		double additionalCosts = totalCosts - oldCosts;
		return additionalCosts;
	}

}
