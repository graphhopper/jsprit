package algorithms;

import algorithms.StateManager.StateImpl;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdateEarliestStartTimeWindowAtActLocations implements ActivityVisitor,StateUpdater{

	private StateManager states;
	
	private ActivityTimeTracker timeTracker;
	
	public UpdateEarliestStartTimeWindowAtActLocations(StateManager states, VehicleRoutingTransportCosts transportCosts) {
		super();
		this.states = states;
		timeTracker = new ActivityTimeTracker(transportCosts);
	}

	@Override
	public void begin(VehicleRoute route) {
		timeTracker.begin(route);
	}

	@Override
	public void visit(TourActivity activity) {
		timeTracker.visit(activity);
		states.putActivityState(activity, StateIdFactory.EARLIEST_OPERATION_START_TIME, new StateImpl(Math.max(timeTracker.getActArrTime(), activity.getTheoreticalEarliestOperationStartTime())));
		
	}

	@Override
	public void finish() {}

}