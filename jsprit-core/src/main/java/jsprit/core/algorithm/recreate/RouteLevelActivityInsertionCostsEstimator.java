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
package jsprit.core.algorithm.recreate;

import java.util.ArrayList;
import java.util.List;

import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.solution.route.state.StateFactory;


class RouteLevelActivityInsertionCostsEstimator implements ActivityInsertionCostsCalculator{

	private VehicleRoutingActivityCosts activityCosts;
	
	private AuxilliaryCostCalculator auxilliaryPathCostCalculator;
	
	private RouteAndActivityStateGetter stateManager;
	
	private int nuOfActivities2LookForward = 0;
	
	public RouteLevelActivityInsertionCostsEstimator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts actCosts, RouteAndActivityStateGetter stateManager) {
		super();
		this.activityCosts = actCosts;
		this.stateManager = stateManager;
		auxilliaryPathCostCalculator = new AuxilliaryCostCalculator(routingCosts, activityCosts);
	}

	@Override
	public ActivityInsertionCosts getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, double depTimeAtPrevAct) {
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
		
	}
	
	private double actCostsOld(VehicleRoute vehicleRoute, TourActivity act) {
		if(act instanceof End){
			return stateManager.getRouteState(vehicleRoute,StateFactory.COSTS).toDouble();
		}
		return stateManager.getActivityState(act,StateFactory.COSTS).toDouble();
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
