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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import basics.Job;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.InsertionEndsListener;
import basics.algo.InsertionListener;
import basics.algo.InsertionListeners;
import basics.algo.InsertionStartsListener;
import basics.algo.IterationStartsListener;
import basics.algo.JobInsertedListener;
import basics.algo.RuinListener;
import basics.algo.RuinListeners;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

public class StateManager implements StateGetter, IterationStartsListener, RuinListener, InsertionStartsListener, JobInsertedListener, InsertionEndsListener {

	
	
	private interface States {
		
		State getState(StateId key);
		
	}
	
	static class StateImpl implements State{
		double state;

		public StateImpl(double state) {
			super();
			this.state = state;
		}

		@Override
		public double toDouble() {
			return state;
		}
		
	}
	
	private static class StatesImpl implements States{

		private Map<StateId,State> states = new HashMap<StateId, State>();
		
		public void putState(StateId key, State state) {
			states.put(key, state);
		}

		@Override
		public State getState(StateId key) {
			return states.get(key);
		}

	}
	
	private Map<VehicleRoute,States> vehicleRouteStates = new HashMap<VehicleRoute, States>();
	
	private Map<TourActivity,States> activityStates = new HashMap<TourActivity, States>();
	
	private RouteActivityVisitor routeActivityVisitor = new RouteActivityVisitor();
	
	private ReverseRouteActivityVisitor revRouteActivityVisitor = new ReverseRouteActivityVisitor();
	
	private Collection<RouteVisitor> routeVisitors = new ArrayList<RouteVisitor>();
	
	private RuinListeners ruinListeners = new RuinListeners();
	
	private InsertionListeners insertionListeners = new InsertionListeners();
	
	private Collection<StateUpdater> updaters = new ArrayList<StateUpdater>();
	
	private Map<StateId,State> defaultRouteStates = new HashMap<StateGetter.StateId, StateGetter.State>();
	
	private Map<StateId,State> defaultActivityStates = new HashMap<StateId, State>();
	
	public void addDefaultRouteState(StateId stateId, State defaultState){
		defaultRouteStates.put(stateId, defaultState);
	}
	
	public void addDefaultActivityState(StateId stateId, State defaultState){
		defaultActivityStates.put(stateId, defaultState);
	}
	
	public void clear(){
		vehicleRouteStates.clear();
		activityStates.clear();
	}

	@Override
	public State getActivityState(TourActivity act, StateId stateId) {
		if(!activityStates.containsKey(act)){
			return getDefaultActState(stateId,act);
		}
		StatesImpl actStates = (StatesImpl) activityStates.get(act);
		State state = actStates.getState(stateId);
		if(state == null){
			return getDefaultActState(stateId,act);
		}
		return state;
	}
	
	public void putActivityState(TourActivity act, StateId stateId, State state){
		if(!activityStates.containsKey(act)){
			activityStates.put(act, new StatesImpl());
		}
		StatesImpl actStates = (StatesImpl) activityStates.get(act);
		actStates.putState(stateId, state);
	}
	
	
	public void putRouteState(VehicleRoute route, StateId stateId, State state){
		if(!vehicleRouteStates.containsKey(route)){
			vehicleRouteStates.put(route, new StatesImpl());
		}
		StatesImpl routeStates = (StatesImpl) vehicleRouteStates.get(route);
		routeStates.putState(stateId, state);
	}

	@Override
	public State getRouteState(VehicleRoute route, StateId stateId) {
		if(!vehicleRouteStates.containsKey(route)){
			return getDefaultRouteState(stateId,route);
		}
		StatesImpl routeStates = (StatesImpl) vehicleRouteStates.get(route);
		State state = routeStates.getState(stateId);
		if(state == null){
			return getDefaultRouteState(stateId, route);
		}
		return state;
	}

	/**
	 * Adds state updater.
	 * 
	 * <p>Note that a state update occurs if route and/or activity states change, i.e. if jobs are removed
	 * or inserted into a route. Thus here, it is assumed that a state updater is either of type InsertionListener, 
	 * RuinListener, ActivityVisitor, ReverseActivityVisitor, RouteVisitor, ReverseRouteVisitor. 
	 * 
	 * <p>The following rule pertain for activity/route visitors:These visitors visits all activities/route in a route subsequently in two cases. First, if insertionStart (after ruinStrategies have removed activities from routes)
	 * and, second, if a job has been inserted and thus if a route has changed.
	 *  
	 * @param updater
	 */
	public void addStateUpdater(StateUpdater updater){
		if(updater instanceof ActivityVisitor) addActivityVisitor((ActivityVisitor) updater);
		if(updater instanceof ReverseActivityVisitor) addActivityVisitor((ReverseActivityVisitor)updater);
		if(updater instanceof RouteVisitor) addRouteVisitor((RouteVisitor) updater);
		if(updater instanceof InsertionListener) addListener((InsertionListener) updater);
		if(updater instanceof RuinListener) addListener((RuinListener) updater);
		updaters.add(updater);
	}
	
	Collection<StateUpdater> getStateUpdaters(){
		return Collections.unmodifiableCollection(updaters);
	}
	
	/**
	 * Adds an activityVisitor.
	 * <p>This visitor visits all activities in a route subsequently in two cases. First, if insertionStart (after ruinStrategies have removed activities from routes)
	 * and, second, if a job has been inserted and thus if a route has changed. 
	 * 
	 * @param activityVistor
	 */
	 void addActivityVisitor(ActivityVisitor activityVistor){
		routeActivityVisitor.addActivityVisitor(activityVistor);
	}

	/**
	 * Adds an reverseActivityVisitor.
	 * <p>This reverseVisitor visits all activities in a route subsequently (starting from the end of the route) in two cases. First, if insertionStart (after ruinStrategies have removed activities from routes)
	 * and, second, if a job has been inserted and thus if a route has changed. 
	 * 
	 * @param reverseActivityVistor
	 */
	 void addActivityVisitor(ReverseActivityVisitor activityVistor){
		revRouteActivityVisitor.addActivityVisitor(activityVistor);
	}

	 void addRouteVisitor(RouteVisitor routeVisitor){
		routeVisitors.add(routeVisitor);
	}

	void addListener(RuinListener ruinListener){
		ruinListeners.addListener(ruinListener);
	}

	void removeListener(RuinListener ruinListener){
		ruinListeners.removeListener(ruinListener);
	}

	void addListener(InsertionListener insertionListener){
		insertionListeners.addListener(insertionListener);
	}

	void removeListener(InsertionListener insertionListener){
		insertionListeners.removeListener(insertionListener);
	}

	private State getDefaultActState(StateId stateId, TourActivity act){
		if(stateId.equals(StateFactory.LOAD)) return new StateImpl(0);
		if(stateId.equals(StateFactory.COSTS)) return new StateImpl(0);
		if(stateId.equals(StateFactory.DURATION)) return new StateImpl(0);
		if(stateId.equals(StateFactory.EARLIEST_OPERATION_START_TIME)) return new StateImpl(act.getTheoreticalEarliestOperationStartTime());
		if(stateId.equals(StateFactory.LATEST_OPERATION_START_TIME)) return new StateImpl(act.getTheoreticalLatestOperationStartTime());
		if(stateId.equals(StateFactory.FUTURE_PICKS)) return new StateImpl(0);
		if(stateId.equals(StateFactory.PAST_DELIVERIES)) return new StateImpl(0);
		return null;
	}
	
	private State getDefaultRouteState(StateId stateId, VehicleRoute route){
		if(stateId.equals(StateFactory.MAXLOAD)) return new StateImpl(0);
		if(stateId.equals(StateFactory.LOAD)) return new StateImpl(0);
		if(stateId.equals(StateFactory.LOAD_AT_END)) return new StateImpl(0);
		if(stateId.equals(StateFactory.LOAD_AT_BEGINNING)) return new StateImpl(0);
		if(stateId.equals(StateFactory.COSTS)) return new StateImpl(0);
		if(stateId.equals(StateFactory.DURATION)) return new StateImpl(0);
		return null;
	}

	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
		insertionListeners.jobInserted(job2insert, inRoute, additionalCosts, additionalTime);
		for(RouteVisitor v : routeVisitors){ v.visit(inRoute); }
		routeActivityVisitor.visit(inRoute);
		revRouteActivityVisitor.visit(inRoute);
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes,Collection<Job> unassignedJobs) {
		insertionListeners.insertionStarts(vehicleRoutes, unassignedJobs);
		for(VehicleRoute route : vehicleRoutes){ 
			for(RouteVisitor v : routeVisitors){ v.visit(route); }
			routeActivityVisitor.visit(route);
			revRouteActivityVisitor.visit(route);
		}
	}
	
	@Override
	public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		clear();
	}

	@Override
	public void ruinStarts(Collection<VehicleRoute> routes) {
		ruinListeners.ruinStarts(routes);
	}

	@Override
	public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
		ruinListeners.ruinEnds(routes, unassignedJobs);		
	}

	@Override
	public void removed(Job job, VehicleRoute fromRoute) {
		ruinListeners.removed(job, fromRoute);
	}

	@Override
	public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes) {
		insertionListeners.insertionEnds(vehicleRoutes);
	}
}
