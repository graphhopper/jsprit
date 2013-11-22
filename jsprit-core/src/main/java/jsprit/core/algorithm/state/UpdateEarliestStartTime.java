package jsprit.core.algorithm.state;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.util.ActivityTimeTracker;

class UpdateEarliestStartTime implements ActivityVisitor,StateUpdater{

	private StateManager states;
	
	private ActivityTimeTracker timeTracker;
	
	public UpdateEarliestStartTime(StateManager states, VehicleRoutingTransportCosts transportCosts) {
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
		states.putInternalActivityState(activity, StateFactory.EARLIEST_OPERATION_START_TIME, StateFactory.createState(Math.max(timeTracker.getActArrTime(), activity.getTheoreticalEarliestOperationStartTime())));
		
	}

	@Override
	public void finish() {}

}