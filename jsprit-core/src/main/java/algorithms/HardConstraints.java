package algorithms;

import basics.Service;
import basics.route.TourActivity;

class HardConstraints {
	
	interface HardRouteLevelConstraint {

		public boolean fulfilled(InsertionScenario iScenario);
		
	}
	
	interface HardActivityLevelConstraint {
		
		public boolean fulfilled(InsertionFacts iFacts, TourActivity act, double arrTime);

	}
	
	static class HardLoadConstraint implements HardRouteLevelConstraint{

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
	
	static class HardTimeWindowConstraint implements HardActivityLevelConstraint {

		private StateManager states;
		
		public HardTimeWindowConstraint(StateManager states) {
			super();
			this.states = states;
		}

		@Override
		public boolean fulfilled(InsertionFacts iFacts, TourActivity act, double arrTime) {
			if(arrTime > states.getActivityState(act, StateTypes.LATEST_OPERATION_START_TIME).toDouble()){
				return false;
			}
			return true;
		}

	}

}
