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

package com.graphhopper.jsprit.core.algorithm.box;

import com.graphhopper.jsprit.core.algorithm.PrettyAlgorithmBuilder;
import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.SearchStrategyModule;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.acceptor.SchrimpfAcceptance;
import com.graphhopper.jsprit.core.algorithm.acceptor.SolutionAcceptor;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.algorithm.module.IndependentRuinAndRecreateModule;
import com.graphhopper.jsprit.core.algorithm.module.RuinAndRecreateModule;
import com.graphhopper.jsprit.core.algorithm.recreate.*;
import com.graphhopper.jsprit.core.algorithm.ruin.*;
import com.graphhopper.jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.selector.WeightedOperatorSelector;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.FiniteFleetManagerFactory;
import com.graphhopper.jsprit.core.problem.vehicle.InfiniteFleetManagerFactory;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import com.graphhopper.jsprit.core.util.Solutions;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Jsprit {

    private final ActivityInsertionCostsCalculator activityInsertion;
    private final JobFilter jobFilter;

    public enum Construction {

        BEST_INSERTION("best_insertion"), REGRET_INSERTION("regret_insertion"), CHEAPEST_INSERTION("cheapest_insertion");

        String name;

        Construction(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

    }

    public enum Strategy {

        RADIAL_BEST("radial_best"),
        RADIAL_REGRET("radial_regret"),
        RADIAL_CHEAPEST("radial_cheapest"),
        RANDOM_BEST("random_best"),
        RANDOM_REGRET("random_regret"),
        RANDOM_CHEAPEST("random_cheapest"),
        WORST_BEST("worst_best"),
        WORST_REGRET("worst_regret"),
        WORST_CHEAPEST("worst_cheapest"),
        CLUSTER_BEST("cluster_best"),
        CLUSTER_REGRET("cluster_regret"),
        CLUSTER_CHEAPEST("cluster_cheapest"),
        STRING_BEST("string_best"),
        STRING_REGRET("string_regret"),
        STRING_CHEAPEST("string_cheapest"),
        TIME_RELATED_BEST("time_related_best"),
        TIME_RELATED_REGRET("time_related_regret"),
        TIME_RELATED_CHEAPEST("time_related_cheapest");


        String strategyName;

        Strategy(String strategyName) {
            this.strategyName = strategyName;
        }

        public String toString() {
            return strategyName;
        }
    }

    /**
     * Ruin operators for independent selection mode.
     * These allow setting weights for individual ruin operators independently
     * of insertion operators.
     */
    public enum RuinOperator {
        RADIAL("ruin.radial"),
        RANDOM("ruin.random"),
        WORST("ruin.worst"),
        CLUSTER_DBSCAN("ruin.cluster_dbscan"),
        CLUSTER_KRUSKAL("ruin.cluster_kruskal"),
        STRING("ruin.string"),
        TIME_RELATED("ruin.time_related");

        String name;

        RuinOperator(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    /**
     * Insertion operators for independent selection mode.
     * These allow setting weights for individual insertion operators independently
     * of ruin operators.
     */
    public enum InsertionOperator {
        BEST("insertion.best"),
        CHEAPEST("insertion.cheapest"),
        REGRET_2("insertion.regret_2"),
        REGRET_3("insertion.regret_3"),
        POSITION_REGRET_3("insertion.position_regret_3");

        String name;

        InsertionOperator(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public enum Parameter {

        FIXED_COST_PARAM("fixed_cost_param"), VEHICLE_SWITCH("vehicle_switch"), REGRET_TIME_WINDOW_SCORER("regret.tw_scorer"),
        REGRET_DISTANCE_SCORER("regret.distance_scorer"), INITIAL_THRESHOLD("initial_threshold"), ITERATIONS("iterations"),
        THREADS("threads"),
        RANDOM_REGRET_MIN_SHARE("random_regret.min_share"),
        RANDOM_REGRET_MAX_SHARE("random_regret.max_share"),
        RANDOM_BEST_MIN_SHARE("random_best.min_share"),
        RANDOM_BEST_MAX_SHARE("random_best.max_share"),
        RADIAL_MIN_SHARE("radial.min_share"),
        RADIAL_MAX_SHARE("radial.max_share"),
        CLUSTER_MIN_SHARE("cluster.min_share"),
        CLUSTER_MAX_SHARE("cluster.max_share"),
        WORST_MIN_SHARE("worst.min_share"),
        WORST_MAX_SHARE("worst.max_share"),
        THRESHOLD_ALPHA("threshold.alpha"),
        THRESHOLD_INI("threshold.ini"),
        THRESHOLD_INI_ABS("threshold.ini_abs"),
        INSERTION_NOISE_LEVEL("insertion.noise_level"),
        INSERTION_NOISE_PROB("insertion.noise_prob"),
        RUIN_WORST_NOISE_LEVEL("worst.noise_level"),
        RUIN_WORST_NOISE_PROB("worst.noise_prob"),
        FAST_REGRET("regret.fast"),
        REGRET_K("regret.k"),
        REGRET_K_STRATEGY("regret.k.strategy"),
        SPATIAL_FILTER("regret.spatial_filter"),
        SPATIAL_FILTER_K("regret.spatial_filter_k"),
        /**
         * @deprecated Learning is now handled via {@link RouteFilterTuner} algorithm listener.
         * This parameter is ignored.
         */
        @Deprecated
        SPATIAL_FILTER_LEARNING_ROUNDS("regret.spatial_filter_learning_rounds"),
        MAX_TRANSPORT_COSTS("max_transport_costs"),
        CONSTRUCTION("construction"),
        BREAK_SCHEDULING("break_scheduling"),
        STRING_K_MIN("string_kmin"),
        STRING_K_MAX("string_kmax"),
        STRING_L_MIN("string_lmin"),
        STRING_L_MAX("string_lmax"),
        MIN_UNASSIGNED("min_unassigned"),
        PROPORTION_UNASSIGNED("proportion_unassigned");



        String paraName;

        Parameter(String name) {
            this.paraName = name;
        }

        public String toString() {
            return paraName;
        }

    }

    public static VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vehicleRoutingProblem) {
        return Jsprit.Builder.newInstance(vehicleRoutingProblem).buildAlgorithm();
    }

    public static class Builder {

        private VehicleRoutingProblem vrp;

        private ExecutorService es;

        private Integer noThreads;

        private StateManager stateManager = null;

        private ConstraintManager constraintManager = null;

        private SolutionCostCalculator objectiveFunction = null;

        private Properties properties;

        private boolean addConstraints = true;

        private Random random = RandomNumberGeneration.newInstance();

        private ActivityInsertionCostsCalculator activityInsertionCalculator;

        private SolutionAcceptor solutionAcceptor;

        private ScoringFunction regretScorer = null;

        private RegretScoringFunction regretScoringFunction = null;

        private Map<SearchStrategy, Double> customStrategies = new HashMap<>();

        private List<StrategyComponents> strategyComponents = new ArrayList<>();

        private VehicleFleetManager fleetManager = null;

        private JobFilter jobFilter = null;

        private JobInsertionCostsCalculatorFactory serviceCalculatorFactory = null;

        private JobInsertionCostsCalculatorFactory shipmentCalculatorFactory = null;

        private InsertionPositionFilter positionFilter = null;

        private InsertionRouteFilter routeFilter = null;

        // Independent operator selection
        private final List<WeightedOperator<InsertionOperatorFactory>> insertionOperators = new ArrayList<>();
        private final List<WeightedOperator<RuinOperatorFactory>> ruinOperators = new ArrayList<>();
        private final Set<String> explicitlySetStrategies = new HashSet<>();

        /**
         * Weighted operator holder.
         */
        record WeightedOperator<T>(T factory, double weight, String name) {
            WeightedOperator(T factory, double weight) {
                this(factory, weight, null);
            }
        }

        public static Builder newInstance(VehicleRoutingProblem vrp) {
            return new Builder(vrp);
        }

        private Builder(VehicleRoutingProblem vrp) {
            this.vrp = vrp;
            properties = new Properties(createDefaultProperties());
        }

        private Properties createDefaultProperties() {
            Properties defaults = new Properties();
            defaults.put(Strategy.RADIAL_BEST.toString(), "0.");
            defaults.put(Strategy.RADIAL_REGRET.toString(), ".5");
            defaults.put(Strategy.RADIAL_CHEAPEST.toString(), "0.");

            defaults.put(Strategy.TIME_RELATED_BEST.toString(), "0.");
            defaults.put(Strategy.TIME_RELATED_REGRET.toString(), "0.");
            defaults.put(Strategy.TIME_RELATED_CHEAPEST.toString(), "0.");

            defaults.put(Strategy.RANDOM_BEST.toString(), ".5");
            defaults.put(Strategy.RANDOM_REGRET.toString(), ".5");
            defaults.put(Strategy.RANDOM_CHEAPEST.toString(), "0.");

            defaults.put(Strategy.STRING_BEST.toString(), "0.0");
            defaults.put(Strategy.STRING_REGRET.toString(), "0.0");
            defaults.put(Strategy.STRING_CHEAPEST.toString(), "0.");

            defaults.put(Parameter.STRING_K_MIN.toString(), "1");
            defaults.put(Parameter.STRING_K_MAX.toString(), "6");
            defaults.put(Parameter.STRING_L_MIN.toString(), "10");
            defaults.put(Parameter.STRING_L_MAX.toString(), "30");

            defaults.put(Strategy.WORST_BEST.toString(), "0.");
            defaults.put(Strategy.WORST_REGRET.toString(), "1.");
            defaults.put(Strategy.WORST_CHEAPEST.toString(), "0.");

            defaults.put(Strategy.CLUSTER_BEST.toString(), "0.");
            defaults.put(Strategy.CLUSTER_REGRET.toString(), "1.");
            defaults.put(Strategy.CLUSTER_CHEAPEST.toString(), "0.");

            // Kruskal cluster ruin - disabled by default (use DBSCAN)
            defaults.put(RuinOperator.CLUSTER_KRUSKAL.toString(), "0.");

            defaults.put(Parameter.FIXED_COST_PARAM.toString(), "0.");
            defaults.put(Parameter.VEHICLE_SWITCH.toString(), "true");
            defaults.put(Parameter.ITERATIONS.toString(), "2000");
            defaults.put(Parameter.REGRET_DISTANCE_SCORER.toString(), ".05");
            defaults.put(Parameter.REGRET_TIME_WINDOW_SCORER.toString(), "-.1");
            defaults.put(Parameter.THREADS.toString(), "1");
            int minShare = (int) Math.min(20, Math.max(3, vrp.getJobs().size() * 0.05));
            int maxShare = (int) Math.min(50, Math.max(5, vrp.getJobs().size() * 0.3));
            defaults.put(Parameter.RADIAL_MIN_SHARE.toString(), String.valueOf(minShare));
            defaults.put(Parameter.RADIAL_MAX_SHARE.toString(), String.valueOf(maxShare));
            defaults.put(Parameter.WORST_MIN_SHARE.toString(), String.valueOf(minShare));
            defaults.put(Parameter.WORST_MAX_SHARE.toString(), String.valueOf(maxShare));
            defaults.put(Parameter.CLUSTER_MIN_SHARE.toString(), String.valueOf(minShare));
            defaults.put(Parameter.CLUSTER_MAX_SHARE.toString(), String.valueOf(maxShare));
            int minShare_ = (int) Math.min(70, Math.max(5, vrp.getJobs().size() * 0.5));
            int maxShare_ = (int) Math.min(70, Math.max(5, vrp.getJobs().size() * 0.5));
            defaults.put(Parameter.RANDOM_REGRET_MIN_SHARE.toString(), String.valueOf(minShare_));
            defaults.put(Parameter.RANDOM_REGRET_MAX_SHARE.toString(), String.valueOf(maxShare_));
            defaults.put(Parameter.RANDOM_BEST_MIN_SHARE.toString(), String.valueOf(minShare_));
            defaults.put(Parameter.RANDOM_BEST_MAX_SHARE.toString(), String.valueOf(maxShare_));
            defaults.put(Parameter.THRESHOLD_ALPHA.toString(), String.valueOf(0.15));
            defaults.put(Parameter.THRESHOLD_INI.toString(), String.valueOf(0.03));
            defaults.put(Parameter.INSERTION_NOISE_LEVEL.toString(), String.valueOf(0.15));
            defaults.put(Parameter.INSERTION_NOISE_PROB.toString(), String.valueOf(0.2));
            defaults.put(Parameter.RUIN_WORST_NOISE_LEVEL.toString(), String.valueOf(0.15));
            defaults.put(Parameter.RUIN_WORST_NOISE_PROB.toString(), String.valueOf(0.2));
            defaults.put(Parameter.VEHICLE_SWITCH.toString(), String.valueOf(true));
            defaults.put(Parameter.FAST_REGRET.toString(), String.valueOf(false));
            defaults.put(Parameter.REGRET_K.toString(), "2");
            defaults.put(Parameter.REGRET_K_STRATEGY.toString(), "sum");
            defaults.put(Parameter.SPATIAL_FILTER.toString(), String.valueOf(false));
            defaults.put(Parameter.SPATIAL_FILTER_K.toString(), "5");
            defaults.put(Parameter.SPATIAL_FILTER_LEARNING_ROUNDS.toString(), "50");
            defaults.put(Parameter.BREAK_SCHEDULING.toString(), String.valueOf(true));
            defaults.put(Parameter.CONSTRUCTION.toString(), Construction.REGRET_INSERTION.toString());

            defaults.put(Parameter.MIN_UNASSIGNED.toString(), String.valueOf(Integer.MAX_VALUE));
            defaults.put(Parameter.PROPORTION_UNASSIGNED.toString(), String.valueOf(1.0));
            return defaults;
        }


        public Builder addSearchStrategy(SearchStrategy searchStrategy, double weight) {
            customStrategies.put(searchStrategy, weight);
            return this;
        }

        /**
         * Adds a custom search strategy by specifying only the ruin and insertion components.
         * The strategy will be assembled using Jsprit's shared acceptor and objective function.
         *
         * @param name      the strategy name/id
         * @param ruin      the ruin strategy
         * @param insertion the insertion strategy
         * @param weight    the probability weight for this strategy
         * @return builder for chaining
         */
        public Builder addSearchStrategy(String name, RuinStrategy ruin, InsertionStrategy insertion, double weight) {
            strategyComponents.add(new StrategyComponents(name, ruin, insertion, weight));
            return this;
        }

        public Builder setVehicleFleetManager(VehicleFleetManager fleetManager) {
            this.fleetManager = fleetManager;
            return this;
        }

        public Builder setExecutorService(ExecutorService es, int noThreads) {
            this.es = es;
            this.noThreads = noThreads;
            return this;
        }

        public Builder setCustomAcceptor(SolutionAcceptor acceptor){
            this.solutionAcceptor = acceptor;
            return this;
        }

        public Builder setRandom(Random random) {
            this.random = random;
            return this;
        }

        public Builder setProperty(String key, String value) {
            properties.put(key, value);
            return this;
        }

        public Builder setProperty(Parameter parameter, String value) {
            setProperty(parameter.toString(), value);
            return this;
        }

        public Builder setProperty(Strategy strategy, String value) {
            explicitlySetStrategies.add(strategy.toString());
            setProperty(strategy.toString(), value);
            return this;
        }

        public Builder setProperty(RuinOperator ruinOperator, String value) {
            setProperty(ruinOperator.toString(), value);
            return this;
        }

        public Builder setProperty(InsertionOperator insertionOperator, String value) {
            setProperty(insertionOperator.toString(), value);
            return this;
        }

        public Builder setStateAndConstraintManager(StateManager stateManager, ConstraintManager constraintManager) {
            this.stateManager = stateManager;
            this.constraintManager = constraintManager;
            return this;
        }

        public Builder setObjectiveFunction(SolutionCostCalculator objectiveFunction) {
            this.objectiveFunction = objectiveFunction;
            return this;
        }

        public Builder addCoreStateAndConstraintStuff(boolean addConstraints) {
            this.addConstraints = addConstraints;
            return this;
        }

        public Builder setActivityInsertionCalculator(ActivityInsertionCostsCalculator activityInsertionCalculator) {
            this.activityInsertionCalculator = activityInsertionCalculator;
            return this;
        }

        public Builder setRegretScorer(ScoringFunction scoringFunction) {
            this.regretScorer = scoringFunction;
            return this;
        }

        public Builder setRegretScoringFunction(RegretScoringFunction scoringFunction) {
            this.regretScoringFunction = scoringFunction;
            return this;
        }

        public Builder setJobFilter(JobFilter jobFilter) {
            this.jobFilter = jobFilter;
            return this;
        }

        /**
         * Set a custom service insertion calculator factory
         */
        public Builder setServiceInsertionCalculatorFactory(JobInsertionCostsCalculatorFactory factory) {
            this.serviceCalculatorFactory = factory;
            return this;
        }

        /**
         * Set a custom shipment insertion calculator factory
         */
        public Builder setShipmentInsertionCalculatorFactory(JobInsertionCostsCalculatorFactory factory) {
            this.shipmentCalculatorFactory = factory;
            return this;
        }

        /**
         * Sets the position filter for reducing position evaluations in shipment insertion.
         * <p>
         * Position filtering selects a subset of candidate positions to evaluate
         * for shipment pickup and delivery, reducing the O(p²) complexity.
         * <p>
         * Example:
         * <pre>
         * SpatialPositionFilter filter = new SpatialPositionFilter(vrp.getTransportCosts(), 5);
         * Jsprit.Builder.newInstance(vrp)
         *     .setPositionFilter(filter)
         *     .buildAlgorithm();
         * </pre>
         *
         * @param filter the position filter, or null to disable filtering
         * @return this builder
         * @see SpatialPositionFilter
         */
        public Builder setPositionFilter(InsertionPositionFilter filter) {
            this.positionFilter = filter;
            return this;
        }

        /**
         * Sets the route filter for reducing route evaluations in regret insertion.
         * <p>
         * Route filtering selects a subset of routes to evaluate for each job,
         * reducing complexity from O(R) to O(k) where R is the number of routes.
         * <p>
         * Example with AdaptiveSpatialFilter (includes learning phase):
         * <pre>
         * // 50 learning iterations, then filter to 10 nearest routes
         * AdaptiveSpatialFilter filter = new AdaptiveSpatialFilter(50, 10);
         * Jsprit.Builder.newInstance(vrp)
         *     .setRouteFilter(filter)
         *     .buildAlgorithm();
         * </pre>
         *
         * @param filter the route filter, or null to disable filtering
         * @return this builder
         * @see InsertionRouteFilter
         * @see AdaptiveSpatialFilter
         */
        public Builder setRouteFilter(InsertionRouteFilter filter) {
            this.routeFilter = filter;
            return this;
        }

        /**
         * Adds an insertion operator with the specified weight.
         *
         * <p>When insertion operators are registered, they are selected independently
         * of ruin operators during each iteration. This allows fine-grained control
         * over the search strategy composition.</p>
         *
         * <p>Example:</p>
         * <pre>
         * Jsprit.Builder.newInstance(vrp)
         *     .addInsertionOperator(0.7, Insertion.regretFast())  // Fast, filtered
         *     .addInsertionOperator(0.3, Insertion.regret())       // Thorough, no filter
         *     .buildAlgorithm();
         * </pre>
         *
         * @param weight  the selection weight (higher = more likely to be selected)
         * @param factory the insertion operator factory
         * @return this builder for chaining
         * @see Insertion
         */
        public Builder addInsertionOperator(double weight, InsertionOperatorFactory factory) {
            insertionOperators.add(new WeightedOperator<>(factory, weight, factory.getName()));
            return this;
        }

        /**
         * Adds an insertion operator with a name for identification.
         *
         * @param weight  the selection weight
         * @param factory the insertion operator factory
         * @param name    the operator name (for logging/debugging)
         * @return this builder for chaining
         */
        public Builder addInsertionOperator(double weight, InsertionOperatorFactory factory, String name) {
            insertionOperators.add(new WeightedOperator<>(factory, weight, name));
            return this;
        }

        /**
         * Adds a ruin operator with the specified weight.
         *
         * <p>When ruin operators are registered, they are selected independently
         * of insertion operators during each iteration.</p>
         *
         * <p>Example:</p>
         * <pre>
         * Jsprit.Builder.newInstance(vrp)
         *     .addRuinOperator(0.3, Ruin.random(0.3))
         *     .addRuinOperator(0.3, Ruin.radial(0.3))
         *     .addRuinOperator(0.2, Ruin.cluster())
         *     .addRuinOperator(0.2, Ruin.kruskalCluster())
         *     .buildAlgorithm();
         * </pre>
         *
         * @param weight  the selection weight (higher = more likely to be selected)
         * @param factory the ruin operator factory
         * @return this builder for chaining
         * @see Ruin
         */
        public Builder addRuinOperator(double weight, RuinOperatorFactory factory) {
            ruinOperators.add(new WeightedOperator<>(factory, weight, factory.getName()));
            return this;
        }

        /**
         * Adds a ruin operator with a name for identification.
         *
         * @param weight  the selection weight
         * @param factory the ruin operator factory
         * @param name    the operator name (for logging/debugging)
         * @return this builder for chaining
         */
        public Builder addRuinOperator(double weight, RuinOperatorFactory factory, String name) {
            ruinOperators.add(new WeightedOperator<>(factory, weight, name));
            return this;
        }

        /**
         * Returns true if independent operator selection mode is enabled.
         * This is the case when either insertion or ruin operators have been registered.
         */
        public boolean isIndependentOperatorMode() {
            return !insertionOperators.isEmpty() || !ruinOperators.isEmpty();
        }

        public VehicleRoutingAlgorithm buildAlgorithm() {
            // Validate: cannot mix independent operators with explicit coupled strategy weights
            if (isIndependentOperatorMode() && !explicitlySetStrategies.isEmpty()) {
                throw new IllegalStateException(
                    "Cannot mix independent operator selection (addInsertionOperator/addRuinOperator) " +
                    "with coupled strategy weights (setProperty(Strategy.*)). " +
                    "Explicitly set strategies: " + explicitlySetStrategies + ". " +
                    "Use one approach or the other, not both."
                );
            }
            // Validate: independent mode requires both insertion and ruin operators
            if (!insertionOperators.isEmpty() && ruinOperators.isEmpty()) {
                throw new IllegalStateException(
                    "Independent mode requires at least one ruin operator. " +
                    "Add ruin operators with addRuinOperator(), e.g.: " +
                    ".addRuinOperator(0.5, Ruin.radial(0.3))"
                );
            }
            if (!ruinOperators.isEmpty() && insertionOperators.isEmpty()) {
                throw new IllegalStateException(
                    "Independent mode requires at least one insertion operator. " +
                    "Add insertion operators with addInsertionOperator(), e.g.: " +
                    ".addInsertionOperator(0.5, Insertion.regret())"
                );
            }
            return new Jsprit(this).create(vrp);
        }

    }

    /**
     * Holds the components for a custom search strategy that will be assembled
     * with the shared acceptor and objective function.
     */
    record StrategyComponents(String name, RuinStrategy ruin, InsertionStrategy insertion, double weight) {}

    static class RuinShareFactoryImpl implements RuinShareFactory

    {

        private int maxShare;

        private int minShare;

        private Random random = RandomNumberGeneration.getRandom();

        public void setRandom(Random random) {
            this.random = random;
        }

        public RuinShareFactoryImpl(int minShare, int maxShare) {
            if (maxShare < minShare)
                throw new IllegalArgumentException("maxShare must be equal or greater than minShare");
            this.minShare = minShare;
            this.maxShare = maxShare;
        }

        public RuinShareFactoryImpl(int minShare, int maxShare, Random random) {
            if (maxShare < minShare)
                throw new IllegalArgumentException("maxShare must be equal or greater than minShare");
            this.minShare = minShare;
            this.maxShare = maxShare;
            this.random = random;
        }

        @Override
        public int createNumberToBeRemoved() {
            return (int) (minShare + (maxShare - minShare) * random.nextDouble());
        }

    }

    private StateManager stateManager;

    private ConstraintManager constraintManager;

    private ExecutorService es;

    private Integer noThreads;

    private boolean setupExecutorInternally = false;

    private boolean addCoreConstraints;

    private SolutionCostCalculator objectiveFunction;

    private Properties properties;

    private Random random;

    private SolutionAcceptor acceptor;

    private ScoringFunction regretScorer;

    private RegretScoringFunction regretScoringFunction;

    private RegretKScoringFunction regretKScoringFunction;

    private int regretK;

    private final Map<SearchStrategy, Double> customStrategies = new HashMap<>();

    private final List<StrategyComponents> strategyComponents = new ArrayList<>();

    private final JobInsertionCostsCalculatorFactory serviceCalculatorFactory;

    private final JobInsertionCostsCalculatorFactory shipmentCalculatorFactory;

    private final InsertionPositionFilter positionFilter;

    private final InsertionRouteFilter routeFilter;

    private VehicleFleetManager vehicleFleetManager;

    // Independent operator selection
    private final List<Builder.WeightedOperator<InsertionOperatorFactory>> insertionOperators;
    private final List<Builder.WeightedOperator<RuinOperatorFactory>> ruinOperators;

    private Jsprit(Builder builder) {
        this.stateManager = builder.stateManager;
        this.constraintManager = builder.constraintManager;
        this.es = builder.es;
        this.noThreads = builder.noThreads;
        this.addCoreConstraints = builder.addConstraints;
        this.properties = builder.properties;
        this.objectiveFunction = builder.objectiveFunction;
        this.random = builder.random;
        this.activityInsertion = builder.activityInsertionCalculator;
        this.acceptor = builder.solutionAcceptor;
        this.jobFilter = builder.jobFilter;
        this.shipmentCalculatorFactory = builder.shipmentCalculatorFactory;
        this.serviceCalculatorFactory = builder.serviceCalculatorFactory;
        this.positionFilter = builder.positionFilter;
        this.routeFilter = builder.routeFilter;
        regretScorer = builder.regretScorer;
        regretScoringFunction = builder.regretScoringFunction;
        customStrategies.putAll(builder.customStrategies);
        strategyComponents.addAll(builder.strategyComponents);
        vehicleFleetManager = builder.fleetManager;
        this.insertionOperators = new ArrayList<>(builder.insertionOperators);
        this.ruinOperators = new ArrayList<>(builder.ruinOperators);
    }

    private void ini(VehicleRoutingProblem vrp) {
        if (regretScorer == null) {
            regretScorer = getRegretScorer(vrp);
        }
        if (regretScoringFunction == null) {
            regretScoringFunction = new DefaultRegretScoringFunction(regretScorer);
        }

        // Parse regret-k configuration
        String kStr = getProperty(Parameter.REGRET_K.toString());
        if (kStr != null && !kStr.isEmpty()) {
            if (kStr.equalsIgnoreCase("all") || kStr.equals("-1")) {
                regretK = -1; // -1 means all
            } else {
                regretK = Integer.parseInt(kStr);
            }
        } else {
            regretK = 2; // default
        }

        // Create regret-k scoring function
        String strategyStr = getProperty(Parameter.REGRET_K_STRATEGY.toString());
        regretKScoringFunction = RegretKScoringFunctionFactory.create(strategyStr, regretK, regretScorer);
    }

    private VehicleRoutingAlgorithm create(final VehicleRoutingProblem vrp) {
        ini(vrp);

        final JobInsertionCostsCalculatorFactory shipmentFactory = this.shipmentCalculatorFactory;

        boolean isInfinite = vrp.getFleetSize().equals(VehicleRoutingProblem.FleetSize.INFINITE);
        if (vehicleFleetManager == null) {
            if (isInfinite) {
                vehicleFleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager(vrp);
            } else {
                FiniteFleetManagerFactory finiteFleetManagerFactory = new FiniteFleetManagerFactory(vrp.getVehicles());
                vehicleFleetManager = finiteFleetManagerFactory.createFleetManager(vrp);
            }
        }

        if (stateManager == null) {
            stateManager = new StateManager(vrp);
        }
        if (constraintManager == null) {
            constraintManager = new ConstraintManager(vrp, stateManager);
        }

        if (noThreads == null) {
            noThreads = toInteger(getProperty(Parameter.THREADS.toString()));
        }
        if (noThreads > 1) {
            if (es == null) {
                setupExecutorInternally = true;
                es = Executors.newFixedThreadPool(noThreads);
            }
        }

        double fixedCostParam = toDouble(getProperty(Parameter.FIXED_COST_PARAM.toString()));
        IncreasingAbsoluteFixedCosts increasingAbsoluteFixedCosts = null;
        if (fixedCostParam > 0d) {
            increasingAbsoluteFixedCosts = new IncreasingAbsoluteFixedCosts(vrp.getJobs().size());
            increasingAbsoluteFixedCosts.setWeightOfFixCost(fixedCostParam);
            constraintManager.addConstraint(increasingAbsoluteFixedCosts);
        }

        double noiseLevel = toDouble(getProperty(Parameter.INSERTION_NOISE_LEVEL.toString()));
        double noiseProbability = toDouble(getProperty(Parameter.INSERTION_NOISE_PROB.toString()));

        JobNeighborhoods jobNeighborhoods = new JobNeighborhoodsFactory().createNeighborhoods(vrp, new AvgServiceAndShipmentDistance(vrp.getTransportCosts()), (int) (vrp.getJobs().values().size() * 0.5));
        jobNeighborhoods.initialise();

        final double maxCosts;
        if (properties.containsKey(Parameter.MAX_TRANSPORT_COSTS.toString())) {
            maxCosts = Double.parseDouble(getProperty(Parameter.MAX_TRANSPORT_COSTS.toString()));
        } else {
            maxCosts = jobNeighborhoods.getMaxDistance();
        }

        IterationStartsListener noiseConfigurator = null;
        if (noiseProbability > 0) {
            if (noThreads > 1) {
                ConcurrentInsertionNoiseMaker noiseMaker = new ConcurrentInsertionNoiseMaker(vrp, maxCosts, noiseLevel, noiseProbability);
                noiseMaker.setRandom(random);
                constraintManager.addConstraint(noiseMaker);
                noiseConfigurator = noiseMaker;
            } else {
                InsertionNoiseMaker noiseMaker = new InsertionNoiseMaker(vrp, maxCosts, noiseLevel, noiseProbability);
                noiseMaker.setRandom(random);
                constraintManager.addConstraint(noiseMaker);
                noiseConfigurator = noiseMaker;
            }
        }

        RuinRadialDynamic radial = new RuinRadialDynamic(vrp, vrp.getJobs().size());
//        RuinRadial radial = new RuinRadial(vrp, vrp.getJobs().size(), jobNeighborhoods);
        radial.setRandom(random);
        radial.setJobFilter(jobFilter);
        RuinShareFactoryImpl radialRuinFactory = new RuinShareFactoryImpl(
            toInteger(properties.getProperty(Parameter.RADIAL_MIN_SHARE.toString())),
            toInteger(properties.getProperty(Parameter.RADIAL_MAX_SHARE.toString())),
            random);
        radial.setRuinShareFactory(radialRuinFactory);

        final RuinRandom randomForRegret = new RuinRandom(vrp, 0.5);
        randomForRegret.setJobFilter(jobFilter);
        randomForRegret.setRandom(random);
        randomForRegret.setRuinShareFactory(new RuinShareFactoryImpl(
            toInteger(properties.getProperty(Parameter.RANDOM_REGRET_MIN_SHARE.toString())),
            toInteger(properties.getProperty(Parameter.RANDOM_REGRET_MAX_SHARE.toString())),
            random)
        );

        final RuinRandom randomForBest = new RuinRandom(vrp, 0.5);
        randomForBest.setRandom(random);
        randomForBest.setJobFilter(jobFilter);
        randomForBest.setRuinShareFactory(new RuinShareFactoryImpl(
                toInteger(properties.getProperty(Parameter.RANDOM_BEST_MIN_SHARE.toString())),
                toInteger(properties.getProperty(Parameter.RANDOM_BEST_MAX_SHARE.toString())),
            random)
        );

        final RuinWorst worst = new RuinWorst(vrp, (int) (vrp.getJobs().values().size() * 0.5));
        worst.setRandom(random);
        worst.setJobFilter(jobFilter);
        worst.setRuinShareFactory(new RuinShareFactoryImpl(
            toInteger(properties.getProperty(Parameter.WORST_MIN_SHARE.toString())),
            toInteger(properties.getProperty(Parameter.WORST_MAX_SHARE.toString())),
            random)
        );
        double ruinWorstNoiseProb = toDouble(getProperty(Parameter.RUIN_WORST_NOISE_PROB.toString()));
        double ruinWorstNoiseLevel = toDouble(getProperty(Parameter.RUIN_WORST_NOISE_LEVEL.toString()));
        IterationStartsListener noise = (i, problem, solutions) -> worst.setNoiseMaker(() -> {
            if (random.nextDouble() < ruinWorstNoiseProb) {
                return ruinWorstNoiseLevel * maxCosts * random.nextDouble();
            } else return 0.;
        });

        final RuinClusters clusters = new RuinClusters(vrp, (int) (vrp.getJobs().values().size() * 0.5), jobNeighborhoods);
        clusters.setRandom(random);
        clusters.setJobFilter(jobFilter);
        clusters.setRuinShareFactory(new RuinShareFactoryImpl(
                toInteger(properties.getProperty(Parameter.CLUSTER_MIN_SHARE.toString())),
                toInteger(properties.getProperty(Parameter.CLUSTER_MAX_SHARE.toString())),
                random)
        );

        // Kruskal MST-based cluster ruin (ranked #2 in Voigt 2025)
        final RuinKruskalClusters kruskalClusters = new RuinKruskalClusters(vrp, (int) (vrp.getJobs().values().size() * 0.5), jobNeighborhoods);
        kruskalClusters.setRandom(random);
        kruskalClusters.setJobFilter(jobFilter);
        kruskalClusters.setRuinShareFactory(new RuinShareFactoryImpl(
                toInteger(properties.getProperty(Parameter.CLUSTER_MIN_SHARE.toString())),
                toInteger(properties.getProperty(Parameter.CLUSTER_MAX_SHARE.toString())),
                random)
        );

        int kMin = toInteger(properties.getProperty(Parameter.STRING_K_MIN.toString()));
        int kMax = toInteger(properties.getProperty(Parameter.STRING_K_MAX.toString()));
        int lMin = toInteger(properties.getProperty(Parameter.STRING_L_MIN.toString()));
        int lMax = toInteger(properties.getProperty(Parameter.STRING_L_MAX.toString()));

        final RuinString stringRuin = new RuinString(vrp, jobNeighborhoods);
        stringRuin.setNoRoutes(kMin, kMax);
        stringRuin.setStringLength(lMin, lMax);
        stringRuin.setRandom(random);
        stringRuin.setJobFilter(jobFilter);

        final RuinTimeRelated ruinTimeRelated = new RuinTimeRelated(vrp);
        ruinTimeRelated.setRuinShareFactory(radialRuinFactory);
        ruinTimeRelated.setRandom(random);
        ruinTimeRelated.setJobFilter(jobFilter);

        AbstractInsertionStrategy regret;

        boolean fastRegret = Boolean.parseBoolean(getProperty(Parameter.FAST_REGRET.toString()));

        // Determine route filter: user-provided or create from properties
        InsertionRouteFilter effectiveRouteFilter = this.routeFilter;
        if (effectiveRouteFilter == null && fastRegret && toBoolean(getProperty(Parameter.SPATIAL_FILTER.toString()))) {
            int spatialFilterK = toInteger(getProperty(Parameter.SPATIAL_FILTER_K.toString()));
            // Note: SPATIAL_FILTER_LEARNING_ROUNDS is deprecated - use RouteFilterTuner listener for adaptive behavior
            effectiveRouteFilter = new AdaptiveSpatialFilter(spatialFilterK);
        }

        if (es != null) {
            if (fastRegret) {
                RegretInsertionConcurrentFast regretInsertion = (RegretInsertionConcurrentFast) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionStrategyBuilder.Strategy.REGRET)
                    .setConcurrentMode(es, noThreads)
                    .setFastRegret(true)
                    .considerFixedCosts(toDouble(getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .setActivityInsertionCostCalculator(activityInsertion)
                        .setServiceInsertionCalculator(this.serviceCalculatorFactory)
                        .setShipmentInsertionCalculatorFactory(shipmentFactory)
                        .setPositionFilter(positionFilter)
                    .build();
                regretInsertion.setRegretScoringFunction(regretScoringFunction);
                regretInsertion.setRegretKScoringFunction(regretKScoringFunction);
                regretInsertion.setRegretK(regretK);
                regretInsertion.setDependencyTypes(constraintManager.getDependencyTypes());
                regretInsertion.setRouteFilter(effectiveRouteFilter);
                regret = regretInsertion;
            }
            else {
                RegretInsertionConcurrent regretInsertion = (RegretInsertionConcurrent) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionStrategyBuilder.Strategy.REGRET)
                    .setConcurrentMode(es, noThreads)
                    .considerFixedCosts(toDouble(getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .setActivityInsertionCostCalculator(activityInsertion)
                        .setServiceInsertionCalculator(this.serviceCalculatorFactory)
                        .setShipmentInsertionCalculatorFactory(shipmentFactory)
                        .setPositionFilter(positionFilter)
                    .build();
                regretInsertion.setRegretScoringFunction(regretScoringFunction);
                regretInsertion.setRegretKScoringFunction(regretKScoringFunction);
                regretInsertion.setRegretK(regretK);
                regret = regretInsertion;
            }
        } else {
            if(fastRegret) {
                RegretInsertionFast regretInsertion = (RegretInsertionFast) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionStrategyBuilder.Strategy.REGRET)
                    .setFastRegret(true)
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .considerFixedCosts(toDouble(getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setActivityInsertionCostCalculator(activityInsertion)
                        .setServiceInsertionCalculator(this.serviceCalculatorFactory)
                        .setShipmentInsertionCalculatorFactory(shipmentFactory)
                        .setPositionFilter(positionFilter)
                    .build();
                regretInsertion.setRegretScoringFunction(regretScoringFunction);
                regretInsertion.setRegretKScoringFunction(regretKScoringFunction);
                regretInsertion.setRegretK(regretK);
                regretInsertion.setDependencyTypes(constraintManager.getDependencyTypes());
                regretInsertion.setRouteFilter(effectiveRouteFilter);
                regret = regretInsertion;
            }
            else{
                RegretInsertion regretInsertion = (RegretInsertion) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionStrategyBuilder.Strategy.REGRET)
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .considerFixedCosts(toDouble(getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setActivityInsertionCostCalculator(activityInsertion)
                        .setServiceInsertionCalculator(this.serviceCalculatorFactory)
                        .setShipmentInsertionCalculatorFactory(shipmentFactory)
                        .build();
                regretInsertion.setRegretScoringFunction(regretScoringFunction);
                regretInsertion.setRegretKScoringFunction(regretKScoringFunction);
                regretInsertion.setRegretK(regretK);
                regret = regretInsertion;
            }
        }
        regret.setRandom(random);

        AbstractInsertionStrategy best;
        if ((vrp.getVehicles().size() == 1 && !isInfinite) || vrp.getJobs().size() < 100 || es == null) {
            BestInsertion bestInsertion = (BestInsertion) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                .setInsertionStrategy(InsertionStrategyBuilder.Strategy.BEST)
                .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
                .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                .setActivityInsertionCostCalculator(activityInsertion)
                    .setServiceInsertionCalculator(this.serviceCalculatorFactory)
                    .setShipmentInsertionCalculatorFactory(shipmentFactory)
                    .setPositionFilter(positionFilter)
                .build();
            best = bestInsertion;
        } else {
            BestInsertionConcurrent bestInsertion = (BestInsertionConcurrent) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                .setInsertionStrategy(InsertionStrategyBuilder.Strategy.BEST)
                .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
                .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                .setConcurrentMode(es, noThreads)
                .setActivityInsertionCostCalculator(activityInsertion)
                    .setServiceInsertionCalculator(this.serviceCalculatorFactory)
                    .setShipmentInsertionCalculatorFactory(shipmentFactory)
                    .setPositionFilter(positionFilter)
                .build();
            best = bestInsertion;
        }
        best.setRandom(random);

        // True cheapest insertion (best insertion as defined in VRP literature)
        AbstractInsertionStrategy cheapest;
        if (es == null) {
            cheapest = (CheapestInsertion) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionStrategyBuilder.Strategy.CHEAPEST)
                    .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .setActivityInsertionCostCalculator(activityInsertion)
                    .setServiceInsertionCalculator(this.serviceCalculatorFactory)
                    .setShipmentInsertionCalculatorFactory(shipmentFactory)
                    .setPositionFilter(positionFilter)
                    .build();
        } else {
            cheapest = (CheapestInsertionConcurrent) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionStrategyBuilder.Strategy.CHEAPEST)
                    .setConcurrentMode(es, noThreads)
                    .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .setActivityInsertionCostCalculator(activityInsertion)
                    .setServiceInsertionCalculator(this.serviceCalculatorFactory)
                    .setShipmentInsertionCalculatorFactory(shipmentFactory)
                    .setPositionFilter(positionFilter)
                    .build();
        }
        cheapest.setRandom(random);

        IterationStartsListener schrimpfThreshold = null;
        if(acceptor == null) {
            final SchrimpfAcceptance schrimpfAcceptance = new SchrimpfAcceptance(1, toDouble(getProperty(Parameter.THRESHOLD_ALPHA.toString())));
            if (properties.containsKey(Parameter.THRESHOLD_INI_ABS.toString())) {
                schrimpfAcceptance.setInitialThreshold(Double.valueOf(properties.getProperty(Parameter.THRESHOLD_INI_ABS.toString())));
            } else {
                schrimpfThreshold = (i, problem, solutions) -> {
                    if (i == 1) {
                        double initialThreshold = Solutions.bestOf(solutions).getCost() * toDouble(getProperty(Parameter.THRESHOLD_INI.toString()));
                        schrimpfAcceptance.setInitialThreshold(initialThreshold);
                    }
                };
            }
            acceptor = schrimpfAcceptance;
        }

        SolutionCostCalculator objectiveFunction = getObjectiveFunction(vrp, maxCosts);
        SearchStrategy radialRegret = new SearchStrategy(Strategy.RADIAL_REGRET.toString(), new SelectBest(), acceptor, objectiveFunction);
        radialRegret.addModule(configureModule(new RuinAndRecreateModule(Strategy.RADIAL_REGRET.toString(), regret, radial)));

        SearchStrategy radialBest = new SearchStrategy(Strategy.RADIAL_BEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        radialBest.addModule(configureModule(new RuinAndRecreateModule(Strategy.RADIAL_BEST.toString(), best, radial)));

        SearchStrategy timeRelatedRegret = new SearchStrategy(Strategy.TIME_RELATED_REGRET.toString(), new SelectBest(), acceptor, objectiveFunction);
        timeRelatedRegret.addModule(configureModule(new RuinAndRecreateModule(Strategy.TIME_RELATED_REGRET.toString(), regret, ruinTimeRelated)));

        SearchStrategy timeRelatedBest = new SearchStrategy(Strategy.TIME_RELATED_BEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        timeRelatedBest.addModule(configureModule(new RuinAndRecreateModule(Strategy.TIME_RELATED_BEST.toString(), best, ruinTimeRelated)));

        SearchStrategy randomBest = new SearchStrategy(Strategy.RANDOM_BEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        randomBest.addModule(configureModule(new RuinAndRecreateModule(Strategy.RANDOM_BEST.toString(), best, randomForBest)));

        SearchStrategy randomRegret = new SearchStrategy(Strategy.RANDOM_REGRET.toString(), new SelectBest(), acceptor, objectiveFunction);
        randomRegret.addModule(configureModule(new RuinAndRecreateModule(Strategy.RANDOM_REGRET.toString(), regret, randomForRegret)));

        SearchStrategy worstRegret = new SearchStrategy(Strategy.WORST_REGRET.toString(), new SelectBest(), acceptor, objectiveFunction);
        worstRegret.addModule(configureModule(new RuinAndRecreateModule(Strategy.WORST_REGRET.toString(), regret, worst)));

        SearchStrategy worstBest = new SearchStrategy(Strategy.WORST_BEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        worstBest.addModule(configureModule(new RuinAndRecreateModule(Strategy.WORST_BEST.toString(), best, worst)));

        final SearchStrategy clustersRegret = new SearchStrategy(Strategy.CLUSTER_REGRET.toString(), new SelectBest(), acceptor, objectiveFunction);
        clustersRegret.addModule(configureModule(new RuinAndRecreateModule(Strategy.CLUSTER_REGRET.toString(), regret, clusters)));

        final SearchStrategy clustersBest = new SearchStrategy(Strategy.CLUSTER_BEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        clustersBest.addModule(configureModule(new RuinAndRecreateModule(Strategy.CLUSTER_BEST.toString(), best, clusters)));

        // Kruskal cluster strategies
        final SearchStrategy kruskalClustersRegret = new SearchStrategy("cluster_kruskal_regret", new SelectBest(), acceptor, objectiveFunction);
        kruskalClustersRegret.addModule(configureModule(new RuinAndRecreateModule("cluster_kruskal_regret", regret, kruskalClusters)));

        final SearchStrategy kruskalClustersBest = new SearchStrategy("cluster_kruskal_best", new SelectBest(), acceptor, objectiveFunction);
        kruskalClustersBest.addModule(configureModule(new RuinAndRecreateModule("cluster_kruskal_best", best, kruskalClusters)));

        SearchStrategy stringRegret = new SearchStrategy(Strategy.STRING_REGRET.toString(), new SelectBest(), acceptor, objectiveFunction);
        stringRegret.addModule(configureModule(new RuinAndRecreateModule(Strategy.STRING_REGRET.toString(), regret, stringRuin)));

        SearchStrategy stringBest = new SearchStrategy(Strategy.STRING_BEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        stringBest.addModule(configureModule(new RuinAndRecreateModule(Strategy.STRING_BEST.toString(), best, stringRuin)));

        // Cheapest insertion strategies (true best insertion from VRP literature)
        SearchStrategy radialCheapest = new SearchStrategy(Strategy.RADIAL_CHEAPEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        radialCheapest.addModule(configureModule(new RuinAndRecreateModule(Strategy.RADIAL_CHEAPEST.toString(), cheapest, radial)));

        SearchStrategy randomCheapest = new SearchStrategy(Strategy.RANDOM_CHEAPEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        randomCheapest.addModule(configureModule(new RuinAndRecreateModule(Strategy.RANDOM_CHEAPEST.toString(), cheapest, randomForBest)));

        SearchStrategy worstCheapest = new SearchStrategy(Strategy.WORST_CHEAPEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        worstCheapest.addModule(configureModule(new RuinAndRecreateModule(Strategy.WORST_CHEAPEST.toString(), cheapest, worst)));

        SearchStrategy clustersCheapest = new SearchStrategy(Strategy.CLUSTER_CHEAPEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        clustersCheapest.addModule(configureModule(new RuinAndRecreateModule(Strategy.CLUSTER_CHEAPEST.toString(), cheapest, clusters)));

        SearchStrategy stringCheapest = new SearchStrategy(Strategy.STRING_CHEAPEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        stringCheapest.addModule(configureModule(new RuinAndRecreateModule(Strategy.STRING_CHEAPEST.toString(), cheapest, stringRuin)));

        SearchStrategy timeRelatedCheapest = new SearchStrategy(Strategy.TIME_RELATED_CHEAPEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        timeRelatedCheapest.addModule(configureModule(new RuinAndRecreateModule(Strategy.TIME_RELATED_CHEAPEST.toString(), cheapest, ruinTimeRelated)));

        PrettyAlgorithmBuilder prettyBuilder = PrettyAlgorithmBuilder.newInstance(vrp, vehicleFleetManager, stateManager, constraintManager);
        prettyBuilder.setRandom(random);
        if (addCoreConstraints) {
            prettyBuilder.addCoreStateAndConstraintStuff();
        }

        // Check if using independent operator selection mode
        boolean independentMode = !insertionOperators.isEmpty() || !ruinOperators.isEmpty();

        // Add default coupled strategies only when NOT in independent mode
        if (!independentMode) {
            prettyBuilder.withStrategy(radialRegret, toDouble(getProperty(Strategy.RADIAL_REGRET.toString())))
                .withStrategy(radialBest, toDouble(getProperty(Strategy.RADIAL_BEST.toString())))
                    .withStrategy(radialCheapest, toDouble(getProperty(Strategy.RADIAL_CHEAPEST.toString())))

                .withStrategy(timeRelatedBest, toDouble(getProperty(Strategy.TIME_RELATED_BEST.toString())))
                .withStrategy(timeRelatedRegret, toDouble(getProperty(Strategy.TIME_RELATED_REGRET.toString())))
                    .withStrategy(timeRelatedCheapest, toDouble(getProperty(Strategy.TIME_RELATED_CHEAPEST.toString())))

                .withStrategy(randomBest, toDouble(getProperty(Strategy.RANDOM_BEST.toString())))
                .withStrategy(randomRegret, toDouble(getProperty(Strategy.RANDOM_REGRET.toString())))
                    .withStrategy(randomCheapest, toDouble(getProperty(Strategy.RANDOM_CHEAPEST.toString())))

                .withStrategy(worstBest, toDouble(getProperty(Strategy.WORST_BEST.toString())))
                .withStrategy(worstRegret, toDouble(getProperty(Strategy.WORST_REGRET.toString())))
                    .withStrategy(worstCheapest, toDouble(getProperty(Strategy.WORST_CHEAPEST.toString())))

                .withStrategy(clustersRegret, toDouble(getProperty(Strategy.CLUSTER_REGRET.toString())))
                .withStrategy(clustersBest, toDouble(getProperty(Strategy.CLUSTER_BEST.toString())))
                    .withStrategy(clustersCheapest, toDouble(getProperty(Strategy.CLUSTER_CHEAPEST.toString())));

            // Add Kruskal cluster strategies (weight split: 70% regret, 30% best)
            double kruskalWeight = toDouble(getProperty(RuinOperator.CLUSTER_KRUSKAL.toString()));
            if (kruskalWeight > 0) {
                prettyBuilder.withStrategy(kruskalClustersRegret, kruskalWeight * 0.7)
                        .withStrategy(kruskalClustersBest, kruskalWeight * 0.3);
            }

            prettyBuilder.withStrategy(stringBest, toDouble(getProperty(Strategy.STRING_BEST.toString())))
                    .withStrategy(stringRegret, toDouble(getProperty(Strategy.STRING_REGRET.toString())))
                    .withStrategy(stringCheapest, toDouble(getProperty(Strategy.STRING_CHEAPEST.toString())));
        }

        // Custom strategies are always added (both modes)
        for (SearchStrategy customStrategy : customStrategies.keySet()) {
            prettyBuilder.withStrategy(customStrategy, customStrategies.get(customStrategy));
        }

        // Strategy components are always added (both modes)
        for (StrategyComponents sc : strategyComponents) {
            SearchStrategy strategy = new SearchStrategy(sc.name(), new SelectBest(), acceptor, objectiveFunction);
            strategy.addModule(configureModule(new RuinAndRecreateModule(sc.name(), sc.insertion(), sc.ruin())));
            prettyBuilder.withStrategy(strategy, sc.weight());
        }

        // Independent operator selection mode
        if (independentMode) {
            // Create context for operator factories
            InsertionOperatorFactory.Context insertionContext = new InsertionOperatorFactory.Context(
                vrp, vehicleFleetManager, stateManager, constraintManager,
                activityInsertion, regretScorer,
                random, es, noThreads != null ? noThreads : 1
            );

            RuinOperatorFactory.Context ruinContext = new RuinOperatorFactory.Context(
                vrp, stateManager, jobNeighborhoods, maxCosts, random
            );

            // Build insertion selector
            WeightedOperatorSelector<InsertionStrategy> insertionSelector = new WeightedOperatorSelector<>(random);
            if (!insertionOperators.isEmpty()) {
                for (Builder.WeightedOperator<InsertionOperatorFactory> op : insertionOperators) {
                    InsertionStrategy insertionStrat = op.factory().create(insertionContext);
                    insertionSelector.add(insertionStrat, op.weight(), op.name());
                }
            } else {
                // Default to regret insertion if no custom insertions specified
                insertionSelector.add(regret, 1.0, "regret");
            }

            // Build ruin selector
            WeightedOperatorSelector<RuinStrategy> ruinSelector = new WeightedOperatorSelector<>(random);
            if (!ruinOperators.isEmpty()) {
                for (Builder.WeightedOperator<RuinOperatorFactory> op : ruinOperators) {
                    RuinStrategy ruinStrategy = op.factory().create(ruinContext);
                    ruinSelector.add(ruinStrategy, op.weight(), op.name());
                }
            } else {
                // Default to random ruin if no custom ruins specified
                ruinSelector.add(randomForRegret, 1.0, "random");
            }

            // Create and configure the independent module
            IndependentRuinAndRecreateModule independentModule = new IndependentRuinAndRecreateModule(
                "independent", insertionSelector, ruinSelector
            );
            independentModule.setRandom(random);
            independentModule.setMinUnassignedJobsToBeReinserted(
                Integer.valueOf(properties.getProperty(Parameter.MIN_UNASSIGNED.toString()))
            );
            independentModule.setProportionOfUnassignedJobsToBeReinserted(
                Double.valueOf(properties.getProperty(Parameter.PROPORTION_UNASSIGNED.toString()))
            );

            // Create the independent strategy with high weight
            SearchStrategy independentStrategy = new SearchStrategy(
                "independent", new SelectBest(), acceptor, objectiveFunction
            );
            independentStrategy.addModule(independentModule);
            prettyBuilder.withStrategy(independentStrategy, 1.0);
        }

        String constructionMethod = getProperty(Parameter.CONSTRUCTION.toString());
        if (constructionMethod.equals(Construction.BEST_INSERTION.toString())) {
            prettyBuilder.constructInitialSolutionWith(best, objectiveFunction);
        } else if (constructionMethod.equals(Construction.CHEAPEST_INSERTION.toString())) {
            prettyBuilder.constructInitialSolutionWith(cheapest, objectiveFunction);
        } else {
            prettyBuilder.constructInitialSolutionWith(regret, objectiveFunction);
        }
        prettyBuilder.withObjectiveFunction(objectiveFunction);


        VehicleRoutingAlgorithm vra = prettyBuilder.build();
        if (schrimpfThreshold != null) {
            vra.addListener(schrimpfThreshold);
        }
        if (noiseConfigurator != null) vra.addListener(noiseConfigurator);
        vra.addListener(noise);
        vra.addListener(clusters);
        if (increasingAbsoluteFixedCosts != null) vra.addListener(increasingAbsoluteFixedCosts);

        if(toBoolean(getProperty(Parameter.BREAK_SCHEDULING.toString()))) {
            vra.addListener(new BreakScheduling(vrp, stateManager, constraintManager));
        }
        handleExecutorShutdown(vra);
        vra.setMaxIterations(Integer.parseInt(properties.getProperty(Parameter.ITERATIONS.toString())));

        return vra;

    }

    private SearchStrategyModule configureModule(RuinAndRecreateModule ruinAndRecreateModule) {
        ruinAndRecreateModule.setRandom(random);
        ruinAndRecreateModule.setMinUnassignedJobsToBeReinserted(Integer.valueOf(properties.getProperty(Parameter.MIN_UNASSIGNED.toString())));
        ruinAndRecreateModule.setProportionOfUnassignedJobsToBeReinserted(Double.valueOf(properties.getProperty(Parameter.PROPORTION_UNASSIGNED.toString())));
        return ruinAndRecreateModule;
    }

    private DefaultScorer getRegretScorer(VehicleRoutingProblem vrp) {
        DefaultScorer scorer = new DefaultScorer(vrp);
        scorer.setTimeWindowParam(Double.valueOf(properties.getProperty(Parameter.REGRET_TIME_WINDOW_SCORER.toString())));
        scorer.setDepotDistanceParam(Double.valueOf(properties.getProperty(Parameter.REGRET_DISTANCE_SCORER.toString())));
        return scorer;
    }


    private void handleExecutorShutdown(VehicleRoutingAlgorithm vra) {
        if (setupExecutorInternally) {
            final Thread hook = new Thread() {
                public void run() {
                    if (!es.isShutdown()) {
                        System.err.println("shutdownHook shuts down executorService");
                        es.shutdown();
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(hook);
            vra.addListener(new AlgorithmEndsListener() {

                @Override
                public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
                    es.shutdown();
                    Runtime.getRuntime().removeShutdownHook(hook);
                }

            });
        }
//        if (es != null) {
//
//            Runtime.getRuntime().addShutdownHook(hook);
//            vra.addListener(new AlgorithmEndsListener() {
//                @Override
//                public void informAlgorithmEnds(VehicleRoutingProblem aProblem,
//                                                Collection<VehicleRoutingProblemSolution> aSolutions) {
//                    Runtime.getRuntime().removeShutdownHook(hook);
//                }
//            });
//        }
    }

    String getProperty(String key) {
        return properties.getProperty(key);
    }

    private boolean toBoolean(String property) {
        return Boolean.valueOf(property);
    }

    private int toInteger(String string) {
        return Integer.valueOf(string);
    }

    private double toDouble(String string) {
        return Double.valueOf(string);
    }

    private SolutionCostCalculator getObjectiveFunction(final VehicleRoutingProblem vrp, final double maxCosts) {
        if (objectiveFunction != null) return objectiveFunction;

        SolutionCostCalculator solutionCostCalculator = new SolutionCostCalculator() {
            @Override
            public double getCosts(VehicleRoutingProblemSolution solution) {
                double costs = 0.;

                for (VehicleRoute route : solution.getRoutes()) {
                    costs += route.getVehicle().getType().getVehicleCostParams().fix;
                    boolean hasBreak = false;
                    TourActivity prevAct = route.getStart();
                    for (TourActivity act : route.getActivities()) {
                        if (act instanceof BreakActivity) hasBreak = true;
                        costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(), act.getLocation(), prevAct.getEndTime(), route.getDriver(), route.getVehicle());
                        costs += vrp.getActivityCosts().getActivityCost(act, act.getArrTime(), route.getDriver(), route.getVehicle());
                        prevAct = act;
                    }
                    costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(), prevAct.getEndTime(), route.getDriver(), route.getVehicle());
                    if (route.getVehicle().getBreak() != null) {
                        if (!hasBreak) {
                            //break defined and required but not assigned penalty
                            if (route.getEnd().getArrTime() > route.getVehicle().getBreak().getTimeWindow().getEnd()) {
                                costs += 4 * (maxCosts * 2 + route.getVehicle().getBreak().getServiceDuration() * route.getVehicle().getType().getVehicleCostParams().perServiceTimeUnit);
                            }
                        }
                    }
                }
                for(Job j : solution.getUnassignedJobs()){
                    costs += maxCosts * 2 * (11 - j.getPriority());
                }
                return costs;
            }
        };
        return solutionCostCalculator;
    }


}
