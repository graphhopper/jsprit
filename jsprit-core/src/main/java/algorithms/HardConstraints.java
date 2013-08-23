package algorithms;

import basics.Service;
import basics.route.TourActivity;

class HardConstraints {
	
	interface HardRouteLevelConstraint {

		public boolean fulfilled(InsertionContext insertionContext);
		
	}
	
	interface HardActivityLevelConstraint {
		
		public boolean fulfilled(InsertionContext iFacts, TourActivity act, double arrTime);

	}
	
	static class HardLoadConstraint implements HardRouteLevelConstraint{

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
	
	static class HardTimeWindowConstraint implements HardActivityLevelConstraint {

		private StateManager states;
		
		public HardTimeWindowConstraint(StateManager states) {
			super();
			this.states = states;
		}

		@Override
		public boolean fulfilled(InsertionContext iFacts, TourActivity act, double arrTime) {
			if(arrTime > states.getActivityState(act, StateTypes.LATEST_OPERATION_START_TIME).toDouble()){
				return false;
			}
			return true;
		}

	}

}
