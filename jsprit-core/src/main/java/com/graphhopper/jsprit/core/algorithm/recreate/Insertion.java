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

/**
 * Factory methods for creating common insertion operator configurations.
 *
 * <p>Provides pre-configured insertion operators for use with independent operator selection:</p>
 * <pre>
 * Jsprit.Builder.newInstance(vrp)
 *     .addInsertionOperator(0.5, Insertion.regretFast())      // Fast with filtering
 *     .addInsertionOperator(0.3, Insertion.regret())          // Standard regret
 *     .addInsertionOperator(0.2, Insertion.best())            // Best insertion
 *     .buildAlgorithm();
 * </pre>
 *
 * <p>All factories return {@link InsertionOperatorFactory} instances that will be
 * instantiated during algorithm construction with proper dependencies.</p>
 *
 * @see InsertionOperatorFactory
 */
public final class Insertion {

    private Insertion() {}

    /**
     * Fast regret insertion with spatial filtering and affected-job tracking.
     *
     * <p>Optimized for speed while maintaining good solution quality.
     * Uses spatial filtering to limit route candidates and affected-job tracking
     * to minimize recalculations.</p>
     *
     * @return factory for fast regret insertion
     */
    public static InsertionOperatorFactory regretFast() {
        return InsertionOperatorFactory.named("regretFast", regretFast(2, 5, true));
    }

    /**
     * Fast regret-k insertion with configurable parameters.
     *
     * @param k regret-k parameter (2 for regret-2, 3 for regret-3, etc.)
     * @param spatialFilterK number of nearest routes to consider (0 to disable)
     * @param affectedJobTracking whether to use affected-job tracking
     * @return factory for fast regret insertion
     */
    public static InsertionOperatorFactory regretFast(int k, int spatialFilterK, boolean affectedJobTracking) {
        return ctx -> {
            InsertionStrategyBuilder builder = new InsertionStrategyBuilder(
                ctx.vrp(), ctx.fleetManager(), ctx.stateManager(), ctx.constraintManager())
                .setInsertionStrategy(InsertionStrategyBuilder.Strategy.REGRET)
                .setFastRegret(true)
                .setActivityInsertionCostCalculator(ctx.activityInsertionCalculator())
                .setRandom(ctx.random());

            if (ctx.isConcurrent()) {
                builder.setConcurrentMode(ctx.executorService(), ctx.numThreads());
            }

            AbstractInsertionStrategy strategy = (AbstractInsertionStrategy) builder.build();

            // Configure regret-k
            if (strategy instanceof RegretInsertionFast regret) {
                regret.setRegretK(k);
                regret.setDependencyTypes(ctx.constraintManager().getDependencyTypes());
                regret.setAffectedJobTrackingEnabled(affectedJobTracking);

                if (spatialFilterK > 0) {
                    regret.setSpatialFilter(new AdaptiveSpatialFilter(0, spatialFilterK));
                }

                if (ctx.scoringFunction() != null) {
                    regret.setScoringFunction(ctx.scoringFunction());
                }
            } else if (strategy instanceof RegretInsertionConcurrentFast regret) {
                regret.setRegretK(k);
                regret.setDependencyTypes(ctx.constraintManager().getDependencyTypes());

                if (spatialFilterK > 0) {
                    regret.setSpatialFilter(new AdaptiveSpatialFilter(0, spatialFilterK));
                }

                if (ctx.scoringFunction() != null) {
                    regret.setScoringFunction(ctx.scoringFunction());
                }
            }

            return strategy;
        };
    }

    /**
     * Standard regret insertion without filtering (thorough but slower).
     *
     * <p>Evaluates all routes for all jobs. Use as a complement to
     * {@link #regretFast()} to ensure no good insertions are missed.</p>
     *
     * @return factory for standard regret insertion
     */
    public static InsertionOperatorFactory regret() {
        return InsertionOperatorFactory.named("regret", regret(2));
    }

    /**
     * Standard regret-k insertion.
     *
     * @param k regret-k parameter (2 for regret-2, 3 for regret-3, etc.)
     * @return factory for regret-k insertion
     */
    public static InsertionOperatorFactory regret(int k) {
        return ctx -> {
            InsertionStrategyBuilder builder = new InsertionStrategyBuilder(
                ctx.vrp(), ctx.fleetManager(), ctx.stateManager(), ctx.constraintManager())
                .setInsertionStrategy(InsertionStrategyBuilder.Strategy.REGRET)
                .setFastRegret(true)  // Still use fast implementation, just without filtering
                .setActivityInsertionCostCalculator(ctx.activityInsertionCalculator())
                .setRandom(ctx.random());

            if (ctx.isConcurrent()) {
                builder.setConcurrentMode(ctx.executorService(), ctx.numThreads());
            }

            AbstractInsertionStrategy strategy = (AbstractInsertionStrategy) builder.build();

            // Configure regret-k without filtering
            if (strategy instanceof RegretInsertionFast regret) {
                regret.setRegretK(k);
                regret.setDependencyTypes(ctx.constraintManager().getDependencyTypes());
                regret.setAffectedJobTrackingEnabled(false);
                regret.setSpatialFilter(null);

                if (ctx.scoringFunction() != null) {
                    regret.setScoringFunction(ctx.scoringFunction());
                }
            } else if (strategy instanceof RegretInsertionConcurrentFast regret) {
                regret.setRegretK(k);
                regret.setDependencyTypes(ctx.constraintManager().getDependencyTypes());
                regret.setSpatialFilter(null);

                if (ctx.scoringFunction() != null) {
                    regret.setScoringFunction(ctx.scoringFunction());
                }
            }

            return strategy;
        };
    }

    /**
     * Best insertion (greedy, inserts cheapest job first).
     *
     * <p>Simple greedy insertion that always picks the job with lowest
     * insertion cost. Fast but may produce suboptimal solutions.</p>
     *
     * @return factory for best insertion
     */
    public static InsertionOperatorFactory best() {
        return InsertionOperatorFactory.named("best", ctx -> {
            InsertionStrategyBuilder builder = new InsertionStrategyBuilder(
                ctx.vrp(), ctx.fleetManager(), ctx.stateManager(), ctx.constraintManager())
                .setInsertionStrategy(InsertionStrategyBuilder.Strategy.BEST)
                .setActivityInsertionCostCalculator(ctx.activityInsertionCalculator())
                .setRandom(ctx.random());

            if (ctx.isConcurrent()) {
                builder.setConcurrentMode(ctx.executorService(), ctx.numThreads());
            }

            return builder.build();
        });
    }

    /**
     * Cheapest insertion (true best insertion from VRP literature).
     *
     * <p>Inserts jobs one at a time in cheapest-first order, always
     * selecting the globally cheapest insertion across all jobs and routes.</p>
     *
     * @return factory for cheapest insertion
     */
    public static InsertionOperatorFactory cheapest() {
        return InsertionOperatorFactory.named("cheapest", ctx -> {
            InsertionStrategyBuilder builder = new InsertionStrategyBuilder(
                ctx.vrp(), ctx.fleetManager(), ctx.stateManager(), ctx.constraintManager())
                .setInsertionStrategy(InsertionStrategyBuilder.Strategy.CHEAPEST)
                .setActivityInsertionCostCalculator(ctx.activityInsertionCalculator())
                .setRandom(ctx.random());

            if (ctx.isConcurrent()) {
                builder.setConcurrentMode(ctx.executorService(), ctx.numThreads());
            }

            return builder.build();
        });
    }

    /**
     * Position-based regret insertion (fast version).
     *
     * <p>Considers all insertion positions across routes, not just the best
     * position per route. More accurate regret calculation but slower.</p>
     *
     * @return factory for position-based regret insertion
     */
    public static InsertionOperatorFactory positionRegret() {
        return InsertionOperatorFactory.named("positionRegret", positionRegret(3, 3));
    }

    /**
     * Position-based regret insertion with configurable parameters.
     *
     * @param k regret-k parameter
     * @param topRoutesToExpand number of top routes to expand to position level
     * @return factory for position-based regret insertion
     */
    public static InsertionOperatorFactory positionRegret(int k, int topRoutesToExpand) {
        return ctx -> {
            InsertionStrategyBuilder builder = new InsertionStrategyBuilder(
                ctx.vrp(), ctx.fleetManager(), ctx.stateManager(), ctx.constraintManager())
                .setInsertionStrategy(InsertionStrategyBuilder.Strategy.POSITION_BASED_REGRET_FAST)
                .setPositionBasedRegretK(k)
                .setTopRoutesToExpand(topRoutesToExpand)
                .setActivityInsertionCostCalculator(ctx.activityInsertionCalculator())
                .setRandom(ctx.random());

            return builder.build();
        };
    }
}
