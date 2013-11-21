package algorithms;

import org.apache.log4j.Logger;

import basics.route.ActivityVisitor;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdatePrevMaxLoad implements ActivityVisitor, StateUpdater {
	private static Logger log = Logger.getLogger(UpdatePrevMaxLoad.class);
	private StateManager stateManager;
	private VehicleRoute route;
	private double currLoad;
	private double prevMaxLoad;
	
	public UpdatePrevMaxLoad(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route; 
		currLoad = stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING).toDouble();
		prevMaxLoad = currLoad;
//		log.debug("prevMaxLoad@start="+prevMaxLoad);
	}

	@Override
	public void visit(TourActivity act) {
		prevMaxLoad = Math.max(prevMaxLoad, stateManager.getActivityState(act, StateFactory.LOAD).toDouble());
//		log.debug("prevMaxLoad@"+act+"="+prevMaxLoad);
		stateManager.putActivityState(act, StateFactory.PAST_DELIVERIES, StateFactory.createState(prevMaxLoad));
		assert prevMaxLoad >= 0 : "maxLoad can never be smaller than 0";
		assert prevMaxLoad <= route.getVehicle().getCapacity() : "maxLoad can never be bigger than vehicleCap";
	}

	@Override
	public void finish() {
//		log.debug("prevMaxLoad@end="+prevMaxLoad);
	}
}