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
import jsprit.core.problem.solution.route.activity.DeliverShipment;
import jsprit.core.problem.solution.route.activity.PickupShipment;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;


/**
 * Constraint that ensures capacity constraint at each activity.
 * 
 * <p>This is critical to consistently calculate pd-problems with capacity constraints. Critical means
 * that is MUST be visited. It also assumes that pd-activities are visited in the order they occur in a tour.
 * 
 * @author schroeder
 *
 */
public class PickupAndDeliverShipmentLoadActivityLevelConstraint implements HardActivityConstraint {
	
	private RouteAndActivityStateGetter stateManager;

    private Capacity defaultValue;
	
	/**
	 * Constructs the constraint ensuring capacity constraint at each activity.
	 * 
	 * <p>This is critical to consistently calculate pd-problems with capacity constraints. Critical means
	 * that is MUST be visited. It also assumes that pd-activities are visited in the order they occur in a tour.
	 * 
	 * 
	 * @param stateManager the stateManager
	 */
	public PickupAndDeliverShipmentLoadActivityLevelConstraint(RouteAndActivityStateGetter stateManager) {
		super();
		this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
	}
	
	/**
	 * Checks whether there is enough capacity to insert newAct between prevAct and nextAct.
	 * 
	 */
	@Override
	public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		if(!(newAct instanceof PickupShipment) && !(newAct instanceof DeliverShipment)){
			return ConstraintsStatus.FULFILLED;
		}
		Capacity loadAtPrevAct;
		if(prevAct instanceof Start){
			loadAtPrevAct = stateManager.getRouteState(iFacts.getRoute(), InternalStates.LOAD_AT_BEGINNING, Capacity.class);
            if(loadAtPrevAct == null) loadAtPrevAct = defaultValue;
		}
		else{
			loadAtPrevAct = stateManager.getActivityState(prevAct, InternalStates.LOAD, Capacity.class);
            if(loadAtPrevAct == null) loadAtPrevAct = defaultValue;
		}
		if(newAct instanceof PickupShipment){
			if(!Capacity.addup(loadAtPrevAct, newAct.getSize()).isLessOrEqual(iFacts.getNewVehicle().getType().getCapacityDimensions())){
				return ConstraintsStatus.NOT_FULFILLED;
			}
		}
		if(newAct instanceof DeliverShipment){
			if(!Capacity.addup(loadAtPrevAct, Capacity.invert(newAct.getSize())).isLessOrEqual(iFacts.getNewVehicle().getType().getCapacityDimensions()))
                return ConstraintsStatus.NOT_FULFILLED_BREAK;
		}
		return ConstraintsStatus.FULFILLED;
	}
	
		
}
