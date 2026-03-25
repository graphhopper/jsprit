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
package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

/**
 * Factory methods for creating common ruin operator configurations.
 *
 * <p>Provides pre-configured ruin operators for use with independent operator selection:</p>
 * <pre>
 * Jsprit.Builder.newInstance(vrp)
 *     .addRuinOperator(0.3, Ruin.random(0.3))
 *     .addRuinOperator(0.3, Ruin.radial(0.3))
 *     .addRuinOperator(0.2, Ruin.cluster())
 *     .addRuinOperator(0.2, Ruin.kruskalCluster())
 *     .buildAlgorithm();
 * </pre>
 *
 * <p>All factories return {@link RuinOperatorFactory} instances that will be
 * instantiated during algorithm construction with proper dependencies.</p>
 *
 * @see RuinOperatorFactory
 */
public final class Ruin {

    private Ruin() {}

    /**
     * Random ruin that removes a fraction of jobs randomly.
     *
     * @param fraction fraction of jobs to remove (0.0-1.0)
     * @return factory for random ruin
     */
    public static RuinOperatorFactory random(double fraction) {
        return RuinOperatorFactory.named("random", random(fraction, fraction));
    }

    /**
     * Random ruin with variable removal fraction.
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @return factory for random ruin
     */
    public static RuinOperatorFactory random(double minFraction, double maxFraction) {
        return RuinOperatorFactory.named("random", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.max(1, (int) (numJobs * minFraction));
            int maxShare = Math.max(minShare, (int) (numJobs * maxFraction));

            RuinRandom ruin = new RuinRandom(ctx.vrp(), 0.5);
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));
            return ruin;
        });
    }

    /**
     * Random ruin with fraction-based scaling and absolute bounds.
     *
     * <p>Calculates share as {@code numJobs * fraction}, then clamps between bounds.</p>
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @param minBound absolute minimum jobs to remove (floor)
     * @param maxBound absolute maximum jobs to remove (ceiling)
     * @return factory for random ruin
     */
    public static RuinOperatorFactory random(double minFraction, double maxFraction, int minBound, int maxBound) {
        return RuinOperatorFactory.named("random", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * minFraction)));
            int maxShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * maxFraction)));
            maxShare = Math.max(minShare, maxShare);

            RuinRandom ruin = new RuinRandom(ctx.vrp(), 0.5);
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));
            return ruin;
        });
    }

    /**
     * Radial ruin that removes nearby jobs.
     *
     * <p>Selects a random job and removes it along with its nearest neighbors.</p>
     *
     * @param fraction fraction of jobs to remove (0.0-1.0)
     * @return factory for radial ruin
     */
    public static RuinOperatorFactory radial(double fraction) {
        return RuinOperatorFactory.named("radial", radial(fraction, fraction));
    }

    /**
     * Radial ruin with variable removal fraction.
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @return factory for radial ruin
     */
    public static RuinOperatorFactory radial(double minFraction, double maxFraction) {
        return RuinOperatorFactory.named("radial", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.max(1, (int) (numJobs * minFraction));
            int maxShare = Math.max(minShare, (int) (numJobs * maxFraction));

            RuinRadialDynamic ruin = new RuinRadialDynamic(ctx.vrp(), numJobs);
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));
            return ruin;
        });
    }

    /**
     * Radial ruin with fraction-based scaling and absolute bounds.
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @param minBound absolute minimum jobs to remove (floor)
     * @param maxBound absolute maximum jobs to remove (ceiling)
     * @return factory for radial ruin
     */
    public static RuinOperatorFactory radial(double minFraction, double maxFraction, int minBound, int maxBound) {
        return RuinOperatorFactory.named("radial", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * minFraction)));
            int maxShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * maxFraction)));
            maxShare = Math.max(minShare, maxShare);

            RuinRadialDynamic ruin = new RuinRadialDynamic(ctx.vrp(), numJobs);
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));
            return ruin;
        });
    }

    /**
     * Cluster ruin using DBSCAN clustering (default).
     *
     * <p>Clusters jobs spatially and removes entire clusters.</p>
     *
     * @return factory for cluster ruin
     */
    public static RuinOperatorFactory cluster() {
        return RuinOperatorFactory.named("cluster", cluster(0.05, 0.3));
    }

    /**
     * Cluster ruin with variable removal fraction.
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @return factory for cluster ruin
     */
    public static RuinOperatorFactory cluster(double minFraction, double maxFraction) {
        return RuinOperatorFactory.named("cluster", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.max(1, (int) (numJobs * minFraction));
            int maxShare = Math.max(minShare, (int) (numJobs * maxFraction));

            RuinClusters ruin = new RuinClusters(ctx.vrp(), numJobs, ctx.jobNeighborhoods());
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));
            return ruin;
        });
    }

    /**
     * Cluster ruin with fraction-based scaling and absolute bounds.
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @param minBound absolute minimum jobs to remove (floor)
     * @param maxBound absolute maximum jobs to remove (ceiling)
     * @return factory for cluster ruin
     */
    public static RuinOperatorFactory cluster(double minFraction, double maxFraction, int minBound, int maxBound) {
        return RuinOperatorFactory.named("cluster", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * minFraction)));
            int maxShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * maxFraction)));
            maxShare = Math.max(minShare, maxShare);

            RuinClusters ruin = new RuinClusters(ctx.vrp(), numJobs, ctx.jobNeighborhoods());
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));
            return ruin;
        });
    }

    /**
     * Kruskal MST-based cluster ruin (ranked #2 in Voigt 2025).
     *
     * <p>Uses minimum spanning tree to identify clusters of related jobs
     * within routes, then removes connected components.</p>
     *
     * @return factory for Kruskal cluster ruin
     */
    public static RuinOperatorFactory kruskalCluster() {
        return RuinOperatorFactory.named("kruskal", kruskalCluster(0.05, 0.3));
    }

    /**
     * Kruskal cluster ruin with variable removal fraction.
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @return factory for Kruskal cluster ruin
     */
    public static RuinOperatorFactory kruskalCluster(double minFraction, double maxFraction) {
        return RuinOperatorFactory.named("kruskal", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.max(1, (int) (numJobs * minFraction));
            int maxShare = Math.max(minShare, (int) (numJobs * maxFraction));

            RuinKruskalClusters ruin = new RuinKruskalClusters(ctx.vrp(), numJobs, ctx.jobNeighborhoods());
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));
            return ruin;
        });
    }

    /**
     * Kruskal cluster ruin with fraction-based scaling and absolute bounds.
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @param minBound absolute minimum jobs to remove (floor)
     * @param maxBound absolute maximum jobs to remove (ceiling)
     * @return factory for Kruskal cluster ruin
     */
    public static RuinOperatorFactory kruskalCluster(double minFraction, double maxFraction, int minBound, int maxBound) {
        return RuinOperatorFactory.named("kruskal", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * minFraction)));
            int maxShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * maxFraction)));
            maxShare = Math.max(minShare, maxShare);

            RuinKruskalClusters ruin = new RuinKruskalClusters(ctx.vrp(), numJobs, ctx.jobNeighborhoods());
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));
            return ruin;
        });
    }

    /**
     * Worst ruin that removes jobs with highest removal benefit.
     *
     * <p>Removes jobs that, when removed, provide the largest cost savings.
     * Good for escaping local optima.</p>
     *
     * @param fraction fraction of jobs to remove (0.0-1.0)
     * @return factory for worst ruin
     */
    public static RuinOperatorFactory worst(double fraction) {
        return RuinOperatorFactory.named("worst", worst(fraction, fraction, 0.15, 0.2));
    }

    /**
     * Worst ruin with configurable parameters.
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @param noiseLevel noise level for randomization (0.0-1.0)
     * @param noiseProbability probability of applying noise (0.0-1.0)
     * @return factory for worst ruin
     */
    public static RuinOperatorFactory worst(double minFraction, double maxFraction,
                                            double noiseLevel, double noiseProbability) {
        return RuinOperatorFactory.named("worst", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.max(1, (int) (numJobs * minFraction));
            int maxShare = Math.max(minShare, (int) (numJobs * maxFraction));

            RuinWorst ruin = new RuinWorst(ctx.vrp(), numJobs);
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));

            // Configure noise
            double maxCosts = ctx.maxTransportCosts();
            ruin.setNoiseMaker(() -> {
                if (ctx.random().nextDouble() < noiseProbability) {
                    return noiseLevel * maxCosts * ctx.random().nextDouble();
                }
                return 0.0;
            });

            return ruin;
        });
    }

    /**
     * Worst ruin with fraction-based scaling and absolute bounds.
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @param minBound absolute minimum jobs to remove (floor)
     * @param maxBound absolute maximum jobs to remove (ceiling)
     * @return factory for worst ruin
     */
    public static RuinOperatorFactory worst(double minFraction, double maxFraction, int minBound, int maxBound) {
        return RuinOperatorFactory.named("worst", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * minFraction)));
            int maxShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * maxFraction)));
            maxShare = Math.max(minShare, maxShare);

            RuinWorst ruin = new RuinWorst(ctx.vrp(), numJobs);
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));

            // Default noise settings
            double maxCosts = ctx.maxTransportCosts();
            ruin.setNoiseMaker(() -> {
                if (ctx.random().nextDouble() < 0.2) {
                    return 0.15 * maxCosts * ctx.random().nextDouble();
                }
                return 0.0;
            });

            return ruin;
        });
    }

    /**
     * String ruin that removes sequences of jobs from routes.
     *
     * <p>Selects random routes and removes contiguous sequences of jobs.</p>
     *
     * @return factory for string ruin with default parameters
     */
    public static RuinOperatorFactory string() {
        return RuinOperatorFactory.named("string", string(1, 6, 10, 30));
    }

    /**
     * String ruin with configurable parameters.
     *
     * @param kMin minimum number of routes to affect
     * @param kMax maximum number of routes to affect
     * @param lMin minimum string length per route
     * @param lMax maximum string length per route
     * @return factory for string ruin
     */
    public static RuinOperatorFactory string(int kMin, int kMax, int lMin, int lMax) {
        return RuinOperatorFactory.named("string", ctx -> {
            RuinString ruin = new RuinString(ctx.vrp(), ctx.jobNeighborhoods());
            ruin.setNoRoutes(kMin, kMax);
            ruin.setStringLength(lMin, lMax);
            ruin.setRandom(ctx.random());
            return ruin;
        });
    }

    /**
     * Time-related ruin that removes jobs with similar time windows.
     *
     * @param fraction fraction of jobs to remove (0.0-1.0)
     * @return factory for time-related ruin
     */
    public static RuinOperatorFactory timeRelated(double fraction) {
        return RuinOperatorFactory.named("timeRelated", timeRelated(fraction, fraction));
    }

    /**
     * Time-related ruin with variable removal fraction.
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @return factory for time-related ruin
     */
    public static RuinOperatorFactory timeRelated(double minFraction, double maxFraction) {
        return RuinOperatorFactory.named("timeRelated", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.max(1, (int) (numJobs * minFraction));
            int maxShare = Math.max(minShare, (int) (numJobs * maxFraction));

            RuinTimeRelated ruin = new RuinTimeRelated(ctx.vrp());
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));
            return ruin;
        });
    }

    /**
     * Time-related ruin with fraction-based scaling and absolute bounds.
     *
     * @param minFraction minimum fraction of jobs to remove
     * @param maxFraction maximum fraction of jobs to remove
     * @param minBound absolute minimum jobs to remove (floor)
     * @param maxBound absolute maximum jobs to remove (ceiling)
     * @return factory for time-related ruin
     */
    public static RuinOperatorFactory timeRelated(double minFraction, double maxFraction, int minBound, int maxBound) {
        return RuinOperatorFactory.named("timeRelated", ctx -> {
            int numJobs = ctx.vrp().getJobs().size();
            int minShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * minFraction)));
            int maxShare = Math.min(maxBound, Math.max(minBound, (int) (numJobs * maxFraction)));
            maxShare = Math.max(minShare, maxShare);

            RuinTimeRelated ruin = new RuinTimeRelated(ctx.vrp());
            ruin.setRandom(ctx.random());
            ruin.setRuinShareFactory(new VariableRuinShareFactory(minShare, maxShare, ctx.random()));
            return ruin;
        });
    }

    /**
     * Simple variable ruin share factory implementation.
     */
    private static class VariableRuinShareFactory implements RuinShareFactory {
        private final int minShare;
        private final int maxShare;
        private final java.util.Random random;

        VariableRuinShareFactory(int minShare, int maxShare, java.util.Random random) {
            this.minShare = minShare;
            this.maxShare = Math.max(minShare, maxShare);
            this.random = random;
        }

        @Override
        public int createNumberToBeRemoved() {
            if (minShare == maxShare) return minShare;
            return minShare + random.nextInt(maxShare - minShare + 1);
        }
    }
}
