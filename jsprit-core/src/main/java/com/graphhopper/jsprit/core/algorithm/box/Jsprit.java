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
import com.graphhopper.jsprit.core.algorithm.module.RuinAndRecreateModule;
import com.graphhopper.jsprit.core.algorithm.recreate.*;
import com.graphhopper.jsprit.core.algorithm.ruin.*;
import com.graphhopper.jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.selector.SelectRandomly;
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
import com.graphhopper.jsprit.core.util.NoiseMaker;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import com.graphhopper.jsprit.core.util.Solutions;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Jsprit {

    private final ActivityInsertionCostsCalculator activityInsertion;

    public enum Construction {

        BEST_INSERTION("best_insertion"), REGRET_INSERTION("regret_insertion"), RANDOM("random"),
        GREEDY_BY_NEIGHBORHOODS_REGRET("greedy_by_neighborhoods_regret"),
        GREEDY_BY_ZIP_CODE_REGRET("greedy_by_zip_code_regret"),
        GREEDY_BY_DISTANCE_REGRET("greedy_by_distance_regret"),
        GREEDY_BY_AVERAGE_REGRET("greedy_by_average_regret");

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
        RANDOM_BEST("random_best"),
        RANDOM_REGRET("random_regret"),
        WORST_BEST("worst_best"),
        WORST_REGRET("worst_regret"),
        CLUSTER_BEST("cluster_best"),
        CLUSTER_REGRET("cluster_regret"),
        STRING_BEST("string_best"),
        STRING_REGRET("string_regret"),
        RANDOM("random"),
        GREEDY_BY_NEIGHBORS_REGRET("greedy_by_neighbors_regret"),
        GREEDY_BY_ZIP_CODE_REGRET("greedy_by_zipcode_regret"),
        GREEDY_BY_DISTANCE_REGRET("greedy_by_distance_regret"),
        GREEDY_BY_AVERAGE_REGRET("greedy_by_average_regret"),
        GREEDY_BY_NEIGHBORS_REGRET_WORST("greedy_by_neighbors_regret_worst"),
        GREEDY_BY_ZIP_CODE_REGRET_WORST("greedy_by_zip_code_regret_worst"),
        GREEDY_BY_DISTANCE_REGRET_WORST("greedy_by_distance_regret_worst"),
        GREEDY_BY_AVERAGE_REGRET_WORST("greedy_by_average_regret_worst"),
        GREEDY_BY_NEIGHBORS_REGRET_FARTHEST("greedy_by_neighbors_regret_farthest"),
        GREEDY_BY_ZIP_CODE_REGRET_FARTHEST("greedy_by_zip_code_regret_farthest"),
        GREEDY_BY_DISTANCE_REGRET_FARTHEST("greedy_by_distance_regret_farthest"),
        GREEDY_BY_AVERAGE_REGRET_FARTHEST("greedy_by_average_regret_farthest"),
        GREEDY_BY_NEIGHBORS_REGRET_USER("greedy_by_neighbors_regret_user"),
        GREEDY_BY_ZIP_CODE_REGRET_USER("greedy_by_zip_code_regret_user"),
        GREEDY_BY_DISTANCE_REGRET_USER("greedy_by_distance_regret_user"),
        GREEDY_BY_AVERAGE_REGRET_USER("greedy_by_average_regret_user");

        String strategyName;

        Strategy(String strategyName) {
            this.strategyName = strategyName;
        }

        public String toString() {
            return strategyName;
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
        RANDOM_RANDOM_MIN_SHARE("random_random.min_share"),
        RANDOM_RANDOM_MAX_SHARE("random_random.max_share"),
        RADIAL_MIN_SHARE("radial.min_share"),
        RADIAL_MAX_SHARE("radial.max_share"),
        CLUSTER_MIN_SHARE("cluster.min_share"),
        CLUSTER_MAX_SHARE("cluster.max_share"),
        WORST_MIN_SHARE("worst.min_share"),
        WORST_MAX_SHARE("worst.max_share"),
        FARTHEST_MIN_SHARE("farthest.min_share"),
        FARTHEST_MAX_SHARE("farthest.max_share"),
        USER_MIN_SHARE("user.min_share"),
        USER_MAX_SHARE("user.max_share"),
        THRESHOLD_ALPHA("threshold.alpha"),
        THRESHOLD_INI("threshold.ini"),
        THRESHOLD_INI_ABS("threshold.ini_abs"),
        INSERTION_NOISE_LEVEL("insertion.noise_level"),
        INSERTION_NOISE_PROB("insertion.noise_prob"),
        RUIN_WORST_NOISE_LEVEL("worst.noise_level"),
        RUIN_WORST_NOISE_PROB("worst.noise_prob"),
        FAST_REGRET("regret.fast"),
        MAX_TRANSPORT_COSTS("max_transport_costs"),
        CONSTRUCTION("construction"),
        BREAK_SCHEDULING("break_scheduling"),
        STRING_K_MIN("string_kmin"),
        STRING_K_MAX("string_kmax"),
        STRING_L_MIN("string_lmin"),
        STRING_L_MAX("string_lmax"),
        MIN_UNASSIGNED("min_unassigned"),
        PROPORTION_UNASSIGNED("proportion_unassigned"),
        DISTANCE_DIFF_FOR_SAME_NEIGHBORHOOD("distance_diff_for_same_neighborhood"),
        RATIO_TO_SORT_JOBS_GREEDY_INSERTION("ratio_to_sort_jobs_greedy_insertion"),
        RATIO_TO_SELECT_NEAREST("ratio_to_select_nearest"),
        RATIO_TO_SELECT_RANDOM("ratio_to_select_random"),
        RATIO_TO_SELECT_FARTHEST("ratio_to_select_farthest"),
        NUMBER_OF_JOBS_TO_SELECT_FROM("number_of_jobs_to_select_from"),
        RATIO_IDLE_ROUTE_TO_BE_REMOVED("ratio_idle_route_to_be_removed"),
        RATIO_IDLE_ROUTE_TO_BE_REMOVED_FINAL_STEP("ratio_idle_route_to_be_removed_final_step");


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

        private Map<SearchStrategy, Double> customStrategies = new HashMap<>();

        private VehicleFleetManager fleetManager = null;

        private RuinRadial radial = null;
        private RuinRandom randomForRegret = null;
        private RuinRandom randomForBest = null;
        private RuinRandom randomForRandom = null;
        private AbstractRuinStrategy ruinStrategy = null;
        private RuinWorst worst = null;
        private RuinClusters clusters = null;
        private RuinString stringRuin = null;

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
            defaults.put(Strategy.RANDOM_BEST.toString(), ".5");
            defaults.put(Strategy.RANDOM_REGRET.toString(), ".5");

            defaults.put(Strategy.STRING_BEST.toString(), "0.0");
            defaults.put(Strategy.STRING_REGRET.toString(), "0.0");

            defaults.put(Strategy.RANDOM.toString(), "0.0");

            defaults.put(Strategy.GREEDY_BY_NEIGHBORS_REGRET.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_NEIGHBORS_REGRET_WORST.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_NEIGHBORS_REGRET_FARTHEST.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_NEIGHBORS_REGRET_USER.toString(), "0.0");

            defaults.put(Strategy.GREEDY_BY_DISTANCE_REGRET.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_DISTANCE_REGRET_WORST.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_DISTANCE_REGRET_FARTHEST.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_DISTANCE_REGRET_USER.toString(), "0.0");

            defaults.put(Strategy.GREEDY_BY_AVERAGE_REGRET.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_AVERAGE_REGRET_WORST.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_AVERAGE_REGRET_FARTHEST.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_AVERAGE_REGRET_USER.toString(), "0.0");

            defaults.put(Strategy.GREEDY_BY_ZIP_CODE_REGRET.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_ZIP_CODE_REGRET_WORST.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_ZIP_CODE_REGRET_FARTHEST.toString(), "0.0");
            defaults.put(Strategy.GREEDY_BY_ZIP_CODE_REGRET_USER.toString(), "0.0");

            defaults.put(Parameter.STRING_K_MIN.toString(), "1");
            defaults.put(Parameter.STRING_K_MAX.toString(), "6");
            defaults.put(Parameter.STRING_L_MIN.toString(), "10");
            defaults.put(Parameter.STRING_L_MAX.toString(), "30");

            defaults.put(Strategy.WORST_BEST.toString(), "0.");
            defaults.put(Strategy.WORST_REGRET.toString(), "1.");
            defaults.put(Strategy.CLUSTER_BEST.toString(), "0.");
            defaults.put(Strategy.CLUSTER_REGRET.toString(), "1.");


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
            defaults.put(Parameter.FARTHEST_MIN_SHARE.toString(), String.valueOf(minShare));
            defaults.put(Parameter.FARTHEST_MAX_SHARE.toString(), String.valueOf(maxShare));
            defaults.put(Parameter.USER_MIN_SHARE.toString(), String.valueOf(minShare));
            defaults.put(Parameter.USER_MAX_SHARE.toString(), String.valueOf(maxShare));
            defaults.put(Parameter.CLUSTER_MIN_SHARE.toString(), String.valueOf(minShare));
            defaults.put(Parameter.CLUSTER_MAX_SHARE.toString(), String.valueOf(maxShare));
            int minShare_ = (int) Math.min(70, Math.max(5, vrp.getJobs().size() * 0.5));
            int maxShare_ = (int) Math.min(70, Math.max(5, vrp.getJobs().size() * 0.5));
            defaults.put(Parameter.RANDOM_REGRET_MIN_SHARE.toString(), String.valueOf(minShare_));
            defaults.put(Parameter.RANDOM_REGRET_MAX_SHARE.toString(), String.valueOf(maxShare_));
            defaults.put(Parameter.RANDOM_BEST_MIN_SHARE.toString(), String.valueOf(minShare_));
            defaults.put(Parameter.RANDOM_BEST_MAX_SHARE.toString(), String.valueOf(maxShare_));
            defaults.put(Parameter.RANDOM_RANDOM_MIN_SHARE.toString(), String.valueOf(minShare_));
            defaults.put(Parameter.RANDOM_RANDOM_MAX_SHARE.toString(), String.valueOf(maxShare_));
            defaults.put(Parameter.THRESHOLD_ALPHA.toString(), String.valueOf(0.15));
            defaults.put(Parameter.THRESHOLD_INI.toString(), String.valueOf(0.03));
            defaults.put(Parameter.INSERTION_NOISE_LEVEL.toString(), String.valueOf(0.15));
            defaults.put(Parameter.INSERTION_NOISE_PROB.toString(), String.valueOf(0.2));
            defaults.put(Parameter.RUIN_WORST_NOISE_LEVEL.toString(), String.valueOf(0.15));
            defaults.put(Parameter.RUIN_WORST_NOISE_PROB.toString(), String.valueOf(0.2));
            defaults.put(Parameter.VEHICLE_SWITCH.toString(), String.valueOf(true));
            defaults.put(Parameter.FAST_REGRET.toString(), String.valueOf(false));
            defaults.put(Parameter.BREAK_SCHEDULING.toString(), String.valueOf(true));
            defaults.put(Parameter.CONSTRUCTION.toString(), Construction.REGRET_INSERTION.toString());

            defaults.put(Parameter.MIN_UNASSIGNED.toString(), String.valueOf(Integer.MAX_VALUE));
            defaults.put(Parameter.PROPORTION_UNASSIGNED.toString(), String.valueOf(1.0));

            defaults.put(Parameter.DISTANCE_DIFF_FOR_SAME_NEIGHBORHOOD.toString(), String.valueOf(100));
            defaults.put(Parameter.RATIO_TO_SORT_JOBS_GREEDY_INSERTION.toString(), String.valueOf(.5));

            defaults.put(Parameter.RATIO_TO_SELECT_NEAREST.toString(), String.valueOf(.33));
            defaults.put(Parameter.RATIO_TO_SELECT_RANDOM.toString(), String.valueOf(.33));
            defaults.put(Parameter.RATIO_TO_SELECT_FARTHEST.toString(), String.valueOf(.33));
            defaults.put(Parameter.NUMBER_OF_JOBS_TO_SELECT_FROM.toString(), String.valueOf(3));
            defaults.put(Parameter.RATIO_IDLE_ROUTE_TO_BE_REMOVED.toString(), String.valueOf(0.9));
            defaults.put(Parameter.RATIO_IDLE_ROUTE_TO_BE_REMOVED_FINAL_STEP.toString(), String.valueOf(0.95));

            return defaults;
        }


        public Builder addSearchStrategy(SearchStrategy searchStrategy, double weight) {
            customStrategies.put(searchStrategy, weight);
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
            setProperty(strategy.toString(), value);
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

        public Builder setRadial(RuinRadial radial) {
            this.radial = radial;
            return this;
        }

        public Builder setRandomForRegret(RuinRandom randomForRegret) {
            this.randomForRegret = randomForRegret;
            return this;
        }

        public Builder setRandomForBest(RuinRandom randomForBest) {
            this.randomForBest = randomForBest;
            return this;
        }

        public Builder setRandomForRandom(RuinRandom randomForRandom) {
            this.randomForRandom = randomForRandom;
            return this;
        }

        public void setRuinStrategy(AbstractRuinStrategy ruinStrategy) {
            this.ruinStrategy = ruinStrategy;
        }

        public Builder setWorst(RuinWorst worst) {
            this.worst = worst;
            return this;
        }

        public Builder setClusters(RuinClusters clusters) {
            this.clusters = clusters;
            return this;
        }

        public Builder setStringRuin(RuinString stringRuin) {
            this.stringRuin = stringRuin;
            return this;
        }

        public VehicleRoutingAlgorithm buildAlgorithm() {
            return new Jsprit(this).create(vrp);
        }
    }

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

    private StateManager stateManager = null;

    private ConstraintManager constraintManager = null;

    private ExecutorService es = null;

    private Integer noThreads;

    private boolean setupExecutorInternally = false;

    private boolean addCoreConstraints;

    private SolutionCostCalculator objectiveFunction = null;

    private Properties properties;

    private Random random;

    private SolutionAcceptor acceptor;

    private ScoringFunction regretScorer;

    private final Map<SearchStrategy, Double> customStrategies = new HashMap<>();

    private VehicleFleetManager vehicleFleetManager;

    private RuinRadial radial;
    private RuinRandom random_for_regret;
    private RuinRandom random_for_best;
    private RuinRandom random_for_random;
    private AbstractRuinStrategy ruin_strategy;
    private RuinWorst worst;
    private RuinFarthest farthest;
    private RuinClusters clusters;
    private RuinString stringRuin;

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
        regretScorer = builder.regretScorer;
        customStrategies.putAll(builder.customStrategies);
        vehicleFleetManager = builder.fleetManager;
        radial = builder.radial;
        random_for_regret = builder.randomForRegret;
        random_for_best = builder.randomForBest;
        random_for_random = builder.randomForRandom;
        ruin_strategy = builder.ruinStrategy;
        worst = builder.worst;
        clusters = builder.clusters;
        stringRuin = builder.stringRuin;
    }

    private void ini(VehicleRoutingProblem vrp) {
        if (regretScorer == null) regretScorer = getRegretScorer(vrp);
    }

    private VehicleRoutingAlgorithm create(final VehicleRoutingProblem vrp) {
        ini(vrp);
        if (vehicleFleetManager == null) {
            if (vrp.getFleetSize().equals(VehicleRoutingProblem.FleetSize.INFINITE)) {
                vehicleFleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
            } else {
                FiniteFleetManagerFactory finiteFleetManagerFactory = new FiniteFleetManagerFactory(vrp.getVehicles());
                finiteFleetManagerFactory.setRandom(random);
                vehicleFleetManager = finiteFleetManagerFactory.createFleetManager();
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
        if(properties.containsKey(Parameter.MAX_TRANSPORT_COSTS.toString())){
            maxCosts = Double.parseDouble(getProperty(Parameter.MAX_TRANSPORT_COSTS.toString()));
        }
        else{
            maxCosts = jobNeighborhoods.getMaxDistance();
        }

        IterationStartsListener noiseConfigurator;
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

        radial = radial == null ? new RuinRadial(vrp, vrp.getJobs().size(), jobNeighborhoods) : radial;
        radial.setRandom(random);
        radial.setRuinShareFactory(new RuinShareFactoryImpl(
                toInteger(properties.getProperty(Parameter.RADIAL_MIN_SHARE.toString())),
                toInteger(properties.getProperty(Parameter.RADIAL_MAX_SHARE.toString())),
                random)
        );

        random_for_regret = random_for_regret == null ? new RuinRandom(vrp, 0.5) : random_for_regret;
        random_for_regret.setRandom(random);
        random_for_regret.setRuinShareFactory(new RuinShareFactoryImpl(
                toInteger(properties.getProperty(Parameter.RANDOM_REGRET_MIN_SHARE.toString())),
                toInteger(properties.getProperty(Parameter.RANDOM_REGRET_MAX_SHARE.toString())),
                random)
        );

        random_for_best = random_for_best == null ? new RuinRandom(vrp, 0.5) : random_for_best;
        random_for_best.setRandom(random);
        random_for_best.setRuinShareFactory(new RuinShareFactoryImpl(
            toInteger(properties.getProperty(Parameter.RANDOM_BEST_MIN_SHARE.toString())),
            toInteger(properties.getProperty(Parameter.RANDOM_BEST_MAX_SHARE.toString())),
            random)
        );

        random_for_random = random_for_random == null ? new RuinRandom(vrp, 0.5) : random_for_random;
        random_for_random.setRandom(random);
        random_for_random.setRuinShareFactory(new RuinShareFactoryImpl(
            toInteger(properties.getProperty(Parameter.RANDOM_RANDOM_MIN_SHARE.toString())),
            toInteger(properties.getProperty(Parameter.RANDOM_RANDOM_MAX_SHARE.toString())),
            random)
        );

        farthest = farthest == null ? new RuinFarthest(vrp,
            toDouble(properties.getProperty(Parameter.RATIO_IDLE_ROUTE_TO_BE_REMOVED.toString())),
            toDouble(properties.getProperty(Parameter.RATIO_IDLE_ROUTE_TO_BE_REMOVED_FINAL_STEP.toString()))) : farthest;
        farthest.setRandom(random);
        farthest.setRuinShareFactory(new RuinShareFactoryImpl(
            toInteger(properties.getProperty(Parameter.FARTHEST_MIN_SHARE.toString())),
            toInteger(properties.getProperty(Parameter.FARTHEST_MAX_SHARE.toString())),
            random)
        );

        ruin_strategy = ruin_strategy == null ? new RuinFarthest(vrp,
            toDouble(properties.getProperty(Parameter.RATIO_IDLE_ROUTE_TO_BE_REMOVED.toString())),
            toDouble(properties.getProperty(Parameter.RATIO_IDLE_ROUTE_TO_BE_REMOVED_FINAL_STEP.toString()))) : ruin_strategy;
        ruin_strategy.setRandom(random);
        ruin_strategy.setRuinShareFactory(new RuinShareFactoryImpl(
            toInteger(properties.getProperty(Parameter.USER_MIN_SHARE.toString())),
            toInteger(properties.getProperty(Parameter.USER_MAX_SHARE.toString())),
            random)
        );

        worst = worst == null ? new RuinWorst(vrp, (int) (vrp.getJobs().values().size() * 0.5)) : worst;
        worst.setRandom(random);
        worst.setRuinShareFactory(new RuinShareFactoryImpl(
            toInteger(properties.getProperty(Parameter.WORST_MIN_SHARE.toString())),
            toInteger(properties.getProperty(Parameter.WORST_MAX_SHARE.toString())),
            random)
        );
        IterationStartsListener noise = new IterationStartsListener() {
            @Override
            public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
                worst.setNoiseMaker(new NoiseMaker() {

                    public double makeNoise() {
                        if (random.nextDouble() < toDouble(getProperty(Parameter.RUIN_WORST_NOISE_PROB.toString()))) {
                            return toDouble(getProperty(Parameter.RUIN_WORST_NOISE_LEVEL.toString()))
                                * maxCosts * random.nextDouble();
                        } else return 0.;
                    }
                });
            }
        };

        clusters = clusters == null ? new RuinClusters(vrp, (int) (vrp.getJobs().values().size() * 0.5), jobNeighborhoods) : clusters;
        clusters.setRandom(random);
        clusters.setRuinShareFactory(new RuinShareFactoryImpl(
                toInteger(properties.getProperty(Parameter.WORST_MIN_SHARE.toString())),
                toInteger(properties.getProperty(Parameter.WORST_MAX_SHARE.toString())),
                random)
        );

        int kMin = toInteger(properties.getProperty(Parameter.STRING_K_MIN.toString()));
        int kMax = toInteger(properties.getProperty(Parameter.STRING_K_MAX.toString()));
        int lMin = toInteger(properties.getProperty(Parameter.STRING_L_MIN.toString()));
        int lMax = toInteger(properties.getProperty(Parameter.STRING_L_MAX.toString()));

        stringRuin = stringRuin == null ? new RuinString(vrp, jobNeighborhoods) : stringRuin;
        stringRuin.setNoRoutes(kMin, kMax);
        stringRuin.setStringLength(lMin, lMax);
        stringRuin.setRandom(random);

        AbstractInsertionStrategy regret;
        final ScoringFunction scorer;

        boolean fastRegret = Boolean.parseBoolean(getProperty(Parameter.FAST_REGRET.toString()));
        if (es != null) {
            if(fastRegret){
                RegretInsertionConcurrentFast regretInsertion = (RegretInsertionConcurrentFast) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionStrategyBuilder.Strategy.REGRET)
                    .setConcurrentMode(es, noThreads)
                    .setFastRegret(true)
                    .considerFixedCosts(toDouble(getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .setActivityInsertionCostCalculator(activityInsertion)
                    .build();
                scorer = regretScorer;
                regretInsertion.setScoringFunction(scorer);
                regretInsertion.setDependencyTypes(constraintManager.getDependencyTypes());
                regret = regretInsertion;
            }
            else {
                RegretInsertionConcurrent regretInsertion = (RegretInsertionConcurrent) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionStrategyBuilder.Strategy.REGRET)
                    .setConcurrentMode(es, noThreads)
                    .considerFixedCosts(toDouble(getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .setActivityInsertionCostCalculator(activityInsertion)
                    .build();
                scorer = regretScorer;
                regretInsertion.setScoringFunction(scorer);
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
                    .build();
                scorer = regretScorer;
                regretInsertion.setScoringFunction(scorer);
                regretInsertion.setDependencyTypes(constraintManager.getDependencyTypes());
                regret = regretInsertion;
            }
            else{
                RegretInsertion regretInsertion = (RegretInsertion) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionStrategyBuilder.Strategy.REGRET)
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .considerFixedCosts(toDouble(getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setActivityInsertionCostCalculator(activityInsertion)
                    .build();
                scorer = regretScorer;
                regretInsertion.setScoringFunction(scorer);
                regret = regretInsertion;
            }
        }
        regret.setRandom(random);

        AbstractInsertionStrategy best;
        if (vrp.getJobs().size() < 250 || es == null) {
            BestInsertion bestInsertion = (BestInsertion) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                .setInsertionStrategy(InsertionStrategyBuilder.Strategy.BEST)
                .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
                .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                .setActivityInsertionCostCalculator(activityInsertion)
                .build();
            best = bestInsertion;
        } else {
            BestInsertionConcurrent bestInsertion = (BestInsertionConcurrent) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                .setInsertionStrategy(InsertionStrategyBuilder.Strategy.BEST)
                .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
                .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                .setConcurrentMode(es, noThreads)
                .setActivityInsertionCostCalculator(activityInsertion)
                .build();
            best = bestInsertion;
        }
        best.setRandom(random);

        final AbstractInsertionStrategy randomInsertion = (AbstractInsertionStrategy) new InsertionBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
            .setInsertionStrategy(InsertionBuilder.Strategy.RANDOM)
            .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
            .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
            .setActivityInsertionCostCalculator(activityInsertion)
            .build();
        randomInsertion.setRandom(random);

        final AbstractInsertionStrategy greedyByNeighborsInsertion = (AbstractInsertionStrategy) new InsertionBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
            .setInsertionStrategy(InsertionBuilder.Strategy.GREEDY_BY_NEIGHBORS)
            .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
            .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
            .setActivityInsertionCostCalculator(activityInsertion)
            .setDistanceDiffForNeighbors(Double.valueOf(properties.getProperty(Parameter.DISTANCE_DIFF_FOR_SAME_NEIGHBORHOOD.toString())))
            .setRatioToSortJobsGreedyInsertion(Double.valueOf(properties.getProperty(Parameter.RATIO_TO_SORT_JOBS_GREEDY_INSERTION.toString())))
            .build();
        greedyByNeighborsInsertion.setRandom(random);

        final AbstractInsertionStrategy greedyByZipCodeInsertion = (AbstractInsertionStrategy) new InsertionBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
            .setInsertionStrategy(InsertionBuilder.Strategy.GREEDY_BY_ZIP_CODE)
            .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
            .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
            .setActivityInsertionCostCalculator(activityInsertion)
            .setDistanceDiffForNeighbors(Double.valueOf(properties.getProperty(Parameter.DISTANCE_DIFF_FOR_SAME_NEIGHBORHOOD.toString())))
            .setRatioToSortJobsGreedyInsertion(Double.valueOf(properties.getProperty(Parameter.RATIO_TO_SORT_JOBS_GREEDY_INSERTION.toString())))
            .build();
        greedyByZipCodeInsertion.setRandom(random);

        final AbstractInsertionStrategy greedyByAverageInsertion = (AbstractInsertionStrategy) new InsertionBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
            .setInsertionStrategy(InsertionBuilder.Strategy.GREEDY_BY_AVERAGE)
            .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
            .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
            .setActivityInsertionCostCalculator(activityInsertion)
            .setRatioToSelectNearest(Double.valueOf(properties.getProperty(Parameter.RATIO_TO_SELECT_NEAREST.toString())))
            .setRatioToSelectRandom(Double.valueOf(properties.getProperty(Parameter.RATIO_TO_SELECT_RANDOM.toString())))
            .setRatioToSelectFarthest(Double.valueOf(properties.getProperty(Parameter.RATIO_TO_SELECT_FARTHEST.toString())))
            .setNJobsToSelectFrom(Integer.valueOf(properties.getProperty(Parameter.NUMBER_OF_JOBS_TO_SELECT_FROM.toString())))
            .build();
        greedyByAverageInsertion.setRandom(random);

        final AbstractInsertionStrategy greedyByDistanceFromDepotInsertion = (AbstractInsertionStrategy) new InsertionBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
            .setInsertionStrategy(InsertionBuilder.Strategy.GREEDY_BY_DISTANCE)
            .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
            .setActivityInsertionCostCalculator(activityInsertion)
            .build();
        greedyByDistanceFromDepotInsertion.setRandom(random);

        IterationStartsListener schrimpfThreshold = null;
        if(acceptor == null) {
            final SchrimpfAcceptance schrimpfAcceptance = new SchrimpfAcceptance(1, toDouble(getProperty(Parameter.THRESHOLD_ALPHA.toString())));
            if (properties.containsKey(Parameter.THRESHOLD_INI_ABS.toString())) {
                schrimpfAcceptance.setInitialThreshold(Double.valueOf(properties.getProperty(Parameter.THRESHOLD_INI_ABS.toString())));
            } else {
                schrimpfThreshold = new IterationStartsListener() {
                    @Override
                    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
                        if (i == 1) {
                            double initialThreshold = Solutions.bestOf(solutions).getCost() * toDouble(getProperty(Parameter.THRESHOLD_INI.toString()));
                            schrimpfAcceptance.setInitialThreshold(initialThreshold);
                        }
                    }
                };
            }
            acceptor = schrimpfAcceptance;
        }

        SolutionCostCalculator objectiveFunction = getObjectiveFunction(vrp, maxCosts);
        SearchStrategy radial_regret = new SearchStrategy(Strategy.RADIAL_REGRET.toString(), new SelectBest(), acceptor, objectiveFunction);
        radial_regret.addModule(configureModule(new RuinAndRecreateModule(Strategy.RADIAL_REGRET.toString(), regret, radial)));

        SearchStrategy radial_best = new SearchStrategy(Strategy.RADIAL_BEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        radial_best.addModule(configureModule(new RuinAndRecreateModule(Strategy.RADIAL_BEST.toString(), best, radial)));

        SearchStrategy random_best = new SearchStrategy(Strategy.RANDOM_BEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        random_best.addModule(configureModule(new RuinAndRecreateModule(Strategy.RANDOM_BEST.toString(), best, random_for_best)));

        SearchStrategy random_regret = new SearchStrategy(Strategy.RANDOM_REGRET.toString(), new SelectBest(), acceptor, objectiveFunction);
        random_regret.addModule(configureModule(new RuinAndRecreateModule(Strategy.RANDOM_REGRET.toString(), regret, random_for_regret)));

        SearchStrategy worst_regret = new SearchStrategy(Strategy.WORST_REGRET.toString(), new SelectBest(), acceptor, objectiveFunction);
        worst_regret.addModule(configureModule(new RuinAndRecreateModule(Strategy.WORST_REGRET.toString(), regret, worst)));

        SearchStrategy worst_best = new SearchStrategy(Strategy.WORST_BEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        worst_best.addModule(configureModule(new RuinAndRecreateModule(Strategy.WORST_BEST.toString(), best, worst)));

        final SearchStrategy clusters_regret = new SearchStrategy(Strategy.CLUSTER_REGRET.toString(), new SelectBest(), acceptor, objectiveFunction);
        clusters_regret.addModule(configureModule(new RuinAndRecreateModule(Strategy.CLUSTER_REGRET.toString(), regret, clusters)));

        final SearchStrategy clusters_best = new SearchStrategy(Strategy.CLUSTER_BEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        clusters_best.addModule(configureModule(new RuinAndRecreateModule(Strategy.CLUSTER_BEST.toString(), best, clusters)));

        SearchStrategy stringRegret = new SearchStrategy(Strategy.STRING_REGRET.toString(), new SelectBest(), acceptor, objectiveFunction);
        stringRegret.addModule(configureModule(new RuinAndRecreateModule(Strategy.STRING_REGRET.toString(), regret, stringRuin)));

        SearchStrategy stringBest = new SearchStrategy(Strategy.STRING_BEST.toString(), new SelectBest(), acceptor, objectiveFunction);
        stringBest.addModule(configureModule(new RuinAndRecreateModule(Strategy.STRING_BEST.toString(), best, stringRuin)));

        final SearchStrategy randomStrategy = new SearchStrategy(Strategy.RANDOM.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        randomStrategy.addModule(new RuinAndRecreateModule(Strategy.RANDOM.toString(), randomInsertion, random_for_random));

        final SearchStrategy greedyByNeighborsStrategy = new SearchStrategy(Strategy.GREEDY_BY_NEIGHBORS_REGRET.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByNeighborsStrategy.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_NEIGHBORS_REGRET.toString(), greedyByNeighborsInsertion, clusters));
        final SearchStrategy greedyByNeighborsStrategyWorst = new SearchStrategy(Strategy.GREEDY_BY_NEIGHBORS_REGRET_WORST.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByNeighborsStrategyWorst.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_NEIGHBORS_REGRET_WORST.toString(), greedyByNeighborsInsertion, worst));
        final SearchStrategy greedyByNeighborsStrategyFarthest = new SearchStrategy(Strategy.GREEDY_BY_NEIGHBORS_REGRET_FARTHEST.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByNeighborsStrategyFarthest.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_NEIGHBORS_REGRET_FARTHEST.toString(), greedyByNeighborsInsertion, farthest));
        final SearchStrategy greedyByNeighborsStrategyUser = new SearchStrategy(Strategy.GREEDY_BY_NEIGHBORS_REGRET_USER.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByNeighborsStrategyUser.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_NEIGHBORS_REGRET_USER.toString(), greedyByNeighborsInsertion, ruin_strategy));

        final SearchStrategy greedyByZipCodeStrategy = new SearchStrategy(Strategy.GREEDY_BY_ZIP_CODE_REGRET.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByZipCodeStrategy.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_ZIP_CODE_REGRET.toString(), greedyByNeighborsInsertion, clusters));
        final SearchStrategy greedyByZipCodeStrategyWorst = new SearchStrategy(Strategy.GREEDY_BY_ZIP_CODE_REGRET_WORST.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByZipCodeStrategyWorst.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_ZIP_CODE_REGRET_WORST.toString(), greedyByNeighborsInsertion, worst));
        final SearchStrategy greedyByZipCodeStrategyFarthest = new SearchStrategy(Strategy.GREEDY_BY_ZIP_CODE_REGRET_FARTHEST.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByZipCodeStrategyFarthest.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_ZIP_CODE_REGRET_FARTHEST.toString(), greedyByNeighborsInsertion, farthest));
        final SearchStrategy greedyByZipCodeStrategyUser = new SearchStrategy(Strategy.GREEDY_BY_ZIP_CODE_REGRET_USER.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByZipCodeStrategyUser.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_ZIP_CODE_REGRET_USER.toString(), greedyByNeighborsInsertion, ruin_strategy));

        final SearchStrategy greedyByDistanceStrategy = new SearchStrategy(Strategy.GREEDY_BY_DISTANCE_REGRET.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByDistanceStrategy.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_DISTANCE_REGRET.toString(), greedyByDistanceFromDepotInsertion, clusters));
        final SearchStrategy greedyByDistanceStrategyWorst = new SearchStrategy(Strategy.GREEDY_BY_DISTANCE_REGRET_WORST.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByDistanceStrategyWorst.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_DISTANCE_REGRET_WORST.toString(), greedyByDistanceFromDepotInsertion, worst));
        final SearchStrategy greedyByDistanceStrategyFarthest = new SearchStrategy(Strategy.GREEDY_BY_DISTANCE_REGRET_FARTHEST.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByDistanceStrategyFarthest.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_DISTANCE_REGRET_FARTHEST.toString(), greedyByDistanceFromDepotInsertion, farthest));
        final SearchStrategy greedyByDistanceStrategyUser = new SearchStrategy(Strategy.GREEDY_BY_DISTANCE_REGRET_USER.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByDistanceStrategyUser.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_DISTANCE_REGRET_USER.toString(), greedyByDistanceFromDepotInsertion, ruin_strategy));

        final SearchStrategy greedyByAverageStrategy = new SearchStrategy(Strategy.GREEDY_BY_AVERAGE_REGRET.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByAverageStrategy.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_AVERAGE_REGRET.toString(), greedyByAverageInsertion, clusters));
        final SearchStrategy greedyByAverageStrategyWorst = new SearchStrategy(Strategy.GREEDY_BY_AVERAGE_REGRET_WORST.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByAverageStrategyWorst.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_AVERAGE_REGRET_WORST.toString(), greedyByAverageInsertion, worst));
        final SearchStrategy greedyByAverageStrategyFarthest = new SearchStrategy(Strategy.GREEDY_BY_AVERAGE_REGRET_FARTHEST.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByAverageStrategyFarthest.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_AVERAGE_REGRET_FARTHEST.toString(), greedyByAverageInsertion, farthest));
        final SearchStrategy greedyByAverageStrategyUser = new SearchStrategy(Strategy.GREEDY_BY_AVERAGE_REGRET_USER.toString(), new SelectRandomly(), acceptor, objectiveFunction);
        greedyByAverageStrategyUser.addModule(new RuinAndRecreateModule(Strategy.GREEDY_BY_AVERAGE_REGRET_USER.toString(), greedyByAverageInsertion, ruin_strategy));

        PrettyAlgorithmBuilder prettyBuilder = PrettyAlgorithmBuilder.newInstance(vrp, vehicleFleetManager, stateManager, constraintManager);
        prettyBuilder.setRandom(random);
        if (addCoreConstraints) {
            prettyBuilder.addCoreStateAndConstraintStuff();
        }

        prettyBuilder.withStrategy(radial_regret, toDouble(getProperty(Strategy.RADIAL_REGRET.toString())))
            .withStrategy(radial_best, toDouble(getProperty(Strategy.RADIAL_BEST.toString())))
            .withStrategy(random_best, toDouble(getProperty(Strategy.RANDOM_BEST.toString())))
            .withStrategy(random_regret, toDouble(getProperty(Strategy.RANDOM_REGRET.toString())))
            .withStrategy(worst_best, toDouble(getProperty(Strategy.WORST_BEST.toString())))
            .withStrategy(worst_regret, toDouble(getProperty(Strategy.WORST_REGRET.toString())))
            .withStrategy(clusters_regret, toDouble(getProperty(Strategy.CLUSTER_REGRET.toString())))
            .withStrategy(clusters_best, toDouble(getProperty(Strategy.CLUSTER_BEST.toString())))
            .withStrategy(stringBest, toDouble(getProperty(Strategy.STRING_BEST.toString())))
            .withStrategy(stringRegret, toDouble(getProperty(Strategy.STRING_REGRET.toString())))
            .withStrategy(randomStrategy, toDouble(getProperty(Strategy.RANDOM.toString())))
            .withStrategy(greedyByNeighborsStrategy, toDouble(getProperty(Strategy.GREEDY_BY_NEIGHBORS_REGRET.toString())))
            .withStrategy(greedyByZipCodeStrategy, toDouble(getProperty(Strategy.GREEDY_BY_ZIP_CODE_REGRET.toString())))
            .withStrategy(greedyByDistanceStrategy, toDouble(getProperty(Strategy.GREEDY_BY_DISTANCE_REGRET.toString())))
            .withStrategy(greedyByAverageStrategy, toDouble(getProperty(Strategy.GREEDY_BY_AVERAGE_REGRET.toString())))
            .withStrategy(greedyByNeighborsStrategyWorst, toDouble(getProperty(Strategy.GREEDY_BY_NEIGHBORS_REGRET_WORST.toString())))
            .withStrategy(greedyByZipCodeStrategyWorst, toDouble(getProperty(Strategy.GREEDY_BY_ZIP_CODE_REGRET_WORST.toString())))
            .withStrategy(greedyByDistanceStrategyWorst, toDouble(getProperty(Strategy.GREEDY_BY_DISTANCE_REGRET_WORST.toString())))
            .withStrategy(greedyByAverageStrategyWorst, toDouble(getProperty(Strategy.GREEDY_BY_AVERAGE_REGRET_WORST.toString())))
            .withStrategy(greedyByNeighborsStrategyFarthest, toDouble(getProperty(Strategy.GREEDY_BY_NEIGHBORS_REGRET_FARTHEST.toString())))
            .withStrategy(greedyByZipCodeStrategyFarthest, toDouble(getProperty(Strategy.GREEDY_BY_ZIP_CODE_REGRET_FARTHEST.toString())))
            .withStrategy(greedyByDistanceStrategyFarthest, toDouble(getProperty(Strategy.GREEDY_BY_DISTANCE_REGRET_FARTHEST.toString())))
            .withStrategy(greedyByAverageStrategyFarthest, toDouble(getProperty(Strategy.GREEDY_BY_AVERAGE_REGRET_FARTHEST.toString())))
            .withStrategy(greedyByNeighborsStrategyUser, toDouble(getProperty(Strategy.GREEDY_BY_NEIGHBORS_REGRET_USER.toString())))
            .withStrategy(greedyByZipCodeStrategyUser, toDouble(getProperty(Strategy.GREEDY_BY_ZIP_CODE_REGRET_USER.toString())))
            .withStrategy(greedyByDistanceStrategyUser, toDouble(getProperty(Strategy.GREEDY_BY_DISTANCE_REGRET_USER.toString())))
            .withStrategy(greedyByAverageStrategyUser, toDouble(getProperty(Strategy.GREEDY_BY_AVERAGE_REGRET_USER.toString())));

        for (SearchStrategy customStrategy : customStrategies.keySet()) {
            prettyBuilder.withStrategy(customStrategy, customStrategies.get(customStrategy));
        }

        if (getProperty(Parameter.CONSTRUCTION.toString()).equals(Construction.RANDOM.toString())) {
            prettyBuilder.constructInitialSolutionWith(randomInsertion, objectiveFunction);
        } else if (getProperty(Parameter.CONSTRUCTION.toString()).equals(Construction.GREEDY_BY_NEIGHBORHOODS_REGRET.toString())) {
            prettyBuilder.constructInitialSolutionWith(greedyByNeighborsInsertion, objectiveFunction);
        } else if (getProperty(Parameter.CONSTRUCTION.toString()).equals(Construction.GREEDY_BY_DISTANCE_REGRET.toString())) {
            prettyBuilder.constructInitialSolutionWith(greedyByDistanceFromDepotInsertion, objectiveFunction);
        } else if (getProperty(Parameter.CONSTRUCTION.toString()).equals(Construction.GREEDY_BY_ZIP_CODE_REGRET.toString())) {
            prettyBuilder.constructInitialSolutionWith(greedyByZipCodeInsertion, objectiveFunction);
        } else if (getProperty(Parameter.CONSTRUCTION.toString()).equals(Construction.GREEDY_BY_AVERAGE_REGRET.toString())) {
            prettyBuilder.constructInitialSolutionWith(greedyByAverageInsertion, objectiveFunction);
        } else if (getProperty(Parameter.CONSTRUCTION.toString()).equals(Construction.BEST_INSERTION.toString())) {
            prettyBuilder.constructInitialSolutionWith(best, objectiveFunction);
        } else {
            prettyBuilder.constructInitialSolutionWith(regret, objectiveFunction);
        }

        prettyBuilder.withObjectiveFunction(objectiveFunction);


        VehicleRoutingAlgorithm vra = prettyBuilder.build();
        if(schrimpfThreshold != null) {
            vra.addListener(schrimpfThreshold);
        }
        vra.addListener(noiseConfigurator);
        vra.addListener(noise);
        vra.addListener(clusters);
        if (increasingAbsoluteFixedCosts != null) vra.addListener(increasingAbsoluteFixedCosts);

        if(toBoolean(getProperty(Parameter.BREAK_SCHEDULING.toString()))) {
            vra.addListener(new BreakScheduling(vrp, stateManager, constraintManager));
        }
        handleExecutorShutdown(vra);
        vra.setMaxIterations(Integer.valueOf(properties.getProperty(Parameter.ITERATIONS.toString())));

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
                        costs += vrp.getActivityCosts().getActivityCost(prevAct, act, act.getArrTime(), route.getDriver(), route.getVehicle());
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
