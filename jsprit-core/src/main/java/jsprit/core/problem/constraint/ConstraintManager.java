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

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Manager that manage hard- and soft constraints, both on route and activity level.
 * 
 * @author schroeder
 *
 */
public class ConstraintManager implements HardActivityConstraint, HardRouteConstraint, SoftActivityConstraint, SoftRouteConstraint{

    public static enum Priority {
		CRITICAL, HIGH, LOW
	}
	
	private static Logger log = LogManager.getLogger(ConstraintManager.class);
	
	private HardActivityLevelConstraintManager actLevelConstraintManager = new HardActivityLevelConstraintManager();
	
	private HardRouteLevelConstraintManager routeLevelConstraintManager = new HardRouteLevelConstraintManager();
	
	private SoftActivityConstraintManager softActivityConstraintManager = new SoftActivityConstraintManager();
	
	private SoftRouteConstraintManager softRouteConstraintManager = new SoftRouteConstraintManager();
	
	private VehicleRoutingProblem vrp;
	
	private RouteAndActivityStateGetter stateManager;
	
	private boolean loadConstraintsSet = false;
	
	private boolean timeWindowConstraintsSet = false;

    private boolean skillconstraintSet = false;
	
	public ConstraintManager(VehicleRoutingProblem vrp, RouteAndActivityStateGetter stateManager) {
		this.vrp = vrp;
		this.stateManager = stateManager;
	}
	
	public ConstraintManager(VehicleRoutingProblem vrp, RouteAndActivityStateGetter stateManager, Collection<jsprit.core.problem.constraint.Constraint> constraints) {
		this.vrp = vrp;
		this.stateManager = stateManager;
		resolveConstraints(constraints);
	}
	
	private void resolveConstraints(Collection<jsprit.core.problem.constraint.Constraint> constraints) {
		for(jsprit.core.problem.constraint.Constraint c : constraints){
			boolean constraintTypeKnown = false;
			if(c instanceof HardActivityConstraint) {
				actLevelConstraintManager.addConstraint((HardActivityConstraint) c, Priority.HIGH);
				constraintTypeKnown = true;
			}
			if(c instanceof HardRouteConstraint) {
				routeLevelConstraintManager.addConstraint((HardRouteConstraint) c);
				constraintTypeKnown = true;
			}
			if(c instanceof SoftRouteConstraint){
				softRouteConstraintManager.addConstraint((SoftRouteConstraint)c);
				constraintTypeKnown = true;
			}
			if(c instanceof SoftActivityConstraint){
				softActivityConstraintManager.addConstraint((SoftActivityConstraint)c);
				constraintTypeKnown = true;
			}
			if(!constraintTypeKnown){
				log.warn("constraint " + c + " unknown thus ignores the constraint. currently, a constraint must implement either HardActivityStateLevelConstraint or HardRouteStateLevelConstraint");
			}
		}
		
	}

	public void addTimeWindowConstraint(){
		if(!timeWindowConstraintsSet){
			addConstraint(new VehicleDependentTimeWindowConstraints(stateManager, vrp.getTransportCosts()),Priority.HIGH);
			timeWindowConstraintsSet = true;
		}
	}

	
	public void addLoadConstraint(){
		if(!loadConstraintsSet){
			addConstraint(new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager),Priority.CRITICAL);
			addConstraint(new ServiceLoadRouteLevelConstraint(stateManager));
			addConstraint(new ServiceLoadActivityLevelConstraint(stateManager),Priority.LOW);
			loadConstraintsSet=true;
		}
	}

    public void addSkillsConstraint() {
        if (!skillconstraintSet){
            addConstraint(new HardSkillConstraint(stateManager));
            skillconstraintSet=true;
        }
    }
	
//	public void add
	
	public void addConstraint(HardActivityConstraint actLevelConstraint, Priority priority){
		actLevelConstraintManager.addConstraint(actLevelConstraint,priority);
	}
	
	public void addConstraint(HardRouteConstraint routeLevelConstraint){
		routeLevelConstraintManager.addConstraint(routeLevelConstraint);
	}
	
	public void addConstraint(SoftActivityConstraint softActivityConstraint){
		softActivityConstraintManager.addConstraint(softActivityConstraint);
	}
	
	public void addConstraint(SoftRouteConstraint softRouteConstraint){
		softRouteConstraintManager.addConstraint(softRouteConstraint);
	}
	
	@Override
	public boolean fulfilled(JobInsertionContext insertionContext) {
		return routeLevelConstraintManager.fulfilled(insertionContext);
	}

	@Override
	public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct,TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		return actLevelConstraintManager.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
	}
	
	public Collection<jsprit.core.problem.constraint.Constraint> getConstraints(){
		List<jsprit.core.problem.constraint.Constraint> constraints = new ArrayList<jsprit.core.problem.constraint.Constraint>();
		constraints.addAll(actLevelConstraintManager.getAllConstraints());
		constraints.addAll(routeLevelConstraintManager.getConstraints());
		constraints.addAll(softActivityConstraintManager.getConstraints());
		constraints.addAll(softRouteConstraintManager.getConstraints());
		return Collections.unmodifiableCollection(constraints);
	}

	@Override
	public double getCosts(JobInsertionContext insertionContext) {
		return softRouteConstraintManager.getCosts(insertionContext);
	}

	@Override
	public double getCosts(JobInsertionContext iFacts, TourActivity prevAct,TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		return softActivityConstraintManager.getCosts(iFacts, prevAct, newAct, nextAct, prevActDepTime);
	}

	
}
