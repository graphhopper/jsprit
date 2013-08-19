package algorithms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import basics.route.TourActivity;
import basics.route.VehicleRoute;

public class StatesContainerImpl implements StatesContainer{

	class StatesImpl implements States{

		private Map<String,State> states = new HashMap<String, State>();
		
		public void putState(String key, State state) {
			states.put(key, state);
		}

		@Override
		public State getState(String key) {
			return states.get(key);
		}

	}
	
	private Map<VehicleRoute,States> vehicleRoutes = new HashMap<VehicleRoute, StatesContainer.States>();
	
	private Map<TourActivity,States> tourActivities = new HashMap<TourActivity, StatesContainer.States>();
	
	@Override
	public Map<VehicleRoute, States> getRouteStates() {
		return Collections.unmodifiableMap(vehicleRoutes);
	}

	public void put(VehicleRoute route, States states) {
		vehicleRoutes.put(route, states);
	}

	@Override
	public Map<TourActivity, States> getActivityStates() {
		return Collections.unmodifiableMap(tourActivities);
	}

	public void put(TourActivity act, States states) {
		tourActivities.put(act, states);
	}

}
