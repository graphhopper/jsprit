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
		if(insertionContext.getJob() instanceof Delivery){
			Capacity loadAtDepot = stateManager.getRouteState(insertionContext.getRoute(), StateFactory.LOAD_AT_BEGINNING, Capacity.class);
			if(!Capacity.addup(loadAtDepot, insertionContext.getJob().getSize()).isLessOrEqual(insertionContext.getNewVehicle().getType().getCapacityDimensions())){
				return false;
			}
		}
		else if(insertionContext.getJob() instanceof Pickup || insertionContext.getJob() instanceof Service){
			Capacity loadAtEnd = stateManager.getRouteState(insertionContext.getRoute(), StateFactory.LOAD_AT_END, Capacity.class);
			if(!Capacity.addup(loadAtEnd, insertionContext.getJob().getSize()).isLessOrEqual(insertionContext.getNewVehicle().getType().getCapacityDimensions())){
				return false;
			}
		}
		return true;
	}
	
}