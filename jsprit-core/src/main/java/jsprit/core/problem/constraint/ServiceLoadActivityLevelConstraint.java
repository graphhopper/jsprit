package jsprit.core.problem.constraint;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.DeliverService;
import jsprit.core.problem.solution.route.activity.PickupService;
import jsprit.core.problem.solution.route.activity.ServiceActivity;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.solution.route.state.StateFactory;


/**
 * Ensures load constraint for inserting ServiceActivity.
 * 
 * <p>When using this, you need to use<br>
 * 
 * 
 * @author schroeder
 *
 */
class ServiceLoadActivityLevelConstraint implements HardActivityStateLevelConstraint {
	
	private RouteAndActivityStateGetter stateManager;
	
	public ServiceLoadActivityLevelConstraint(RouteAndActivityStateGetter stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		Capacity futureMaxLoad;
		Capacity prevMaxLoad;
		if(prevAct instanceof Start){
			futureMaxLoad = stateManager.getRouteState(iFacts.getRoute(), StateFactory.MAXLOAD, Capacity.class);
			prevMaxLoad = stateManager.getRouteState(iFacts.getRoute(), StateFactory.LOAD_AT_BEGINNING, Capacity.class);
		}
		else{
			futureMaxLoad = stateManager.getActivityState(prevAct, StateFactory.FUTURE_MAXLOAD, Capacity.class);
			prevMaxLoad = stateManager.getActivityState(prevAct, StateFactory.PAST_MAXLOAD, Capacity.class);
			
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