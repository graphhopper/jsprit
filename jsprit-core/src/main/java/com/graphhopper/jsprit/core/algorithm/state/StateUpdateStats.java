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

import java.util.Map;
import java.util.TreeMap;

/**
 * What {@link StateManager#trackRouteChanges(boolean)} measures: how many of the routes handed to
 * {@code informInsertionStarts} are unchanged since their states were last computed, and, when states are
 * compared, which states of such an unchanged route come out differently when computed again.
 * <p>
 * A slot of an unchanged route can differ in three ways:
 * <ul>
 * <li>changed: a value was stored and the fresh computation writes another one. The state depends on something
 * the route signature (vehicle, departure time, activity sequence, the activities' time windows) does not
 * cover; its updater must not be skipped for unchanged routes.</li>
 * <li>added: nothing was stored and the fresh computation writes a value, e.g. a state that is only written at
 * insertion start and not after a job was inserted into a new route.</li>
 * <li>left over: a value was stored that the fresh computation does not write, e.g. the state of a vehicle the
 * route had before a switch, or of an updater that writes for the route's own vehicle only.</li>
 * </ul>
 */
public class StateUpdateStats {

    /** Collects for every state manager that was switched on with the system property {@code jsprit.states.trackRouteChanges}. */
    public static final StateUpdateStats GLOBAL = new StateUpdateStats();

    private long insertionStarts;

    private long routes;

    private long unchangedRoutes;

    private long activities;

    private long unchangedActivities;

    private long comparedRoutes;

    private long differingRoutes;

    public static final int CHANGED = 0;

    public static final int ADDED = 1;

    public static final int LEFT_OVER = 2;

    /** state -> {changed slots, added slots, left-over slots, routes with at least one changed slot} */
    private final Map<String, long[]> differences = new TreeMap<>();

    synchronized void addInsertionStart(int routes, int unchangedRoutes, int activities, int unchangedActivities) {
        insertionStarts++;
        this.routes += routes;
        this.unchangedRoutes += unchangedRoutes;
        this.activities += activities;
        this.unchangedActivities += unchangedActivities;
    }

    synchronized void addComparison(Map<String, int[]> differingSlotsByState) {
        comparedRoutes++;
        boolean changed = false;
        for (Map.Entry<String, int[]> e : differingSlotsByState.entrySet()) {
            long[] counts = differences.computeIfAbsent(e.getKey(), k -> new long[4]);
            for (int i = 0; i < 3; i++) counts[i] += e.getValue()[i];
            if (e.getValue()[CHANGED] > 0) {
                counts[3]++;
                changed = true;
            }
        }
        if (changed) differingRoutes++;
    }

    public synchronized void reset() {
        insertionStarts = routes = unchangedRoutes = activities = unchangedActivities = comparedRoutes = differingRoutes = 0;
        differences.clear();
    }

    /** Number of {@code informInsertionStarts} calls, including the single-route calls of {@code reCalculateStates}. */
    public synchronized long getInsertionStarts() {
        return insertionStarts;
    }

    /** Non-empty routes seen at insertion start. */
    public synchronized long getRoutes() {
        return routes;
    }

    public synchronized long getUnchangedRoutes() {
        return unchangedRoutes;
    }

    public synchronized long getActivities() {
        return activities;
    }

    public synchronized long getUnchangedActivities() {
        return unchangedActivities;
    }

    /** Unchanged routes whose freshly computed states were compared with the ones computed before. */
    public synchronized long getComparedRoutes() {
        return comparedRoutes;
    }

    /** Compared routes with at least one changed slot. */
    public synchronized long getDifferingRoutes() {
        return differingRoutes;
    }

    /**
     * Per state ("activity:name", "activity/vehicle:name", "route:name", "route/vehicle:name") the number of
     * changed, added and left-over slots ({@link #CHANGED}, {@link #ADDED}, {@link #LEFT_OVER}) and, at index 3,
     * the number of unchanged routes with at least one changed slot.
     */
    public synchronized Map<String, long[]> getDifferences() {
        Map<String, long[]> copy = new TreeMap<>();
        for (Map.Entry<String, long[]> e : differences.entrySet()) copy.put(e.getKey(), e.getValue().clone());
        return copy;
    }

    @Override
    public synchronized String toString() {
        StringBuilder s = new StringBuilder();
        s.append("insertion_starts=").append(insertionStarts)
            .append(" routes=").append(routes)
            .append(" unchanged_routes=").append(unchangedRoutes)
            .append(" activities=").append(activities)
            .append(" unchanged_activities=").append(unchangedActivities)
            .append(" compared_routes=").append(comparedRoutes)
            .append(" differing_routes=").append(differingRoutes);
        for (Map.Entry<String, long[]> e : differences.entrySet()) {
            s.append("\n  ").append(e.getKey()).append(": changed=").append(e.getValue()[CHANGED]).append(" (routes=").append(e.getValue()[3])
                .append(") added=").append(e.getValue()[ADDED]).append(" left_over=").append(e.getValue()[LEFT_OVER]);
        }
        return s.toString();
    }
}
