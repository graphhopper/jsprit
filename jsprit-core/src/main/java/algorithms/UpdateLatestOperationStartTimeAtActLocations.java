package algorithms;

import algorithms.BackwardInTimeListeners.BackwardInTimeListener;
import algorithms.StateManager.StateImpl;
import basics.route.End;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdateLatestOperationStartTimeAtActLocations implements BackwardInTimeListener{

	private StateManagerImpl states;
	
	public UpdateLatestOperationStartTimeAtActLocations(StateManagerImpl states) {
		super();
		this.states = states;
	}

	@Override
	public void start(VehicleRoute route, End end, double latestArrivalTime) {}

	@Override
	public void prevActivity(TourActivity act,double latestDepartureTime, double latestOperationStartTime) {
		states.putActivityState(act, StateTypes.LATEST_OPERATION_START_TIME, new StateImpl(latestOperationStartTime));
	}

	@Override
	public void end(Start start, double latestDepartureTime) {}

	

}
