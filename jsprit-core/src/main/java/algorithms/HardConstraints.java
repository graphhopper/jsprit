package algorithms;

import basics.Service;

class HardConstraints {
	
	static class HardLoadConstraint implements HardConstraint{

		private StateManager states;
		
		public HardLoadConstraint(StateManager states) {
			super();
			this.states = states;
		}

		@Override
		public boolean fulfilled(InsertionScenario iScenario) {
			int currentLoad = (int) states.getRouteState(iScenario.getiFacts().getRoute(), StateTypes.LOAD).toDouble();
			Service service = (Service) iScenario.getiFacts().getJob();
			if(currentLoad + service.getCapacityDemand() > iScenario.getiFacts().getNewVehicle().getCapacity()){
				return false;
			}
			return true;
		}
		
	}

}
