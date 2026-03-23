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
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.*;

/**
 * A bounded priority queue for insertion data that:
 * 1. Maintains at most one entry per route (route replacement)
 * 2. Optionally limits total entries to top-k by cost
 * <p>
 * This eliminates the O(n²) memory growth of the original TreeSet approach
 * by ensuring entries are replaced rather than accumulated.
 * <p>
 * Memory complexity: O(min(k, R)) where k is the limit and R is number of routes
 * <p>
 * Thread-safety: This class is NOT thread-safe. External synchronization is required
 * for concurrent access.
 */
class BoundedInsertionQueue {

    /**
     * Entry holding insertion data with its associated route.
     * Immutable after construction.
     */
    static class Entry implements Comparable<Entry> {
        private final InsertionData insertionData;
        private final VehicleRoute route;
        private final int sequenceId; // For stable ordering when costs are equal

        Entry(InsertionData insertionData, VehicleRoute route, int sequenceId) {
            this.insertionData = insertionData;
            this.route = route;
            this.sequenceId = sequenceId;
        }

        InsertionData getInsertionData() {
            return insertionData;
        }

        VehicleRoute getRoute() {
            return route;
        }

        double getCost() {
            return insertionData.getInsertionCost();
        }

        @Override
        public int compareTo(Entry other) {
            int costCompare = Double.compare(this.getCost(), other.getCost());
            if (costCompare != 0) return costCompare;
            // Use sequence ID for stable ordering when costs are equal
            return Integer.compare(this.sequenceId, other.sequenceId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return sequenceId == entry.sequenceId;
        }

        @Override
        public int hashCode() {
            return sequenceId;
        }
    }

    // O(1) lookup by route for replacement
    private final Map<VehicleRoute, Entry> byRoute;

    // Sorted by cost for iteration
    private final TreeSet<Entry> sorted;

    // Maximum number of entries to keep (0 or negative means unlimited)
    private final int maxSize;

    // Counter for generating unique sequence IDs
    private int sequenceCounter;

    /**
     * Creates an unbounded queue (no limit on entries).
     */
    BoundedInsertionQueue() {
        this(0);
    }

    /**
     * Creates a bounded queue with the specified maximum size.
     *
     * @param maxSize maximum number of entries to keep (0 or negative for unlimited)
     */
    BoundedInsertionQueue(int maxSize) {
        this.maxSize = maxSize;
        this.byRoute = new HashMap<>();
        this.sorted = new TreeSet<>();
        this.sequenceCounter = 0;
    }

    /**
     * Adds or replaces an entry for the given route.
     * If an entry for this route already exists, it is replaced.
     * If the queue is bounded and full, the worst (highest cost) entry is removed
     * if the new entry is better.
     *
     * @param insertionData the insertion data
     * @param route         the route this insertion is for
     * @return true if the entry was added, false if rejected (worse than all existing entries in a full bounded queue)
     */
    boolean addOrReplace(InsertionData insertionData, VehicleRoute route) {
        if (insertionData instanceof InsertionData.NoInsertionFound) {
            return false;
        }

        Entry newEntry = new Entry(insertionData, route, sequenceCounter++);

        // Check if we already have an entry for this route
        Entry existingForRoute = byRoute.get(route);
        if (existingForRoute != null) {
            // Replace if new entry is better (lower cost)
            if (newEntry.getCost() < existingForRoute.getCost()) {
                sorted.remove(existingForRoute);
                byRoute.put(route, newEntry);
                sorted.add(newEntry);
                enforceMaxSize();
                return true;
            }
            // Keep existing entry if it's better or equal
            return false;
        }

        // No existing entry for this route
        if (maxSize > 0 && byRoute.size() >= maxSize) {
            // Queue is full - check if new entry is better than worst
            Entry worst = sorted.last();
            if (newEntry.getCost() < worst.getCost()) {
                // Remove worst entry
                sorted.remove(worst);
                byRoute.remove(worst.getRoute());
                // Add new entry
                byRoute.put(route, newEntry);
                sorted.add(newEntry);
                return true;
            }
            // New entry is worse than all existing entries
            return false;
        }

        // Queue has room - add new entry
        byRoute.put(route, newEntry);
        sorted.add(newEntry);
        enforceMaxSize();
        return true;
    }

    /**
     * Removes the entry for the given route if it exists.
     *
     * @param route the route to remove
     * @return true if an entry was removed
     */
    boolean remove(VehicleRoute route) {
        Entry existing = byRoute.remove(route);
        if (existing != null) {
            sorted.remove(existing);
            return true;
        }
        return false;
    }

    /**
     * Gets the entry for the given route.
     *
     * @param route the route to look up
     * @return the entry, or null if not found
     */
    Entry getForRoute(VehicleRoute route) {
        return byRoute.get(route);
    }

    /**
     * Returns all entries sorted by cost (ascending).
     * The returned iterator is a snapshot and safe to use while modifying the queue.
     */
    List<Entry> getSortedEntries() {
        return new ArrayList<>(sorted);
    }

    /**
     * Returns the best (lowest cost) entry, or null if empty.
     */
    Entry getBest() {
        return sorted.isEmpty() ? null : sorted.first();
    }

    /**
     * Returns the second-best entry, or null if fewer than 2 entries.
     */
    Entry getSecondBest() {
        if (sorted.size() < 2) return null;
        Iterator<Entry> iter = sorted.iterator();
        iter.next(); // skip first
        return iter.next();
    }

    /**
     * Returns the top k entries sorted by cost.
     */
    List<Entry> getTopK(int k) {
        List<Entry> result = new ArrayList<>(Math.min(k, sorted.size()));
        int count = 0;
        for (Entry e : sorted) {
            if (count >= k) break;
            result.add(e);
            count++;
        }
        return result;
    }

    /**
     * Returns the number of entries in the queue.
     */
    int size() {
        return byRoute.size();
    }

    /**
     * Returns true if the queue is empty.
     */
    boolean isEmpty() {
        return byRoute.isEmpty();
    }

    /**
     * Clears all entries from the queue.
     */
    void clear() {
        byRoute.clear();
        sorted.clear();
    }

    /**
     * Returns the maximum size limit (0 means unlimited).
     */
    int getMaxSize() {
        return maxSize;
    }

    /**
     * Enforces the maximum size by removing worst entries.
     */
    private void enforceMaxSize() {
        if (maxSize <= 0) return;
        while (byRoute.size() > maxSize) {
            Entry worst = sorted.pollLast();
            if (worst != null) {
                byRoute.remove(worst.getRoute());
            }
        }
    }

    @Override
    public String toString() {
        return "BoundedInsertionQueue{size=" + size() + ", maxSize=" + maxSize +
                ", bestCost=" + (isEmpty() ? "N/A" : getBest().getCost()) + "}";
    }
}
