package jsprit.core.algorithm.state;

import jsprit.core.problem.Capacity;
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
class UpdateMaxCapacityUtilisationAtRoute implements ActivityVisitor, StateUpdater {
	
	private StateManager stateManager;
	
	private Capacity currentLoad = Capacity.Builder.newInstance().build();
	
	private VehicleRoute route;
	
	private Capacity maxLoad = Capacity.Builder.newInstance().build();
	
	public UpdateMaxCapacityUtilisationAtRoute(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}
	
	@Override
	public void begin(VehicleRoute route) {
		currentLoad = stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING, Capacity.class);
		maxLoad = currentLoad;
		this.route = route;
	}

	@Override
	public void visit(TourActivity act) {
		currentLoad = Capacity.addup(currentLoad, act.getSize());
		maxLoad = Capacity.max(maxLoad, currentLoad);
	}

	@Override
	public void finish() {
		stateManager.putInternalRouteState_(route, StateFactory.MAXLOAD, Capacity.class, maxLoad);
	}
}