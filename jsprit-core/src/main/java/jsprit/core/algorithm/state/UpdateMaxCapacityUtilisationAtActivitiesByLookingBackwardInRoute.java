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
package jsprit.core.algorithm.state;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Determines and memorizes the maximum capacity utilization at each activity by looking backward in route,
 * i.e. the maximum capacity utilization at previous activities.
 * 
 * @author schroeder
 *
 */
class UpdateMaxCapacityUtilisationAtActivitiesByLookingBackwardInRoute implements ActivityVisitor, StateUpdater {
	
	private StateManager stateManager;
	
	private VehicleRoute route;
	
	private Capacity maxLoad;

    private Capacity defaultValue;
	
	public UpdateMaxCapacityUtilisationAtActivitiesByLookingBackwardInRoute(StateManager stateManager) {
		this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route;
		maxLoad = stateManager.getRouteState(route, InternalStates.LOAD_AT_BEGINNING, Capacity.class);
        if(maxLoad == null) maxLoad = defaultValue;
	}

	@Override
	public void visit(TourActivity act) {
		maxLoad = Capacity.max(maxLoad, stateManager.getActivityState(act, InternalStates.LOAD, Capacity.class));
		stateManager.putInternalTypedActivityState(act, InternalStates.PAST_MAXLOAD, maxLoad);
//		assert maxLoad.isGreaterOrEqual(Capacity.Builder.newInstance().build()) : "maxLoad can never be smaller than 0";
//		assert maxLoad.isLessOrEqual(route.getVehicle().getType().getCapacityDimensions()) : "maxLoad can never be bigger than vehicleCap";
	}

	@Override
	public void finish() {}
}
