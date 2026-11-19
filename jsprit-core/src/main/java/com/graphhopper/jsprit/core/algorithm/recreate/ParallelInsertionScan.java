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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scans the insertion positions of one long route on several threads.
 * <p>
 * The insertion calculators evaluate a job at every position of a route (for a shipment at every pickup
 * position times every later delivery position). The evaluations are independent of each other: the route
 * and the states are read-only while a job is evaluated, and the running timing can be picked up at any
 * position from the activity end times the state manager caches on the route. So the position range is
 * cut into pieces, each piece is scanned by one task of the algorithm's {@link ForkJoinPool} with its own
 * scratch objects, and the pieces are merged in position order: the cheapest insertion wins, among equal
 * costs the earliest position, and once a piece has hit the position where the sequential loop would have
 * stopped (every time window answered {@code NOT_FULFILLED_BREAK}) the later pieces are discarded.
 * <p>
 * The result is the same on every run with the same number of threads. It is the sequential result wherever
 * a delivery never lowers the insertion costs; where it does (rounding to a few 1e-15 is enough), the
 * sequential loop's pruning by its running best and the pieces' pruning can skip different positions.
 * <p>
 * The pieces call the hard and soft constraints, the activity insertion cost calculator, the transport
 * costs and a position filter from several threads at once, so these must be stateless or thread-safe
 * (jsprit's own are; the insertion context, the activities being inserted and the scratch arrays are per
 * piece). The end times the state manager caches on the route's activities must be current, which the
 * algorithm's {@code UpdateActivityTimes} updater guarantees after every insertion.
 * <p>
 * This is the only parallelism available for a problem with one vehicle: the insertion strategies run one
 * task per route and otherwise insert job after job. Enabled by {@code Jsprit.Builder.setExecutorService}
 * with a {@link ForkJoinPool} (jsprit's own executor is one) and more than one thread; the thresholds say how
 * long a route has to be before the split pays its fork/join costs ({@code <= 0} switches it off).
 */
public final class ParallelInsertionScan {

    /**
     * Route length (activities) from which a shipment scan is split. One pickup position costs a delivery loop
     * over the rest of the route, so the split pays early: measured on single-vehicle shipment problems with
     * 6 threads, 646 shipments 20.6 s -> 6.2 s, while 40-shipment routes (80 activities) lost 2-4 % to the
     * fork/join overhead, hence the threshold between.
     */
    public static final int DEFAULT_MIN_SHIPMENT_ACTIVITIES = 150;
    /**
     * Route length (activities) from which a service scan is split; 0 = off. One position is one evaluation
     * of a few hundred nanoseconds, and single-vehicle problems with 365-462 services measured no gain with
     * 6 threads (the fork/join of a dozen tasks costs as much as the scan), so the split is off by default.
     */
    public static final int DEFAULT_MIN_SERVICE_ACTIVITIES = 0;

    /** Pickup positions one shipment task should at least cover; the delivery loop behind every one of them is the real work. */
    private static final int MIN_SHIPMENT_POSITIONS_PER_TASK = 8;
    /** Positions one service task should at least cover. */
    private static final int MIN_SERVICE_POSITIONS_PER_TASK = 32;

    /** One piece of a scan: evaluates the positions {@code from} (inclusive) to {@code to} (exclusive) and returns its result. */
    @FunctionalInterface
    public interface RangeScan<R> {
        R scan(int from, int to);
    }

    /**
     * What the ranges of one evaluation tell each other while they run: the earliest position at which a range
     * broke off. The sequential loop would never have looked behind it, so the ranges behind stop at once instead
     * of computing results the merge discards. Nothing else is shared: what a range evaluates must not depend on
     * how far the others have got, or the result depends on the timing of the threads.
     */
    public static final class Shared {
        private final AtomicInteger stopAt = new AtomicInteger(Integer.MAX_VALUE);

        /** A range broke off at {@code position}: nothing behind it counts. */
        public void stopAt(int position) {
            stopAt.accumulateAndGet(position, Math::min);
        }

        /** Whether a range has broken off before {@code position}. */
        public boolean stopsBefore(int position) {
            return position > stopAt.get();
        }
    }

    private final ForkJoinPool pool;
    private final int parallelism;
    private final int minShipmentActivities;
    private final int minServiceActivities;

    public ParallelInsertionScan(ForkJoinPool pool, int parallelism, int minShipmentActivities, int minServiceActivities) {
        if (pool == null) throw new IllegalArgumentException("pool must not be null");
        if (parallelism < 2) throw new IllegalArgumentException("parallelism must be at least 2, got " + parallelism);
        this.pool = pool;
        this.parallelism = parallelism;
        this.minShipmentActivities = minShipmentActivities;
        this.minServiceActivities = minServiceActivities;
    }

    /**
     * The scan for an executor, or null when the executor cannot run it: not a {@link ForkJoinPool} (a fixed
     * pool could deadlock on the nested tasks), a single thread, or both thresholds off.
     */
    public static ParallelInsertionScan of(ExecutorService executor, int noThreads, int minShipmentActivities, int minServiceActivities) {
        if (!(executor instanceof ForkJoinPool pool) || noThreads < 2) return null;
        if (minShipmentActivities <= 0 && minServiceActivities <= 0) return null;
        return new ParallelInsertionScan(pool, noThreads, minShipmentActivities, minServiceActivities);
    }

    public int getParallelism() {
        return parallelism;
    }

    /** Number of tasks for a shipment scan over {@code positions} insertion positions (route length + 1); 1 means scan sequentially. */
    public int shipmentTasks(int positions) {
        if (minShipmentActivities <= 0 || positions - 1 < minShipmentActivities) return 1;
        return Math.max(1, Math.min(2 * parallelism, positions / MIN_SHIPMENT_POSITIONS_PER_TASK));
    }

    /** Number of tasks for a service scan over {@code positions} insertion positions; 1 means scan sequentially. */
    public int serviceTasks(int positions) {
        if (minServiceActivities <= 0 || positions - 1 < minServiceActivities) return 1;
        return Math.max(1, Math.min(2 * parallelism, positions / MIN_SERVICE_POSITIONS_PER_TASK));
    }

    /**
     * Range bounds for a shipment scan: pickup position i costs the delivery loop over the positions - i
     * positions behind it, so the ranges are cut where the cumulative triangular work is equal: the
     * first range is the shortest, the last one the longest. Returns {@code tasks + 1} increasing bounds from 0 to {@code positions}.
     */
    public static int[] triangularBounds(int positions, int tasks) {
        if (tasks < 1) throw new IllegalArgumentException("tasks must be at least 1");
        double total = positions * (positions + 1) / 2.0;
        List<Integer> bounds = new ArrayList<>(tasks + 1);
        bounds.add(0);
        double work = 0;
        int next = 1;
        for (int i = 0; i < positions && next < tasks; i++) {
            work += positions - i;
            while (next < tasks && work >= next * total / tasks) {
                if (i + 1 > bounds.get(bounds.size() - 1) && i + 1 < positions) bounds.add(i + 1);
                next++;
            }
        }
        bounds.add(positions);
        return bounds.stream().mapToInt(Integer::intValue).toArray();
    }

    /** Range bounds for a service scan: every position costs the same, so the ranges have equal length. */
    public static int[] linearBounds(int positions, int tasks) {
        if (tasks < 1) throw new IllegalArgumentException("tasks must be at least 1");
        List<Integer> bounds = new ArrayList<>(tasks + 1);
        bounds.add(0);
        for (int k = 1; k < tasks; k++) {
            int b = (int) ((long) positions * k / tasks);
            if (b > bounds.get(bounds.size() - 1) && b < positions) bounds.add(b);
        }
        bounds.add(positions);
        return bounds.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Runs one task per range on the pool and returns the results in range order. Works from outside the
     * pool and from one of its workers (the insertion strategies already run their per-route tasks on it).
     */
    public <R> List<R> run(int[] bounds, RangeScan<R> scan) {
        int n = bounds.length - 1;
        if (n == 1) {
            List<R> single = new ArrayList<>(1);
            single.add(scan.scan(bounds[0], bounds[1]));
            return single;
        }
        List<ForkJoinTask<R>> tasks = new ArrayList<>(n);
        for (int k = 0; k < n; k++) {
            final int from = bounds[k], to = bounds[k + 1];
            tasks.add(ForkJoinTask.adapt(() -> scan.scan(from, to)));
        }
        return pool.invoke(new RecursiveTask<List<R>>() {
            @Override
            protected List<R> compute() {
                ForkJoinTask.invokeAll(tasks);
                List<R> results = new ArrayList<>(tasks.size());
                for (ForkJoinTask<R> task : tasks) results.add(task.join());
                return results;
            }
        });
    }

    @Override
    public String toString() {
        return "[name=parallelInsertionScan][parallelism=" + parallelism + "][minShipmentActivities=" + minShipmentActivities + "][minServiceActivities=" + minServiceActivities + "]";
    }
}
