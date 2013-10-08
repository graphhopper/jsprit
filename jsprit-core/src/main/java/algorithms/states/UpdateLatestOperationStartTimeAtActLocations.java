package algorithms.states;

import org.apache.log4j.Logger;

import algorithms.ReverseActivityVisitor;
import algorithms.StateManagerImpl;
import algorithms.StateManagerImpl.StateImpl;
import algorithms.StateTypes;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

public class UpdateLatestOperationStartTimeAtActLocations implements ReverseActivityVisitor{

	private static Logger log = Logger.getLogger(UpdateLatestOperationStartTimeAtActLocations.class);
	
	private StateManagerImpl states;
	
	private VehicleRoute route;
	
	private VehicleRoutingTransportCosts transportCosts;
	
	private double latestArrTimeAtPrevAct;
	
	private TourActivity prevAct;
	
	public UpdateLatestOperationStartTimeAtActLocations(StateManagerImpl states, VehicleRoutingTransportCosts tpCosts) {
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
		
		states.putActivityState(activity, StateTypes.LATEST_OPERATION_START_TIME, new StateImpl(latestArrivalTime));
		
		latestArrTimeAtPrevAct = latestArrivalTime;
		prevAct = activity;
	}

	@Override
	public void finish() {}
}