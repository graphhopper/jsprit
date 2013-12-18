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
import jsprit.core.problem.solution.route.state.StateFactory.States;


public class StateManager implements RouteAndActivityStateGetter, IterationStartsListener, RuinListener, InsertionStartsListener, JobInsertedListener, InsertionEndsListener {
	
	private Map<VehicleRoute,States> vehicleRouteStates = new HashMap<VehicleRoute, States>();
	
	private Map<TourActivity,States> activityStates = new HashMap<TourActivity, States>();
	
	private RouteActivityVisitor routeActivityVisitor = new RouteActivityVisitor();
	
	private ReverseRouteActivityVisitor revRouteActivityVisitor = new ReverseRouteActivityVisitor();
	
	private Collection<RouteVisitor> routeVisitors = new ArrayList<RouteVisitor>();
	
	private RuinListeners ruinListeners = new RuinListeners();
	
	private InsertionListeners insertionListeners = new InsertionListeners();
	
	private Collection<StateUpdater> updaters = new ArrayList<StateUpdater>();
	
	private Map<StateId,State> defaultRouteStates = new HashMap<StateId, State>();
	
	private Map<StateId,State> defaultActivityStates = new HashMap<StateId, State>();
	
	private VehicleRoutingTransportCosts routingCosts;
	
	private boolean updateLoad = false;
	
	private boolean updateTWs = false;
	
	public StateManager(VehicleRoutingProblem vrp) {
		super();
		this.routingCosts = vrp.getTransportCosts();
	}
	
	public StateManager(VehicleRoutingTransportCosts routingCosts){
		this.routingCosts = routingCosts;
	}
	

	public void addDefaultRouteState(StateId stateId, State defaultState){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
		defaultRouteStates.put(stateId, defaultState);
	}
	
	public void addDefaultActivityState(StateId stateId, State defaultState){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
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
		States actStates = activityStates.get(act);
		State state = actStates.getState(stateId);
		if(state == null){
			return getDefaultActState(stateId,act);
		}
		return state;
	}
	
	void putInternalActivityState(TourActivity act, StateId stateId, State state){
		if(!activityStates.containsKey(act)){
			activityStates.put(act, StateFactory.createStates());
		}
		States actStates = activityStates.get(act);
		actStates.putState(stateId, state);
	}

	public void putActivityState(TourActivity act, StateId stateId, State state){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
		putInternalActivityState(act, stateId, state);
	}

	void putInternalRouteState(VehicleRoute route, StateId stateId, State state){
		if(!vehicleRouteStates.containsKey(route)){
			vehicleRouteStates.put(route, StateFactory.createStates());
		}
		States routeStates = (States) vehicleRouteStates.get(route);
		routeStates.putState(stateId, state);
	}

	public void putRouteState(VehicleRoute route, StateId stateId, State state){
		 if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
         putInternalRouteState(route, stateId, state);
	}

	@Override
	public State getRouteState(VehicleRoute route, StateId stateId) {
		if(!vehicleRouteStates.containsKey(route)){
			return getDefaultRouteState(stateId,route);
		}
		States routeStates = vehicleRouteStates.get(route);
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
		if(stateId.equals(StateFactory.LOAD)) return StateFactory.createState(0);
		if(stateId.equals(StateFactory.COSTS)) return StateFactory.createState(0);
		if(stateId.equals(StateFactory.DURATION)) return StateFactory.createState(0);
		if(stateId.equals(StateFactory.EARLIEST_OPERATION_START_TIME)) return StateFactory.createState(act.getTheoreticalEarliestOperationStartTime());
		if(stateId.equals(StateFactory.LATEST_OPERATION_START_TIME)) return StateFactory.createState(act.getTheoreticalLatestOperationStartTime());
		if(stateId.equals(StateFactory.FUTURE_MAXLOAD)) return StateFactory.createState(0);
		if(stateId.equals(StateFactory.PAST_MAXLOAD)) return StateFactory.createState(0);
		if(defaultActivityStates.containsKey(stateId)) return defaultActivityStates.get(stateId);
		return null;
	}
	
	private State getDefaultRouteState(StateId stateId, VehicleRoute route){
		if(stateId.equals(StateFactory.MAXLOAD)) return StateFactory.createState(0);
		if(stateId.equals(StateFactory.LOAD)) return StateFactory.createState(0);
		if(stateId.equals(StateFactory.LOAD_AT_END)) return StateFactory.createState(0);
		if(stateId.equals(StateFactory.LOAD_AT_BEGINNING)) return StateFactory.createState(0);
		if(stateId.equals(StateFactory.COSTS)) return StateFactory.createState(0);
		if(stateId.equals(StateFactory.DURATION)) return StateFactory.createState(0);
		if(defaultRouteStates.containsKey(stateId)) return defaultRouteStates.get(stateId);
		return null;
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
			addActivityVisitor(new UpdateMaxLoad(this));
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
