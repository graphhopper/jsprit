package algorithms;

import basics.Service;

public class HardLoadConstraint implements HardRouteLevelConstraint{

	private StateManager states;
	
	public HardLoadConstraint(StateManager states) {
		super();
		this.states = states;
	}

	@Override
	public boolean fulfilled(InsertionContext insertionContext) {
		int currentLoad = (int) states.getRouteState(insertionContext.getRoute(), StateIdFactory.LOAD).toDouble();
		Service service = (Service) insertionContext.getJob();
		if(currentLoad + service.getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
			return false;
		}
		return true;
	}
}