package algorithms;

import org.apache.log4j.Logger;

import basics.route.ReverseActivityVisitor;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdateMaxLoad implements ReverseActivityVisitor, StateUpdater {
	private static Logger log = Logger.getLogger(UpdateMaxLoad.class);
	private StateManager stateManager;
	private VehicleRoute route;
	private double maxLoad;
	private double currLoad;
	
	public UpdateMaxLoad(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route;
		maxLoad = stateManager.getRouteState(route, StateFactory.LOAD_AT_END).toDouble();
//		currLoad = maxLoad;
//		log.debug("maxLoad@end="+maxLoad);
	}

	@Override
	public void visit(TourActivity act) {
		maxLoad = Math.max(maxLoad, stateManager.getActivityState(act, StateFactory.LOAD).toDouble());
//		currLoad -= act.getCapacityDemand();
//		log.debug("maxLoad@"+act+"="+maxLoad);
		stateManager.putActivityState(act, StateFactory.FUTURE_PICKS, StateFactory.createState(maxLoad));
		assert maxLoad <= route.getVehicle().getCapacity() : "maxLoad can never be bigger than vehicleCap";
		assert maxLoad >= 0 : "maxLoad can never be smaller than 0";
	}

	@Override
	public void finish() {
//		stateManager.putRouteState(route, StateFactory.MAXLOAD, StateFactory.createState(maxLoad));
//		log.debug("maxLoad@start="+maxLoad);
	}
}