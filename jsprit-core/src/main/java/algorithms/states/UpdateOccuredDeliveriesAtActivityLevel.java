package algorithms.states;

import algorithms.ActivityVisitor;
import algorithms.StateManagerImpl;
import algorithms.StateManagerImpl.StateImpl;
import algorithms.StateTypes;
import basics.route.DeliveryActivity;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

public class UpdateOccuredDeliveriesAtActivityLevel implements ActivityVisitor {
	private StateManagerImpl stateManager;
	private int deliveries = 0; 
	private VehicleRoute route;
	
	public UpdateOccuredDeliveriesAtActivityLevel(StateManagerImpl stateManager) {
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
		stateManager.putActivityState(act, StateTypes.PAST_DELIVERIES, new StateImpl(deliveries));
		assert deliveries >= 0 : "deliveries < 0";
		assert deliveries <= route.getVehicle().getCapacity() : "deliveries > vehicleCap";
	}

	@Override
	public void finish() {
		deliveries = 0;
		route = null;
	}
}