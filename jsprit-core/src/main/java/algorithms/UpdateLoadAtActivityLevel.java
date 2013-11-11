package algorithms;

import org.apache.log4j.Logger;

import basics.route.ActivityVisitor;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

/**
 * Updates load at activity level. 
 * 
 * <p>Note that this assumes that StateTypes.LOAD_AT_DEPOT is already updated, i.e. it starts by setting loadAtDepot to StateTypes.LOAD_AT_DEPOT.
 * If StateTypes.LOAD_AT_DEPOT is not set, it starts with 0 load at depot.
 * 
 * <p>Thus it DEPENDS on StateTypes.LOAD_AT_DEPOT
 *  
 * @author stefan
 *
 */
class UpdateLoadAtActivityLevel implements ActivityVisitor, StateUpdater {
	
	private static Logger logger = Logger.getLogger(UpdateLoadAtActivityLevel.class);
	
	private StateManager stateManager;
	private int currentLoad = 0;
	private VehicleRoute route;
	
	/**
	 * Updates load at activity level. 
	 * 
	 * <p>Note that this assumes that StateTypes.LOAD_AT_DEPOT is already updated, i.e. it starts by setting loadAtDepot to StateTypes.LOAD_AT_DEPOT.
	 * If StateTypes.LOAD_AT_DEPOT is not set, it starts with 0 load at depot.
	 *
	 * <p>Thus it DEPENDS on StateTypes.LOAD_AT_DEPOT
	 * 
	 * <p>If you want to update StateTypes.LOAD_AT_DEPOT see {@link UpdateLoadsAtStartAndEndOfRouteWhenInsertionStarts}, {@link UpdateLoadsAtStartAndEndOfRouteWhenJobHasBeenInserted}
	 * 
	 * <p>The loads can be retrieved by <br>
	 * <code>stateManager.getActivityState(activity,StateTypes.LOAD);</code>
	 * 
	 * 
	 * 
	 * @see {@link UpdateLoadsAtStartAndEndOfRouteWhenInsertionStarts}, {@link UpdateLoadsAtStartAndEndOfRouteWhenJobHasBeenInserted}
	 * @author stefan
	 *
	 */
	public UpdateLoadAtActivityLevel(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}
	
	@Override
	public void begin(VehicleRoute route) {
		currentLoad = (int) stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING).toDouble();
//		logger.info(route + " load@beginning=" + currentLoad);
		this.route = route;
	}

	@Override
	public void visit(TourActivity act) {
		currentLoad += act.getCapacityDemand();
		stateManager.putActivityState(act, StateFactory.LOAD, StateFactory.createState(currentLoad));
//		logger.info(act + " load@act=" + currentLoad + " vehcap=" + route.getVehicle().getCapacity());
		assert currentLoad <= route.getVehicle().getCapacity() : "currentLoad at activity must not be > vehicleCapacity";
		assert currentLoad >= 0 : "currentLoad at act must not be < 0";
	}

	@Override
	public void finish() {
//		logger.info("end of " + route);
//		stateManager.putRouteState(route, StateFactory., state)
		currentLoad = 0;
	}
}