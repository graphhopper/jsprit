package algorithms.states;

import algorithms.ActivityVisitor;
import algorithms.StateManagerImpl;
import algorithms.StateManagerImpl.StateImpl;
import algorithms.StateTypes;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

public class UpdateLoadAtAllLevels implements ActivityVisitor{

	private double load = 0.0;
	
	private StateManagerImpl states;
	
	private VehicleRoute vehicleRoute;
	
	public UpdateLoadAtAllLevels(StateManagerImpl states) {
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
		states.putActivityState(activity, StateTypes.LOAD, new StateImpl(load));
	}

	@Override
	public void finish() {
		states.putRouteState(vehicleRoute, StateTypes.LOAD, new StateImpl(load));
		load=0;
		vehicleRoute = null;
	}

}