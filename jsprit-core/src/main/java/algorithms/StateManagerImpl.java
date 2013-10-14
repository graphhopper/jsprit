/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import basics.Job;
import basics.algo.InsertionStartsListener;
import basics.algo.JobInsertedListener;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class StateManagerImpl implements StateManager, InsertionStartsListener, JobInsertedListener {

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
	
	private Map<VehicleRoute,States> vehicleRouteStates = new HashMap<VehicleRoute, StateManager.States>();
	
	private Map<TourActivity,States> activityStates = new HashMap<TourActivity, StateManager.States>();
	
	private RouteActivityVisitor routeActivityVisitor = new RouteActivityVisitor();
	
	private ReverseRouteActivityVisitor revRouteActivityVisitor = new ReverseRouteActivityVisitor();
	
	private Collection<RouteVisitor> routeVisitors = new ArrayList<RouteVisitor>();
	
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
		if(stateType.equals(StateTypes.FUTURE_PICKS)) return new StateImpl(0);
		if(stateType.equals(StateTypes.PAST_DELIVERIES)) return new StateImpl(0);
		return null;
	}
	
	private State getDefaultRouteState(String stateType, VehicleRoute route){
		if(stateType.equals(StateTypes.LOAD)) return new StateImpl(0);
		if(stateType.equals(StateTypes.LOAD_AT_DEPOT)) return new StateImpl(0);
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

	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
		for(RouteVisitor v : routeVisitors){ v.visit(inRoute); }
		routeActivityVisitor.visit(inRoute);
		revRouteActivityVisitor.visit(inRoute);
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes,Collection<Job> unassignedJobs) {
		for(VehicleRoute route : vehicleRoutes){ 
			for(RouteVisitor v : routeVisitors){ v.visit(route); }
			routeActivityVisitor.visit(route);
			revRouteActivityVisitor.visit(route);
		}
	}
	
	public void addActivityVisitor(ActivityVisitor activityVistor){
		routeActivityVisitor.addActivityVisitor(activityVistor);
	}
	
	public void addActivityVisitor(ReverseActivityVisitor activityVistor){
		revRouteActivityVisitor.addActivityVisitor(activityVistor);
	}

	public void addRouteVisitor(RouteVisitor routeVisitor){
		routeVisitors.add(routeVisitor);
	}
}
