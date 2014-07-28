/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package jsprit.core.problem.solution.route.state;

import jsprit.core.algorithm.state.StateId;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;

public interface RouteAndActivityStateGetter {


	public <T> T getActivityState(TourActivity act, StateId stateId, Class<T> type);

    public <T> T getActivityState(TourActivity act, Vehicle vehicle, StateId stateId, Class<T> type);
	
	public <T> T getRouteState(VehicleRoute route, StateId stateId, Class<T> type);

    public <T> T getRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, Class<T> type);

}
