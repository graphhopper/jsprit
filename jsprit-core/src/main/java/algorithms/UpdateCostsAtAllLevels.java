package algorithms;

import org.apache.log4j.Logger;

import algorithms.ForwardInTimeListeners.ForwardInTimeListener;
import algorithms.StateManager.StateImpl;
import basics.costs.ForwardTransportCost;
import basics.costs.VehicleRoutingActivityCosts;
import basics.route.End;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.TourActivity.JobActivity;
import basics.route.VehicleRoute;

class UpdateCostsAtAllLevels implements ForwardInTimeListener{

	private static Logger log = Logger.getLogger(UpdateCostsAtAllLevels.class);
	
	private VehicleRoutingActivityCosts activityCost;

	private ForwardTransportCost transportCost;
	
	private StateManagerImpl states;
	
	private double totalOperationCost = 0.0;
	
	private VehicleRoute vehicleRoute = null;
	
	private TourActivity prevAct = null;
	
	private double startTimeAtPrevAct = 0.0;
	
	public UpdateCostsAtAllLevels(VehicleRoutingActivityCosts activityCost, ForwardTransportCost transportCost, StateManagerImpl states) {
		super();
		this.activityCost = activityCost;
		this.transportCost = transportCost;
		this.states = states;
	}

	@Override
	public void start(VehicleRoute route, Start start, double departureTime) {
		vehicleRoute = route;
		vehicleRoute.getVehicleRouteCostCalculator().reset();
		prevAct = start;
		startTimeAtPrevAct = departureTime;
//		log.info(start + " depTime=" + departureTime);
	}

	@Override
	public void nextActivity(TourActivity act, double arrTime, double endTime) {
//		log.info(act + " job " + ((JobActivity)act).getJob().getId() + " arrTime=" + arrTime + " endTime=" +  endTime);
		double transportCost = this.transportCost.getTransportCost(prevAct.getLocationId(), act.getLocationId(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
		double actCost = activityCost.getActivityCost(act, arrTime, vehicleRoute.getDriver(), vehicleRoute.getVehicle());

		vehicleRoute.getVehicleRouteCostCalculator().addTransportCost(transportCost);
		vehicleRoute.getVehicleRouteCostCalculator().addActivityCost(actCost);

		if(transportCost > 10000 || actCost > 100000){
			throw new IllegalStateException("aaaääähh");
		}
		
		totalOperationCost += transportCost;
		totalOperationCost += actCost;

		states.putActivityState(act, StateTypes.COSTS, new StateImpl(totalOperationCost));

		prevAct = act;
		startTimeAtPrevAct = endTime;	
	}

	@Override
	public void end(End end, double arrivalTime) {
//		log.info(end + " arrTime=" + arrivalTime);
		double transportCost = this.transportCost.getTransportCost(prevAct.getLocationId(), end.getLocationId(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
		double actCost = activityCost.getActivityCost(end, arrivalTime, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
		
		vehicleRoute.getVehicleRouteCostCalculator().addTransportCost(transportCost);
		vehicleRoute.getVehicleRouteCostCalculator().addActivityCost(actCost);
		
		if(transportCost > 10000 || actCost > 100000){
			throw new IllegalStateException("aaaääähh");
		}
		
		totalOperationCost += transportCost;
		totalOperationCost += actCost;
		
		states.putRouteState(vehicleRoute, StateTypes.COSTS, new StateImpl(totalOperationCost));
		
		//this is rather strange and likely to change
		vehicleRoute.getVehicleRouteCostCalculator().price(vehicleRoute.getDriver());
		vehicleRoute.getVehicleRouteCostCalculator().price(vehicleRoute.getVehicle());
		vehicleRoute.getVehicleRouteCostCalculator().finish();
		
		startTimeAtPrevAct = 0.0;
		prevAct = null;
		vehicleRoute = null;
		totalOperationCost = 0.0;
	}

}
