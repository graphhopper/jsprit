package algorithms;

import basics.Delivery;
import basics.Pickup;
import basics.Service;

/**
 * lsjdfjsdlfjsa
 * 
 * @author stefan
 *
 */
public class HardPickupAndDeliveryLoadConstraint implements HardRouteLevelConstraint {

	private StateManager stateManager;
	
	public HardPickupAndDeliveryLoadConstraint(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public boolean fulfilled(InsertionContext insertionContext) {
		if(insertionContext.getJob() instanceof Delivery){
			int loadAtDepot = (int) stateManager.getRouteState(insertionContext.getRoute(), StateIdFactory.LOAD_AT_DEPOT).toDouble();
			if(loadAtDepot + insertionContext.getJob().getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
				return false;
			}
		}
		else if(insertionContext.getJob() instanceof Pickup || insertionContext.getJob() instanceof Service){
			int loadAtEnd = (int) stateManager.getRouteState(insertionContext.getRoute(), StateIdFactory.LOAD).toDouble();
			if(loadAtEnd + insertionContext.getJob().getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
				return false;
			}
		}
		return true;
	}
	
}