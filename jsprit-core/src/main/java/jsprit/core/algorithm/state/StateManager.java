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
package jsprit.core.algorithm.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.algorithm.recreate.listener.InsertionEndsListener;
import jsprit.core.algorithm.recreate.listener.InsertionListener;
import jsprit.core.algorithm.recreate.listener.InsertionListeners;
import jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import jsprit.core.algorithm.ruin.listener.RuinListener;
import jsprit.core.algorithm.ruin.listener.RuinListeners;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.ReverseRouteActivityVisitor;
import jsprit.core.problem.solution.route.RouteActivityVisitor;
import jsprit.core.problem.solution.route.RouteVisitor;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.solution.route.state.StateFactory.State;
import jsprit.core.problem.solution.route.state.StateFactory.StateId;

/**
 * Manages states.
 * 
 * <p>Some condition, rules or constraints are stateful. This StateManager manages these states, i.e. it offers
 * methods to add, store and retrieve states based on vehicle-routes and tour-activities.
 * 
 * @author schroeder
 *
 */
public class StateManager implements RouteAndActivityStateGetter, IterationStartsListener, RuinListener, InsertionStartsListener, JobInsertedListener, InsertionEndsListener {
	
	static class States_ {
		
		private Map<StateId,Object> states = new HashMap<StateId,Object>();
		
		public <T> void putState(StateId id, Class<T> type, T state){
			states.put(id, type.cast(state));
		}
		
		public <T> T getState(StateId id, Class<T> type){
			if(states.containsKey(id)){
				T s = type.cast(states.get(id));
				return s;
			}
			return null;
		}
		
	}

	private Map<VehicleRoute,States_> vehicleRouteStates_ = new HashMap<VehicleRoute, States_>();
	
	private Map<TourActivity,States_> activityStates_ = new HashMap<TourActivity, States_>();
	
	private RouteActivityVisitor routeActivityVisitor = new RouteActivityVisitor();
	
	private ReverseRouteActivityVisitor revRouteActivityVisitor = new ReverseRouteActivityVisitor();
	
	private Collection<RouteVisitor> routeVisitors = new ArrayList<RouteVisitor>();
	
	private RuinListeners ruinListeners = new RuinListeners();
	
	private InsertionListeners insertionListeners = new InsertionListeners();
	
	private Collection<StateUpdater> updaters = new ArrayList<StateUpdater>();
	
	private Map<StateId,Object> defaultRouteStates_ = new HashMap<StateId,Object>();
	
	private Map<StateId,Object> defaultActivityStates_ = new HashMap<StateId,Object>();
	
	private VehicleRoutingTransportCosts routingCosts;
	
	private boolean updateLoad = false;
	
	private boolean updateTWs = false;
	
	/**
	 * @deprecated use <code>StateManager(VehicleRoutingTransportCosts tpcosts)</code> instead.
	 * @param vrp
	 */
	@Deprecated
	public StateManager(VehicleRoutingProblem vrp) {
		super();
		this.routingCosts = vrp.getTransportCosts();
		addDefaultStates();
	}
	
	private void addDefaultStates() {
		defaultActivityStates_.put(StateFactory.LOAD, Capacity.Builder.newInstance().build());
		
		
		defaultActivityStates_.put(StateFactory.COSTS, StateFactory.createState(0));
		defaultActivityStates_.put(StateFactory.DURATION, StateFactory.createState(0));
		defaultActivityStates_.put(StateFactory.FUTURE_MAXLOAD, StateFactory.createState(0));
		defaultActivityStates_.put(StateFactory.PAST_MAXLOAD, StateFactory.createState(0));
		
		defaultRouteStates_.put(StateFactory.LOAD, Capacity.Builder.newInstance().build());
		
		defaultRouteStates_.put(StateFactory.COSTS, StateFactory.createState(0));
		defaultRouteStates_.put(StateFactory.DURATION, StateFactory.createState(0));
		defaultRouteStates_.put(StateFactory.FUTURE_MAXLOAD, StateFactory.createState(0));
		defaultRouteStates_.put(StateFactory.PAST_MAXLOAD, StateFactory.createState(0));
		
		defaultRouteStates_.put(StateFactory.MAXLOAD, StateFactory.createState(0));
		
		defaultRouteStates_.put(StateFactory.LOAD_AT_END, Capacity.Builder.newInstance().build());
		defaultRouteStates_.put(StateFactory.LOAD_AT_BEGINNING, Capacity.Builder.newInstance().build());
		
	}

	public StateManager(VehicleRoutingTransportCosts routingCosts){
		this.routingCosts = routingCosts;
		addDefaultStates();
	}
	
	/**
	 * @deprecated use the generic methode <code>addDefaultRouteState(StateId stateId, Class<T> type, T defaultState)</code> instead.
	 * @param stateId
	 * @param defaultState
	 */
	@Deprecated
	public void addDefaultRouteState(StateId stateId, State defaultState){
		addDefaultRouteState(stateId, State.class, defaultState);
	}
	
	/**
	 * Generic method to add a default route state.
	 * 
	 * <p>for example if you want to store 'maximum weight' at route-level, the default might be zero and you
	 * can add the default simply by coding <br>
	 * <code>addDefaultRouteState(StateFactory.createStateId("max_weight"), Integer.class, 0)</code>
	 * 
	 * @param stateId
	 * @param type
	 * @param defaultState
	 */
	public <T> void addDefaultRouteState(StateId stateId, Class<T> type, T defaultState){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
		defaultRouteStates_.put(stateId, type.cast(defaultState));
	}
	
	/**
	 * @deprecated use generic method <code>addDefaultActivityState(StateId stateId, Class<T> type, T defaultState)</code>
	 * @param stateId
	 * @param defaultState
	 */
	@Deprecated
	public void addDefaultActivityState(StateId stateId, State defaultState){
		addDefaultActivityState(stateId, State.class, defaultState);
	}
	
	/**
	 * 
	 * @param stateId
	 * @param type
	 * @param defaultState
	 */
	public <T> void addDefaultActivityState(StateId stateId, Class<T> type, T defaultState){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
		defaultActivityStates_.put(stateId, type.cast(defaultState));
	}
	
	public void clear(){
		vehicleRouteStates_.clear();
		activityStates_.clear();
	}

	@Deprecated
	@Override
	public State getActivityState(TourActivity act, StateId stateId) {
		if(!activityStates_.containsKey(act)){
			return getDefaultActivityState_(act,stateId,State.class);
		}
		States_ actStates = activityStates_.get(act);
		State state = actStates.getState(stateId, State.class);
		if(state == null){
			return getDefaultActivityState_(act,stateId,State.class);
		}
		return state;
	}
	
	@Override
	public <T> T getActivityState(TourActivity act, StateId stateId, Class<T> type) {
		if(!activityStates_.containsKey(act)){
			return getDefaultActivityState_(act, stateId, type);
		}
		States_ states = activityStates_.get(act);
		T state = states.getState(stateId, type);
		if(state == null) return getDefaultActivityState_(act, stateId, type);
		return state;
	}

	private <T> T getDefaultActivityState_(TourActivity act, StateId stateId,Class<T> type) {
		if(defaultActivityStates_.containsKey(stateId)){
			return type.cast(defaultActivityStates_.get(stateId));
		}
		if(stateId.equals(StateFactory.EARLIEST_OPERATION_START_TIME)){
			return type.cast(StateFactory.createState(act.getTheoreticalEarliestOperationStartTime()));
		}
		if(stateId.equals(StateFactory.LATEST_OPERATION_START_TIME)){
			return type.cast(StateFactory.createState(act.getTheoreticalLatestOperationStartTime()));
		}
		return null;
	}

	@Override
	public <T> T getRouteState(VehicleRoute route, StateId stateId, Class<T> type) {
		if(!vehicleRouteStates_.containsKey(route)){
			return getDefaultRouteState_(stateId, type);
		}
		States_ states = vehicleRouteStates_.get(route);
		T state = states.getState(stateId, type);
		if(state == null) return getDefaultRouteState_(stateId, type);
		return state;
	}

	private <T> T getDefaultRouteState_(StateId stateId, Class<T> type) {
		if(defaultRouteStates_.containsKey(stateId)){
			return type.cast(defaultRouteStates_.get(stateId));
		}
		return null;
	}
	
	@Deprecated
	public void putActivityState(TourActivity act, StateId stateId, State state){
		putActivityState_(act, stateId, State.class, state);
	}
	
	public <T> void putActivityState_(TourActivity act, StateId stateId, Class<T> type, T state){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
		putInternalActivityState_(act, stateId, type, state);
	}
	
	@Deprecated
	void putInternalActivityState(TourActivity act, StateId stateId, State state){
		putInternalActivityState_(act, stateId, State.class, state);
	}
	
	<T> void putInternalActivityState_(TourActivity act, StateId stateId, Class<T> type, T state){
		if(!activityStates_.containsKey(act)){
			activityStates_.put(act, new States_());
		}
		States_ actStates = activityStates_.get(act);
		actStates.putState(stateId, type, state);
	}

	@Deprecated
	void putInternalRouteState(VehicleRoute route, StateId stateId, State state){
		putInternalRouteState_(route, stateId, State.class, state);
	}
	
	<T> void putInternalRouteState_(VehicleRoute route, StateId stateId, Class<T> type, T state){
		if(!vehicleRouteStates_.containsKey(route)){
			vehicleRouteStates_.put(route, new States_());
		}
		States_ routeStates = vehicleRouteStates_.get(route);
		routeStates.putState(stateId, type, state);
	}

	@Deprecated
	public void putRouteState(VehicleRoute route, StateId stateId, State state){
		 putRouteState_(route, stateId, State.class, state);
	}
	
	public <T> void putRouteState_(VehicleRoute route, StateId stateId, Class<T> type, T state){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
        putInternalRouteState_(route, stateId, type, state);
	}

	@Deprecated
	@Override
	public State getRouteState(VehicleRoute route, StateId stateId) {
		if(!vehicleRouteStates_.containsKey(route)){
			return getDefaultRouteState_(stateId,State.class);
		}
		States_ routeStates = vehicleRouteStates_.get(route);
		State state = routeStates.getState(stateId,State.class);
		if(state == null){
			return getDefaultRouteState_(stateId, State.class);
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

	
	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
//		log.debug("insert " + job2insert + " in " + inRoute);
		insertionListeners.informJobInserted(job2insert, inRoute, additionalCosts, additionalTime);
		for(RouteVisitor v : routeVisitors){ v.visit(inRoute); }
		routeActivityVisitor.visit(inRoute);
		revRouteActivityVisitor.visit(inRoute);
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes,Collection<Job> unassignedJobs) {
		insertionListeners.informInsertionStarts(vehicleRoutes, unassignedJobs);
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
//		log.debug("ruin ends");
		ruinListeners.ruinEnds(routes, unassignedJobs);		
	}

	@Override
	public void removed(Job job, VehicleRoute fromRoute) {
		ruinListeners.removed(job, fromRoute);
	}

	@Override
	public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes) {
		insertionListeners.informInsertionEndsListeners(vehicleRoutes);
	}
	
	public void updateLoadStates() {
		if(!updateLoad){
			updateLoad=true;
			UpdateLoads updateLoads = new UpdateLoads(this);
			addActivityVisitor(updateLoads);
			addListener(updateLoads);
			addActivityVisitor(new UpdatePrevMaxLoad(this));
			addActivityVisitor(new UpdateMaxLoadForwardLooking(this));
			addActivityVisitor(new UpdateMaxLoad_(this));
		}
	}

	public void updateTimeWindowStates() {
		if(!updateTWs){
			updateTWs=true;
			addActivityVisitor(new UpdateTimeWindow(this, routingCosts));
		}
	}

	
}
