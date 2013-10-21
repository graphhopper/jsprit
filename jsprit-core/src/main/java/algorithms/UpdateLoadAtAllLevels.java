package algorithms;

import algorithms.StateManager.StateImpl;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdateLoadAtAllLevels implements ActivityVisitor,StateUpdater{

	private double load = 0.0;
	
	private StateManager states;
	
	private VehicleRoute vehicleRoute;
	
	public UpdateLoadAtAllLevels(StateManager states) {
		super();
		this.states = states;
	}

	@Override
	public void begin(VehicleRoute route) {
		vehicleRoute = route;
	}

	@Override
	public void visit(TourActivity activity) {
		load += (double)activity.getCapacityDemand();
		states.putActivityState(activity, StateIdFactory.LOAD, new StateImpl(load));
	}

	@Override
	public void finish() {
		states.putRouteState(vehicleRoute, StateIdFactory.LOAD, new StateImpl(load));
		load=0;
		vehicleRoute = null;
	}

}