package jsprit.core.problem.constraint;

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
		int futureMaxLoad;
		int prevMaxLoad;
		if(prevAct instanceof Start){
			futureMaxLoad = (int)stateManager.getRouteState(iFacts.getRoute(), StateFactory.MAXLOAD).toDouble();
			prevMaxLoad = (int)stateManager.getRouteState(iFacts.getRoute(), StateFactory.LOAD_AT_BEGINNING).toDouble();
		}
		else{
			futureMaxLoad = (int) stateManager.getActivityState(prevAct, StateFactory.FUTURE_MAXLOAD).toDouble();
			prevMaxLoad = (int) stateManager.getActivityState(prevAct, StateFactory.PAST_MAXLOAD).toDouble();
			
		}
		if(newAct instanceof PickupService || newAct instanceof ServiceActivity){
			if(newAct.getCapacityDemand() + futureMaxLoad > iFacts.getNewVehicle().getCapacity()){
//				log.debug("insertionOf("+newAct+").BETWEEN("+prevAct+").AND("+nextAct+")=NOT_POSSIBLE");
				return ConstraintsStatus.NOT_FULFILLED;
			}
		}
		if(newAct instanceof DeliverService){
			if(Math.abs(newAct.getCapacityDemand()) + prevMaxLoad > iFacts.getNewVehicle().getCapacity()){
//				log.debug("insertionOf("+newAct+").BETWEEN("+prevAct+").AND("+nextAct+")=NOT_POSSIBLE[break=neverBePossibleAnymore]");
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
			
		}
//		log.debug("insertionOf("+newAct+").BETWEEN("+prevAct+").AND("+nextAct+")=POSSIBLE");
		return ConstraintsStatus.FULFILLED;
	}		
}