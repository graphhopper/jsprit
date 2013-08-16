package algorithms;

import java.util.Collection;

import algorithms.RuinStrategy.RuinListener;
import basics.Job;
import basics.algo.JobInsertedListener;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.VehicleRoute;

public class UpdateRoute implements JobInsertedListener, RuinListener{

	private TourStateUpdater routeStateUpdater;
	
	public UpdateRoute(RouteStates routeStates, VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts) {
		routeStateUpdater = new TourStateUpdater(routeStates, routingCosts, activityCosts);
	}

	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute) {
		routeStateUpdater.updateRoute(inRoute);
	}

	@Override
	public void ruinStarts(Collection<VehicleRoute> routes) {}

	@Override
	public void ruinEnds(Collection<VehicleRoute> routes,Collection<Job> unassignedJobs) {
		for(VehicleRoute route : routes) routeStateUpdater.updateRoute(route);
	}

	@Override
	public void removed(Job job, VehicleRoute fromRoute) {}

}
