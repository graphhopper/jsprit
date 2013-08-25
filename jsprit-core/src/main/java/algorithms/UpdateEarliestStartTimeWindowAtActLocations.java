package algorithms;

import algorithms.ForwardInTimeListeners.ForwardInTimeListener;
import algorithms.StateManager.StateImpl;
import basics.route.End;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdateEarliestStartTimeWindowAtActLocations implements ForwardInTimeListener{

	private StateManagerImpl states;
	
	public UpdateEarliestStartTimeWindowAtActLocations(StateManagerImpl states) {
		super();
		this.states = states;
	}

	@Override
	public void start(VehicleRoute route, Start start, double departureTime) {}

	@Override
	public void nextActivity(TourActivity act, double arrTime, double endTime) {
		states.putActivityState(act, StateTypes.EARLIEST_OPERATION_START_TIME, new StateImpl(Math.max(arrTime, act.getTheoreticalEarliestOperationStartTime())));
	}

	@Override
	public void end(End end, double arrivalTime) {}

}
