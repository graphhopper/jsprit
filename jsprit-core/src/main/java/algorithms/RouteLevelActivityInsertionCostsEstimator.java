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

import java.util.ArrayList;
import java.util.List;

import algorithms.HardConstraints.HardActivityLevelConstraint;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.End;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class RouteLevelActivityInsertionCostsEstimator implements ActivityInsertionCostsCalculator{

	private HardActivityLevelConstraint hardConstraint;

	private VehicleRoutingTransportCosts routingCosts;
	
	private VehicleRoutingActivityCosts activityCosts;
	
	private AuxilliaryCostCalculator auxilliaryPathCostCalculator;
	
	private StateManager stateManager;
	
	private int nuOfActivities2LookForward = 0;
	
	public RouteLevelActivityInsertionCostsEstimator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts actCosts, HardActivityLevelConstraint hardActivityLevelConstraint, StateManager stateManager) {
		super();
		this.routingCosts = routingCosts;
		this.activityCosts = actCosts;
		this.hardConstraint = hardActivityLevelConstraint;
		auxilliaryPathCostCalculator = new AuxilliaryCostCalculator(routingCosts, activityCosts);
	}

	@Override
	public ActivityInsertionCosts calculate(InsertionContext iFacts, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, double depTimeAtPrevAct) {
		if(!hardConstraint.fulfilled(iFacts, prevAct, newAct, nextAct, depTimeAtPrevAct)){
			return null;
		}
		
		List<TourActivity> path = new ArrayList<TourActivity>();
		path.add(prevAct); path.add(newAct); path.add(nextAct);
		int actIndex;
		if(prevAct instanceof Start) actIndex = 0;
		else actIndex = iFacts.getRoute().getTourActivities().getActivities().indexOf(prevAct);
		if(nuOfActivities2LookForward > 0){ path.addAll(getForwardLookingPath(iFacts.getRoute(),actIndex)); }

		/**
		 * calculates the path costs with new vehicle, c(forwardPath,newVehicle).
		 */
		double forwardPathCost_newVehicle = auxilliaryPathCostCalculator.costOfPath(path, depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle()); 

		double additionalCosts = forwardPathCost_newVehicle - (actCostsOld(iFacts.getRoute(), path.get(path.size()-1)) - actCostsOld(iFacts.getRoute(), prevAct));
		
		return new ActivityInsertionCosts(additionalCosts, 0.0);
		
		
//		
//		
//		double tp_costs_prevAct_newAct = routingCosts.getTransportCost(prevAct.getLocationId(), newAct.getLocationId(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
//		double tp_time_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
//		
//		double newAct_arrTime = depTimeAtPrevAct + tp_time_prevAct_newAct;
//		
//		double newAct_endTime = CalcUtils.getActivityEndTime(newAct_arrTime, newAct);
//		
//		double act_costs_newAct = activityCosts.getActivityCost(newAct, newAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
//		
//		double tp_costs_newAct_nextAct = routingCosts.getTransportCost(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
//		double tp_time_newAct_nextAct = routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
//		
//		double nextAct_arrTime = newAct_endTime + tp_time_newAct_nextAct;
//				
//		double act_costs_nextAct = activityCosts.getActivityCost(nextAct, nextAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
//		
//		double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct + act_costs_newAct + act_costs_nextAct; 
//		
//		double oldCosts;
//		double oldTime;
//		if(iFacts.getRoute().isEmpty()){
//			oldCosts = 0.0;
//			oldTime = 0.0;
//		}
//		else{
//			double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocationId(), nextAct.getLocationId(), prevAct.getEndTime(), iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
//			double arrTime_nextAct = routingCosts.getTransportTime(prevAct.getLocationId(), nextAct.getLocationId(), prevAct.getEndTime(), iFacts.getNewDriver(), iFacts.getNewVehicle());
//			
//			double actCost_nextAct = activityCosts.getActivityCost(nextAct, arrTime_nextAct, iFacts.getRoute().getDriver(), iFacts.getRoute().getVehicle());
//			oldCosts = tp_costs_prevAct_nextAct + actCost_nextAct;
//			oldTime = (nextAct.getArrTime() - iFacts.getRoute().getDepartureTime());
//		}
//		
//		double additionalCosts = totalCosts - oldCosts;
//		double additionalTime = (nextAct_arrTime - iFacts.getNewDepTime()) - oldTime;
//
//		return new ActivityInsertionCosts(additionalCosts,additionalTime);
	}
	
	private double actCostsOld(VehicleRoute vehicleRoute, TourActivity act) {
		if(act instanceof End){
			return stateManager.getRouteState(vehicleRoute,StateTypes.COSTS).toDouble();
		}
		return stateManager.getActivityState(act,StateTypes.COSTS).toDouble();
	}
	
	private List<TourActivity> getForwardLookingPath(VehicleRoute route, int actIndex) {
		List<TourActivity> forwardLookingPath = new ArrayList<TourActivity>();
		int nuOfActsInPath = 0;
		int index = actIndex + 1;
		while(index < route.getTourActivities().getActivities().size() && nuOfActsInPath < nuOfActivities2LookForward){
			forwardLookingPath.add(route.getTourActivities().getActivities().get(index));
			index++;
			nuOfActsInPath++;
		}
		if(nuOfActsInPath < nuOfActivities2LookForward){
			forwardLookingPath.add(route.getEnd());
		}
		return forwardLookingPath;
	}

}
