package jsprit.core.algorithm.state;

import java.util.Collection;

import jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.StateFactory;


/**
 * Updates load at start and end of route as well as at each activity. And update is triggered when either 
 * activityVisitor has been started, the insertion process has been started or a job has been inserted. 
 * 
 * <p>Note that this only works properly if you register this class as ActivityVisitor AND InsertionStartsListener AND JobInsertedListener.
 * The reason behind is that activity states are dependent on route-level states and vice versa. If this is properly registered, 
 * this dependency is solved automatically.
 *  
 * @author stefan
 *
 */
class UpdateLoads implements ActivityVisitor, StateUpdater, InsertionStartsListener, JobInsertedListener {
	
	private StateManager stateManager;
	
	/*
	 * default has one dimension with a value of zero
	 */
	private Capacity currentLoad = Capacity.Builder.newInstance().build();
	
	private VehicleRoute route;
	
	public UpdateLoads(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}
	
	@Override
	public void begin(VehicleRoute route) {
		currentLoad = stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING, Capacity.class);
		this.route = route;
	}

	@Override
	public void visit(TourActivity act) {
		currentLoad = Capacity.addup(currentLoad, act.getCapacity());
		stateManager.putInternalActivityState_(act, StateFactory.LOAD, Capacity.class, currentLoad);
		assert currentLoad.isLessOrEqual(route.getVehicle().getType().getCapacityDimensions()) : "currentLoad at activity must not be > vehicleCapacity";
		assert currentLoad.isGreaterOrEqual(Capacity.Builder.newInstance().build()) : "currentLoad at act must not be < 0 in one of the applied dimensions";
	}

	@Override
	public void finish() {
		currentLoad = Capacity.Builder.newInstance().build();
	}
	
	void insertionStarts(VehicleRoute route) {
		Capacity loadAtDepot = Capacity.Builder.newInstance().build();
		Capacity loadAtEnd = Capacity.Builder.newInstance().build();
		for(Job j : route.getTourActivities().getJobs()){
			if(j instanceof Delivery){
				loadAtDepot = Capacity.addup(loadAtDepot, j.getSize());
			}
			else if(j instanceof Pickup || j instanceof Service){
				loadAtEnd = Capacity.addup(loadAtEnd, j.getSize());
			}
		}
		stateManager.putInternalRouteState_(route, StateFactory.LOAD_AT_BEGINNING, Capacity.class, loadAtDepot);
		stateManager.putInternalRouteState_(route, StateFactory.LOAD_AT_END, Capacity.class, loadAtEnd);
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
		for(VehicleRoute route : vehicleRoutes){ insertionStarts(route); }
	}
	
	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
		if(job2insert instanceof Delivery){
			Capacity loadAtDepot = stateManager.getRouteState(inRoute, StateFactory.LOAD_AT_BEGINNING, Capacity.class);
			stateManager.putInternalRouteState_(inRoute, StateFactory.LOAD_AT_BEGINNING, Capacity.class, Capacity.addup(loadAtDepot, job2insert.getSize()));
		}
		else if(job2insert instanceof Pickup || job2insert instanceof Service){
			Capacity loadAtEnd = stateManager.getRouteState(inRoute, StateFactory.LOAD_AT_END, Capacity.class);
			stateManager.putInternalRouteState_(inRoute, StateFactory.LOAD_AT_END, Capacity.class, Capacity.addup(loadAtEnd, job2insert.getSize()));
		}
	}


}