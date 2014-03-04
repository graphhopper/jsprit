package jsprit.core.algorithm.state;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.StateFactory;

/**
 * A {@link ReverseActivityVisitor} that looks forward in the vehicle route and determines
 * the maximum capacity utilization (in terms of loads) at subsequent activities. 
 * 
 * <p>Assume a vehicle route with the following activity sequence {start,pickup(1,4),delivery(2,3),pickup(3,2),end} where
 * pickup(1,2) = pickup(id,cap-demand).<br> 
 * Future maxLoad for each activity are calculated as follows:<br>
 * loadAt(end)=6 (since two pickups need to be delivered to depot)<br>
 * pickup(3)=max(loadAt(pickup(3)), futureMaxLoad(end))=max(6,6)=6
 * delivery(2)=max(loadAt(delivery(2),futureMaxLoad(pickup(3))=max(4,6)=6
 * pickup(1)=max(7,6)=7
 * start=max(7,7)=7
 * activity (apart from start and end), the maximum capacity is determined when forward looking into the route.
 * That is at each activity we know how much capacity is available whithout breaking future capacity constraints.
 * 
 * 
 * @author schroeder
 *
 */
class UpdateMaxCapacityUtilisationAtActivitiesByLookingForwardInRoute implements ReverseActivityVisitor, StateUpdater {
	
	private StateManager stateManager;
	
	private VehicleRoute route;
	
	private Capacity maxLoad = Capacity.Builder.newInstance().build();
//	private double maxLoad;
	
	public UpdateMaxCapacityUtilisationAtActivitiesByLookingForwardInRoute(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route;
		maxLoad = stateManager.getRouteState(route, StateFactory.LOAD_AT_END, Capacity.class);
//		maxLoad = stateManager.getRouteState(route, StateFactory.LOAD_AT_END).toDouble();
	}

	@Override
	public void visit(TourActivity act) {
		maxLoad = Capacity.max(maxLoad, stateManager.getActivityState(act, StateFactory.LOAD, Capacity.class));
//		maxLoad = Math.max(maxLoad, stateManager.getActivityState(act, StateFactory.LOAD).toDouble());
		stateManager.putInternalTypedActivityState(act, StateFactory.FUTURE_MAXLOAD, Capacity.class, maxLoad);
//		stateManager.putInternalActivityState(act, StateFactory.FUTURE_MAXLOAD, StateFactory.createState(maxLoad));
		assert maxLoad.isLessOrEqual(route.getVehicle().getType().getCapacityDimensions()) : "maxLoad can in every capacity dimension never be bigger than vehicleCap";
//		assert maxLoad <= route.getVehicle().getCapacity() : "maxLoad can never be bigger than vehicleCap";
		assert maxLoad.isGreaterOrEqual(Capacity.Builder.newInstance().build()) : "maxLoad can never be smaller than 0";
		//		assert maxLoad >= 0 : "maxLoad can never be smaller than 0";
	}

	@Override
	public void finish() {}
}