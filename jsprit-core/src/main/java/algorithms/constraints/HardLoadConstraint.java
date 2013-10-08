package algorithms.constraints;

import algorithms.InsertionContext;
import algorithms.StateManager;
import algorithms.StateTypes;
import basics.Service;

public class HardLoadConstraint implements HardRouteLevelConstraint{

	private StateManager states;
	
	public HardLoadConstraint(StateManager states) {
		super();
		this.states = states;
	}

	@Override
	public boolean fulfilled(InsertionContext insertionContext) {
		int currentLoad = (int) states.getRouteState(insertionContext.getRoute(), StateTypes.LOAD).toDouble();
		Service service = (Service) insertionContext.getJob();
		if(currentLoad + service.getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
			return false;
		}
		return true;
	}
}