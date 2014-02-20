package jsprit.core.algorithm.state;

import java.util.Collection;

import jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.StateFactory;


/**
 * Updates load at activity level. 
 * 
 * <p>Note that this assumes that StateTypes.LOAD_AT_DEPOT is already updated, i.e. it starts by setting loadAtDepot to StateTypes.LOAD_AT_DEPOT.
 * If StateTypes.LOAD_AT_DEPOT is not set, it starts with 0 load at depot.
 * 
 * <p>Thus it DEPENDS on StateTypes.LOAD_AT_DEPOT
 *  
 * @author stefan
 *
 */
class UpdateLoads implements ActivityVisitor, StateUpdater, InsertionStartsListener, JobInsertedListener {
	private StateManager stateManager;
	private int currentLoad = 0;
	private VehicleRoute route;
	/**
	 * Updates load at activity level. 
	 * 
	 * <p>Note that this assumes that StateTypes.LOAD_AT_DEPOT is already updated, i.e. it starts by setting loadAtDepot to StateTypes.LOAD_AT_DEPOT.
	 * If StateTypes.LOAD_AT_DEPOT is not set, it starts with 0 load at depot.
	 *
	 * <p>Thus it DEPENDS on StateTypes.LOAD_AT_DEPOT
	 * 
	 * <p>The loads can be retrieved by <br>
	 * <code>stateManager.getActivityState(activity,StateTypes.LOAD);</code>
	 * 
	 * @author stefan
	 *
	 */
	public UpdateLoads(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}
	
	@Override
	public void begin(VehicleRoute route) {
		currentLoad = (int) stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING).toDouble();
		this.route = route;
	}

	@Override
	public void visit(TourActivity act) {
		currentLoad += act.getCapacityDemand();
		stateManager.putInternalActivityState(act, StateFactory.LOAD, StateFactory.createState(currentLoad));
		assert currentLoad <= route.getVehicle().getCapacity() : "currentLoad at activity must not be > vehicleCapacity";
		assert currentLoad >= 0 : "currentLoad at act must not be < 0";
	}

	@Override
	public void finish() {
		currentLoad = 0;
	}
	
	void insertionStarts(VehicleRoute route) {
		int loadAtDepot = 0;
		int loadAtEnd = 0;
		for(Job j : route.getTourActivities().getJobs()){
			if(j instanceof Delivery){
				loadAtDepot += j.getCapacityDemand();
			}
			else if(j instanceof Pickup || j instanceof Service){
				loadAtEnd += j.getCapacityDemand();
			}
		}
		stateManager.putInternalRouteState(route, StateFactory.LOAD_AT_BEGINNING, StateFactory.createState(loadAtDepot));
		stateManager.putInternalRouteState(route, StateFactory.LOAD_AT_END, StateFactory.createState(loadAtEnd));
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
		for(VehicleRoute route : vehicleRoutes){ insertionStarts(route); }
	}
	
	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
		if(job2insert instanceof Delivery){
			int loadAtDepot = (int) stateManager.getRouteState(inRoute, StateFactory.LOAD_AT_BEGINNING).toDouble();
			stateManager.putInternalRouteState(inRoute, StateFactory.LOAD_AT_BEGINNING, StateFactory.createState(loadAtDepot + job2insert.getCapacityDemand()));
		}
		else if(job2insert instanceof Pickup || job2insert instanceof Service){
			int loadAtEnd = (int) stateManager.getRouteState(inRoute, StateFactory.LOAD_AT_END).toDouble();
			stateManager.putInternalRouteState(inRoute, StateFactory.LOAD_AT_END, StateFactory.createState(loadAtEnd + job2insert.getCapacityDemand()));
		}
	}


}