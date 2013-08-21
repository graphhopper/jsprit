package algorithms;

import algorithms.ForwardInTimeListeners.ForwardInTimeListener;
import algorithms.StatesContainer.StateImpl;
import basics.route.End;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

/**
 * It does not update start and end activities.
 * 
 * @author stefan
 *
 */
class UpdateLoadAtAllLevels implements ForwardInTimeListener{

	private double load = 0.0;
	
	private StatesContainerImpl states;
	
	private VehicleRoute vehicleRoute;
	
	public UpdateLoadAtAllLevels(StatesContainerImpl states) {
		super();
		this.states = states;
	}

	@Override
	public void start(VehicleRoute route) { vehicleRoute = route; }

	@Override
	public void nextActivity(TourActivity act, double arrTime, double endTime) {
		if(act instanceof Start || act instanceof End){ return; }
		load += (double)act.getCapacityDemand();
		states.putActivityState(act, StateTypes.LOAD, new StateImpl(load));
	}

	@Override
	public void finnish() {
		states.putRouteState(vehicleRoute, StateTypes.LOAD, new StateImpl(load));
		load=0;
		vehicleRoute = null;
	}

}
