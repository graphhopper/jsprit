package algorithms;

import algorithms.ForwardInTimeListeners.ForwardInTimeListener;
import algorithms.StatesContainer.StateImpl;
import basics.route.End;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdateEarliestStartTimeWindowAtActLocations implements ForwardInTimeListener{

	private StatesContainerImpl states;
	
	public UpdateEarliestStartTimeWindowAtActLocations(StatesContainerImpl states) {
		super();
		this.states = states;
	}

	@Override
	public void start(VehicleRoute route) {}

	@Override
	public void nextActivity(TourActivity act, double arrTime, double endTime) {
		if(act instanceof Start || act instanceof End) return;
		states.putActivityState(act, StateTypes.EARLIEST_OPERATION_START_TIME, new StateImpl(Math.max(arrTime, act.getTheoreticalEarliestOperationStartTime())));
	}

	@Override
	public void finnish() {}

}
