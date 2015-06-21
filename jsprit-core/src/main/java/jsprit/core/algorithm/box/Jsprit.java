package jsprit.core.algorithm.box;

import jsprit.core.algorithm.PrettyAlgorithmBuilder;
import jsprit.core.algorithm.SearchStrategy;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.acceptor.SchrimpfAcceptance;
import jsprit.core.algorithm.listener.AlgorithmEndsListener;
import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.algorithm.module.RuinAndRecreateModule;
import jsprit.core.algorithm.recreate.*;
import jsprit.core.algorithm.ruin.*;
import jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import jsprit.core.algorithm.selector.SelectBest;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.FiniteFleetManagerFactory;
import jsprit.core.problem.vehicle.InfiniteFleetManagerFactory;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.util.NoiseMaker;
import jsprit.core.util.RandomNumberGeneration;
import jsprit.core.util.Solutions;

import java.util.Collection;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Jsprit {

    public enum Construction {

        BEST_INSERTION("best_insertion"), REGRET_INSERTION("regret_insertion");

        String name;

        Construction(String name){
            this.name = name;
        }

        public String toString(){
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
        CLUSTER_REGRET("cluster_regret");

        String strategyName;

        Strategy(String strategyName){
            this.strategyName = strategyName;
        }

        public String toString(){
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
        INSERTION_NOISE_LEVEL("insertion.noise_level"),
        INSERTION_NOISE_PROB("insertion.noise_prob"),
        RUIN_WORST_NOISE_LEVEL("worst.noise_level"),
        RUIN_WORST_NOISE_PROB("worst.noise_prob"),
        CONSTRUCTION("construction");

        String paraName;

        Parameter(String name){
            this.paraName = name;
        }

        public String toString(){
            return paraName;
        }

    }

    public static VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vehicleRoutingProblem){
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

        public static Builder newInstance(VehicleRoutingProblem vrp){
            return new Builder(vrp);
        }

        private Builder(VehicleRoutingProblem vrp){
            this.vrp = vrp;
            properties = new Properties(createDefaultProperties());
        }

        private Properties createDefaultProperties() {
            Properties defaults = new Properties();
            defaults.put(Strategy.RADIAL_BEST.toString(),"0.");
            defaults.put(Strategy.RADIAL_REGRET.toString(),".5");
            defaults.put(Strategy.RANDOM_BEST.toString(),".5");
            defaults.put(Strategy.RANDOM_REGRET.toString(),".5");
            defaults.put(Strategy.WORST_BEST.toString(),"0.");
            defaults.put(Strategy.WORST_REGRET.toString(),"1.");
            defaults.put(Strategy.CLUSTER_BEST.toString(),"0.");
            defaults.put(Strategy.CLUSTER_REGRET.toString(),"1.");
            defaults.put(Parameter.FIXED_COST_PARAM.toString(),"0.");
            defaults.put(Parameter.VEHICLE_SWITCH.toString(),"true");
            defaults.put(Parameter.ITERATIONS.toString(),"2000");
            defaults.put(Parameter.REGRET_DISTANCE_SCORER.toString(),".05");
            defaults.put(Parameter.REGRET_TIME_WINDOW_SCORER.toString(),"-.1");
            defaults.put(Parameter.THREADS.toString(),"1");
            int minShare = (int)Math.min(20, Math.max(3,vrp.getJobs().size() * 0.05));
            int maxShare = (int)Math.min(50, Math.max(5,vrp.getJobs().size() * 0.3));
            defaults.put(Parameter.RADIAL_MIN_SHARE.toString(),String.valueOf(minShare));
            defaults.put(Parameter.RADIAL_MAX_SHARE.toString(),String.valueOf(maxShare));
            defaults.put(Parameter.WORST_MIN_SHARE.toString(),String.valueOf(minShare));
            defaults.put(Parameter.WORST_MAX_SHARE.toString(),String.valueOf(maxShare));
            defaults.put(Parameter.CLUSTER_MIN_SHARE.toString(),String.valueOf(minShare));
            defaults.put(Parameter.CLUSTER_MAX_SHARE.toString(),String.valueOf(maxShare));
            int minShare_ = (int)Math.min(70, Math.max(5,vrp.getJobs().size() * 0.5));
            int maxShare_ = (int)Math.min(70, Math.max(5,vrp.getJobs().size() * 0.5));
            defaults.put(Parameter.RANDOM_REGRET_MIN_SHARE.toString(),String.valueOf(minShare_));
            defaults.put(Parameter.RANDOM_REGRET_MAX_SHARE.toString(),String.valueOf(maxShare_));
            defaults.put(Parameter.RANDOM_BEST_MIN_SHARE.toString(),String.valueOf(minShare_));
            defaults.put(Parameter.RANDOM_BEST_MAX_SHARE.toString(),String.valueOf(maxShare_));
            defaults.put(Parameter.THRESHOLD_ALPHA.toString(),String.valueOf(0.15));
            defaults.put(Parameter.THRESHOLD_INI.toString(),String.valueOf(0.03));
            defaults.put(Parameter.INSERTION_NOISE_LEVEL.toString(),String.valueOf(0.15));
            defaults.put(Parameter.INSERTION_NOISE_PROB.toString(),String.valueOf(0.2));
            defaults.put(Parameter.RUIN_WORST_NOISE_LEVEL.toString(),String.valueOf(0.15));
            defaults.put(Parameter.RUIN_WORST_NOISE_PROB.toString(),String.valueOf(0.2));
            defaults.put(Parameter.VEHICLE_SWITCH.toString(),String.valueOf(true));
            defaults.put(Parameter.CONSTRUCTION.toString(),Construction.REGRET_INSERTION.toString());
            return defaults;
        }


        public Builder setExecutorService(ExecutorService es, int noThreads) {
            this.es = es;
            this.noThreads = noThreads;
            return this;
        }

        public Builder setRandom(Random random){
            this.random = random;
            return this;
        }

        public Builder setProperty(String key, String value){
            properties.put(key,value);
            return this;
        }

        public Builder setProperty(Parameter parameter, String value){
            setProperty(parameter.toString(),value);
            return this;
        }

        public Builder setProperty(Strategy strategy, String value){
            setProperty(strategy.toString(),value);
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

        public VehicleRoutingAlgorithm buildAlgorithm(){
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
            if(maxShare < minShare) throw new IllegalArgumentException("maxShare must be equal or greater than minShare");
            this.minShare = minShare;
            this.maxShare = maxShare;
        }

        public RuinShareFactoryImpl(int minShare, int maxShare, Random random) {
            if(maxShare < minShare) throw new IllegalArgumentException("maxShare must be equal or greater than minShare");
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

    private Jsprit(Builder builder) {
        this.stateManager = builder.stateManager;
        this.constraintManager = builder.constraintManager;
        this.es = builder.es;
        this.noThreads = builder.noThreads;
        this.addCoreConstraints = builder.addConstraints;
        this.properties = builder.properties;
        this.objectiveFunction = builder.objectiveFunction;
        this.random = builder.random;
    }

    private VehicleRoutingAlgorithm create(final VehicleRoutingProblem vrp){
        VehicleFleetManager fm;
        if(vrp.getFleetSize().equals(VehicleRoutingProblem.FleetSize.INFINITE)){
            fm = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
        }
        else fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        if(stateManager == null){
            stateManager = new StateManager(vrp);
        }
        if(constraintManager == null){
            constraintManager = new ConstraintManager(vrp,stateManager);
        }

        double noiseLevel = toDouble(getProperty(Parameter.INSERTION_NOISE_LEVEL.toString()));
        double noiseProbability = toDouble(getProperty(Parameter.INSERTION_NOISE_PROB.toString()));
        final InsertionNoiseMaker noiseMaker = new InsertionNoiseMaker(vrp, noiseLevel, noiseProbability);
        noiseMaker.setRandom(random);
        constraintManager.addConstraint(noiseMaker);

        JobNeighborhoods jobNeighborhoods = new JobNeighborhoodsFactory().createNeighborhoods(vrp, new AvgServiceAndShipmentDistance(vrp.getTransportCosts()), (int) (vrp.getJobs().values().size() * 0.5));
        jobNeighborhoods.initialise();

        RuinRadial radial = new RuinRadial(vrp,vrp.getJobs().size(),jobNeighborhoods);
        radial.setRandom(random);
        radial.setRuinShareFactory(new RuinShareFactoryImpl(
                toInteger(properties.getProperty(Parameter.RADIAL_MIN_SHARE.toString())),
                toInteger(properties.getProperty(Parameter.RADIAL_MAX_SHARE.toString())),
                        random)
        );

        final RuinRandom random_for_regret = new RuinRandom(vrp,0.5);
        random_for_regret.setRandom(random);
        random_for_regret.setRuinShareFactory(new RuinShareFactoryImpl(
                        toInteger(properties.getProperty(Parameter.RANDOM_REGRET_MIN_SHARE.toString())),
                        toInteger(properties.getProperty(Parameter.RANDOM_REGRET_MAX_SHARE.toString())),
                        random)
        );

        final RuinRandom random_for_best = new RuinRandom(vrp,0.5);
        random_for_best.setRandom(random);
        random_for_best.setRuinShareFactory(new RuinShareFactoryImpl(
                        toInteger(properties.getProperty(Parameter.RANDOM_BEST_MIN_SHARE.toString())),
                        toInteger(properties.getProperty(Parameter.RANDOM_BEST_MAX_SHARE.toString())),
                        random)
        );

        final RuinWorst worst = new RuinWorst(vrp, (int) (vrp.getJobs().values().size()*0.5));
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
                        if(random.nextDouble() < toDouble(getProperty(Parameter.RUIN_WORST_NOISE_PROB.toString()))) {
                            return toDouble(getProperty(Parameter.RUIN_WORST_NOISE_LEVEL.toString()))
                                    * noiseMaker.maxCosts * random.nextDouble();
                        }
                        else return 0.;
                    }
                });
            }
        };

        final RuinClusters clusters = new RuinClusters(vrp,(int) (vrp.getJobs().values().size()*0.5),jobNeighborhoods);
        clusters.setRandom(random);
        clusters.setRuinShareFactory(new RuinShareFactoryImpl(
                        toInteger(properties.getProperty(Parameter.WORST_MIN_SHARE.toString())),
                        toInteger(properties.getProperty(Parameter.WORST_MAX_SHARE.toString())),
                        random)
        );

        AbstractInsertionStrategy regret;
        final RegretInsertion.DefaultScorer scorer;
        if(noThreads == null){
            noThreads = toInteger(getProperty(Parameter.THREADS.toString()));
        }
        if(noThreads > 1){
            if(es == null){
                setupExecutorInternally = true;
                es = Executors.newFixedThreadPool(noThreads);
            }
        }
        if(es != null) {
            RegretInsertionConcurrent regretInsertion = (RegretInsertionConcurrent) new InsertionBuilder(vrp, fm, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionBuilder.Strategy.REGRET)
                    .setConcurrentMode(es, noThreads)
                    .considerFixedCosts(toDouble(getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .build();
            scorer = getRegretScorer(vrp);
            regretInsertion.setScoringFunction(scorer);
            regret = regretInsertion;
        }
        else {
            RegretInsertion regretInsertion = (RegretInsertion) new InsertionBuilder(vrp, fm, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionBuilder.Strategy.REGRET)
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .considerFixedCosts(toDouble(getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .build();
            scorer = getRegretScorer(vrp);
            regretInsertion.setScoringFunction(scorer);
            regret = regretInsertion;
        }
        regret.setRandom(random);

        AbstractInsertionStrategy best;
        if(vrp.getJobs().size() < 250 || es == null) {
            BestInsertion bestInsertion = (BestInsertion) new InsertionBuilder(vrp, fm, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionBuilder.Strategy.BEST)
                    .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .build();
            best = bestInsertion;
        }
        else{
            BestInsertionConcurrent bestInsertion = (BestInsertionConcurrent) new InsertionBuilder(vrp, fm, stateManager, constraintManager)
                    .setInsertionStrategy(InsertionBuilder.Strategy.BEST)
                    .considerFixedCosts(Double.valueOf(properties.getProperty(Parameter.FIXED_COST_PARAM.toString())))
                    .setAllowVehicleSwitch(toBoolean(getProperty(Parameter.VEHICLE_SWITCH.toString())))
                    .setConcurrentMode(es, noThreads)
                    .build();
            best = bestInsertion;
        }
        best.setRandom(random);

        final SchrimpfAcceptance schrimpfAcceptance = new SchrimpfAcceptance(1,toDouble(getProperty(Parameter.THRESHOLD_ALPHA.toString())));
        IterationStartsListener schrimpfThreshold = new IterationStartsListener() {
            @Override
            public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
                if(i == 1){
                    double initialThreshold = Solutions.bestOf(solutions).getCost() * toDouble(getProperty(Parameter.THRESHOLD_INI.toString()));
                    schrimpfAcceptance.setInitialThreshold(initialThreshold);
                }
            }
        };

        SolutionCostCalculator objectiveFunction = getObjectiveFunction(vrp, noiseMaker.maxCosts);
        SearchStrategy radial_regret = new SearchStrategy(Strategy.RADIAL_REGRET.toString(),new SelectBest(),schrimpfAcceptance, objectiveFunction);
        radial_regret.addModule(new RuinAndRecreateModule(Strategy.RADIAL_REGRET.toString(), regret, radial));

        SearchStrategy radial_best = new SearchStrategy(Strategy.RADIAL_BEST.toString(),new SelectBest(),schrimpfAcceptance,objectiveFunction);
        radial_best.addModule(new RuinAndRecreateModule(Strategy.RADIAL_BEST.toString(),best,radial));

        SearchStrategy random_best = new SearchStrategy(Strategy.RANDOM_BEST.toString(),new SelectBest(),schrimpfAcceptance,objectiveFunction);
        random_best.addModule(new RuinAndRecreateModule(Strategy.RANDOM_BEST.toString(),best,random_for_best));

        SearchStrategy random_regret = new SearchStrategy(Strategy.RANDOM_REGRET.toString(),new SelectBest(),schrimpfAcceptance,objectiveFunction);
        random_regret.addModule(new RuinAndRecreateModule(Strategy.RANDOM_REGRET.toString(),regret,random_for_regret));

        SearchStrategy worst_regret = new SearchStrategy(Strategy.WORST_REGRET.toString(),new SelectBest(),schrimpfAcceptance,objectiveFunction);
        worst_regret.addModule(new RuinAndRecreateModule(Strategy.WORST_REGRET.toString(),regret,worst));

        SearchStrategy worst_best = new SearchStrategy(Strategy.WORST_BEST.toString(),new SelectBest(),schrimpfAcceptance,objectiveFunction);
        worst_best.addModule(new RuinAndRecreateModule(Strategy.WORST_BEST.toString(),best,worst));

        final SearchStrategy clusters_regret = new SearchStrategy(Strategy.CLUSTER_REGRET.toString(),new SelectBest(),schrimpfAcceptance,objectiveFunction);
        clusters_regret.addModule(new RuinAndRecreateModule(Strategy.CLUSTER_REGRET.toString(),regret,clusters));

        final SearchStrategy clusters_best = new SearchStrategy(Strategy.CLUSTER_BEST.toString(),new SelectBest(),schrimpfAcceptance,objectiveFunction);
        clusters_best.addModule(new RuinAndRecreateModule(Strategy.CLUSTER_BEST.toString(),best,clusters));


        PrettyAlgorithmBuilder prettyBuilder  = PrettyAlgorithmBuilder.newInstance(vrp, fm, stateManager, constraintManager);
        prettyBuilder.setRandom(random);
        if(addCoreConstraints){
            prettyBuilder.addCoreStateAndConstraintStuff();
        }
        prettyBuilder.withStrategy(radial_regret, toDouble(getProperty(Strategy.RADIAL_REGRET.toString())))
                .withStrategy(radial_best, toDouble(getProperty(Strategy.RADIAL_BEST.toString())))
                .withStrategy(random_best, toDouble(getProperty(Strategy.RANDOM_BEST.toString())))
                .withStrategy(random_regret, toDouble(getProperty(Strategy.RANDOM_REGRET.toString())))
                .withStrategy(worst_best, toDouble(getProperty(Strategy.WORST_BEST.toString())))
                .withStrategy(worst_regret, toDouble(getProperty(Strategy.WORST_REGRET.toString())))
                .withStrategy(clusters_regret,toDouble(getProperty(Strategy.CLUSTER_REGRET.toString())))
                .withStrategy(clusters_best,toDouble(getProperty(Strategy.CLUSTER_BEST.toString())));
        if (getProperty(Parameter.CONSTRUCTION.toString()).equals(Construction.BEST_INSERTION.toString())){
            prettyBuilder.constructInitialSolutionWith(best, objectiveFunction);
        }
        else{
            prettyBuilder.constructInitialSolutionWith(regret, objectiveFunction);
        }


        VehicleRoutingAlgorithm vra = prettyBuilder.build();
        vra.addListener(schrimpfThreshold);
        vra.addListener(noiseMaker);
        vra.addListener(noise);
        vra.addListener(clusters);
        handleExecutorShutdown(vra);
        vra.setMaxIterations(Integer.valueOf(properties.getProperty(Parameter.ITERATIONS.toString())));
        return vra;

    }

    private RegretInsertion.DefaultScorer getRegretScorer(VehicleRoutingProblem vrp) {
        RegretInsertion.DefaultScorer scorer;
        scorer = new RegretInsertion.DefaultScorer(vrp);
        scorer.setTimeWindowParam(Double.valueOf(properties.getProperty(Parameter.REGRET_TIME_WINDOW_SCORER.toString())));
        scorer.setDepotDistanceParam(Double.valueOf(properties.getProperty(Parameter.REGRET_DISTANCE_SCORER.toString())));
        return scorer;
    }


    private void handleExecutorShutdown(VehicleRoutingAlgorithm vra) {
        if(setupExecutorInternally){
            vra.addListener(new AlgorithmEndsListener() {

                @Override
                public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
                    es.shutdown();
                }

            });
        }
        if(es != null) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    if (!es.isShutdown()) {
                        System.err.println("shutdowHook shuts down executorService");
                        es.shutdown();
                    }
                }
            });
        }
    }

    String getProperty(String key){
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
        if(objectiveFunction != null) return objectiveFunction;

        SolutionCostCalculator solutionCostCalculator = new SolutionCostCalculator() {
            @Override
            public double getCosts(VehicleRoutingProblemSolution solution) {
                double costs = 0.;
                for(VehicleRoute route : solution.getRoutes()){
                    costs += route.getVehicle().getType().getVehicleCostParams().fix;
                    TourActivity prevAct = route.getStart();
                    for(TourActivity act : route.getActivities()){
                        costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(),act.getLocation(),prevAct.getEndTime(),route.getDriver(),route.getVehicle());
                        prevAct = act;
                    }
                    costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(),route.getEnd().getLocation(),prevAct.getEndTime(),route.getDriver(),route.getVehicle());
                }
                costs += solution.getUnassignedJobs().size() * maxCosts * 2;
                return costs;
            }
        };
        return solutionCostCalculator;
    }

}
