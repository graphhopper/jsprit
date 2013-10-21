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
class HardPickupAndDeliveryLoadRouteLevelConstraint implements HardRouteLevelConstraint {

	private StateGetter stateManager;
	
	public HardPickupAndDeliveryLoadRouteLevelConstraint(StateGetter stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public boolean fulfilled(InsertionContext insertionContext) {
		if(insertionContext.getJob() instanceof Delivery){
			int loadAtDepot = (int) stateManager.getRouteState(insertionContext.getRoute(), StateIdFactory.LOAD_AT_BEGINNING).toDouble();
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