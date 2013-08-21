package algorithms;

import algorithms.BackwardInTimeListeners.BackwardInTimeListener;
import algorithms.StatesContainer.StateImpl;
import basics.route.End;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdateLatestOperationStartTimeAtActLocations implements BackwardInTimeListener{

	private StatesContainerImpl states;
	
	public UpdateLatestOperationStartTimeAtActLocations(StatesContainerImpl states) {
		super();
		this.states = states;
	}

	@Override
	public void start(VehicleRoute route) {}

	@Override
	public void prevActivity(TourActivity act,double latestDepartureTime, double latestOperationStartTime) {
//		if(latestOperationStartTime < act.getArrTime()) throw new IllegalStateException(act.toString() + "; latestStart="+latestOperationStartTime+";actArrTime="+act.getArrTime());
		if(act instanceof Start || act instanceof End) return;
		states.putActivityState(act, StateTypes.LATEST_OPERATION_START_TIME, new StateImpl(latestOperationStartTime));
	}

	@Override
	public void finnish() {}

}
