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
import jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * A {@link ReverseActivityVisitor} that looks forward in the vehicle route and determines
 * the maximum capacity utilization (in terms of loads) at subsequent activities. 
 * 
 * <p>Assume a vehicle route with the following activity sequence {start,pickup(1,4),delivery(2,3),pickup(3,2),end} where
 * pickup(1,2) = pickup(id,cap-demand).<br> 
 * Future maxLoad for each activity are calculated as follows:<br>
 * loadAt(end)=6 (since two pickups need to be delivered to depot)<br>
 * pickup(3)=max(loadAt(pickup(3)), futureMaxLoad(end))=max(6,6)=6
 * delivery(2)=max(loadAt(delivery(2),futureMaxLoad(pickup(3))=max(4,6)=6
 * pickup(1)=max(7,6)=7
 * start=max(7,7)=7
 * activity (apart from start and end), the maximum capacity is determined when forward looking into the route.
 * That is at each activity we know how much capacity is available whithout breaking future capacity constraints.
 * 
 * 
 * @author schroeder
 *
 */
class UpdateMaxCapacityUtilisationAtActivitiesByLookingForwardInRoute implements ReverseActivityVisitor, StateUpdater {
	
	private StateManager stateManager;
	
	private VehicleRoute route;
	
	private Capacity maxLoad;

    private Capacity defaultValue;
	
	public UpdateMaxCapacityUtilisationAtActivitiesByLookingForwardInRoute(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route;
		maxLoad = stateManager.getRouteState(route, InternalStates.LOAD_AT_END, Capacity.class);
        if(maxLoad == null) maxLoad = defaultValue;
	}

	@Override
	public void visit(TourActivity act) {
		maxLoad = Capacity.max(maxLoad, stateManager.getActivityState(act, InternalStates.LOAD, Capacity.class));
		stateManager.putInternalTypedActivityState(act, InternalStates.FUTURE_MAXLOAD, maxLoad);
//		assert maxLoad.isLessOrEqual(route.getVehicle().getType().getCapacityDimensions()) : "maxLoad can in every capacity dimension never be bigger than vehicleCap";
//		assert maxLoad.isGreaterOrEqual(Capacity.Builder.newInstance().build()) : "maxLoad can never be smaller than 0";
	}

	@Override
	public void finish() {}
}
