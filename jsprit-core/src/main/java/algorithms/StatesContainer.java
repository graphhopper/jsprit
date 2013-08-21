package algorithms;

import java.util.Map;

import basics.route.TourActivity;
import basics.route.VehicleRoute;

interface StatesContainer {
	
	interface State {
		double toDouble();
	}
	
	class StateImpl implements State{
		double state;

		public StateImpl(double state) {
			super();
			this.state = state;
		}

		@Override
		public double toDouble() {
			return state;
		}
		
//		public void setState(double val){
//			state=val;
//		}
	}
	
	interface States {
		
//		void putState(String key, State state);
		
		State getState(String key);
		
	}
	
	
	
	Map<VehicleRoute, States> getRouteStates();
	
//	void put(VehicleRoute route, States states);
	
	Map<TourActivity, States> getActivityStates();
	
//	void put(TourActivity act, States states);
	
	State getActivityState(TourActivity act, String stateType);
	
	State getRouteState(VehicleRoute route, String stateType);

}
