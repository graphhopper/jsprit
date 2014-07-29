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

import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.algorithm.recreate.listener.*;
import jsprit.core.algorithm.ruin.listener.RuinListener;
import jsprit.core.algorithm.ruin.listener.RuinListeners;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.VehicleRoutingProblem;
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
import jsprit.core.problem.vehicle.Vehicle;

import java.util.*;

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
			if(states.containsKey(id)){
				return type.cast(states.get(id));
			}
			return null;
		}
		
		public boolean containsKey(StateId stateId){
			return states.containsKey(stateId);
		}
		
		public void clear(){
			states.clear();
		}
		
	}
	
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

	private boolean updateLoad = false;
	
	private boolean updateTWs = false;

    private int stateIndexCounter = 21;

    private Map<String,StateId> createdStateIds = new HashMap<String, StateId>();

    private int initialNuStates = 30;

    private int nuActivities;

    private int nuVehicleTypeKeys;

    private Object[][] activity_states;

    private Object[][][] vehicle_dependent_activity_states;

    private Object[][] route_states;

    private Object[][][] vehicle_dependent_route_states;

    private VehicleRoutingProblem vrp;

    int getMaxIndexOfVehicleTypeIdentifiers(){ return nuVehicleTypeKeys; }

    /**
     * Create and returns a stateId with the specified state-name.
     *
     * <p>If a stateId with the specified name has already been created, it returns the created stateId.</p>
     * <p>If the specified is equal to a name that is already used internally, it throws an IllegalStateException</p>
     * @param name the specified name of the state
     * @return the stateId with which a state can be identified, no matter if it is a problem, route or activity state.
     * @throws java.lang.IllegalStateException if name of state is already used internally
     */
    public StateId createStateId(String name){
        if(createdStateIds.containsKey(name)) return createdStateIds.get(name);
        if(stateIndexCounter>=activity_states[0].length){
            activity_states = new Object[vrp.getNuActivities()+1][stateIndexCounter+1];
            route_states = new Object[vrp.getNuActivities()+1][stateIndexCounter+1];
            vehicle_dependent_activity_states = new Object[nuActivities][nuVehicleTypeKeys][stateIndexCounter+1];
            vehicle_dependent_route_states = new Object[nuActivities][nuVehicleTypeKeys][stateIndexCounter+1];
        }
        StateId id = StateFactory.createId(name, stateIndexCounter);
        incStateIndexCounter();
        createdStateIds.put(name, id);
        return id;
    }

    private void incStateIndexCounter() {
        stateIndexCounter++;
    }

    private void addDefaultStates() {
		defaultActivityStates_.put(InternalStates.LOAD, Capacity.Builder.newInstance().build());
		defaultActivityStates_.put(InternalStates.COSTS, 0.);
		defaultActivityStates_.put(InternalStates.DURATION, 0.);
		defaultActivityStates_.put(InternalStates.FUTURE_MAXLOAD, Capacity.Builder.newInstance().build());
		defaultActivityStates_.put(InternalStates.PAST_MAXLOAD, Capacity.Builder.newInstance().build());
		
		defaultRouteStates_.put(InternalStates.LOAD, Capacity.Builder.newInstance().build());
		
		defaultRouteStates_.put(InternalStates.COSTS, 0.);
		defaultRouteStates_.put(InternalStates.DURATION, 0.);
		defaultRouteStates_.put(InternalStates.FUTURE_MAXLOAD, Capacity.Builder.newInstance().build());
		defaultRouteStates_.put(InternalStates.PAST_MAXLOAD, Capacity.Builder.newInstance().build());
		
		defaultRouteStates_.put(InternalStates.MAXLOAD, Capacity.Builder.newInstance().build());
		
		defaultRouteStates_.put(InternalStates.LOAD_AT_END, Capacity.Builder.newInstance().build());
		defaultRouteStates_.put(InternalStates.LOAD_AT_BEGINNING, Capacity.Builder.newInstance().build());
		
	}

    /**
     * Constructs the stateManager with the specified VehicleRoutingProblem.
     *
     * @param vehicleRoutingProblem the corresponding VehicleRoutingProblem
     */
    public StateManager(VehicleRoutingProblem vehicleRoutingProblem){
        this.vrp = vehicleRoutingProblem;
        nuActivities = Math.max(10, vrp.getNuActivities() + 1);
        nuVehicleTypeKeys = Math.max(3, getNuVehicleTypes(vrp) + 2);
        activity_states = new Object[nuActivities][initialNuStates];
        route_states = new Object[nuActivities][initialNuStates];
        vehicle_dependent_activity_states = new Object[nuActivities][nuVehicleTypeKeys][initialNuStates];
        vehicle_dependent_route_states = new Object[nuActivities][nuVehicleTypeKeys][initialNuStates];
        addDefaultStates();
    }

    private int getNuVehicleTypes(VehicleRoutingProblem vrp) {
        int maxIndex = 0;
        for(Vehicle v : vrp.getVehicles()){
            maxIndex = Math.max(maxIndex,v.getVehicleTypeIdentifier().getIndex());
        }
        return maxIndex;
    }

    @Deprecated
    public <T> void addDefaultProblemState(StateId stateId, Class<T> type, T defaultState){
		defaultProblemStates_.putState(stateId, type, defaultState); 
	}

    /**
     * Associates the specified state to the stateId. If there already exists a state value for the stateId, this old
     * value is replaced by the new value.
     *
     * @param stateId the stateId which is the associated key to the problem state
     * @param type the type of the problem state
     * @param state the actual state value
     * @param <T> the type of the state value
     */
	public <T> void putProblemState(StateId stateId, Class<T> type, T state){
		problemStates_.putState(stateId, type, state);
	}

    /**
     * Returns mapped state value that is associated to the specified stateId, or null if no value is associated to
     * the specified stateId.
     *
     * @param stateId the stateId which is the associated key to the problem state
     * @param type the type class of the state value
     * @param <T>  the type
     * @return the state value that is associated to the specified stateId or null if no value is associated
     */
	public <T> T getProblemState(StateId stateId, Class<T> type){
		return problemStates_.getState(stateId, type);
	}

    @Deprecated
	<T> T getDefaultProblemState(StateId stateId, Class<T> type){
		if(defaultProblemStates_.containsKey(stateId)) return defaultProblemStates_.getState(stateId, type); 
		return null;
	}

    @Deprecated
	public <T> void addDefaultRouteState(StateId stateId, Class<T> type, T defaultState){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
		defaultRouteStates_.put(stateId, type.cast(defaultState));
	}
	
	@Deprecated
	public <T> void addDefaultActivityState(StateId stateId, Class<T> type, T defaultState){
		if(StateFactory.isReservedId(stateId)) StateFactory.throwReservedIdException(stateId.toString());
		defaultActivityStates_.put(stateId, type.cast(defaultState));
	}
	
	/**
	 * Clears all states, i.e. set all value to null.
	 * 
	 */
	public void clear(){
        fill_twoDimArr(activity_states, null);
        fill_twoDimArr(route_states, null);
        fill_threeDimArr(vehicle_dependent_activity_states, null);
        fill_threeDimArr(vehicle_dependent_route_states, null);
		problemStates_.clear();
	}

    private void fill_threeDimArr(Object[][][] states, Object o) {
        for(Object[][] twoDimArr : states){
            for(Object[] oneDimArr : twoDimArr){
                Arrays.fill(oneDimArr,o);
            }
        }
    }

    private void fill_twoDimArr(Object[][] states, Object o) {
        for(Object[] rows : states){
            Arrays.fill(rows,o);
        }
    }

    /**
	 * Returns associated state for the specified activity and stateId, or it returns null if no value is associated.
     * <p>If type class is not equal to the associated type class of the requested state value, it throws a ClassCastException.</p>
	 *
     * @param act the activity for which a state value is associated to
     * @param stateId the stateId for which a state value is associated to
     * @param type the type of class of the associated state value
     * @param <T> the type
     * @return the state value that is associated to the specified activity and stateId, or null if no value is associated.
     * @throws java.lang.ClassCastException if type class is not equal to the associated type class of the requested state value
	 */
	@Override
	public <T> T getActivityState(TourActivity act, StateId stateId, Class<T> type) {
		if(act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
        if(act.getIndex()<0) return null;
        T state;
        try{
           state = type.cast(activity_states[act.getIndex()][stateId.getIndex()]);
        }
        catch (ClassCastException e){
            throw getClassCastException(e,stateId,type.toString(),activity_states[act.getIndex()][stateId.getIndex()].getClass().toString());
        }
        return state;
	}

    /**
     * Returns true if a state value is associated to the specified activity, vehicle and stateId.
     *
     * @param act the activity for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the problem state
     * @return true if a state value is associated otherwise false
     */
    public boolean hasActivityState(TourActivity act, Vehicle vehicle, StateId stateId){
        if(act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
        return vehicle_dependent_activity_states[act.getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()] != null;
    }

    /**
     * Returns the associated state value to the specified activity, vehicle and stateId, or null if no state value is
     * associated.
     * <p>If type class is not equal to the associated type class of the requested state value, it throws a ClassCastException.</p>
     * @param act the activity for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the problem state
     * @param type the class of the associated state value
     * @param <T> the type of the class
     * @return the associated state value to the specified activity, vehicle and stateId, or null if no state value is
     * associated.
     * @throws java.lang.ClassCastException if type class is not equal to the associated type class of the requested state value
     */
    public <T> T getActivityState(TourActivity act, Vehicle vehicle, StateId stateId, Class<T> type) {
        if(act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
        if(act.getIndex()<0) return null;
        T state;
        try {
            state = type.cast(vehicle_dependent_activity_states[act.getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()]);
        }
        catch(ClassCastException e){
            Object state_class = vehicle_dependent_activity_states[act.getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()];
            throw getClassCastException(e,stateId,type.toString(),state_class.getClass().toString());
        }
        return state;
    }

    private ClassCastException getClassCastException(ClassCastException e, StateId stateId, String requestedTypeClass, String memorizedTypeClass){
        return new ClassCastException(e + "\n" + "state with stateId '" + stateId.toString() + "' is of " + memorizedTypeClass + ". cannot cast it to " + requestedTypeClass + ".");
    }

    @Deprecated
	private <T> T getDefaultTypedActivityState(TourActivity act, StateId stateId, Class<T> type) {
		if(defaultActivityStates_.containsKey(stateId)){
			return type.cast(defaultActivityStates_.get(stateId));
		}
		if(stateId.equals(InternalStates.EARLIEST_OPERATION_START_TIME)){
			return type.cast(act.getTheoreticalEarliestOperationStartTime());
		}
		if(stateId.equals(InternalStates.LATEST_OPERATION_START_TIME)){
			return type.cast(act.getTheoreticalLatestOperationStartTime());
		}
		return null;
	}

    /**
     * Returns the route state that is associated to the route and stateId, or null if no state is associated.
     * <p>If type class is not equal to the associated type class of the requested state value, it throws a ClassCastException.</p>
     *
     * @param route the route which the associated route key to the route state
     * @param stateId the stateId which is the associated key to the route state
     * @param type the class of the associated state value
     * @param <T> the type of the class
     * @return the route state that is associated to the route and stateId, or null if no state is associated.
     * @throws java.lang.ClassCastException if type class is not equal to the associated type class of the requested state value
     */
	@Override
	public <T> T getRouteState(VehicleRoute route, StateId stateId, Class<T> type) {
        if(route.isEmpty()) return null;
        T state;
        int index_of_first_act = route.getActivities().get(0).getIndex();
        if(index_of_first_act == 0) throw new IllegalStateException("first activity in route has no index. this should not be.");
        try{
            state = type.cast(route_states[index_of_first_act][stateId.getIndex()]);
        }
        catch (ClassCastException e){
            throw getClassCastException(e,stateId,type.toString(),route_states[index_of_first_act][stateId.getIndex()].getClass().toString());
        }
        return state;
	}

    /**
     * Returns true if a state is assigned to the specified route, vehicle and stateId. Otherwise it returns false.
     *
     * @param route the route for which the state is requested
     * @param vehicle the vehicle for which the state is requested
     * @param stateId the stateId(entifier) for the state that is requested
     * @return true if state exists and false otherwise
     */
    public boolean hasRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId) {
        return vehicle_dependent_route_states[route.getActivities().get(0).getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()] != null;
    }

    /**
     * Returns the route state that is assigned to the specified route, vehicle and stateId.
     * <p>Returns null if no state can be found</p>
     * @param route the route for which the state is requested
     * @param vehicle the vehicle for which the state is requested
     * @param stateId the stateId(entifier) for the state that is requested
     * @param type the type class of the requested state
     * @param <T> the type of the class
     * @return the actual route state that is assigned to the route, vehicle and stateId
     * @throws java.lang.ClassCastException if specified type is not equal to the memorized type
     */
    public <T> T getRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, Class<T> type) {
        if(route.isEmpty()) return null;
        int index_of_first_act = route.getActivities().get(0).getIndex();
        if(index_of_first_act == 0) throw new IllegalStateException("first activity in route has no index. this should not be.");
        T state;
        try{
           state = type.cast(vehicle_dependent_route_states[index_of_first_act][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()]);
        }
        catch( ClassCastException e){
            throw getClassCastException(e, stateId, type.toString(), vehicle_dependent_route_states[index_of_first_act][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()].getClass().toString());
        }
        return state;
    }

    @Deprecated
	private <T> T getDefaultTypedRouteState(StateId stateId, Class<T> type) {
		if(defaultRouteStates_.containsKey(stateId)){
			return type.cast(defaultRouteStates_.get(stateId));
		}
		return null;
	}

    @Deprecated
	public <T> void putTypedActivityState(TourActivity act, StateId stateId, Class<T> type, T state){
        if(stateId.getIndex()<10) throw new IllegalStateException("either you use a reserved stateId that is applied\n" +
                "internally or your stateId has been created without index, e.g. StateFactory.createId(stateName)\n" +
                " does not assign indeces thus do not use it anymore, but use\n " +
                "stateManager.createStateId(name)\n" +
                " instead.\n");
		putInternalTypedActivityState(act, stateId, state);
	}

    /**
     * Method to memorize state 'state' of type 'type' of act and stateId.
     *
     * <p><b>For example: </b><br>
     * <code>Capacity loadAtMyActivity = Capacity.Builder.newInstance().addCapacityDimension(0,10).build();<br>
     * stateManager.putTypedActivityState(myActivity, StateFactory.createStateId("act-load"), Capacity.class, loadAtMyActivity);</code>
     * <p>you can retrieve the load at myActivity by <br>
     * <code>Capacity load = stateManager.getActivityState(myActivity, StateFactory.createStateId("act-load"), Capacity.class);</code>
     *
     * @param act for which a new state should be memorized
     * @param stateId stateId of state
     * @param type class of state-value
     * @param state state-value
     */
    @Deprecated
    public <T> void putActivityState(TourActivity act, StateId stateId, Class<T> type, T state){
        if(act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
        if(stateId.getIndex()<10) throw new IllegalStateException("either you use a reserved stateId that is applied\n" +
                "internally or your stateId has been created without index, e.g. StateFactory.createId(stateName)\n" +
                " does not assign indeces thus do not use it anymore, but use\n " +
                "stateManager.createStateId(name)\n" +
                " instead.\n");
        putInternalTypedActivityState(act, stateId, state);
    }

    /**
     * Associates the specified activity and stateId to the state value. If a state value is already associated to the
     * specified activity and stateId, it is replaced by the new state value.
     *
     * @param act the activity for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param state the state that is associated to the activity and stateId
     * @param <T> the type of the state
     * @throws java.lang.IllegalStateException if stateId is equall to a stateId that is already used internally.
     */
    public <T> void putActivityState(TourActivity act, StateId stateId, T state){
        if(act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
        if(stateId.getIndex()<10) throw new IllegalStateException("either you use a reserved stateId that is applied\n" +
                "internally or your stateId has been created without index, e.g. StateFactory.createId(stateName)\n" +
                " does not assign indeces thus do not use it anymore, but use\n " +
                "stateManager.createStateId(name)\n" +
                " instead.\n");
        putInternalTypedActivityState(act, stateId, state);
    }

    /**
     * Associates the specified activity, vehicle and stateId to the state value. If a state value is already associated to the
     * specified activity and stateId, it is replaced by the new state value.
     *
     * @param act the activity for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param state the state that is associated to the activity and stateId
     * @param <T> the type of the state
     * @throws java.lang.IllegalStateException if stateId is equall to a stateId that is already used internally.
     */
    public <T> void putActivityState(TourActivity act, Vehicle vehicle, StateId stateId, T state){
        if(act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
        if(stateId.getIndex()<10) throw new IllegalStateException("either you use a reserved stateId that is applied\n" +
                "internally or your stateId has been created without index, e.g. StateFactory.createId(stateName)\n" +
                " does not assign indeces thus do not use it anymore, but use\n " +
                "stateManager.createStateId(name)\n" +
                " instead.\n");
        putInternalTypedActivityState(act, vehicle, stateId, state);
    }

    private Object[][] resizeArr(Object[][] states, int newLength) {
        int oldSize = states.length;
        Object[][] new_states = new Object[newLength][stateIndexCounter];
        System.arraycopy(states,0,new_states,0,Math.min(oldSize,newLength));
        return new_states;
    }

    <T> void putInternalTypedActivityState(TourActivity act, StateId stateId, T state){
        activity_states[act.getIndex()][stateId.getIndex()]=state;
	}

    <T> void putInternalTypedActivityState(TourActivity act, Vehicle vehicle, StateId stateId, T state){
        vehicle_dependent_activity_states[act.getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()]=state;
    }

    @Deprecated
	public <T> void putTypedRouteState(VehicleRoute route, StateId stateId, Class<T> type, T state){
		putRouteState(route, stateId, state);
	}

    /**
     * Associates the specified route, vehicle and stateId to the state value. If a state value is already associated to the
     * specified activity and stateId, it is replaced by the new state value.
     *
     * @param route the route for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param state the state that is associated to the activity and stateId
     * @param <T> the type of the state
     * @throws java.lang.IllegalStateException if stateId is equall to a stateId that is already used internally.
     */
    public <T> void putRouteState(VehicleRoute route, StateId stateId, T state){
        if(stateId.getIndex()<10) StateFactory.throwReservedIdException(stateId.toString());
        putTypedInternalRouteState(route, stateId, state);
    }

    /**
     * Associates the specified route, vehicle and stateId to the state value. If a state value is already associated to the
     * specified activity and stateId, it is replaced by the new state value.
     *
     * @param route the route for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param state the state that is associated to the activity and stateId
     * @param <T> the type of the state
     * @throws java.lang.IllegalStateException if stateId is equall to a stateId that is already used internally.
     */
    public <T> void putRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, T state){
        if(vehicle.getIndex() == 0) throw new IllegalStateException("vehicle index is 0. this should not be.");
        if(stateId.getIndex()<10) StateFactory.throwReservedIdException(stateId.toString());
        putTypedInternalRouteState(route, vehicle, stateId, state);
    }

    <T> void putTypedInternalRouteState(VehicleRoute route, StateId stateId, T state){
        if(route.isEmpty()) return;
        route_states[route.getActivities().get(0).getIndex()][stateId.getIndex()] = state;
    }

    <T> void putTypedInternalRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, T state){
        if(route.isEmpty()) return;
        vehicle_dependent_route_states[route.getActivities().get(0).getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()] = state;
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
	 * @param updater the update to be added
	 */
	public void addStateUpdater(StateUpdater updater){
		if(updater instanceof ActivityVisitor) addActivityVisitor((ActivityVisitor) updater);
		if(updater instanceof ReverseActivityVisitor) addActivityVisitor((ReverseActivityVisitor)updater);
		if(updater instanceof RouteVisitor) addRouteVisitor((RouteVisitor) updater);
		if(updater instanceof InsertionListener) addListener((InsertionListener) updater);
		if(updater instanceof RuinListener) addListener((RuinListener) updater);
		updaters.add(updater);
	}

    /**
     * Returns an unmodifiable collections of stateUpdaters that have been added to this stateManager.
     *
     * @return an unmodifiable collections of stateUpdaters that have been added to this stateManager.
     */
	Collection<StateUpdater> getStateUpdaters(){
		return Collections.unmodifiableCollection(updaters);
	}
	
	/**
	 * Adds an activityVisitor.
	 * <p>This visitor visits all activities in a route subsequently in two cases. First, if insertionStart (after ruinStrategies have removed activities from routes)
	 * and, second, if a job has been inserted and thus if a route has changed. 
	 * 
	 * @param activityVistor the activity-visitor to be added
	 */
	 void addActivityVisitor(ActivityVisitor activityVistor){
		routeActivityVisitor.addActivityVisitor(activityVistor);
	}

	/**
	 * Adds an reverseActivityVisitor.
	 * <p>This reverseVisitor visits all activities in a route subsequently (starting from the end of the route) in two cases. First, if insertionStart (after ruinStrategies have removed activities from routes)
	 * and, second, if a job has been inserted and thus if a route has changed. 
	 * 
	 * @param activityVistor activityVisitor to add
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

    /**
     * Updates load states.
     */
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

    /**
     * Updates time-window states.
     */
	public void updateTimeWindowStates() {
        updateTWs=true;
	}

    public boolean timeWindowUpdateIsActivated(){
        return updateTWs;
    }

    /**
     * Updates skill states.
     */
    public void updateSkillStates() {
        addActivityVisitor(new UpdateSkills(this));
    }

	
}
