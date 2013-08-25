package algorithms;

import java.util.Collection;

import algorithms.RuinStrategy.RuinListener;
import basics.Job;
import basics.algo.JobInsertedListener;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.VehicleRoute;

class UpdateStates implements JobInsertedListener, RuinListener{

	private IterateRouteForwardInTime iterateForward;
	
	private IterateRouteBackwardInTime iterateBackward;
	
	public UpdateStates(StateManagerImpl states, VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts) {
		
		iterateForward = new IterateRouteForwardInTime(routingCosts);
		iterateForward.addListener(new UpdateActivityTimes());
		iterateForward.addListener(new UpdateCostsAtAllLevels(activityCosts, routingCosts, states));
		iterateForward.addListener(new UpdateLoadAtAllLevels(states));
//		iterateForward.addListener(new UpdateEarliestStartTimeWindowAtActLocations(states));
		
		iterateBackward = new IterateRouteBackwardInTime(routingCosts);
		iterateBackward.addListener(new UpdateLatestOperationStartTimeAtActLocations(states));
	}
	
	public void update(VehicleRoute route){
		iterateForward.iterate(route);
		iterateBackward.iterate(route);
	}

	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
		iterateForward.iterate(inRoute);
		iterateBackward.iterate(inRoute);
	}

	@Override
	public void ruinStarts(Collection<VehicleRoute> routes) {}

	@Override
	public void ruinEnds(Collection<VehicleRoute> routes,Collection<Job> unassignedJobs) {
		for(VehicleRoute route : routes) {
			iterateForward.iterate(route);
			iterateBackward.iterate(route);
		}
	}

	@Override
	public void removed(Job job, VehicleRoute fromRoute) {}

}
