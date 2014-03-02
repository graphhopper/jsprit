package jsprit.core.algorithm.state;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.StateFactory;

/**
 * Updates and memorizes latest operation start times at activities.
 * 
 * @author schroeder
 *
 */
class UpdatePracticalTimeWindows implements ReverseActivityVisitor, StateUpdater{

	private StateManager states;
	
	private VehicleRoute route;
	
	private VehicleRoutingTransportCosts transportCosts;
	
	private double latestArrTimeAtPrevAct;
	
	private TourActivity prevAct;
	
	public UpdatePracticalTimeWindows(StateManager states, VehicleRoutingTransportCosts tpCosts) {
		super();
		this.states = states;
		this.transportCosts = tpCosts;
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route;
		latestArrTimeAtPrevAct = route.getEnd().getTheoreticalLatestOperationStartTime();
		prevAct = route.getEnd();
	}

	@Override
	public void visit(TourActivity activity) {
		double potentialLatestArrivalTimeAtCurrAct = latestArrTimeAtPrevAct - transportCosts.getBackwardTransportTime(activity.getLocationId(), prevAct.getLocationId(), latestArrTimeAtPrevAct, route.getDriver(),route.getVehicle()) - activity.getOperationTime();
		double latestArrivalTime = Math.min(activity.getTheoreticalLatestOperationStartTime(), potentialLatestArrivalTimeAtCurrAct);
		
		states.putInternalActivityState_(activity, StateFactory.LATEST_OPERATION_START_TIME, Double.class, latestArrivalTime);
		
		latestArrTimeAtPrevAct = latestArrivalTime;
		prevAct = activity;
	}

	@Override
	public void finish() {}
}