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

        private VehicleFleetManager fleetManager = null;

        private JobFilter jobFilter = null;

        private JobInsertionCostsCalculatorFactory serviceCalculatorFactory = null;

        private JobInsertionCostsCalculatorFactory shipmentCalculatorFactory = null;


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

    private final Map<SearchStrategy, Double> customStrategies = new HashMap<>();

    private final JobInsertionCostsCalculatorFactory serviceCalculatorFactory;

    private final JobInsertionCostsCalculatorFactory shipmentCalculatorFactory;

    private VehicleFleetManager vehicleFleetManager;

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
        regretScorer = builder.regretScorer;
        regretScoringFunction = builder.regretScoringFunction;
        customStrategies.putAll(builder.customStrategies);
        vehicleFleetManager = builder.fleetManager;
    }

    private void ini(VehicleRoutingProblem vrp) {
        if (regretScorer == null) {
            regretScorer = getRegretScorer(vrp);
        }
        if (regretScoringFunction == null) {
            regretScoringFunction = new DefaultRegretScoringFunction(regretScorer);
        }
    }

    private VehicleRoutingAlgorithm create(final VehicleRoutingProblem vrp) {
        ini(vrp);
        boolean isInfinite = vrp.getFleetSize().equals(VehicleRoutingProblem.FleetSize.INFINITE);
        if (vehicleFleetManager == null) {
            if (isInfinite) {
                vehicleFleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
            } else {
                FiniteFleetManagerFactory finiteFleetManagerFactory = new FiniteFleetManagerFactory(vrp.getVehicles());
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
            toInteger(properties.getProperty(Parameter.WORST_MIN_SHARE.toString())),
                toInteger(properties.getProperty(Parameter.WORST_MAX_SHARE.toString())),
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
                        .setShipmentInsertionCalculatorFactory(this.shipmentCalculatorFactory)
                    .build();
                regretInsertion.setRegretScoringFunction(regretScoringFunction);
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
                        .setServiceInsertionCalculator(this.serviceCalculatorFactory)
                        .setShipmentInsertionCalculatorFactory(this.shipmentCalculatorFactory)
                    .build();
                regretInsertion.setRegretScoringFunction(regretScoringFunction);
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
                        .setShipmentInsertionCalculatorFactory(this.shipmentCalculatorFactory)
                    .build();
                regretInsertion.setRegretScoringFunction(regretScoringFunction);
                regretInsertion.setDependencyTypes(constraintManager.getDependencyTypes());
                regret = regretInsertion;
            }
            else{
                RegretInsertion regretInsertion = (RegretInsertion) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionStrategyBuilder.Strategy.REGRET)
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .considerFixedCosts(toDouble(getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setActivityInsertionCostCalculator(activityInsertion)
                        .setServiceInsertionCalculator(this.serviceCalculatorFactory)
                        .setShipmentInsertionCalculatorFactory(this.shipmentCalculatorFactory)
                        .build();
                regretInsertion.setRegretScoringFunction(regretScoringFunction);
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
                    .setShipmentInsertionCalculatorFactory(this.shipmentCalculatorFactory)
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
                    .setShipmentInsertionCalculatorFactory(this.shipmentCalculatorFactory)
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
                    .setShipmentInsertionCalculatorFactory(this.shipmentCalculatorFactory)
                    .build();
        } else {
            cheapest = (CheapestInsertionConcurrent) new InsertionStrategyBuilder(vrp, vehicleFleetManager, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionStrategyBuilder.Strategy.CHEAPEST)
                    .setConcurrentMode(es, noThreads)
                    .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .setActivityInsertionCostCalculator(activityInsertion)
                    .setServiceInsertionCalculator(this.serviceCalculatorFactory)
                    .setShipmentInsertionCalculatorFactory(this.shipmentCalculatorFactory)
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
                .withStrategy(clustersCheapest, toDouble(getProperty(Strategy.CLUSTER_CHEAPEST.toString())))

            .withStrategy(stringBest, toDouble(getProperty(Strategy.STRING_BEST.toString())))
                .withStrategy(stringRegret, toDouble(getProperty(Strategy.STRING_REGRET.toString())))
                .withStrategy(stringCheapest, toDouble(getProperty(Strategy.STRING_CHEAPEST.toString())));

        for (SearchStrategy customStrategy : customStrategies.keySet()) {
            prettyBuilder.withStrategy(customStrategy, customStrategies.get(customStrategy));
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
