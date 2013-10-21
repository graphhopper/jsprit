package algorithms;

import algorithms.StateManager.StateImpl;
import basics.route.DeliveryActivity;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdateOccuredDeliveriesAtActivityLevel implements ActivityVisitor, StateUpdater {
	private StateManager stateManager;
	private int deliveries = 0; 
	private VehicleRoute route;
	
	public UpdateOccuredDeliveriesAtActivityLevel(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route; 
	}

	@Override
	public void visit(TourActivity act) {
		if(act instanceof DeliveryActivity){
			deliveries += Math.abs(act.getCapacityDemand());
		}
		stateManager.putActivityState(act, StateIdFactory.PAST_DELIVERIES, new StateImpl(deliveries));
		assert deliveries >= 0 : "deliveries < 0";
		assert deliveries <= route.getVehicle().getCapacity() : "deliveries > vehicleCap";
	}

	@Override
	public void finish() {
		deliveries = 0;
		route = null;
	}
}