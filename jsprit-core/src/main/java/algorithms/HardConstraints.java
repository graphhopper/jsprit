package algorithms;

import basics.Service;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.TourActivity;

class HardConstraints {
	
	interface HardRouteLevelConstraint {

		public boolean fulfilled(InsertionContext insertionContext);
		
	}
	
	interface HardActivityLevelConstraint {
		
		public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime);

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
		
		private VehicleRoutingTransportCosts routingCosts;
		
		public HardTimeWindowConstraint(StateManager states, VehicleRoutingTransportCosts routingCosts) {
			super();
			this.states = states;
			this.routingCosts = routingCosts;
		}

		@Override
		public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
			double arrTimeAtNewAct = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
			if(arrTimeAtNewAct > states.getActivityState(newAct, StateTypes.LATEST_OPERATION_START_TIME).toDouble()){
				return false;
			}
			double endTimeAtNewAct = CalcUtils.getActivityEndTime(arrTimeAtNewAct, newAct);
			double arrTimeAtNextAct = endTimeAtNewAct + routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), endTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
			if(arrTimeAtNextAct > states.getActivityState(nextAct, StateTypes.LATEST_OPERATION_START_TIME).toDouble()){
				return false;
			}
			return true;
		}
	}

}
