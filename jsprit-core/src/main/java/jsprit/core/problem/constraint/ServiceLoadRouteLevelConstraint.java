package jsprit.core.problem.constraint;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.solution.route.state.StateFactory;

/**
 * lsjdfjsdlfjsa
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
//			int loadAtDepot = (int) stateManager.getRouteState(insertionContext.getRoute(), StateFactory.LOAD_AT_BEGINNING).toDouble();
			if(!Capacity.addup(loadAtDepot, insertionContext.getJob().getSize()).isLessOrEqual(insertionContext.getNewVehicle().getType().getCapacityDimensions())){
				return false;
			}
//			if(loadAtDepot + insertionContext.getJob().getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
//				return false;
//			}
		}
		else if(insertionContext.getJob() instanceof Pickup || insertionContext.getJob() instanceof Service){
			Capacity loadAtEnd = stateManager.getRouteState(insertionContext.getRoute(), StateFactory.LOAD_AT_END, Capacity.class);
//			int loadAtEnd = (int) stateManager.getRouteState(insertionContext.getRoute(), StateFactory.LOAD_AT_END).toDouble();
			if(!Capacity.addup(loadAtEnd, insertionContext.getJob().getSize()).isLessOrEqual(insertionContext.getNewVehicle().getType().getCapacityDimensions())){
				return false;
			}
//			
//			if(loadAtEnd + insertionContext.getJob().getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
//				return false;
//			}
		}
		return true;
	}
	
}