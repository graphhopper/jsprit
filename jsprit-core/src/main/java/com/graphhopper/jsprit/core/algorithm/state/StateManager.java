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
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
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
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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

    // Growth factor for array resizing
    private static final double GROWTH_FACTOR = 1.5;
    // Initial capacity - should be tuned based on typical usage
    private static final int INITIAL_CAPACITY = 32;

    private final RouteActivityVisitor routeActivityVisitor = new RouteActivityVisitor();

    private final ReverseRouteActivityVisitor revRouteActivityVisitor = new ReverseRouteActivityVisitor();

    private final Collection<RouteVisitor> routeVisitors = new ArrayList<>();

    private final RuinListeners ruinListeners = new RuinListeners();

    private final InsertionListeners insertionListeners = new InsertionListeners();

    private final Collection<StateUpdater> updaters = new ArrayList<>();

    private boolean updateLoad = false;

    private boolean updateTWs = false;

    private final int initialNoStates = 21;

    private int stateIndexCounter;

    private final Map<String, StateId> createdStateIds = new HashMap<>();

    private final int nuActivities;

    private final int nuVehicleTypeKeys;

    private Object[] problemStates;

    private Object[][] activityStates;

    private Object[][][] vehicleDependentActivityStates;

    private Object[] nullArray;

//    private final Map<VehicleRoute, Object[]> routeStateMap;
//
//    private final Map<VehicleRoute, Object[][]> vehicleDependentRouteStateMap;

    private final TIntObjectMap<Object[]> routeStateMap;
    private final TIntObjectMap<Object[][]> vehicleDependentRouteStateMap;

    private int nextRouteIndex = 0;
    private final Set<Integer> availableIndices;


    int getMaxIndexOfVehicleTypeIdentifiers() {
        return nuVehicleTypeKeys;
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
        nuActivities = Math.max(10, vehicleRoutingProblem.getNuActivities() + 1);
        nuVehicleTypeKeys = Math.max(3, getNuVehicleTypes(vehicleRoutingProblem) + 2);
        routeStateMap = new TIntObjectHashMap<>(51, 0.5f, -1);
        vehicleDependentRouteStateMap = new TIntObjectHashMap<>(51, 0.5f, -1);
        availableIndices = new HashSet<>();
        initArrays();
    }

    private void initArrays() {
        activityStates = new Object[nuActivities][StateManager.INITIAL_CAPACITY];
        vehicleDependentActivityStates = new Object[nuActivities][nuVehicleTypeKeys][StateManager.INITIAL_CAPACITY];
        problemStates = new Object[StateManager.INITIAL_CAPACITY];
        nullArray = new Object[StateManager.INITIAL_CAPACITY];
    }

    private int getOrCreateRouteIndex(VehicleRoute route) {
        int index = route.getIndex();
        if (index == -1) {
            // This should now happen less frequently since routes in solutions
            // are pre-indexed at iteration start
            if (!availableIndices.isEmpty()) {
                index = availableIndices.iterator().next();
                availableIndices.remove(index);
            } else {
                index = nextRouteIndex++;
            }
            route.setIndex(index);
            initializeRouteStates(index);
        }
        return index;
    }

    public void releaseRouteIndex(VehicleRoute route) {
        int index = route.getIndex();
        if (index != -1) {
            routeStateMap.remove(index);
            vehicleDependentRouteStateMap.remove(index);
            availableIndices.add(index);
            route.setIndex(-1);
        }
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
        // Check existing state
        if (createdStateIds.containsKey(name)) {
            return createdStateIds.get(name);
        }

        // Check if we need to grow arrays
        if (stateIndexCounter >= activityStates[0].length) {
            growArrays();
        }

        // Create new state ID
        StateId id = StateFactory.createId(name, stateIndexCounter);
        incStateIndexCounter();
        createdStateIds.put(name, id);
        return id;
    }

    private void growArrays() {
        // Calculate new capacity
        int oldCapacity = activityStates[0].length;
        int newCapacity = Math.max((int) (oldCapacity * GROWTH_FACTOR), oldCapacity + 1);

        // Create new arrays
        Object[][] newActivityStates = new Object[nuActivities][newCapacity];
        Object[][][] newVehicleDependentActivityStates = new Object[nuActivities][nuVehicleTypeKeys][newCapacity];
        Object[] newProblemStates = new Object[newCapacity];

        // Copy existing data using System.arraycopy
        copyStates(activityStates, newActivityStates);
        copyStates(vehicleDependentActivityStates, newVehicleDependentActivityStates);
        System.arraycopy(problemStates, 0, newProblemStates, 0, problemStates.length);

        // Assign new arrays
        activityStates = newActivityStates;
        vehicleDependentActivityStates = newVehicleDependentActivityStates;
        problemStates = newProblemStates;
        nullArray = new Object[newCapacity];
    }

    private void copyStates(Object[][] source, Object[][] target) {
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, target[i], 0, source[i].length);
        }
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
     * Clears all states by setting all values to null.
     * Uses optimized sequential clearing with System.arraycopy.
     */
    public void clear() {
        // Clear activity states
        clearActivityStates();

        // Clear vehicle dependent activity states
        clearVehicleDependentStates();

        // Clear maps
        routeStateMap.clear();
        vehicleDependentRouteStateMap.clear();

        // Clear problem states
        System.arraycopy(nullArray, 0, problemStates, 0, problemStates.length);
    }

    private void clearActivityStates() {
        final int rowLength = activityStates[0].length;
        for (int i = 0; i < activityStates.length; i++) {
            System.arraycopy(nullArray, 0, activityStates[i], 0, rowLength);
        }
    }

    private void clearVehicleDependentStates() {
        final int innerLength = vehicleDependentActivityStates[0][0].length;
        for (int i = 0; i < vehicleDependentActivityStates.length; i++) {
            Object[][] middleArray = vehicleDependentActivityStates[i];
            for (int j = 0; j < middleArray.length; j++) {
                System.arraycopy(nullArray, 0, middleArray[j], 0, innerLength);
            }
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
        final int actIndex = act.getIndex();
        if (actIndex <= 0) {
            if (actIndex == 0) throw new IllegalStateException("activity index is 0. this should not be.");
            return null;
        }
        final int stateIndex = stateId.getIndex();
        final Object state = activityStates[actIndex][stateIndex];
        try {
            return type.cast(state);
        } catch (ClassCastException e) {
            throw getClassCastException(e, stateId, type.toString(), state.getClass().toString());
        }
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
        if (route == null) return null;
        int routeIndex = getOrCreateRouteIndex(route);
        Object[] states = routeStateMap.get(routeIndex);
        if (states == null) return null;

        try {
            return type.cast(states[stateId.getIndex()]);
        } catch (ClassCastException e) {
            throw getClassCastException(e, stateId, type.toString(),
                states[stateId.getIndex()].getClass().toString());
        }
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
        int routeIndex = getOrCreateRouteIndex(route);
        Object[][] states = vehicleDependentRouteStateMap.get(routeIndex);
        if (states == null) {
            return false;
        }
        return states[vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()] != null;
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
        int routeIndex = getOrCreateRouteIndex(route);
        Object[][] states = vehicleDependentRouteStateMap.get(routeIndex);
        if (states == null) {
            return null;
        }

        int vehicleTypeIndex = vehicle.getVehicleTypeIdentifier().getIndex();
        int stateIndex = stateId.getIndex();

        Object state = states[vehicleTypeIndex][stateIndex];
        if (state == null) {
            return null;
        }

        try {
            return type.cast(state);
        } catch (ClassCastException e) {
            throw getClassCastException(e, stateId, type.toString(), state.getClass().toString());
        }
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
        int routeIndex = getOrCreateRouteIndex(route);
        Object[] states = routeStateMap.get(routeIndex);
        if (states == null) {
            states = new Object[stateIndexCounter];
            routeStateMap.put(routeIndex, states);
        }
        states[stateId.getIndex()] = state;
    }

    <T> void putTypedInternalRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, T state) {
        int routeIndex = getOrCreateRouteIndex(route);
        Object[][] states = vehicleDependentRouteStateMap.get(routeIndex);

        if (states == null) {
            states = new Object[nuVehicleTypeKeys][stateIndexCounter];
            vehicleDependentRouteStateMap.put(routeIndex, states);
        }

        states[vehicle.getVehicleTypeIdentifier().getIndex()][stateId.getIndex()] = state;
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
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, InsertionData insertionData) {
        insertionListeners.informJobInserted(job2insert, inRoute, insertionData);
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
        informInsertionStarts(Collections.singletonList(route), Collections.emptyList());
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        nextRouteIndex = 0;
        availableIndices.clear();
        clear();

        // Assign new indices to all routes in all solutions
        for (VehicleRoutingProblemSolution solution : solutions) {
            Collection<VehicleRoute> routes = solution.getRoutes();
            assignNewIndicesToRoutes(routes);
        }
    }

    private void assignNewIndicesToRoutes(Collection<VehicleRoute> routes) {
        for (VehicleRoute route : routes) {
            // Reset any existing index
            route.setIndex(-1);

            // Assign new sequential index
            int newIndex = nextRouteIndex++;
            route.setIndex(newIndex);

            // Pre-allocate state arrays for this route if needed
            initializeRouteStates(newIndex);
        }
    }

    private void initializeRouteStates(int routeIndex) {
        // Initialize route states if they will be needed
        if (!routeStateMap.containsKey(routeIndex)) {
            routeStateMap.put(routeIndex, new Object[stateIndexCounter]);
        }

        if (!vehicleDependentRouteStateMap.containsKey(routeIndex)) {
            vehicleDependentRouteStateMap.put(routeIndex,
                new Object[nuVehicleTypeKeys][stateIndexCounter]);
        }
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
    public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes, Collection<Job> badJobs) {
        insertionListeners.informInsertionEndsListeners(vehicleRoutes, badJobs);
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
