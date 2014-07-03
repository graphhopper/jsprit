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
import jsprit.core.problem.Skills;
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
import jsprit.core.problem.solution.route.state.StateFactory.StateId;

/**
 * Manages states.
 * 
 * <p>Some condition, rules or constraints are stateful. This StateManager manages these states, i.e. it offers
 * methods to add, store and retrieve states based on the problem, vehicle-routes and tour-activities.
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
			if(states.containsKey(id)) return type.cast(states.get(id));
			return null;
		}
		
		public boolean containsKey(StateId stateId){
			return states.containsKey(stateId);
		}
		
		public void clear(){
			states.clear();
		}
		
	}

	private Map<VehicleRoute,States_> vehicleRouteStates_ = new HashMap<VehicleRoute, States_>();
	
	private Map<TourActivity,States_> activityStates_ = new HashMap<TourActivity, States_>();
	
	private States_ problemStates_ = new States_();
	
	private States_ defaultProblemStates_ = new States_();
	
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

    private boolean updateSkills = false;
	
	private void addDefaultStates() {
		defaultActivityStates_.put(StateFactory.LOAD, Capacity.Builder.newInstance().build());
		defaultActivityStates_.put(StateFactory.COSTS, 0.);
		defaultActivityStates_.put(StateFactory.DURATION, 0.);
		defaultActivityStates_.put(StateFactory.FUTURE_MAXLOAD, Capacity.Builder.newInstance().build());
		defaultActivityStates_.put(StateFactory.PAST_MAXLOAD, Capacity.Builder.newInstance().build());
		
		defaultRouteStates_.put(StateFactory.LOAD, Capacity.Builder.newInstance().build());
		defaultRouteStates_.put(StateFactory.SKILLS, Skills.Builder.newInstance().build());
		defaultRouteStates_.put(StateFactory.COSTS, 0.);
		defaultRouteStates_.put(StateFactory.DURATION, 0.);
		defaultRouteStates_.put(StateFactory.FUTURE_MAXLOAD, Capacity.Builder.newInstance().build());
		defaultRouteStates_.put(StateFactory.PAST_MAXLOAD, Capacity.Builder.newInstance().build());
		
		defaultRouteStates_.put(StateFactory.MAXLOAD, Capacity.Builder.newInstance().build());
		
		defaultRouteStates_.put(StateFactory.LOAD_AT_END, Capacity.Builder.newInstance().build());
		defaultRouteStates_.put(StateFactory.LOAD_AT_BEGINNING, Capacity.Builder.newInstance().build());
		
	}

	public StateManager(VehicleRoutingTransportCosts routingCosts){
		this.routingCosts = routingCosts;
		addDefaultStates();
	}
	
	public <T> void addDefaultProblemState(StateId stateId, Class<T> type, T defaultState){
		defaultProblemStates_.putState(stateId, type, defaultState); 
	}
	
	public <T> void putProblemState(StateId stateId, Class<T> type, T state){
		problemStates_.putState(stateId, type, state); 
	}
	
	public <T> T getProblemState(StateId stateId, Class<T> type){
		if(!problemStates_.containsKey(stateId)){
			return getDefaultProblemState(stateId, type);
		}
		return problemStates_.getState(stateId, type);
	}
	
	<T> T getDefaultProblemState(StateId stateId, Class<T> type){
		if(defaultProblemStates_.containsKey(stateId)) return defaultProblemStates_.getState(stateId, type); 
		return null;
	}
	
	/**
	 * Generic method to add a default route state.
	 * 
	 * <p>for example if you want to store 'maximum weight' at route-level, the default might be zero and you
	 * can add the default simply by coding <br>
	 * <code>addDefaultRouteState(StateFactory.createStateId("max_weight"), Integer.class, 0)</code>
	 * 
	 * @param stateId id of state
	 * @param type type of memorized state
	 * @param defaultState actual state
	 */
	public <T> void addDefaultRouteState(StateId stateId, Class<T> type, T defaultState){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
		defaultRouteStates_.put(stateId, type.cast(defaultState));
	}
	
	/**
	 * Generic method to add default activity state.
	 *
     * @param stateId id of state
     * @param type type of memorized state
     * @param defaultState actual state
	 */
	public <T> void addDefaultActivityState(StateId stateId, Class<T> type, T defaultState){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
		defaultActivityStates_.put(stateId, type.cast(defaultState));
	}
	
	/**
	 * Clears all states.
	 * 
	 */
	public void clear(){
		vehicleRouteStates_.clear();
		activityStates_.clear();
		problemStates_.clear();
	}

	/**
	 * Returns activity state of type 'type'.
	 * 
	 */
	@Override
	public <T> T getActivityState(TourActivity act, StateId stateId, Class<T> type) {
		if(!activityStates_.containsKey(act)){
			return getDefaultTypedActivityState(act, stateId, type);
		}
		States_ states = activityStates_.get(act);
		T state = states.getState(stateId, type);
		if(state == null) return getDefaultTypedActivityState(act, stateId, type);
		return state;
	}

	/**
	 *
     * @param act tour activity
     * @param stateId id of state
     * @param type of actual state
     *
	 * @return state
	 */
	private <T> T getDefaultTypedActivityState(TourActivity act, StateId stateId,Class<T> type) {
		if(defaultActivityStates_.containsKey(stateId)){
			return type.cast(defaultActivityStates_.get(stateId));
		}
		if(stateId.equals(StateFactory.EARLIEST_OPERATION_START_TIME)){
			return type.cast(act.getTheoreticalEarliestOperationStartTime());
		}
		if(stateId.equals(StateFactory.LATEST_OPERATION_START_TIME)){
			return type.cast(act.getTheoreticalLatestOperationStartTime());
		}
		return null;
	}

	/**
	 * Return route state of type 'type'.
	 * 
	 * @return route-state
	 * @throws ClassCastException if state of route and stateId is of another type
	 */
	@Override
	public <T> T getRouteState(VehicleRoute route, StateId stateId, Class<T> type) {
		if(!vehicleRouteStates_.containsKey(route)){
			return getDefaultTypedRouteState(stateId, type);
		}
		States_ states = vehicleRouteStates_.get(route);
		T state = states.getState(stateId, type);
		if(state == null) return getDefaultTypedRouteState(stateId, type);
		return state;
	}

	private <T> T getDefaultTypedRouteState(StateId stateId, Class<T> type) {
		if(defaultRouteStates_.containsKey(stateId)){
			return type.cast(defaultRouteStates_.get(stateId));
		}
		return null;
	}
	
	/**
	 * Generic method to memorize state 'state' of type 'type' of act and stateId.
	 * 
	 * <p><b>For example: </b><br>
	 * <code>Capacity loadAtMyActivity = Capacity.Builder.newInstance().addCapacityDimension(0,10).build();<br>
	 * stateManager.putTypedActivityState(myActivity, StateFactory.createStateId("act-load"), Capacity.class, loadAtMyActivity);</code>
	 * <p>you can retrieve the load at myActivity by <br>
	 * <code>Capacity load = stateManager.getActivityState(myActivity, StateFactory.createStateId("act-load"), Capacity.class);</code>
	 * 
	 * @param act tour activity
	 * @param stateId stateId of state to be memorized
	 * @param type type of state
	 * @param state acutall state
	 */
	public <T> void putTypedActivityState(TourActivity act, StateId stateId, Class<T> type, T state){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
		putInternalTypedActivityState(act, stateId, type, state);
	}
	
	<T> void putInternalTypedActivityState(TourActivity act, StateId stateId, Class<T> type, T state){
		if(!activityStates_.containsKey(act)){
			activityStates_.put(act, new States_());
		}
		States_ actStates = activityStates_.get(act);
		actStates.putState(stateId, type, state);
	}

	<T> void putTypedInternalRouteState(VehicleRoute route, StateId stateId, Class<T> type, T state){
		if(!vehicleRouteStates_.containsKey(route)){
			vehicleRouteStates_.put(route, new States_());
		}
		States_ routeStates = vehicleRouteStates_.get(route);
		routeStates.putState(stateId, type, state);
	}

	/**
	 * Generic method to memorize state 'state' of type 'type' of route and stateId.
	 * 
	 * <p><b>For example:</b> <br>
	 * <code>double totalRouteDuration = 100.0;<br>
	 * stateManager.putTypedActivityState(myRoute, StateFactory.createStateId("route-duration"), Double.class, totalRouteDuration);</code>
	 * <p>you can retrieve the duration of myRoute then by <br>
	 * <code>double totalRouteDuration = stateManager.getRouteState(myRoute, StateFactory.createStateId("route-duration"), Double.class);</code> 
	 * 
	 * @param route vehilcRoute that gets a state
	 * @param stateId id of state
	 * @param type of state
	 * @param state actual state
	 */
	public <T> void putTypedRouteState(VehicleRoute route, StateId stateId, Class<T> type, T state){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
        putTypedInternalRouteState(route, stateId, type, state);
	}

	/**
	 * Adds state updater.
	 * 
	 * <p>Note that a state update occurs if route and/or activity states have changed, i.e. if jobs are removed
	 * or inserted into a route. Thus here, it is assumed that a state updater is either of type InsertionListener, 
	 * RuinListener, ActivityVisitor, ReverseActivityVisitor, RouteVisitor, ReverseRouteVisitor. 
	 * 
	 * <p>The following rule pertain for activity/route visitors:These visitors visits all activities/route in a route subsequently in two cases. First, if insertionStart (after ruinStrategies have removed activities from routes)
	 * and, second, if a job has been inserted and thus if a route has changed.
	 *  
	 * @param updater to be inserted here
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
	 * @param activityVisitor
	 */
	 void addActivityVisitor(ActivityVisitor activityVisitor){
		routeActivityVisitor.addActivityVisitor(activityVisitor);
	}

	/**
	 * Adds an reverseActivityVisitor.
	 * <p>This reverseVisitor visits all activities in a route subsequently (starting from the end of the route) in two cases. First, if insertionStart (after ruinStrategies have removed activities from routes)
	 * and, second, if a job has been inserted and thus if a route has changed. 
	 * 
	 * @param activityVisitor
	 */
	 void addActivityVisitor(ReverseActivityVisitor activityVisitor){
		revRouteActivityVisitor.addActivityVisitor(activityVisitor);
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
        update(inRoute);
	}

    public void update(VehicleRoute inRoute) {
        for(RouteVisitor v : routeVisitors){ v.visit(inRoute); }
        routeActivityVisitor.visit(inRoute);
        revRouteActivityVisitor.visit(inRoute);
    }

    @Override
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes,Collection<Job> unassignedJobs) {
		insertionListeners.informInsertionStarts(vehicleRoutes, unassignedJobs);
		for(VehicleRoute route : vehicleRoutes){
            update(route);
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
			addActivityVisitor(new UpdateMaxCapacityUtilisationAtActivitiesByLookingBackwardInRoute(this));
			addActivityVisitor(new UpdateMaxCapacityUtilisationAtActivitiesByLookingForwardInRoute(this));
			addActivityVisitor(new UpdateMaxCapacityUtilisationAtRoute(this));
		}
	}

	public void updateTimeWindowStates() {
		if(!updateTWs){
			updateTWs=true;
			addActivityVisitor(new UpdatePracticalTimeWindows(this, routingCosts));
		}
	}

    public void updateSkillStates(){
        if(!updateSkills){
            updateSkills=true;
            addActivityVisitor(new UpdateSkills(this));
        }
    }
	
}
