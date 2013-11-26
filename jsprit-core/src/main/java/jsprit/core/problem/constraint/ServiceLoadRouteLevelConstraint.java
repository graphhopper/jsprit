package jsprit.core.problem.constraint;

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
			int loadAtDepot = (int) stateManager.getRouteState(insertionContext.getRoute(), StateFactory.LOAD_AT_BEGINNING).toDouble();
			if(loadAtDepot + insertionContext.getJob().getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
				return false;
			}
		}
		else if(insertionContext.getJob() instanceof Pickup || insertionContext.getJob() instanceof Service){
			int loadAtEnd = (int) stateManager.getRouteState(insertionContext.getRoute(), StateFactory.LOAD_AT_END).toDouble();
			if(loadAtEnd + insertionContext.getJob().getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
				return false;
			}
		}
		return true;
	}
	
}