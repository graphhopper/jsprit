package jsprit.core.algorithm.state;

import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.StateFactory;


class UpdateMaxLoad implements ReverseActivityVisitor, StateUpdater {
	private StateManager stateManager;
	private VehicleRoute route;
	private double maxLoad;
	public UpdateMaxLoad(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route;
		maxLoad = stateManager.getRouteState(route, StateFactory.LOAD_AT_END).toDouble();
	}

	@Override
	public void visit(TourActivity act) {
		maxLoad = Math.max(maxLoad, stateManager.getActivityState(act, StateFactory.LOAD).toDouble());
		stateManager.putInternalActivityState(act, StateFactory.FUTURE_MAXLOAD, StateFactory.createState(maxLoad));
		assert maxLoad <= route.getVehicle().getCapacity() : "maxLoad can never be bigger than vehicleCap";
		assert maxLoad >= 0 : "maxLoad can never be smaller than 0";
	}

	@Override
	public void finish() {}
}