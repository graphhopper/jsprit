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
package jsprit.core.problem.constraint;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.solution.route.state.StateFactory;

/**
 * Ensures that capacity constraint is met, i.e. that current load plus
 * new job size does not exceeds capacity of new vehicle.
 * 
 * <p>If job is neither Pickup, Delivery nor Service, it returns true.
 * 
 * @author stefan
 *
 */
class ServiceLoadRouteLevelConstraint implements HardRouteStateLevelConstraint {

	private RouteAndActivityStateGetter stateManager;
	
	public ServiceLoadRouteLevelConstraint(RouteAndActivityStateGetter stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public boolean fulfilled(JobInsertionContext insertionContext) {
		Capacity maxLoadAtRoute = stateManager.getRouteState(insertionContext.getRoute(), StateFactory.MAXLOAD, Capacity.class);
		Capacity capacityDimensions = insertionContext.getNewVehicle().getType().getCapacityDimensions();
		if(!maxLoadAtRoute.isLessOrEqual(capacityDimensions)){
			return false;
		}
		if(insertionContext.getJob() instanceof Delivery){
			Capacity loadAtDepot = stateManager.getRouteState(insertionContext.getRoute(), StateFactory.LOAD_AT_BEGINNING, Capacity.class);
			if(!Capacity.addup(loadAtDepot, insertionContext.getJob().getSize()).isLessOrEqual(capacityDimensions)){
				return false;
			}
		}
		else if(insertionContext.getJob() instanceof Pickup || insertionContext.getJob() instanceof Service){
			Capacity loadAtEnd = stateManager.getRouteState(insertionContext.getRoute(), StateFactory.LOAD_AT_END, Capacity.class);
			if(!Capacity.addup(loadAtEnd, insertionContext.getJob().getSize()).isLessOrEqual(capacityDimensions)){
				return false;
			}
		}
		return true;
	}
	
}
