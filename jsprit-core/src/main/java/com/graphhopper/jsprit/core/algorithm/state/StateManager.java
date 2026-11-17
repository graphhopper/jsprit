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
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleClasses;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleDependent;

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

    private Collection<RouteVisitor> routeVisitors = new ArrayList<>();

    private RuinListeners ruinListeners = new RuinListeners();

    private InsertionListeners insertionListeners = new InsertionListeners();

    private Collection<StateUpdater> updaters = new ArrayList<>();

    private boolean updateLoad = false;

    private boolean updateTWs = false;

    private static final int RESERVED_STATE_IDS = InternalStates.RESERVED_STATE_IDS;

    private int stateIndexCounter;

    private int doubleStateIndexCounter;

    private Map<String, StateId> createdStateIds = new HashMap<>();

    private int nuActivities;

    /** number of vehicle classes plus margin; 0 until the classes are known */
    private int nuVehicleTypeKeys;

    /** null until the declared classes are fixed, see {@link #withDeclaredVehicleClasses(VehicleRoutingProblem)} */
    private VehicleClasses vehicleClasses;

    /** what the registered updaters read from a vehicle; only collected for declared classes */
    private final List<VehicleDependent> vehicleKeyParts = new ArrayList<>();

    private final boolean declaredVehicleClasses;

    private int vehicleClassCount() {
        if (vehicleClasses == null) fixVehicleClasses();
        return nuVehicleTypeKeys;
    }

    private VehicleClasses fixVehicleClasses() {
        final List<VehicleDependent> parts = new ArrayList<>(vehicleKeyParts);
        vehicleClasses = VehicleClasses.by(vrp.getVehicles(), vehicle -> {
            List<Object> key = new ArrayList<>(parts.size());
            for (VehicleDependent part : parts) key.add(part.vehicleKey(vehicle));
            return key;
        });
        nuVehicleTypeKeys = Math.max(3, vehicleClasses.size() + 1);
        return vehicleClasses;
    }

    /*
     * Storage. Every state id has one index; object states and double states share that index
     * space but live in different arrays: Object[] for the former, double[] plus a "set" mask for the
     * latter (a double state has no null, the mask carries the "not set" that constraints rely on
     * to fall back). Activity states are indexed by the activity index the problem assigned, route
     * states hang off the route object (a route can change its vehicle, so the vehicle index is no key).
     */
    private Object[] problemStates;

    private Object[][] activityStates;

    private double[][] activityDoubles;

    private boolean[][] activityDoublesSet;

    /*
     * Vehicle-dependent activity states: one flat array per state id, indexed activity * classes + vehicle
     * class, allocated when the id is first written with a vehicle. Only the few ids that are
     * vehicle-dependent pay for the vehicle dimension, and clearing is one fill per such id.
     */
    private Object[][] vehicleDependentActivityStates;

    private double[][] vehicleDependentActivityDoubles;

    private boolean[][] vehicleDependentActivityDoublesSet;

    private int vehicleSlot(int actIndex, int vehicleClass) {
        // a flat index would silently land in the next activity's slots
        if (vehicleClass < 0 || vehicleClass >= vehicleClassCount())
            throw new IndexOutOfBoundsException("vehicle class " + vehicleClass + " is outside [0, " + vehicleClassCount() + ")");
        return actIndex * vehicleClassCount() + vehicleClass;
    }

    private boolean isVehicleDoubleSet(int actIndex, int vehicleClass, int doubleIndex) {
        boolean[] set = vehicleDependentActivityDoublesSet[doubleIndex];
        return set != null && set[vehicleSlot(actIndex, vehicleClass)];
    }

    /** only valid if {@link #isVehicleDoubleSet(int, int, int)} */
    private double vehicleDouble(int actIndex, int vehicleClass, int doubleIndex) {
        return vehicleDependentActivityDoubles[doubleIndex][vehicleSlot(actIndex, vehicleClass)];
    }

    private void putVehicleDouble(int actIndex, int vehicleClass, int doubleIndex, double value) {
        double[] values = vehicleDependentActivityDoubles[doubleIndex];
        if (values == null) {
            values = vehicleDependentActivityDoubles[doubleIndex] = new double[nuActivities * vehicleClassCount()];
            vehicleDependentActivityDoublesSet[doubleIndex] = new boolean[nuActivities * vehicleClassCount()];
        }
        int slot = vehicleSlot(actIndex, vehicleClass);
        values[slot] = value;
        vehicleDependentActivityDoublesSet[doubleIndex][slot] = true;
    }

    private void unsetVehicleDouble(int actIndex, int vehicleClass, int doubleIndex) {
        boolean[] set = vehicleDependentActivityDoublesSet[doubleIndex];
        if (set != null) set[vehicleSlot(actIndex, vehicleClass)] = false;
    }

    private Object vehicleObject(int actIndex, int vehicleClass, int stateIndex) {
        Object[] values = vehicleDependentActivityStates[stateIndex];
        return values == null ? null : values[vehicleSlot(actIndex, vehicleClass)];
    }

    private void putVehicleObject(int actIndex, int vehicleClass, int stateIndex, Object value) {
        Object[] values = vehicleDependentActivityStates[stateIndex];
        if (values == null) {
            if (value == null) return;
            values = vehicleDependentActivityStates[stateIndex] = new Object[nuActivities * vehicleClassCount()];
        }
        values[vehicleSlot(actIndex, vehicleClass)] = value;
    }

    private static final class RouteStates {
        final Object[] objects;
        final double[] doubles;
        final boolean[] set;

        RouteStates(int length, int doubleLength) {
            objects = new Object[length];
            doubles = new double[doubleLength];
            set = new boolean[doubleLength];
        }
    }

    private static final class VehicleDependentRouteStates {
        final Object[][] objects;
        final double[][] doubles;
        final boolean[][] set;

        VehicleDependentRouteStates(int typeKeys, int length, int doubleLength) {
            objects = new Object[typeKeys][length];
            doubles = new double[typeKeys][doubleLength];
            set = new boolean[typeKeys][doubleLength];
        }
    }

    private final Map<VehicleRoute, RouteStates> routeStateMap;

    private final Map<VehicleRoute, VehicleDependentRouteStates> vehicleDependentRouteStateMap;

    private final VehicleRoutingProblem vrp;

    /** system property: reading a vehicle-dependent state that is missing for the asked vehicle only throws, see {@link #setFailOnMissingVehicleState(boolean)} */
    public static final String FAIL_ON_MISSING_VEHICLE_STATE = "jsprit.states.failOnMissing";

    /** system property, "count" or "compare": every state manager reports to {@link StateUpdateStats#GLOBAL}, see {@link #trackRouteChanges(boolean)} */
    public static final String TRACK_ROUTE_CHANGES = "jsprit.states.trackRouteChanges";

    private boolean failOnMissingVehicleState = Boolean.getBoolean(FAIL_ON_MISSING_VEHICLE_STATE);

    private RouteChangeTracker routeChangeTracker;

    private final BitSet sparseStates = new BitSet();

    /**
     * Verification mode, off by default. A vehicle-dependent state is written per vehicle type; when it is
     * read for a vehicle type it was not written for, although the same state of the same activity or route
     * exists for another vehicle type, the read throws instead of answering "no state". Constraints treat
     * "no state" as "no limit", so an updater that covers too few vehicles otherwise goes unnoticed. A state
     * that exists for no vehicle type at all (empty route, updater not registered) still reads as "no state".
     * States whose absence is the answer are exempt, see {@link #declareSparse(StateId)}.
     */
    public void setFailOnMissingVehicleState(boolean failOnMissingVehicleState) {
        this.failOnMissingVehicleState = failOnMissingVehicleState;
    }

    /**
     * Diagnostics, off by default: counts at every insertion start how many routes are unchanged since their
     * states were last computed and, with compareStates, whether computing the states of an unchanged route
     * again yields the stored values. The search is not affected.
     */
    public StateUpdateStats trackRouteChanges(boolean compareStates) {
        routeChangeTracker = new RouteChangeTracker(this, new StateUpdateStats(), compareStates);
        return routeChangeTracker.getStats();
    }

    /**
     * Declares a vehicle-dependent state as sparse: it is written only for the vehicle types it applies to and
     * its absence for the others is meaningful (a flag such as "switch not feasible", a state kept for the
     * route's own vehicle only). The verification mode does not complain about such a state.
     */
    public void declareSparse(StateId stateId) {
        sparseStates.set(stateId.getIndex());
    }

    private String stateName(StateId stateId) {
        return stateId.toString();
    }

    private IllegalStateException missingVehicleState(String where, Vehicle vehicle, StateId stateId, int others) {
        return new IllegalStateException("state " + stateName(stateId) + " of " + where + " is not set for vehicle " + vehicle.getId()
            + " (vehicle class " + getTypeKeyIndex(vehicle) + ") but for " + others + " other vehicle type(s): its updater does not cover every vehicle the state is read for");
    }

    private void checkMissing(TourActivity act, Vehicle vehicle, StateId stateId) {
        if (sparseStates.get(stateId.getIndex())) return;
        int others = 0;
        int actIndex = act.getIndex();
        if (stateId.isDouble()) {
            for (int k = 0; k < vehicleClassCount(); k++) if (isVehicleDoubleSet(actIndex, k, stateId.getDoubleIndex())) others++;
        } else {
            for (int k = 0; k < vehicleClassCount(); k++) if (vehicleObject(actIndex, k, stateId.getIndex()) != null) others++;
        }
        if (others > 0) throw missingVehicleState("activity " + actIndex + " (" + act + ")", vehicle, stateId, others);
    }

    private void checkMissing(VehicleRoute route, VehicleDependentRouteStates states, Vehicle vehicle, StateId stateId) {
        if (sparseStates.get(stateId.getIndex())) return;
        int others = 0;
        if (stateId.isDouble()) {
            for (boolean[] set : states.set) if (set[stateId.getDoubleIndex()]) others++;
        } else {
            for (Object[] objects : states.objects) if (objects[stateId.getIndex()] != null) others++;
        }
        if (others > 0) throw missingVehicleState("the route of vehicle " + route.getVehicle().getId(), vehicle, stateId, others);
    }

    /** name of the state with this index, for diagnostics */
    String stateName(int index) {
        return stateNames(false).getOrDefault(index, "state#" + index);
    }

    /** name of the double state with this double index, for diagnostics */
    String doubleStateName(int doubleIndex) {
        return stateNames(true).getOrDefault(doubleIndex, "double#" + doubleIndex);
    }

    private Map<Integer, String> stateNames(boolean byDoubleIndex) {
        Map<Integer, String> names = new HashMap<>();
        List<StateId> ids = new ArrayList<>(createdStateIds.values());
        ids.addAll(Arrays.asList(InternalStates.MAXLOAD, InternalStates.LOAD, InternalStates.COSTS, InternalStates.LOAD_AT_BEGINNING,
            InternalStates.LOAD_AT_END, InternalStates.DURATION, InternalStates.LATEST_OPERATION_START_TIME,
            InternalStates.EARLIEST_OPERATION_START_TIME, InternalStates.FUTURE_MAXLOAD, InternalStates.PAST_MAXLOAD, InternalStates.SKILLS,
            InternalStates.WAITING, InternalStates.TIME_SLACK, InternalStates.FUTURE_WAITING, InternalStates.EARLIEST_WITHOUT_WAITING,
            InternalStates.SWITCH_NOT_FEASIBLE));
        for (StateId id : ids) {
            if (!byDoubleIndex) names.put(id.getIndex(), id.toString());
            else if (id.isDouble()) names.put(id.getDoubleIndex(), id.toString());
        }
        return names;
    }

    RouteChangeTracker.Slots copyActivitySlots(int actIndex) {
        RouteChangeTracker.Slots slots = new RouteChangeTracker.Slots();
        slots.objects = activityStates[actIndex].clone();
        slots.doubles = activityDoubles[actIndex].clone();
        slots.set = activityDoublesSet[actIndex].clone();
        slots.vObjects = new Object[vehicleClassCount()][vehicleDependentActivityStates.length];
        slots.vDoubles = new double[vehicleClassCount()][vehicleDependentActivityDoubles.length];
        slots.vSet = new boolean[vehicleClassCount()][vehicleDependentActivityDoubles.length];
        for (int k = 0; k < vehicleClassCount(); k++) {
            for (int i = 0; i < vehicleDependentActivityStates.length; i++) slots.vObjects[k][i] = vehicleObject(actIndex, k, i);
            for (int i = 0; i < vehicleDependentActivityDoubles.length; i++) {
                if (isVehicleDoubleSet(actIndex, k, i)) {
                    slots.vSet[k][i] = true;
                    slots.vDoubles[k][i] = vehicleDouble(actIndex, k, i);
                }
            }
        }
        return slots;
    }

    RouteChangeTracker.Slots copyRouteSlots(VehicleRoute route) {
        RouteChangeTracker.Slots slots = new RouteChangeTracker.Slots();
        RouteStates states = routeStateMap.get(route);
        if (states != null) {
            slots.objects = states.objects.clone();
            slots.doubles = states.doubles.clone();
            slots.set = states.set.clone();
        }
        VehicleDependentRouteStates vStates = vehicleDependentRouteStateMap.get(route);
        if (vStates != null) {
            slots.vObjects = deepCopy(vStates.objects);
            slots.vDoubles = deepCopy(vStates.doubles);
            slots.vSet = deepCopy(vStates.set);
        }
        return slots;
    }

    private static Object[][] deepCopy(Object[][] rows) {
        Object[][] copy = new Object[rows.length][];
        for (int i = 0; i < rows.length; i++) copy[i] = rows[i].clone();
        return copy;
    }

    private static double[][] deepCopy(double[][] rows) {
        double[][] copy = new double[rows.length][];
        for (int i = 0; i < rows.length; i++) copy[i] = rows[i].clone();
        return copy;
    }

    private static boolean[][] deepCopy(boolean[][] rows) {
        boolean[][] copy = new boolean[rows.length][];
        for (int i = 0; i < rows.length; i++) copy[i] = rows[i].clone();
        return copy;
    }

    int getMaxIndexOfVehicleTypeIdentifiers() {
        return vehicleClassCount();
    }

    public VehicleRoutingProblem getVrp() {
        return vrp;
    }

    @SuppressWarnings("deprecation")
    private int getVehicleIndex(Vehicle vehicle) {
        int index = vrp.getVehicleIndex(vehicle);
        if (index <= 0) {
            // Fallback to deprecated method for backwards compatibility
            // (e.g., when vehicle is not in VRP but was indexed elsewhere)
            index = vehicle.getIndex();
        }
        return index;
    }

    /** the vehicle dimension of the vehicle-dependent states: the vehicle's class */
    private int getTypeKeyIndex(Vehicle vehicle) {
        VehicleClasses classes = vehicleClasses;
        if (classes == null) classes = fixVehicleClasses();
        return classes.classOf(vehicle);
    }

    /**
     * The partition of the fleet that decides which vehicles share their vehicle-dependent states. State
     * updaters compute vehicle-dependent states for its {@link VehicleClasses#representatives()}.
     */
    public VehicleClasses getVehicleClasses() {
        return vehicleClasses != null ? vehicleClasses : fixVehicleClasses();
    }

    /**
     * A state manager whose vehicle classes follow from what its updaters declare: every registered state updater
     * that implements {@link VehicleDependent} names what it reads from a vehicle, and two vehicles share their
     * vehicle-dependent states if they are equal in all of that. An updater that reads a new vehicle attribute
     * brings its own key; there is no central list to keep in step.
     * <p>
     * The classes are fixed when they are first needed (the first vehicle-dependent state is written or read,
     * or {@link #getVehicleClasses()} is called); registering a {@link VehicleDependent} updater after that throws.
     * An updater that writes vehicle-dependent states and does not implement {@link VehicleDependent} is not seen
     * here: its states are shared by vehicles it may tell apart. Use this only when every such updater declares.
     */
    public static StateManager withDeclaredVehicleClasses(VehicleRoutingProblem vehicleRoutingProblem) {
        return new StateManager(vehicleRoutingProblem, null, true);
    }

    /**
     * Create and returns a stateId with the specified name for an Object-valued state.
     * <p>
     * <p>If a stateId with the specified name has already been created, it returns the created stateId.</p>
     * <p>If the specified is equal to a name that is already used internally, it throws an IllegalStateException</p>
     *
     * @param name the specified name of the state
     * @return the stateId with which a state can be identified, no matter if it is a problem, route or activity state.
     * @throws java.lang.IllegalStateException if name of state is already used internally
     */
    public StateId createStateId(String name) {
        return createStateId(name, false);
    }

    /**
     * Like {@link #createStateId(String)} for a double-valued state, stored in primitive arrays:
     * put and get do not box. Read it with {@link #getActivityDouble(TourActivity, StateId)} and the
     * route counterparts; the generic getters still work and box on the way out.
     */
    public StateId createDoubleStateId(String name) {
        return createStateId(name, true);
    }

    private StateId createStateId(String name, boolean isDouble) {
        StateId existing = createdStateIds.get(name);
        if (existing != null) {
            if (existing.isDouble() != isDouble)
                throw new IllegalStateException("state-id '" + name + "' already exists with a different type (double vs. object)");
            return existing;
        }
        if (stateIndexCounter >= activityStates[0].length) {
            growStateArrays(Math.max(stateIndexCounter + 1, 2 * activityStates[0].length));
        }
        int doubleIndex = -1;
        if (isDouble) {
            if (doubleStateIndexCounter >= activityDoubles[0].length) {
                growDoubleStateArrays(Math.max(doubleStateIndexCounter + 1, 2 * activityDoubles[0].length));
            }
            doubleIndex = doubleStateIndexCounter++;
        }
        StateId id = StateFactory.createId(name, stateIndexCounter, doubleIndex);
        incStateIndexCounter();
        createdStateIds.put(name, id);
        return id;
    }

    private void incStateIndexCounter() {
        stateIndexCounter++;
    }

    /**
     * Widens the state dimension of all arrays, keeping the values already stored. The old code
     * replaced the arrays, which silently dropped every state written before the id was created,
     * and grew by one slot per id, i.e. reallocated on every createStateId beyond the initial length.
     */
    private void growStateArrays(int newLength) {
        for (int i = 0; i < activityStates.length; i++) {
            activityStates[i] = Arrays.copyOf(activityStates[i], newLength);
        }
        vehicleDependentActivityStates = Arrays.copyOf(vehicleDependentActivityStates, newLength);
        problemStates = Arrays.copyOf(problemStates, newLength);
    }

    private void growDoubleStateArrays(int newLength) {
        for (int i = 0; i < activityDoubles.length; i++) {
            activityDoubles[i] = Arrays.copyOf(activityDoubles[i], newLength);
            activityDoublesSet[i] = Arrays.copyOf(activityDoublesSet[i], newLength);
        }
        vehicleDependentActivityDoubles = Arrays.copyOf(vehicleDependentActivityDoubles, newLength);
        vehicleDependentActivityDoublesSet = Arrays.copyOf(vehicleDependentActivityDoublesSet, newLength);
    }

    /**
     * Constructs the stateManager with the specified VehicleRoutingProblem.
     *
     * @param vehicleRoutingProblem the corresponding VehicleRoutingProblem
     */
    public StateManager(VehicleRoutingProblem vehicleRoutingProblem) {
        this(vehicleRoutingProblem, VehicleClasses.byTypeKey(vehicleRoutingProblem));
    }

    /**
     * Constructs the stateManager with vehicle classes of the caller's choice: vehicles of one class share their
     * vehicle-dependent states. The classes must tell apart any two vehicles for which a state updater can
     * compute different values; with the default, {@link VehicleClasses#byTypeKey(VehicleRoutingProblem)}, that
     * is type, start and end location, operating times, skills and the return flag.
     */
    public StateManager(VehicleRoutingProblem vehicleRoutingProblem, VehicleClasses vehicleClasses) {
        this(vehicleRoutingProblem, vehicleClasses, false);
        if (vehicleClasses == null) throw new IllegalArgumentException("vehicle classes must not be null");
    }

    private StateManager(VehicleRoutingProblem vehicleRoutingProblem, VehicleClasses vehicleClasses, boolean declaredVehicleClasses) {
        this.vehicleClasses = vehicleClasses;
        this.declaredVehicleClasses = declaredVehicleClasses;
        stateIndexCounter = RESERVED_STATE_IDS;
        doubleStateIndexCounter = InternalStates.RESERVED_DOUBLE_STATE_IDS;
        int initialStateArrayLength = 30;
        int initialDoubleStateArrayLength = 16;
        this.vrp = vehicleRoutingProblem;
        nuActivities = Math.max(10, vrp.getNuActivities() + 1);
        if (vehicleClasses != null) nuVehicleTypeKeys = Math.max(3, vehicleClasses.size() + 1);
        activityStates = new Object[nuActivities][initialStateArrayLength];
        activityDoubles = new double[nuActivities][initialDoubleStateArrayLength];
        activityDoublesSet = new boolean[nuActivities][initialDoubleStateArrayLength];
        vehicleDependentActivityStates = new Object[initialStateArrayLength][];
        vehicleDependentActivityDoubles = new double[initialDoubleStateArrayLength][];
        vehicleDependentActivityDoublesSet = new boolean[initialDoubleStateArrayLength][];
        routeStateMap = new HashMap<>();
        vehicleDependentRouteStateMap = new HashMap<>();
        problemStates = new Object[initialStateArrayLength];
        declareSparse(InternalStates.SWITCH_NOT_FEASIBLE);
        String track = System.getProperty(TRACK_ROUTE_CHANGES);
        if (track != null) routeChangeTracker = new RouteChangeTracker(this, StateUpdateStats.GLOBAL, "compare".equals(track));
    }

    /**
     * Associates the specified state to the stateId. If there already exists a state value for the stateId, this old
     * value is replaced by the new value.
     *
     * @param stateId the stateId which is the associated key to the problem state
     * @param type    the class of the associated state value
     * @param state   the actual state value
     * @param <T>     the type of the state value
     */
    public <T> void putProblemState(StateId stateId, Class<T> type, T state) {
        problemStates[stateId.getIndex()] = state;
    }

    /**
     * Returns mapped state value that is associated to the specified stateId, or null if no value is associated.
     *
     * @param stateId the stateId which is the associated key to the problem state
     * @param type    the class of the associated state value
     * @param <T>     the type of the class
     * @return the state value that is associated to the specified stateId or null if no value is associated
     */
    public <T> T getProblemState(StateId stateId, Class<T> type) {
        return type.cast(problemStates[stateId.getIndex()]);
    }

    /**
     * Clears all states, i.e. set all value to null (object states) or "not set" (double states).
     */
    public void clear() {
        fill_twoDimArr(activityStates, null);
        fill_twoDimArr(activityDoublesSet);
        for (Object[] values : vehicleDependentActivityStates) if (values != null) Arrays.fill(values, null);
        for (boolean[] set : vehicleDependentActivityDoublesSet) if (set != null) Arrays.fill(set, false);
        routeStateMap.clear();
        vehicleDependentRouteStateMap.clear();
        Arrays.fill(problemStates, null);
    }

    private void fill_twoDimArr(Object[][] states, Object o) {
        for (Object[] rows : states) {
            Arrays.fill(rows, o);
        }
    }

    private void fill_twoDimArr(boolean[][] set) {
        for (boolean[] rows : set) {
            Arrays.fill(rows, false);
        }
    }

    private static void checkActivityIndex(TourActivity act) {
        if (act.getIndex() == 0) throw new IllegalStateException("activity index is 0. this should not be.");
    }

    /**
     * Returns associated state for the specified activity and stateId, or it returns null if no value is associated.
     * <p>If type class is not equal to the associated type class of the requested state value, it throws a ClassCastException.</p>
     *
     * @param act     the activity for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param type    the class of the associated state value
     * @param <T>     the type of the class
     * @return the state value that is associated to the specified activity and stateId, or null if no value is associated.
     * @throws java.lang.ClassCastException if type class is not equal to the associated type class of the requested state value
     */
    @Override
    public <T> T getActivityState(TourActivity act, StateId stateId, Class<T> type) {
        final int actIndex = act.getIndex();
        if (actIndex <= 0) {
            if (actIndex == 0) throw new IllegalStateException("activity index is 0. this should not be.");
            return null;
        }
        final int stateIndex = stateId.getIndex();
        if (stateId.isDouble()) {
            if (!activityDoublesSet[actIndex][stateId.getDoubleIndex()]) return null;
            return type.cast(activityDoubles[actIndex][stateId.getDoubleIndex()]);
        }
        final Object state = activityStates[actIndex][stateIndex];
        try {
            return type.cast(state);
        } catch (ClassCastException e) {
            throw getClassCastException(e, stateId, type.toString(), state.getClass().toString());
        }
    }

    @Override
    public double getActivityDouble(TourActivity act, StateId stateId) {
        final int actIndex = act.getIndex();
        if (actIndex <= 0) {
            if (actIndex == 0) throw new IllegalStateException("activity index is 0. this should not be.");
            return Double.NaN;
        }
        final int stateIndex = stateId.getIndex();
        if (stateId.isDouble()) {
            return activityDoublesSet[actIndex][stateId.getDoubleIndex()] ? activityDoubles[actIndex][stateId.getDoubleIndex()] : Double.NaN;
        }
        Object state = activityStates[actIndex][stateIndex];
        return state == null ? Double.NaN : (Double) state;
    }

    /**
     * Returns true if a vehicle-dependent state is set for the activity, the vehicle (type) and the stateId.
     */
    public boolean hasActivityState(TourActivity act, Vehicle vehicle, StateId stateId) {
        checkActivityIndex(act);
        if (act.getIndex() < 0) return false;
        int typeKeyIndex = getTypeKeyIndex(vehicle);
        if (stateId.isDouble()) return isVehicleDoubleSet(act.getIndex(), typeKeyIndex, stateId.getDoubleIndex());
        return vehicleObject(act.getIndex(), typeKeyIndex, stateId.getIndex()) != null;
    }

    /**
     * Returns true if a state is set for the activity and the stateId.
     */
    public boolean hasActivityState(TourActivity act, StateId stateId) {
        checkActivityIndex(act);
        if (act.getIndex() < 0) return false;
        if (stateId.isDouble()) return activityDoublesSet[act.getIndex()][stateId.getDoubleIndex()];
        return activityStates[act.getIndex()][stateId.getIndex()] != null;
    }

    /**
     * Returns associated state for the specified activity, vehicle and stateId, or it returns null if no value is associated.
     * <p>If type class is not equal to the associated type class of the requested state value, it throws a ClassCastException.</p>
     *
     * @param act     the activity for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param type    the class of the associated state value
     * @param <T>     the type of the class
     * @return the state value that is associated to the specified activity and stateId, or null if no value is associated.
     * @throws java.lang.ClassCastException if type class is not equal to the associated type class of the requested state value
     */
    public <T> T getActivityState(TourActivity act, Vehicle vehicle, StateId stateId, Class<T> type) {
        checkActivityIndex(act);
        if (act.getIndex() < 0) return null; //act.getIndex() < 0 indicates that act is either Start (-1) or End (-2)
        int typeKeyIndex = getTypeKeyIndex(vehicle);
        if (stateId.isDouble()) {
            if (!isVehicleDoubleSet(act.getIndex(), typeKeyIndex, stateId.getDoubleIndex())) {
                if (failOnMissingVehicleState) checkMissing(act, vehicle, stateId);
                return null;
            }
            return type.cast(vehicleDouble(act.getIndex(), typeKeyIndex, stateId.getDoubleIndex()));
        }
        T state;
        try {
            state = type.cast(vehicleObject(act.getIndex(), typeKeyIndex, stateId.getIndex()));
        } catch (ClassCastException e) {
            Object state_class = vehicleObject(act.getIndex(), typeKeyIndex, stateId.getIndex());
            throw getClassCastException(e, stateId, type.toString(), state_class.getClass().toString());
        }
        if (state == null && failOnMissingVehicleState) checkMissing(act, vehicle, stateId);
        return state;
    }

    @Override
    public double getActivityDouble(TourActivity act, Vehicle vehicle, StateId stateId) {
        checkActivityIndex(act);
        if (act.getIndex() < 0) return Double.NaN;
        int typeKeyIndex = getTypeKeyIndex(vehicle);
        if (stateId.isDouble()) {
            if (isVehicleDoubleSet(act.getIndex(), typeKeyIndex, stateId.getDoubleIndex()))
                return vehicleDouble(act.getIndex(), typeKeyIndex, stateId.getDoubleIndex());
            if (failOnMissingVehicleState) checkMissing(act, vehicle, stateId);
            return Double.NaN;
        }
        Object state = vehicleObject(act.getIndex(), typeKeyIndex, stateId.getIndex());
        if (state == null && failOnMissingVehicleState) checkMissing(act, vehicle, stateId);
        return state == null ? Double.NaN : (Double) state;
    }

    private ClassCastException getClassCastException(ClassCastException e, StateId stateId, String requestedTypeClass, String memorizedTypeClass) {
        return new ClassCastException(e + "\n" + "state with stateId '" + stateId.toString() + "' is of " + memorizedTypeClass + ". cannot cast it to " + requestedTypeClass + ".");
    }

    /**
     * Returns associated state for the specified route and stateId, or it returns null if no value is associated.
     * <p>If type class is not equal to the associated type class of the requested state value, it throws a ClassCastException.</p>
     *
     * @param route   the route for which a state value is associated to
     * @param stateId the stateId which is the associated key to the route state
     * @param type    the class of the associated state value
     * @param <T>     the type of the class
     * @return the state value that is associated to the specified route and stateId, or null if no value is associated.
     * @throws java.lang.ClassCastException if type class is not equal to the associated type class of the requested state value
     */
    @Override
    public <T> T getRouteState(VehicleRoute route, StateId stateId, Class<T> type) {
        if (route == null) return null;
        RouteStates states = routeStateMap.get(route);
        if (states == null) return null;
        int stateIndex = stateId.getIndex();
        if (stateId.isDouble()) {
            if (!states.set[stateId.getDoubleIndex()]) return null;
            return type.cast(states.doubles[stateId.getDoubleIndex()]);
        }
        try {
            return type.cast(states.objects[stateIndex]);
        } catch (ClassCastException e) {
            throw getClassCastException(e, stateId, type.toString(), states.objects[stateIndex].getClass().toString());
        }
    }

    @Override
    public double getRouteDouble(VehicleRoute route, StateId stateId) {
        if (route == null) return Double.NaN;
        RouteStates states = routeStateMap.get(route);
        if (states == null) return Double.NaN;
        int stateIndex = stateId.getIndex();
        if (stateId.isDouble()) return states.set[stateId.getDoubleIndex()] ? states.doubles[stateId.getDoubleIndex()] : Double.NaN;
        Object state = states.objects[stateIndex];
        return state == null ? Double.NaN : (Double) state;
    }

    /**
     * Returns true if a vehicle-dependent state is set for the route, the vehicle (type) and the stateId.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean hasRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId) {
        VehicleDependentRouteStates states = vehicleDependentRouteStateMap.get(route);
        if (states == null) return false;
        int typeKeyIndex = getTypeKeyIndex(vehicle);
        if (stateId.isDouble()) return states.set[typeKeyIndex][stateId.getDoubleIndex()];
        return states.objects[typeKeyIndex][stateId.getIndex()] != null;
    }

    /**
     * Returns associated state for the specified route, vehicle and stateId, or it returns null if no value is associated.
     * <p>If type class is not equal to the associated type class of the requested state value, it throws a ClassCastException.</p>
     *
     * @param route   the route for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the route state
     * @param type    the class of the associated state value
     * @param <T>     the type of the class
     * @return the state value that is associated to the specified route and stateId, or null if no value is associated.
     * @throws java.lang.ClassCastException if type class is not equal to the associated type class of the requested state value
     */
    public <T> T getRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, Class<T> type) {
        VehicleDependentRouteStates states = vehicleDependentRouteStateMap.get(route);
        if (states == null) return null;
        int typeKeyIndex = getTypeKeyIndex(vehicle);
        int stateIndex = stateId.getIndex();
        if (stateId.isDouble()) {
            if (!states.set[typeKeyIndex][stateId.getDoubleIndex()]) {
                if (failOnMissingVehicleState) checkMissing(route, states, vehicle, stateId);
                return null;
            }
            return type.cast(states.doubles[typeKeyIndex][stateId.getDoubleIndex()]);
        }
        if (failOnMissingVehicleState && states.objects[typeKeyIndex][stateIndex] == null) checkMissing(route, states, vehicle, stateId);
        try {
            return type.cast(states.objects[typeKeyIndex][stateIndex]);
        } catch (ClassCastException e) {
            throw getClassCastException(e, stateId, type.toString(), states.objects[typeKeyIndex][stateIndex].getClass().toString());
        }
    }

    @Override
    public double getRouteDouble(VehicleRoute route, Vehicle vehicle, StateId stateId) {
        VehicleDependentRouteStates states = vehicleDependentRouteStateMap.get(route);
        if (states == null) return Double.NaN;
        int typeKeyIndex = getTypeKeyIndex(vehicle);
        int stateIndex = stateId.getIndex();
        if (stateId.isDouble()) {
            if (states.set[typeKeyIndex][stateId.getDoubleIndex()]) return states.doubles[typeKeyIndex][stateId.getDoubleIndex()];
            if (failOnMissingVehicleState) checkMissing(route, states, vehicle, stateId);
            return Double.NaN;
        }
        Object state = states.objects[typeKeyIndex][stateIndex];
        if (state == null && failOnMissingVehicleState) checkMissing(route, states, vehicle, stateId);
        return state == null ? Double.NaN : (Double) state;
    }

    private static void checkNotReserved(StateId stateId) {
        if (stateId.getIndex() < RESERVED_STATE_IDS)
            throw new IllegalStateException("either you use a reserved stateId that is applied\n" +
                "internally or your stateId has been created without index, e.g. StateFactory.createId(stateName)\n" +
                " does not assign indeces thus do not use it anymore, but use\n " +
                "stateManager.createStateId(name)\n" +
                " instead.\n");
    }

    /**
     * Associates the specified activity and stateId to the state value. If a state value is already associated to the
     * specified activity and stateId, it is replaced by the new state value.
     *
     * @param act     the activity for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param state   the state value
     * @param <T>     the type of the state value
     * @throws java.lang.IllegalStateException if state is a reserved state (i.e. created with StateFactory.createId(stateName)
     */
    public <T> void putActivityState(TourActivity act, StateId stateId, T state) {
        checkActivityIndex(act);
        checkNotReserved(stateId);
        putInternalTypedActivityState(act, stateId, state);
    }

    /**
     * Primitive variant of {@link #putActivityState(TourActivity, StateId, Object)} for a double value: no boxing
     * for ids created with {@link #createDoubleStateId(String)}; for an Object-valued id the value is boxed to a
     * Double as the generic method would. Deliberately not an overload of putActivityState: an int or long
     * argument would silently widen to double and end up stored as a Double.
     */
    public void putActivityDouble(TourActivity act, StateId stateId, double state) {
        checkActivityIndex(act);
        checkNotReserved(stateId);
        putInternalActivityDouble(act, stateId, state);
    }

    /**
     * Associates the specified activity, vehicle and stateId to the state value. If a state value is already associated to the
     * specified activity and stateId, it is replaced by the new state value.
     *
     * @param act     the activity for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the activity state
     * @param state   the state value
     * @param <T>     the type of the state value
     * @throws java.lang.IllegalStateException if state is a reserved state (i.e. created with StateFactory.createId(stateName)
     */
    public <T> void putActivityState(TourActivity act, Vehicle vehicle, StateId stateId, T state) {
        checkActivityIndex(act);
        checkNotReserved(stateId);
        putInternalTypedActivityState(act, vehicle, stateId, state);
    }

    /** Primitive, vehicle-dependent variant, see {@link #putActivityDouble(TourActivity, StateId, double)}. */
    public void putActivityDouble(TourActivity act, Vehicle vehicle, StateId stateId, double state) {
        checkActivityIndex(act);
        checkNotReserved(stateId);
        putInternalActivityDouble(act, vehicle, stateId, state);
    }

    <T> void putInternalTypedActivityState(TourActivity act, StateId stateId, T state) {
        if (stateId.isDouble()) {
            if (state == null) {
                activityDoublesSet[act.getIndex()][stateId.getDoubleIndex()] = false;
            } else {
                activityDoubles[act.getIndex()][stateId.getDoubleIndex()] = (Double) state;
                activityDoublesSet[act.getIndex()][stateId.getDoubleIndex()] = true;
            }
            return;
        }
        activityStates[act.getIndex()][stateId.getIndex()] = state;
    }

    void putInternalActivityDouble(TourActivity act, StateId stateId, double state) {
        if (stateId.isDouble()) {
            activityDoubles[act.getIndex()][stateId.getDoubleIndex()] = state;
            activityDoublesSet[act.getIndex()][stateId.getDoubleIndex()] = true;
            return;
        }
        activityStates[act.getIndex()][stateId.getIndex()] = state;
    }

    <T> void putInternalTypedActivityState(TourActivity act, Vehicle vehicle, StateId stateId, T state) {
        int typeKeyIndex = getTypeKeyIndex(vehicle);
        if (stateId.isDouble()) {
            if (state == null) {
                unsetVehicleDouble(act.getIndex(), typeKeyIndex, stateId.getDoubleIndex());
            } else {
                putVehicleDouble(act.getIndex(), typeKeyIndex, stateId.getDoubleIndex(), (Double) state);
            }
            return;
        }
        putVehicleObject(act.getIndex(), typeKeyIndex, stateId.getIndex(), state);
    }

    void putInternalActivityDouble(TourActivity act, Vehicle vehicle, StateId stateId, double state) {
        int typeKeyIndex = getTypeKeyIndex(vehicle);
        if (stateId.isDouble()) {
            putVehicleDouble(act.getIndex(), typeKeyIndex, stateId.getDoubleIndex(), state);
            return;
        }
        putVehicleObject(act.getIndex(), typeKeyIndex, stateId.getIndex(), state);
    }

    /**
     * Associates the specified route and stateId to the state value. If a state value is already associated to the
     * specified route and stateId, it is replaced by the new state value.
     *
     * @param route   the route for which a state value is associated to
     * @param stateId the stateId which is the associated key to the route state
     * @param state   the state value
     * @param <T>     the type of the state value
     * @throws java.lang.IllegalStateException if state is a reserved state (i.e. created with StateFactory.createId(stateName)
     */
    public <T> void putRouteState(VehicleRoute route, StateId stateId, T state) {
        if (stateId.getIndex() < RESERVED_STATE_IDS) StateFactory.throwReservedIdException(stateId.toString());
        putTypedInternalRouteState(route, stateId, state);
    }

    /** Primitive variant, see {@link #putActivityDouble(TourActivity, StateId, double)}. */
    public void putRouteDouble(VehicleRoute route, StateId stateId, double state) {
        if (stateId.getIndex() < RESERVED_STATE_IDS) StateFactory.throwReservedIdException(stateId.toString());
        putInternalRouteDouble(route, stateId, state);
    }

    /**
     * Associates the specified route, vehicle and stateId to the state value. If a state value is already associated to the
     * specified route and stateId, it is replaced by the new state value.
     *
     * @param route   the route for which a state value is associated to
     * @param vehicle the vehicle for which a state value is associated to
     * @param stateId the stateId which is the associated key to the route state
     * @param state   the state value
     * @param <T>     the type of the state value
     * @throws java.lang.IllegalStateException if state is a reserved state (i.e. created with StateFactory.createId(stateName)
     */
    public <T> void putRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, T state) {
        if (getVehicleIndex(vehicle) <= 0)
            throw new IllegalStateException("vehicle index is 0 or not found. Make sure the vehicle has been added to the VehicleRoutingProblem.");
        if (stateId.getIndex() < RESERVED_STATE_IDS) StateFactory.throwReservedIdException(stateId.toString());
        putTypedInternalRouteState(route, vehicle, stateId, state);
    }

    /** Primitive, vehicle-dependent variant, see {@link #putActivityDouble(TourActivity, StateId, double)}. */
    public void putRouteDouble(VehicleRoute route, Vehicle vehicle, StateId stateId, double state) {
        if (getVehicleIndex(vehicle) <= 0)
            throw new IllegalStateException("vehicle index is 0 or not found. Make sure the vehicle has been added to the VehicleRoutingProblem.");
        if (stateId.getIndex() < RESERVED_STATE_IDS) StateFactory.throwReservedIdException(stateId.toString());
        putInternalRouteDouble(route, vehicle, stateId, state);
    }

    private RouteStates routeStates(VehicleRoute route) {
        RouteStates states = routeStateMap.get(route);
        if (states == null) {
            states = new RouteStates(stateIndexCounter, doubleStateIndexCounter);
            routeStateMap.put(route, states);
        }
        return states;
    }

    private VehicleDependentRouteStates vehicleDependentRouteStates(VehicleRoute route) {
        VehicleDependentRouteStates states = vehicleDependentRouteStateMap.get(route);
        if (states == null) {
            states = new VehicleDependentRouteStates(vehicleClassCount(), stateIndexCounter, doubleStateIndexCounter);
            vehicleDependentRouteStateMap.put(route, states);
        }
        return states;
    }

    <T> void putTypedInternalRouteState(VehicleRoute route, StateId stateId, T state) {
        RouteStates states = routeStates(route);
        int stateIndex = stateId.getIndex();
        if (stateId.isDouble()) {
            if (state == null) {
                states.set[stateId.getDoubleIndex()] = false;
            } else {
                states.doubles[stateId.getDoubleIndex()] = (Double) state;
                states.set[stateId.getDoubleIndex()] = true;
            }
            return;
        }
        states.objects[stateIndex] = state;
    }

    void putInternalRouteDouble(VehicleRoute route, StateId stateId, double state) {
        RouteStates states = routeStates(route);
        int stateIndex = stateId.getIndex();
        if (stateId.isDouble()) {
            states.doubles[stateId.getDoubleIndex()] = state;
            states.set[stateId.getDoubleIndex()] = true;
            return;
        }
        states.objects[stateIndex] = state;
    }

    <T> void putTypedInternalRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, T state) {
        VehicleDependentRouteStates states = vehicleDependentRouteStates(route);
        int typeKeyIndex = getTypeKeyIndex(vehicle);
        int stateIndex = stateId.getIndex();
        if (stateId.isDouble()) {
            if (state == null) {
                states.set[typeKeyIndex][stateId.getDoubleIndex()] = false;
            } else {
                states.doubles[typeKeyIndex][stateId.getDoubleIndex()] = (Double) state;
                states.set[typeKeyIndex][stateId.getDoubleIndex()] = true;
            }
            return;
        }
        states.objects[typeKeyIndex][stateIndex] = state;
    }

    void putInternalRouteDouble(VehicleRoute route, Vehicle vehicle, StateId stateId, double state) {
        VehicleDependentRouteStates states = vehicleDependentRouteStates(route);
        int typeKeyIndex = getTypeKeyIndex(vehicle);
        int stateIndex = stateId.getIndex();
        if (stateId.isDouble()) {
            states.doubles[typeKeyIndex][stateId.getDoubleIndex()] = state;
            states.set[typeKeyIndex][stateId.getDoubleIndex()] = true;
            return;
        }
        states.objects[typeKeyIndex][stateIndex] = state;
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
        if (declaredVehicleClasses && updater instanceof VehicleDependent) {
            if (vehicleClasses != null)
                throw new IllegalStateException("the vehicle classes are already fixed; register " + updater.getClass().getSimpleName()
                    + " before the first vehicle-dependent state is written or read");
            vehicleKeyParts.add((VehicleDependent) updater);
        }
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

    /*
     * Vehicles in use. A fleet manager reads them from the routes, and it hears about routes through the calls
     * that keep the route states right: every code path that gives a route a vehicle has to make one of them anyway.
     */
    private VehicleFleetManager[] fleetManagers = new VehicleFleetManager[0];

    /**
     * From now on the fleet manager is reset with the routes of every
     * {@link #informInsertionStarts(Collection, Collection)} (they are taken to be the whole solution) and
     * synchronised with the route of every {@link #informJobInserted(Job, VehicleRoute, InsertionData)} and
     * {@link #reCalculateStates(VehicleRoute)}. Called where an insertion calculator is built from a fleet
     * manager and this state manager; adding the same fleet manager twice has no effect.
     */
    public void addFleetManager(VehicleFleetManager fleetManager) {
        for (VehicleFleetManager known : fleetManagers) if (known == fleetManager) return;
        fleetManagers = Arrays.copyOf(fleetManagers, fleetManagers.length + 1);
        fleetManagers[fleetManagers.length - 1] = fleetManager;
    }

    @Override
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, InsertionData insertionData) {
        for (VehicleFleetManager fleetManager : fleetManagers) fleetManager.sync(inRoute);
        insertionListeners.informJobInserted(job2insert, inRoute, insertionData);
        for (RouteVisitor v : routeVisitors) {
            v.visit(inRoute);
        }
        routeActivityVisitor.visit(inRoute);
        revRouteActivityVisitor.visit(inRoute);
        if (routeChangeTracker != null) routeChangeTracker.record(inRoute);
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        for (VehicleFleetManager fleetManager : fleetManagers) fleetManager.reset(vehicleRoutes);
        updateStates(vehicleRoutes, unassignedJobs);
    }

    private void updateStates(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        if (routeChangeTracker != null) routeChangeTracker.beforeInsertionStart(vehicleRoutes);
        insertionListeners.informInsertionStarts(vehicleRoutes, unassignedJobs);
        for (VehicleRoute route : vehicleRoutes) {
            for (RouteVisitor v : routeVisitors) {
                v.visit(route);
            }
            routeActivityVisitor.visit(route);
            revRouteActivityVisitor.visit(route);
        }
        if (routeChangeTracker != null) routeChangeTracker.afterInsertionStart(vehicleRoutes);
    }

    /**
     * Recomputes the states of one route after it was changed outside the insertion flow. Unlike
     * {@link #informInsertionStarts(Collection, Collection)} this does not say that the route is the whole
     * solution: the vehicles of the other routes stay in use.
     */
    public void reCalculateStates(VehicleRoute route){
        for (VehicleFleetManager fleetManager : fleetManagers) fleetManager.sync(route);
        updateStates(Arrays.asList(route), Collections.emptyList());
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        if (routeChangeTracker != null) routeChangeTracker.beforeClear();
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
