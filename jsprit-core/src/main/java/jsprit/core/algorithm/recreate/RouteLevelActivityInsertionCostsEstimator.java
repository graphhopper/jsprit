
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

package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

import java.util.ArrayList;
import java.util.List;


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
	public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, double depTimeAtPrevAct) {
		List<TourActivity> path = new ArrayList<TourActivity>();
		path.add(prevAct); path.add(newAct); path.add(nextAct);
		int actIndex;
		if(prevAct instanceof Start) actIndex = 0;
		else actIndex = iFacts.getRoute().getTourActivities().getActivities().indexOf(nextAct);
		if(nuOfActivities2LookForward > 0 && !(nextAct instanceof End)){ path.addAll(getForwardLookingPath(iFacts.getRoute(),actIndex)); }

		/*
		 * calculates the path costs with new vehicle, c(forwardPath,newVehicle).
		 */
		double forwardPathCost_newVehicle = auxilliaryPathCostCalculator.costOfPath(path, depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
		return forwardPathCost_newVehicle - (actCostsOld(iFacts.getRoute(), path.get(path.size()-1)) - actCostsOld(iFacts.getRoute(), prevAct));
	}
	
	private double actCostsOld(VehicleRoute vehicleRoute, TourActivity act) {
        Double cost_at_act;
        if(act instanceof End){
            cost_at_act = stateManager.getRouteState(vehicleRoute, InternalStates.COSTS, Double.class);
		}
        else{
            cost_at_act = stateManager.getActivityState(act, InternalStates.COSTS, Double.class);
        }
        if(cost_at_act == null) cost_at_act = 0.;
        return cost_at_act;
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

    public void setForwardLooking(int nActivities) {
        this.nuOfActivities2LookForward = nActivities;
    }
}
