package jsprit.core.algorithm.state;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.StateFactory;

/**
 * Determines and memorizes the maximum capacity utilization at each activity by looking backward in route,
 * i.e. the maximum capacity utilization at previous activities.
 * 
 * @author schroeder
 *
 */
class UpdateMaxCapacityUtilisationAtActivitiesByLookingBackwardInRoute implements ActivityVisitor, StateUpdater {
	
	private StateManager stateManager;
	
	private VehicleRoute route;
	
	private Capacity maxLoad;
	
	public UpdateMaxCapacityUtilisationAtActivitiesByLookingBackwardInRoute(StateManager stateManager) {
		this.stateManager = stateManager;
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route;
		maxLoad = stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING, Capacity.class);
	}

	@Override
	public void visit(TourActivity act) {
		maxLoad = Capacity.max(maxLoad, stateManager.getActivityState(act, StateFactory.LOAD, Capacity.class));
		stateManager.putInternalActivityState_(act, StateFactory.PAST_MAXLOAD, Capacity.class, maxLoad);
		assert maxLoad.isGreaterOrEqual(Capacity.Builder.newInstance().build()) : "maxLoad can never be smaller than 0";
		assert maxLoad.isLessOrEqual(route.getVehicle().getType().getCapacityDimensions()) : "maxLoad can never be bigger than vehicleCap";
	}

	@Override
	public void finish() {}
}