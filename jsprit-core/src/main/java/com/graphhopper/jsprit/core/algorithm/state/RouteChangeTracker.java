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

import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.*;

/**
 * Diagnostics behind {@link StateManager#trackRouteChanges(boolean)}; it changes nothing the search sees.
 * <p>
 * Every time the state manager has computed the states of a route (insertion start, job inserted) the route
 * is recorded: vehicle, departure time, the sequence of activity indices and the activities' time windows.
 * Each activity index points to the record it was last computed under. A route handed to
 * {@code informInsertionStarts} is unchanged when all its activities point to one record and that record
 * equals the route as it is now. Routes are copied between iterations, activity indices survive the copy, so
 * the record is found through the indices and not through the route object.
 * <p>
 * With comparison on, the states of the recorded routes are copied before the state manager clears them at
 * iteration start, and after the fresh computation of an unchanged route every slot is compared with the copy.
 * Object states are compared with {@code equals}; an object that is modified in place instead of replaced
 * compares equal to itself and is not detected. A slot can differ in three ways, see {@link StateUpdateStats}.
 */
final class RouteChangeTracker {

    /** The states of one activity or of one route; the second dimension of the v-arrays is the vehicle type key. */
    static final class Slots {
        Object[] objects;
        double[] doubles;
        boolean[] set;
        Object[][] vObjects;
        double[][] vDoubles;
        boolean[][] vSet;
    }

    private static final class Record {
        final VehicleRoute route;
        final Vehicle vehicle;
        final double departureTime;
        final int[] activities;
        final double[] earliest;
        final double[] latest;
        Slots[] activitySlots;
        Slots routeSlots;

        Record(VehicleRoute route) {
            this.route = route;
            vehicle = route.getVehicle();
            departureTime = route.getDepartureTime();
            List<TourActivity> acts = route.getActivities();
            activities = new int[acts.size()];
            earliest = new double[acts.size()];
            latest = new double[acts.size()];
            for (int i = 0; i < activities.length; i++) {
                TourActivity act = acts.get(i);
                activities[i] = act.getIndex();
                earliest[i] = act.getTheoreticalEarliestOperationStartTime();
                latest[i] = act.getTheoreticalLatestOperationStartTime();
            }
        }

        boolean equalsRoute(VehicleRoute route) {
            List<TourActivity> acts = route.getActivities();
            if (acts.size() != activities.length) return false;
            if (vehicle != route.getVehicle()) return false;
            if (departureTime != route.getDepartureTime()) return false;
            for (int i = 0; i < activities.length; i++) {
                TourActivity act = acts.get(i);
                if (activities[i] != act.getIndex()) return false;
                if (earliest[i] != act.getTheoreticalEarliestOperationStartTime()) return false;
                if (latest[i] != act.getTheoreticalLatestOperationStartTime()) return false;
            }
            return true;
        }
    }

    private final StateManager stateManager;

    private final StateUpdateStats stats;

    private final boolean compare;

    /** activity index -> the record its states were last computed under */
    private Record[] recordOf = new Record[64];

    /** filled by {@link #beforeInsertionStart}, read by {@link #afterInsertionStart} */
    private final Map<VehicleRoute, Record> unchanged = new IdentityHashMap<>();

    RouteChangeTracker(StateManager stateManager, StateUpdateStats stats, boolean compare) {
        this.stateManager = stateManager;
        this.stats = stats;
        this.compare = compare;
    }

    StateUpdateStats getStats() {
        return stats;
    }

    /** The states are about to be cleared: keep a copy of what the recorded routes have. */
    void beforeClear() {
        if (!compare) return;
        Set<Record> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Record record : recordOf) {
            if (record == null || record.activitySlots != null || !seen.add(record)) continue;
            if (isCurrent(record)) copyStates(record);
        }
    }

    void beforeInsertionStart(Collection<VehicleRoute> routes) {
        unchanged.clear();
        int nRoutes = 0, nUnchanged = 0, nActivities = 0, nUnchangedActivities = 0;
        for (VehicleRoute route : routes) {
            if (route.isEmpty()) continue;
            int size = route.getActivities().size();
            nRoutes++;
            nActivities += size;
            Record record = recordOf(route);
            if (record == null) continue;
            nUnchanged++;
            nUnchangedActivities += size;
            if (compare) {
                // no iteration start since the record was written: the store still holds its states
                if (record.activitySlots == null) copyStates(record);
                unchanged.put(route, record);
            }
        }
        stats.addInsertionStart(nRoutes, nUnchanged, nActivities, nUnchangedActivities);
    }

    void afterInsertionStart(Collection<VehicleRoute> routes) {
        for (VehicleRoute route : routes) {
            if (route.isEmpty()) continue;
            Record before = unchanged.get(route);
            if (before != null) compare(before, route);
            record(route);
        }
        unchanged.clear();
    }

    /** The states of the route have just been computed. */
    void record(VehicleRoute route) {
        if (route.isEmpty()) return;
        Record record = new Record(route);
        for (int index : record.activities) {
            if (index <= 0) return;
            if (index >= recordOf.length) recordOf = Arrays.copyOf(recordOf, Math.max(index + 1, 2 * recordOf.length));
        }
        for (int index : record.activities) recordOf[index] = record;
    }

    /** The record all activities of the route point to, if it still describes the route; null otherwise. */
    private Record recordOf(VehicleRoute route) {
        Record record = null;
        for (TourActivity act : route.getActivities()) {
            int index = act.getIndex();
            if (index <= 0 || index >= recordOf.length) return null;
            Record r = recordOf[index];
            if (r == null || (record != null && r != record)) return null;
            record = r;
        }
        return record != null && record.equalsRoute(route) ? record : null;
    }

    private boolean isCurrent(Record record) {
        for (int index : record.activities) {
            if (recordOf[index] != record) return false;
        }
        return true;
    }

    private void copyStates(Record record) {
        record.activitySlots = new Slots[record.activities.length];
        for (int i = 0; i < record.activities.length; i++) {
            record.activitySlots[i] = stateManager.copyActivitySlots(record.activities[i]);
        }
        record.routeSlots = stateManager.copyRouteSlots(record.route);
    }

    private void compare(Record before, VehicleRoute route) {
        Map<String, int[]> differences = new TreeMap<>();
        for (int i = 0; i < before.activities.length; i++) {
            compare(before.activitySlots[i], stateManager.copyActivitySlots(before.activities[i]), "activity", differences);
        }
        compare(before.routeSlots, stateManager.copyRouteSlots(route), "route", differences);
        stats.addComparison(differences);
    }

    private void compare(Slots before, Slots now, String kind, Map<String, int[]> differences) {
        compareObjects(before.objects, now.objects, kind + ":", differences);
        compareDoubles(before.doubles, before.set, now.doubles, now.set, kind + ":", differences);
        int typeKeys = Math.max(length(before.vObjects), length(now.vObjects));
        for (int k = 0; k < typeKeys; k++) {
            compareObjects(row(before.vObjects, k), row(now.vObjects, k), kind + "/vehicle:", differences);
        }
        typeKeys = Math.max(length(before.vSet), length(now.vSet));
        for (int k = 0; k < typeKeys; k++) {
            compareDoubles(row(before.vDoubles, k), row(before.vSet, k), row(now.vDoubles, k), row(now.vSet, k), kind + "/vehicle:", differences);
        }
    }

    private void compareObjects(Object[] before, Object[] now, String prefix, Map<String, int[]> differences) {
        int n = Math.max(before == null ? 0 : before.length, now == null ? 0 : now.length);
        for (int i = 0; i < n; i++) {
            Object a = before != null && i < before.length ? before[i] : null;
            Object b = now != null && i < now.length ? now[i] : null;
            if (!Objects.deepEquals(a, b)) count(prefix + stateManager.stateName(i), category(a != null, b != null), differences);
        }
    }

    private void compareDoubles(double[] before, boolean[] beforeSet, double[] now, boolean[] nowSet, String prefix, Map<String, int[]> differences) {
        int n = Math.max(beforeSet == null ? 0 : beforeSet.length, nowSet == null ? 0 : nowSet.length);
        for (int i = 0; i < n; i++) {
            boolean a = beforeSet != null && i < beforeSet.length && beforeSet[i];
            boolean b = nowSet != null && i < nowSet.length && nowSet[i];
            if (a != b || (a && Double.doubleToLongBits(before[i]) != Double.doubleToLongBits(now[i]))) {
                count(prefix + stateManager.doubleStateName(i), category(a, b), differences);
            }
        }
    }

    private static int category(boolean setBefore, boolean setNow) {
        return setBefore && setNow ? StateUpdateStats.CHANGED : setNow ? StateUpdateStats.ADDED : StateUpdateStats.LEFT_OVER;
    }

    private static void count(String state, int category, Map<String, int[]> differences) {
        differences.computeIfAbsent(state, k -> new int[3])[category]++;
    }

    private static int length(Object[] rows) {
        return rows == null ? 0 : rows.length;
    }

    private static <T> T row(T[] rows, int k) {
        return rows != null && k < rows.length ? rows[k] : null;
    }
}
