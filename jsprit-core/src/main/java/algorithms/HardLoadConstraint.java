package algorithms;

import basics.Service;

class HardLoadConstraint implements HardRouteLevelConstraint{

	private StateGetter states;
	
	public HardLoadConstraint(StateGetter states) {
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