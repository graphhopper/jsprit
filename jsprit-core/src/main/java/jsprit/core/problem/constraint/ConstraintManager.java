package jsprit.core.problem.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.Constraint;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

import org.apache.log4j.Logger;

@SuppressWarnings("deprecation")
public class ConstraintManager implements HardActivityStateLevelConstraint, HardRouteStateLevelConstraint{

	public static enum Priority {
		CRITICAL, HIGH, LOW
	}
	
	private static Logger log = Logger.getLogger(ConstraintManager.class);
	
	private HardActivityLevelConstraintManager actLevelConstraintManager = new HardActivityLevelConstraintManager();
	
	private HardRouteLevelConstraintManager routeLevelConstraintManager = new HardRouteLevelConstraintManager();
	
	private VehicleRoutingProblem vrp;
	
	private RouteAndActivityStateGetter stateManager;
	
	private boolean loadConstraintsSet = false;
	
	private boolean timeWindowConstraintsSet = false;
	
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
			if(c instanceof HardActivityStateLevelConstraint) {
				actLevelConstraintManager.addConstraint((HardActivityStateLevelConstraint) c, Priority.HIGH);
				constraintTypeKnown = true;
			}
			if(c instanceof HardRouteStateLevelConstraint) {
				routeLevelConstraintManager.addConstraint((HardRouteStateLevelConstraint) c);
				constraintTypeKnown = true;
			}
			if(!constraintTypeKnown){
				log.warn("constraint " + c + " unknown thus ignores the constraint. currently, a constraint must implement either HardActivityStateLevelConstraint or HardRouteStateLevelConstraint");
			}
		}
		
	}

	public void addTimeWindowConstraint(){
		if(!timeWindowConstraintsSet){
			addConstraint(new TimeWindowConstraint(stateManager, vrp.getTransportCosts()),Priority.HIGH);
			timeWindowConstraintsSet = true;
		}
	}

	public void addLoadConstraint(){
		if(!loadConstraintsSet){
			if(vrp.getProblemConstraints().contains(Constraint.DELIVERIES_FIRST)){
				addConstraint(new ServiceDeliveriesFirstConstraint(),Priority.HIGH);
			}
			addConstraint(new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager),Priority.CRITICAL);
			addConstraint(new ServiceLoadRouteLevelConstraint(stateManager));
			addConstraint(new ServiceLoadActivityLevelConstraint(stateManager),Priority.LOW);
			loadConstraintsSet=true;
		}
	}
	
	public void addConstraint(HardActivityStateLevelConstraint actLevelConstraint, Priority priority){
		actLevelConstraintManager.addConstraint(actLevelConstraint,priority);
	}
	
	public void addConstraint(HardRouteStateLevelConstraint routeLevelConstraint){
		routeLevelConstraintManager.addConstraint(routeLevelConstraint);
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
		return Collections.unmodifiableCollection(constraints);
	}
	
}