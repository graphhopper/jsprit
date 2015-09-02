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

import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.*;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;


/**
 * Ensures load constraint for inserting ServiceActivity.
 * 
 * <p>When using this, you need to use<br>
 * 
 * 
 * @author schroeder
 *
 */
public class ServiceLoadActivityLevelConstraint implements HardActivityConstraint {
	
	private RouteAndActivityStateGetter stateManager;

    private Capacity defaultValue;
	
	public ServiceLoadActivityLevelConstraint(RouteAndActivityStateGetter stateManager) {
		super();
		this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
	}

	@Override
	public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		Capacity futureMaxLoad;
		Capacity prevMaxLoad;
		if(prevAct instanceof Start){
			futureMaxLoad = stateManager.getRouteState(iFacts.getRoute(), InternalStates.MAXLOAD, Capacity.class);
            if(futureMaxLoad == null) futureMaxLoad = defaultValue;
			prevMaxLoad = stateManager.getRouteState(iFacts.getRoute(), InternalStates.LOAD_AT_BEGINNING, Capacity.class);
            if(prevMaxLoad == null) prevMaxLoad = defaultValue;
		}
		else{
			futureMaxLoad = stateManager.getActivityState(prevAct, InternalStates.FUTURE_MAXLOAD, Capacity.class);
            if(futureMaxLoad == null) futureMaxLoad = defaultValue;
			prevMaxLoad = stateManager.getActivityState(prevAct, InternalStates.PAST_MAXLOAD, Capacity.class);
            if(prevMaxLoad == null) prevMaxLoad = defaultValue;
			
		}
		if(newAct instanceof PickupService || newAct instanceof ServiceActivity){
			if(!Capacity.addup(newAct.getSize(), futureMaxLoad).isLessOrEqual(iFacts.getNewVehicle().getType().getCapacityDimensions())){
				return ConstraintsStatus.NOT_FULFILLED;
			}
		}
		if(newAct instanceof DeliverService){
			if(!Capacity.addup(Capacity.invert(newAct.getSize()), prevMaxLoad).isLessOrEqual(iFacts.getNewVehicle().getType().getCapacityDimensions())){
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
		}
		return ConstraintsStatus.FULFILLED;
	}		
}
