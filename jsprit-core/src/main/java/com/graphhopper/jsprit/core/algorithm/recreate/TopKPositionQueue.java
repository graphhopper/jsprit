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

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * A bounded priority queue that keeps only the top-k positions by cost.
 * Unlike {@link BoundedInsertionQueue}, this allows multiple positions from the same route.
 *
 * <p>Optimized for the position-based regret-k use case where we need:
 * <ul>
 *   <li>Fast {@link #worstCost()} check for pruning (O(1))</li>
 *   <li>Fast {@link #offer} for adding positions (O(log k))</li>
 *   <li>All top-k positions for regret-k calculation (k=2,3,4,...)</li>
 * </ul>
 *
 * <p>Uses a max-heap internally so we can efficiently remove the worst element
 * when the queue exceeds capacity.
 */
class TopKPositionQueue {

    /**
     * A position entry with insertion data and associated route.
     */
    static class Position {
        private final InsertionData insertionData;
        private final VehicleRoute route;

        Position(InsertionData insertionData, VehicleRoute route) {
            this.insertionData = insertionData;
            this.route = route;
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
    }

    // Max-heap: worst (highest cost) element at head for efficient removal
    private final PriorityQueue<Position> maxHeap;
    private final int k;

    /**
     * Creates a queue that keeps the top-k lowest cost positions.
     * Use k=2 for regret-2, k=3 for regret-3, etc.
     *
     * @param k maximum number of positions to keep (must be positive)
     */
    TopKPositionQueue(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive, got: " + k);
        }
        this.k = k;
        // Max-heap: compare by cost descending so worst (highest) is at head
        this.maxHeap = new PriorityQueue<>(k + 1, (a, b) -> Double.compare(b.getCost(), a.getCost()));
    }

    /**
     * Returns the cost threshold for pruning.
     * Positions with cost >= this value cannot make it into top-k.
     *
     * @return the worst (highest) cost in the queue, or Double.MAX_VALUE if not full
     */
    double worstCost() {
        if (maxHeap.size() < k) {
            return Double.MAX_VALUE;
        }
        return maxHeap.peek().getCost();
    }

    /**
     * Attempts to add a position to the queue.
     * The position is added only if it's better than the current worst,
     * or if the queue isn't full yet.
     *
     * @param insertionData the insertion data for this position
     * @param route the route this position is in
     * @return true if the position was added
     */
    boolean offer(InsertionData insertionData, VehicleRoute route) {
        if (!insertionData.isFound()) {
            return false;
        }

        double cost = insertionData.getInsertionCost();

        // If queue is full and this position isn't better, reject
        if (maxHeap.size() >= k && cost >= worstCost()) {
            return false;
        }

        // Add the position
        maxHeap.offer(new Position(insertionData, route));

        // If over capacity, remove the worst
        if (maxHeap.size() > k) {
            maxHeap.poll();
        }

        return true;
    }

    /**
     * Quick check if a cost could potentially make it into top-k.
     * Use this for cheap lower-bound pruning before computing full insertion cost.
     *
     * @param lowerBoundCost a lower bound on the actual insertion cost
     * @return true if a position with this lower bound could make it into top-k
     */
    boolean couldMakeTopK(double lowerBoundCost) {
        return maxHeap.size() < k || lowerBoundCost < worstCost();
    }

    /**
     * Returns all positions sorted by cost (ascending).
     */
    List<Position> getSortedPositions() {
        List<Position> result = new ArrayList<>(maxHeap);
        result.sort((a, b) -> Double.compare(a.getCost(), b.getCost()));
        return result;
    }

    /**
     * Returns the best (lowest cost) position, or null if empty.
     */
    Position getBest() {
        if (maxHeap.isEmpty()) {
            return null;
        }
        // Need to find min in max-heap - not efficient but called rarely
        Position best = null;
        for (Position p : maxHeap) {
            if (best == null || p.getCost() < best.getCost()) {
                best = p;
            }
        }
        return best;
    }

    /**
     * Returns the second-best position, or null if fewer than 2 positions.
     */
    Position getSecondBest() {
        if (maxHeap.size() < 2) {
            return null;
        }
        List<Position> sorted = getSortedPositions();
        return sorted.get(1);
    }

    /**
     * Converts to RegretKAlternatives for scoring.
     */
    RegretKAlternatives toAlternatives() {
        List<Position> sorted = getSortedPositions();
        List<RegretKAlternatives.Alternative> alternatives = new ArrayList<>(sorted.size());
        for (Position p : sorted) {
            alternatives.add(new RegretKAlternatives.Alternative(p.getInsertionData(), p.getRoute()));
        }
        return new RegretKAlternatives(alternatives);
    }

    int size() {
        return maxHeap.size();
    }

    boolean isEmpty() {
        return maxHeap.isEmpty();
    }

    boolean isFull() {
        return maxHeap.size() >= k;
    }

    void clear() {
        maxHeap.clear();
    }

    int getK() {
        return k;
    }

    @Override
    public String toString() {
        return "TopKPositionQueue{k=" + k + ", size=" + size() +
                ", worstCost=" + (isEmpty() ? "N/A" : worstCost()) + "}";
    }
}
