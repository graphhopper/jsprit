/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms;

import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.TourActivity;

/**
 * Calculates activity insertion costs locally, i.e. by comparing the additional costs of insertion the new activity k between
 * activity i (prevAct) and j (nextAct).
 * Additional costs are then basically calculated as delta c = c_ik + c_kj - c_ij.
 * 
 * <p>Note once time has an effect on costs this class requires activity endTimes.
 * 
 * @author stefan
 *
 */
class LocalActivityInsertionCostsCalculator implements ActivityInsertionCostsCalculator{

	private VehicleRoutingTransportCosts routingCosts;
	private VehicleRoutingActivityCosts activityCosts;
	
	
	public LocalActivityInsertionCostsCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts actCosts) {
		super();
		this.routingCosts = routingCosts;
		this.activityCosts = actCosts;
	}

	@Override
	public ActivityInsertionCosts calculate(InsertionContext iFacts, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, double depTimeAtPrevAct) {
		
		double tp_costs_prevAct_newAct = routingCosts.getTransportCost(prevAct.getLocationId(), newAct.getLocationId(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
		double tp_time_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double newAct_arrTime = depTimeAtPrevAct + tp_time_prevAct_newAct;
		
		double newAct_endTime = CalculationUtils.getActivityEndTime(newAct_arrTime, newAct);
		
		double act_costs_newAct = activityCosts.getActivityCost(newAct, newAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double tp_costs_newAct_nextAct = routingCosts.getTransportCost(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		double tp_time_newAct_nextAct = routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double nextAct_arrTime = newAct_endTime + tp_time_newAct_nextAct;
				
		double act_costs_nextAct = activityCosts.getActivityCost(nextAct, nextAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
		
		double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct + act_costs_newAct + act_costs_nextAct; 
		
		double oldCosts;
		double oldTime;
		if(iFacts.getRoute().isEmpty()){
			oldCosts = 0.0;
			oldTime = 0.0;
		}
		else{
			double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocationId(), nextAct.getLocationId(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
			double arrTime_nextAct = routingCosts.getTransportTime(prevAct.getLocationId(), nextAct.getLocationId(), prevAct.getEndTime(), iFacts.getNewDriver(), iFacts.getNewVehicle());
			
			double actCost_nextAct = activityCosts.getActivityCost(nextAct, arrTime_nextAct, iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
			oldCosts = tp_costs_prevAct_nextAct + actCost_nextAct;
			oldTime = (nextAct.getArrTime() - iFacts.getRoute().getDepartureTime());
		}
		
		double additionalCosts = totalCosts - oldCosts;
		double additionalTime = (nextAct_arrTime - iFacts.getNewDepTime()) - oldTime;

		return new ActivityInsertionCosts(additionalCosts,additionalTime);
	}

}
