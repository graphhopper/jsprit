package jsprit.core.algorithm.state;

import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.StateFactory;


class UpdatePrevMaxLoad implements ActivityVisitor, StateUpdater {
	private StateManager stateManager;
	private VehicleRoute route;
	private double currLoad;
	private double prevMaxLoad;
	
	public UpdatePrevMaxLoad(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route; 
		currLoad = stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING).toDouble();
		prevMaxLoad = currLoad;
	}

	@Override
	public void visit(TourActivity act) {
		prevMaxLoad = Math.max(prevMaxLoad, stateManager.getActivityState(act, StateFactory.LOAD).toDouble());
		stateManager.putInternalActivityState(act, StateFactory.PAST_MAXLOAD, StateFactory.createState(prevMaxLoad));
		assert prevMaxLoad >= 0 : "maxLoad can never be smaller than 0";
		assert prevMaxLoad <= route.getVehicle().getCapacity() : "maxLoad can never be bigger than vehicleCap";
	}

	@Override
	public void finish() {}
}