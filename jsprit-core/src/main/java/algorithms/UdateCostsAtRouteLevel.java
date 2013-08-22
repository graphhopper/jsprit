package algorithms;

import java.util.Collection;

import basics.Job;
import basics.algo.InsertionEndsListener;
import basics.algo.InsertionStartsListener;
import basics.algo.JobInsertedListener;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.VehicleRoute;

class UdateCostsAtRouteLevel implements JobInsertedListener, InsertionStartsListener, InsertionEndsListener{
	
	private StateManagerImpl states;
	
	private VehicleRoutingTransportCosts tpCosts;
	
	private VehicleRoutingActivityCosts actCosts;
	
	public UdateCostsAtRouteLevel(StateManagerImpl states, VehicleRoutingTransportCosts tpCosts, VehicleRoutingActivityCosts actCosts) {
		super();
		this.states = states;
		this.tpCosts = tpCosts;
		this.actCosts = actCosts;
	}

	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
//		inRoute.getVehicleRouteCostCalculator().addTransportCost(additionalCosts);
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
		IterateRouteForwardInTime forwardInTime = new IterateRouteForwardInTime(tpCosts);
		forwardInTime.addListener(new UpdateCostsAtAllLevels(actCosts, tpCosts, states));
		for(VehicleRoute route : vehicleRoutes){
			forwardInTime.iterate(route);
		}
		
	}

	@Override
	public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes) {
		IterateRouteForwardInTime forwardInTime = new IterateRouteForwardInTime(tpCosts);
		forwardInTime.addListener(new UpdateCostsAtAllLevels(actCosts, tpCosts, states));
		for(VehicleRoute route : vehicleRoutes){
			forwardInTime.iterate(route);
		}
		
	}

}
