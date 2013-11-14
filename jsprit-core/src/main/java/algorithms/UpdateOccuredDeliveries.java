package algorithms;

import basics.route.ActivityVisitor;
import basics.route.DeliverService;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdateOccuredDeliveries implements ActivityVisitor, StateUpdater {
	private StateManager stateManager;
	private int deliveries = 0; 
	private VehicleRoute route;
	
	public UpdateOccuredDeliveries(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route; 
	}

	@Override
	public void visit(TourActivity act) {
		if(act instanceof DeliverService){
			deliveries += Math.abs(act.getCapacityDemand());
		}
		stateManager.putActivityState(act, StateFactory.PAST_DELIVERIES, StateFactory.createState(deliveries));
		assert deliveries >= 0 : "deliveries < 0";
		assert deliveries <= route.getVehicle().getCapacity() : "deliveries > vehicleCap";
	}

	@Override
	public void finish() {
		deliveries = 0;
		route = null;
	}
}