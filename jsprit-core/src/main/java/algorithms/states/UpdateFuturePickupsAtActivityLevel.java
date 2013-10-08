package algorithms.states;

import algorithms.ReverseActivityVisitor;
import algorithms.StateManagerImpl;
import algorithms.StateManagerImpl.StateImpl;
import algorithms.StateTypes;
import basics.route.PickupActivity;
import basics.route.ServiceActivity;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

public class UpdateFuturePickupsAtActivityLevel implements ReverseActivityVisitor {
	private StateManagerImpl stateManager;
	private int futurePicks = 0;
	private VehicleRoute route;
	
	public UpdateFuturePickupsAtActivityLevel(StateManagerImpl stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route;
	}

	@Override
	public void visit(TourActivity act) {
		stateManager.putActivityState(act, StateTypes.FUTURE_PICKS, new StateImpl(futurePicks));
		if(act instanceof PickupActivity || act instanceof ServiceActivity){
			futurePicks += act.getCapacityDemand();
		}
		assert futurePicks <= route.getVehicle().getCapacity() : "sum of pickups must not be > vehicleCap";
		assert futurePicks >= 0 : "sum of pickups must not < 0";
	}

	@Override
	public void finish() {
		futurePicks = 0;
		route = null;
	}
}