/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.algorithm.state;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Updates load at activity level. 
 * 
 * <p>Note that this assumes that StateTypes.LOAD_AT_DEPOT is already updated, i.e. it starts by setting loadAtDepot to StateTypes.LOAD_AT_DEPOT.
 * If StateTypes.LOAD_AT_DEPOT is not set, it starts with 0 load at depot.
 * 
 * <p>Thus it DEPENDS on StateTypes.LOAD_AT_DEPOT
 *  
 * @author stefan
 *
 */
class UpdateMaxCapacityUtilisationAtRoute implements ActivityVisitor, StateUpdater {
	
	private StateManager stateManager;
	
	private Capacity currentLoad = Capacity.Builder.newInstance().build();
	
	private VehicleRoute route;
	
	private Capacity maxLoad;

    private Capacity defaultValue;
	
	public UpdateMaxCapacityUtilisationAtRoute(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
	}
	
	@Override
	public void begin(VehicleRoute route) {
		currentLoad = stateManager.getRouteState(route, InternalStates.LOAD_AT_BEGINNING, Capacity.class);
        if(currentLoad == null) currentLoad = defaultValue;
		maxLoad = currentLoad;
		this.route = route;
	}

	@Override
	public void visit(TourActivity act) {
		currentLoad = Capacity.addup(currentLoad, act.getSize());
		maxLoad = Capacity.max(maxLoad, currentLoad);
	}

	@Override
	public void finish() {
		stateManager.putTypedInternalRouteState(route, InternalStates.MAXLOAD, maxLoad);
	}
}
