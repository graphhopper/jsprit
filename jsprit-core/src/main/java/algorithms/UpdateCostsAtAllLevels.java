package algorithms;

import org.apache.log4j.Logger;

import algorithms.StateManagerImpl.StateImpl;
import basics.costs.ForwardTransportCost;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;

/**
 * Updates total costs (i.e. transport and activity costs) at route and activity level.
 * 
 * <p>Thus it modifies <code>stateManager.getRouteState(route, StateTypes.COSTS)</code> and <br>
 * <code>stateManager.getActivityState(activity, StateTypes.COSTS)</code>
 * 
 * 
 * @param activityCost
 * @param transportCost
 * @param states
 */
public class UpdateCostsAtAllLevels implements ActivityVisitor{

	private static Logger log = Logger.getLogger(UpdateCostsAtAllLevels.class);
	
	private VehicleRoutingActivityCosts activityCost;

	private ForwardTransportCost transportCost;
	
	private StateManagerImpl states;
	
	private double totalOperationCost = 0.0;
	
	private VehicleRoute vehicleRoute = null;
	
	private TourActivity prevAct = null;
	
	private double startTimeAtPrevAct = 0.0;
	
	private ActivityTimeTracker timeTracker;
	
	/**
	 * Updates total costs (i.e. transport and activity costs) at route and activity level.
	 * 
	 * <p>Thus it modifies <code>stateManager.getRouteState(route, StateTypes.COSTS)</code> and <br>
	 * <code>stateManager.getActivityState(activity, StateTypes.COSTS)</code>
	 * 
	 * 
	 * @param activityCost
	 * @param transportCost
	 * @param states
	 */
	public UpdateCostsAtAllLevels(VehicleRoutingActivityCosts activityCost, VehicleRoutingTransportCosts transportCost, StateManagerImpl states) {
		super();
		this.activityCost = activityCost;
		this.transportCost = transportCost;
		this.states = states;
		timeTracker = new ActivityTimeTracker(transportCost);
	}

	@Override
	public void begin(VehicleRoute route) {
		vehicleRoute = route;
		vehicleRoute.getVehicleRouteCostCalculator().reset();
		timeTracker.begin(route);
		prevAct = route.getStart();
		startTimeAtPrevAct = timeTracker.getActEndTime();
	}

	@Override
	public void visit(TourActivity act) {
		timeTracker.visit(act);
		
		double transportCost = this.transportCost.getTransportCost(prevAct.getLocationId(), act.getLocationId(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
		double actCost = activityCost.getActivityCost(act, timeTracker.getActArrTime(), vehicleRoute.getDriver(), vehicleRoute.getVehicle());

		vehicleRoute.getVehicleRouteCostCalculator().addTransportCost(transportCost);
		vehicleRoute.getVehicleRouteCostCalculator().addActivityCost(actCost);
		
		totalOperationCost += transportCost;
		totalOperationCost += actCost;

		states.putActivityState(act, StateIdFactory.COSTS, new StateImpl(totalOperationCost));

		prevAct = act;
		startTimeAtPrevAct = timeTracker.getActEndTime();
	}

	@Override
	public void finish() {
		timeTracker.finish();
		double transportCost = this.transportCost.getTransportCost(prevAct.getLocationId(), vehicleRoute.getEnd().getLocationId(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
		double actCost = activityCost.getActivityCost(vehicleRoute.getEnd(), timeTracker.getActEndTime(), vehicleRoute.getDriver(), vehicleRoute.getVehicle());
		
		vehicleRoute.getVehicleRouteCostCalculator().addTransportCost(transportCost);
		vehicleRoute.getVehicleRouteCostCalculator().addActivityCost(actCost);
		
		totalOperationCost += transportCost;
		totalOperationCost += actCost;
//		totalOperationCost += getFixCosts(vehicleRoute.getVehicle());
		
		states.putRouteState(vehicleRoute, StateIdFactory.COSTS, new StateImpl(totalOperationCost));
		
		//this is rather strange and likely to change
		vehicleRoute.getVehicleRouteCostCalculator().price(vehicleRoute.getDriver());
		vehicleRoute.getVehicleRouteCostCalculator().price(vehicleRoute.getVehicle());
		vehicleRoute.getVehicleRouteCostCalculator().finish();
		
		startTimeAtPrevAct = 0.0;
		prevAct = null;
		vehicleRoute = null;
		totalOperationCost = 0.0;
	}

	private double getFixCosts(Vehicle vehicle) {
		if(vehicle == null) return 0.0;
		if(vehicle.getType() == null) return 0.0;
		return vehicle.getType().getVehicleCostParams().fix;
	}

}