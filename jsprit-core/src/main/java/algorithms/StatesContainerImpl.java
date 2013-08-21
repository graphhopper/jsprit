package algorithms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import basics.route.TourActivity;
import basics.route.VehicleRoute;

class StatesContainerImpl implements StatesContainer{

	static class StatesImpl implements States{

		private Map<String,State> states = new HashMap<String, State>();
		
		public void putState(String key, State state) {
			states.put(key, state);
		}

		@Override
		public State getState(String key) {
			return states.get(key);
		}

	}
	
	private Map<VehicleRoute,States> vehicleRouteStates = new HashMap<VehicleRoute, StatesContainer.States>();
	
	private Map<TourActivity,States> activityStates = new HashMap<TourActivity, StatesContainer.States>();
	
	public Map<VehicleRoute, States> getRouteStates() {
		return Collections.unmodifiableMap(vehicleRouteStates);
	}
	
	public States getRouteStates(VehicleRoute route){
		return vehicleRouteStates.get(route);
	}

	public void put(VehicleRoute route, States states) {
		vehicleRouteStates.put(route, states);
	}

	public Map<TourActivity, States> getActivityStates() {
		return Collections.unmodifiableMap(activityStates);
	}

	public States getActivityStates(TourActivity act){
		return activityStates.get(act);
	}
	
	public void put(TourActivity act, States states) {
		activityStates.put(act, states);
	}
	
	public void clear(){
		vehicleRouteStates.clear();
		activityStates.clear();
	}

	@Override
	public State getActivityState(TourActivity act, String stateType) {
		if(!activityStates.containsKey(act)){
			return getDefaultActState(stateType,act);
		}
		StatesImpl actStates = (StatesImpl) activityStates.get(act);
		State state = actStates.getState(stateType);
		if(state == null){
			return getDefaultActState(stateType,act);
		}
		return state;
	}
	
	public void putActivityState(TourActivity act, String stateType, State state){
		if(!activityStates.containsKey(act)){
			activityStates.put(act, new StatesImpl());
		}
		StatesImpl actStates = (StatesImpl) activityStates.get(act);
		actStates.putState(stateType, state);
	}
	
	
	private State getDefaultActState(String stateType, TourActivity act){
		if(stateType.equals(StateTypes.LOAD)) return new StateImpl(0);
		if(stateType.equals(StateTypes.COSTS)) return new StateImpl(0);
		if(stateType.equals(StateTypes.DURATION)) return new StateImpl(0);
		if(stateType.equals(StateTypes.EARLIEST_OPERATION_START_TIME)) return new StateImpl(act.getTheoreticalEarliestOperationStartTime());
		if(stateType.equals(StateTypes.LATEST_OPERATION_START_TIME)) return new StateImpl(act.getTheoreticalLatestOperationStartTime());
		return null;
	}
	
	private State getDefaultRouteState(String stateType, VehicleRoute route){
		if(stateType.equals(StateTypes.LOAD)) return new StateImpl(0);
		if(stateType.equals(StateTypes.COSTS)) return new StateImpl(0);
		if(stateType.equals(StateTypes.DURATION)) return new StateImpl(0);
		return null;
	}

	@Override
	public State getRouteState(VehicleRoute route, String stateType) {
		if(!vehicleRouteStates.containsKey(route)){
			return getDefaultRouteState(stateType,route);
		}
		StatesImpl routeStates = (StatesImpl) vehicleRouteStates.get(route);
		State state = routeStates.getState(stateType);
		if(state == null){
			return getDefaultRouteState(stateType, route);
		}
		return state;
	}
	
	public void putRouteState(VehicleRoute route, String stateType, State state){
		if(!vehicleRouteStates.containsKey(route)){
			vehicleRouteStates.put(route, new StatesImpl());
		}
		StatesImpl routeStates = (StatesImpl) vehicleRouteStates.get(route);
		routeStates.putState(stateType, state);
	}
	
	

}
