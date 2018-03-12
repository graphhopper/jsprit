/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.*;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListeners;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.ReverseRouteActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.RouteActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.RouteVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.*;

/**
 * Manages states.
 * <p>
 * <p>Some condition, rules or constraints are stateful. This StateManager manages these states, i.e. it offers
 * methods to add, store and retrieve states based on the problem, vehicle-routes and tour-activities.
 *
 * @author schroeder
 */
public class StateManager implements RouteAndActivityStateGetter, IterationStartsListener, RuinListener, InsertionStartsListener, JobInsertedListener, InsertionEndsListener {

    private RouteActivityVisitor routeActivityVisitor = new RouteActivityVisitor();

    private ReverseRouteActivityVisitor revRouteActivityVisitor = new ReverseRouteActivityVisitor();

    private Collection<RouteVisitor> routeVisitors = new ArrayList<RouteVisitor>();

    private RuinListeners ruinListeners = new RuinListeners();

    private InsertionListeners insertionListeners = new InsertionListeners();

    private Collection<StateUpdater> updaters = new ArrayList<StateUpdater>();

    private boolean updateLoad = false;

    private boolean updateTWs = false;

    private final int initialNoStates = 21;

    private int stateIndexCounter;

    private Map<String, StateId> createdStateIds = new HashMap<String, StateId>();

    private int nuActivities;

    private int nuVehicleTypeKeys;

    private Object[] problemStates;

    private Object[][] activityStates;

    private Object[][][] vehicleDependentActivityStates;

    private Map<VehicleRoute, Object[]> routeStateMap;

    private Map<VehicleRoute, Object[][]> vehicleDependentRouteStateMap;

    private Object[][] routeStatesArr;

    private Object[][][] vehicleDependentRouteStatesArr;

    private VehicleRoutingProblem vrp;

    private final boolean isIndexedBased;

    int getMaxIndexOfVehicleTypeIdentifiers() {
        return nuVehicleTypeKeys;
    }

    /**
     * Create and returns a stateId with the specified state-name.
     * <p>
     * <p>If a stateId with the specified name has already been created, it returns the created stateId.</p>
     * <p>If the specified is equal to a name that is already used internally, it throws an IllegalStateException</p>
     *
     * @param name the specified name of the state
     * @return the stateId with which a state can be identified, no matter if it is a problem, route or activity state.
     * @throws java.lang.IllegalStateException if name of state is already used internally
     */
    public StateId createStateId(String name) {
        if (createdStateIds.containsKey(name)) return createdStateIds.get(name);
        if (stateIndexCounter >= activityStates[0].length) {
            activityStates = new Object[nuActivities][stateIndexCounter + 1];
            vehicleDependentActivityStates = new Object[nuActivities][nuVehicleTypeKeys][stateIndexCounter + 1];
            routeStatesArr = new Object[vrp.getVehicles().size() + 2][stateIndexCounter+1];
            vehicleDependentRouteStatesArr = new Object[vrp.getVehicles().size() + 2][nuVehicleTypeKeys][stateIndexCounter+1];
            problemStates = new Object[stateIndexCounter+1];
        }
        StateId id = StateFactory.createId(name, stateIndexCounter);
        incStateIndexCounter();
        createdStateIds.put(name, id);
        return id;
    }

    private void incStateIndexCounter() {
        stateIndexCounter++;
    }


    /**
     * Constructs the stateManager with the specified VehicleRoutingProblem.
     *
     * @param vehicleRoutingProblem the corresponding VehicleRoutingProblem
     */
    public StateManager(VehicleRoutingProblem vehicleRoutingProblem) {
        stateIndexCounter = initialNoStates;
        int initialStateArrayLength = 30;
        this.vrp = vehicleRoutingProblem;
        nuActivities = Math.max(10, vrp.getNuActivities() + 1);
        nuVehicleTypeKeys = Math.max(3, getNuVehicleTypes(vrp) + 2);
        activityStates = new Object[nuActivities][initialStateArrayLength];
        vehicleDependentActivityStates = new Object[nuActivities][nuVehicleTypeKeys][initialStateArrayLength];
//        if(vehicleRoutingProblem.getFleetSize().equals(VehicleRoutingProblem.FleetSize.FINITE)){
//            isIndexedBased = true;
//            routeStatesArr = new Object[vrp.getVehicles().size() + 2][initialStateArrayLength];
//            vehicleDependentRouteStatesArr = new Object[vrp.getVehicles().size() + 2][nuVehicleTypeKeys][initialStateArrayLength];
//        }
//        else {
            isIndexedBased = false;
            routeStateMap = new HashMap<VehicleRoute, Object[]>();
            vehicleDependentRouteStateMap = new HashMap<VehicleRoute, Object[][]>();
//        }
        problemStates = new Object[initialStateArrayLength];
    }

    private int getNuVehicleTypes(VehicleRoutingProblem vrp) {
        int maxIndex = 0;
        for (Vehicle v : vrp.getVehicles()) {
            maxIndex = Math.max(maxIndex, v.getVehicleTypeIdentifier().getIndex());
        }
        return maxIndex;
    }

    /**
     * Associates the specified state to the stateId. If there already exists a state value for the stateId, this old
     * value is replaced by the new value.
     *
     * @param stateId the stateId which is the associated key to the problem state
     * @param type    the type of the problem state
     * @param state   the actual state value
     * @param <T>     the type of the state value
     */
    public <T> void putProblemState(StateId stateId, Class<T> type, T state) {
        problemStates[stateId.getIndex()] = state;
//         problemStates.putState(stateId, type, state);
    }

    /**
     * Returns mapped state value that is associated to the specified stateId, or null if no value is associated to
     * the specified stateId.
     *
     * @param stateId the stateId which is the associated key to the problem state
     * @param type    the type class of the state value
     * @param <T>     the type
     * @return the state value that is associated to the specified stateId or null if no value is associated
     */
    public <T> T getProblemState(StateId stateId, Class<T> type) {
        return type.cast(problemStates[stateId.getIndex()]);
    }

    /**
     * Clears all states, i.e. set all value to null.
     */
    public void clear() {
        fill_twoDimArr(activityStates, null);
        fill_threeDimArr(vehicleDependentActivityStates, null);
        if(isIndexedBased) {
            fill_twoDimArr(routeStatesArr, null);
            fill_threeDimArr(vehicleDependentRouteStatesArr, null);
        }
        else{
            routeStateMap.clear();
            vehicleDependentRouteStateMap.clear();
        }
        Arrays.fill(problemStates,null);
    }

    private void fill_threeDimArr(Object[][][] states, Object o) {
        for (Object[][] twoDimArr : states) {
            for (Object[] oneDimArr : twoDimArr) {
                Arrays.fill(oneDimArr, o);
            }
        }
    }

    private void fill_twoDimArr(Object[][] states, Object o) {
        for (Object[] rows : states) {
            Arrays.fill(rows, o);
        }
    }

    /**
     * Returns associated state for the specified activity and stateId, or it returns null if no value is associated.
     * <p>If type class is not equal to the associated type class of the requested state value, it throws a ClassCastException.</p>
     *
     * @param act     the activity for which a state value is associated to
     * @param stateId the stateId for which a state value is associated to
     * @param type    the type of class of the associated state value
     * @param <T>     the type
     * @return the state value that is associated to the specified activity and stateId, or null if no value is associated.
     * @throws java.lang.ClassCastException    if type class is not equal to the associated type class of the requested state value
     * @throws java.lang.IllegalStateException if <code>act.getIndex()==0</code> since this suggests that act has no index at all
     */
    @Override
    public <T> T getActivityState(TourActivity act, StateId stateId, Class<T> type) {
        if (act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
        if (act.getIndex() < 0) return null;
        T state;
        try {
            state = type.cast(activityStates[act.getIndex()][stateId.getIndex()]);
        } catch (ClassCastException e) {
            throw getClassCastException(e, stateId, type.toString(), activityStates[act.getIndex()][stateId.getIndex()].getClass().toString());
        }
        return state;
    }

    /**
     * Returns true if a state value is associated to the specified activity, vehicle and stateId.
     *
     * @param act     the activity for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the problem state
     * @return true if a state value is associated otherwise false
     * @throws java.lang.IllegalStateException if <code>act.getIndex()==0</code> since this suggests that act has no index at all
     */
    public boolean hasActivityState(TourActivity act, Vehicle vehicle, StateId stateId) {
        if (act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
        return vehicleDependentActivityStates[act.getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()] != null;
    }

    /**
     * Returns the associated state value to the specified activity, vehicle and stateId, or null if no state value is
     * associated.
     * <p>If type class is not equal to the associated type class of the requested state value, it throws a ClassCastException.</p>
     *
     * @param act     the activity for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the problem state
     * @param type    the class of the associated state value
     * @param <T>     the type of the class
     * @return the associated state value to the specified activity, vehicle and stateId, or null if no state value is
     * associated.
     * @throws java.lang.ClassCastException    if type class is not equal to the associated type class of the requested state value
     * @throws java.lang.IllegalStateException if <code>act.getIndex()==0</code> since this suggests that act has no index at all
     */
    public <T> T getActivityState(TourActivity act, Vehicle vehicle, StateId stateId, Class<T> type) {
        if (act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
        if (act.getIndex() < 0) return null; //act.getIndex() < 0 indicates that act is either Start (-1) or End (-2)
        T state;
        try {
            state = type.cast(vehicleDependentActivityStates[act.getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()]);
        } catch (ClassCastException e) {
            Object state_class = vehicleDependentActivityStates[act.getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()];
            throw getClassCastException(e, stateId, type.toString(), state_class.getClass().toString());
        }
        return state;
    }

    private ClassCastException getClassCastException(ClassCastException e, StateId stateId, String requestedTypeClass, String memorizedTypeClass) {
        return new ClassCastException(e + "\n" + "state with stateId '" + stateId.toString() + "' is of " + memorizedTypeClass + ". cannot cast it to " + requestedTypeClass + ".");
    }

    /**
     * Returns the route state that is associated to the route and stateId, or null if no state is associated.
     * <p>If type class is not equal to the associated type class of the requested state value, it throws a ClassCastException.</p>
     *
     * @param route   the route which the associated route key to the route state
     * @param stateId the stateId which is the associated key to the route state
     * @param type    the class of the associated state value
     * @param <T>     the type of the class
     * @return the route state that is associated to the route and stateId, or null if no state is associated.
     * @throws java.lang.ClassCastException    if type class is not equal to the associated type class of the requested state value
     * @throws java.lang.IllegalStateException if <code>!route.isEmpty()</code> and <code>act(0).getIndex()==0</code> since this suggests that act has no index at all
     */
    @Override
    public <T> T getRouteState(VehicleRoute route, StateId stateId, Class<T> type) {
        if (route.isEmpty()) return null;
        T state = null;
        if(isIndexedBased){
            try {
                state = type.cast(routeStatesArr[route.getVehicle().getIndex()][stateId.getIndex()]);
            } catch (ClassCastException e) {
                throw getClassCastException(e,stateId,type.toString(),routeStatesArr[route.getVehicle().getIndex()][stateId.getIndex()].getClass().toString());
            }
        }
        else {
            try {
                if (routeStateMap.containsKey(route)) {
                    state = type.cast(routeStateMap.get(route)[stateId.getIndex()]);
                }
            } catch (ClassCastException e) {
                throw getClassCastException(e, stateId, type.toString(), routeStateMap.get(route)[stateId.getIndex()].getClass().toString());
            }
        }
        return state;
    }

    /**
     * Returns true if a state is assigned to the specified route, vehicle and stateId. Otherwise it returns false.
     *
     * @param route   the route for which the state is requested
     * @param vehicle the vehicle for which the state is requested
     * @param stateId the stateId(entifier) for the state that is requested
     * @return true if state exists and false otherwise
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean hasRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId) {
        if (!vehicleDependentRouteStateMap.containsKey(route)) return false;
        return vehicleDependentRouteStateMap.get(route)[vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()] != null;
//        return vehicle_dependent_route_states[route.getActivities().get(0).getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()] != null;
    }

    /**
     * Returns the route state that is assigned to the specified route, vehicle and stateId.
     * <p>Returns null if no state can be found</p>
     *
     * @param route   the route for which the state is requested
     * @param vehicle the vehicle for which the state is requested
     * @param stateId the stateId(entifier) for the state that is requested
     * @param type    the type class of the requested state
     * @param <T>     the type of the class
     * @return the actual route state that is assigned to the route, vehicle and stateId
     * @throws java.lang.ClassCastException    if specified type is not equal to the memorized type
     * @throws java.lang.IllegalStateException if <code>!route.isEmpty()</code> and <code>act(0).getIndex()==0</code> since this suggests that act has no index at all
     */
    public <T> T getRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, Class<T> type) {
        if (route.isEmpty()) return null;
        T state = null;
        if(isIndexedBased){
            try {
                state = type.cast(vehicleDependentRouteStatesArr[route.getVehicle().getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()]);
            } catch (ClassCastException e) {
                throw getClassCastException(e, stateId, type.toString(), vehicleDependentRouteStatesArr[route.getVehicle().getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()].getClass().toString());
            }
        }
        else {
            try {
                if (vehicleDependentRouteStateMap.containsKey(route)) {
                    state = type.cast(vehicleDependentRouteStateMap.get(route)[vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()]);
                }
            } catch (ClassCastException e) {
                throw getClassCastException(e, stateId, type.toString(), vehicleDependentRouteStateMap.get(route)[vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()].getClass().toString());
            }
        }
        return state;
    }

    /**
     * Associates the specified activity and stateId to the state value. If a state value is already associated to the
     * specified activity and stateId, it is replaced by the new state value.
     *
     * @param act     the activity for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param state   the state that is associated to the activity and stateId
     * @param <T>     the type of the state
     * @throws java.lang.IllegalStateException if <code>act.getIndex() == 0</code>
     *                                         || stateId.getIndex < noInternalStates
     */
    public <T> void putActivityState(TourActivity act, StateId stateId, T state) {
        if (act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
        if (stateId.getIndex() < initialNoStates)
            throw new IllegalStateException("either you use a reserved stateId that is applied\n" +
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
     * @param act     the activity for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param state   the state that is associated to the activity and stateId
     * @param <T>     the type of the state
     * @throws java.lang.IllegalStateException if <code>act.getIndex() == 0</code>
     *                                         || stateId.getIndex < noInternalStates
     */
    public <T> void putActivityState(TourActivity act, Vehicle vehicle, StateId stateId, T state) {
        if (act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
        if (stateId.getIndex() < initialNoStates)
            throw new IllegalStateException("either you use a reserved stateId that is applied\n" +
                "internally or your stateId has been created without index, e.g. StateFactory.createId(stateName)\n" +
                " does not assign indeces thus do not use it anymore, but use\n " +
                "stateManager.createStateId(name)\n" +
                " instead.\n");
        putInternalTypedActivityState(act, vehicle, stateId, state);
    }

    <T> void putInternalTypedActivityState(TourActivity act, StateId stateId, T state) {
        activityStates[act.getIndex()][stateId.getIndex()] = state;
    }

    <T> void putInternalTypedActivityState(TourActivity act, Vehicle vehicle, StateId stateId, T state) {
        vehicleDependentActivityStates[act.getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()] = state;
    }

    /**
     * Associates the specified route, vehicle and stateId to the state value. If a state value is already associated to the
     * specified activity and stateId, it is replaced by the new state value.
     *
     * @param route   the route for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param state   the state that is associated to the activity and stateId
     * @param <T>     the type of the state
     * @throws java.lang.IllegalStateException if stateId is equal to a stateId that is already used internally.
     */
    public <T> void putRouteState(VehicleRoute route, StateId stateId, T state) {
        if (stateId.getIndex() < initialNoStates) StateFactory.throwReservedIdException(stateId.toString());
        putTypedInternalRouteState(route, stateId, state);
    }

    /**
     * Associates the specified route, vehicle and stateId to the state value. If a state value is already associated to the
     * specified activity and stateId, it is replaced by the new state value.
     *
     * @param route   the route for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param state   the state that is associated to the activity and stateId
     * @param <T>     the type of the state
     * @throws java.lang.IllegalStateException if <code>vehicle.getIndex() == 0</code> || <code>stateId.getIndex() < noInternalStates</code>
     */
    public <T> void putRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, T state) {
        if (vehicle.getIndex() == 0) throw new IllegalStateException("vehicle index is 0. this should not be.");
        if (stateId.getIndex() < initialNoStates) StateFactory.throwReservedIdException(stateId.toString());
        putTypedInternalRouteState(route, vehicle, stateId, state);
    }

    <T> void putTypedInternalRouteState(VehicleRoute route, StateId stateId, T state) {
        if (route.isEmpty()) return;
        if(isIndexedBased){
            routeStatesArr[route.getVehicle().getIndex()][stateId.getIndex()] = state;
        }
        else {
            if (!routeStateMap.containsKey(route)) {
                routeStateMap.put(route, new Object[stateIndexCounter]);
            }
            routeStateMap.get(route)[stateId.getIndex()] = state;
        }
    }

    <T> void putTypedInternalRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, T state) {
        if (route.isEmpty()) return;
        if(isIndexedBased){
            vehicleDependentRouteStatesArr[route.getVehicle().getIndex()][vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()] = state;
        }
        else {
            if (!vehicleDependentRouteStateMap.containsKey(route)) {
                vehicleDependentRouteStateMap.put(route, new Object[nuVehicleTypeKeys][stateIndexCounter]);
            }
            vehicleDependentRouteStateMap.get(route)[vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()] = state;
        }

    }

    /**
     * Adds state updater.
     * <p>
     * <p>Note that a state update occurs if route and/or activity states have changed, i.e. if jobs are removed
     * or inserted into a route. Thus here, it is assumed that a state updater is either of type InsertionListener,
     * RuinListener, ActivityVisitor, ReverseActivityVisitor, RouteVisitor, ReverseRouteVisitor.
     * <p>
     * <p>The following rule pertain for activity/route visitors:These visitors visits all activities/route in a route subsequently in two cases. First, if insertionStart (after ruinStrategies have removed activities from routes)
     * and, second, if a job has been inserted and thus if a route has changed.
     *
     * @param updater the update to be added
     */
    public void addStateUpdater(StateUpdater updater) {
        if (updater instanceof ActivityVisitor) addActivityVisitor((ActivityVisitor) updater);
        if (updater instanceof ReverseActivityVisitor) addActivityVisitor((ReverseActivityVisitor) updater);
        if (updater instanceof RouteVisitor) addRouteVisitor((RouteVisitor) updater);
        if (updater instanceof InsertionListener) addListener((InsertionListener) updater);
        if (updater instanceof RuinListener) addListener((RuinListener) updater);
        updaters.add(updater);
    }

    public void addAllStateUpdater(Collection<StateUpdater> updaters) {
        for (StateUpdater u : updaters) addStateUpdater(u);
    }

    /**
     * Returns an unmodifiable collections of stateUpdaters that have been added to this stateManager.
     *
     * @return an unmodifiable collections of stateUpdaters that have been added to this stateManager.
     */
    @SuppressWarnings("UnusedDeclaration")
    Collection<StateUpdater> getStateUpdaters() {
        return Collections.unmodifiableCollection(updaters);
    }

    /**
     * Adds an activityVisitor.
     * <p>This visitor visits all activities in a route subsequently in two cases. First, if insertionStart (after ruinStrategies have removed activities from routes)
     * and, second, if a job has been inserted and thus if a route has changed.
     *
     * @param activityVistor the activity-visitor to be added
     */
    void addActivityVisitor(ActivityVisitor activityVistor) {
        routeActivityVisitor.addActivityVisitor(activityVistor);
    }

    /**
     * Adds an reverseActivityVisitor.
     * <p>This reverseVisitor visits all activities in a route subsequently (starting from the end of the route) in two cases. First, if insertionStart (after ruinStrategies have removed activities from routes)
     * and, second, if a job has been inserted and thus if a route has changed.
     *
     * @param activityVistor activityVisitor to add
     */
    void addActivityVisitor(ReverseActivityVisitor activityVistor) {
        revRouteActivityVisitor.addActivityVisitor(activityVistor);
    }

    void addRouteVisitor(RouteVisitor routeVisitor) {
        routeVisitors.add(routeVisitor);
    }

    void addListener(RuinListener ruinListener) {
        ruinListeners.addListener(ruinListener);
    }

    void addListener(InsertionListener insertionListener) {
        insertionListeners.addListener(insertionListener);
    }

    @Override
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
//		log.debug("insert " + job2insert + " in " + inRoute);
        insertionListeners.informJobInserted(job2insert, inRoute, additionalCosts, additionalTime);
        for (RouteVisitor v : routeVisitors) {
            v.visit(inRoute);
        }
        routeActivityVisitor.visit(inRoute);
        revRouteActivityVisitor.visit(inRoute);
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        insertionListeners.informInsertionStarts(vehicleRoutes, unassignedJobs);
        for (VehicleRoute route : vehicleRoutes) {
            for (RouteVisitor v : routeVisitors) {
                v.visit(route);
            }
            routeActivityVisitor.visit(route);
            revRouteActivityVisitor.visit(route);
        }
    }

    public void reCalculateStates(VehicleRoute route){
        informInsertionStarts(Arrays.asList(route),Collections.<Job>emptyList());
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
        if (!updateLoad) {
            updateLoad = true;
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
        updateTWs = true;
    }

    public boolean timeWindowUpdateIsActivated() {
        return updateTWs;
    }

    /**
     * Updates skill states.
     */
    public void updateSkillStates() {
        addActivityVisitor(new UpdateSkills(this));
    }

}
