package algorithms;

import java.util.Collection;

import algorithms.StateManager.StateImpl;
import basics.Job;
import basics.Service;
import basics.algo.InsertionStartsListener;
import basics.algo.JobInsertedListener;
import basics.route.VehicleRoute;

/**
 * Updates load at route level, i.e. modifies StateTypes.LOAD for each route.
 * 
 * @author stefan
 *
 */
class UpdateLoadAtRouteLevel implements JobInsertedListener, InsertionStartsListener, StateUpdater{

	private StateManager states;
	
	/**
	 * Updates load at route level, i.e. modifies StateTypes.LOAD for each route.
	 * 
	 * @author stefan
	 *
	 */
	public UpdateLoadAtRouteLevel(StateManager states) {
		super();
		this.states = states;
	}

	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
		if(!(job2insert instanceof Service)){
			return;
		}
		double oldLoad = states.getRouteState(inRoute, StateIdFactory.LOAD).toDouble();
		states.putRouteState(inRoute, StateIdFactory.LOAD, new StateImpl(oldLoad + job2insert.getCapacityDemand()));
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
		for(VehicleRoute route : vehicleRoutes){
			int load = 0;
			for(Job j : route.getTourActivities().getJobs()){
				load += j.getCapacityDemand();
			}
			states.putRouteState(route, StateIdFactory.LOAD, new StateImpl(load));
		}
		
	}

}