/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.algorithm.io;


import jsprit.core.algorithm.*;
import jsprit.core.algorithm.acceptor.*;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap.*;
import jsprit.core.algorithm.listener.AlgorithmEndsListener;
import jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.PrioritizedVRAListener;
import jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.Priority;
import jsprit.core.algorithm.module.RuinAndRecreateModule;
import jsprit.core.algorithm.recreate.InsertionStrategy;
import jsprit.core.algorithm.recreate.listener.InsertionListener;
import jsprit.core.algorithm.ruin.RadialRuinStrategyFactory;
import jsprit.core.algorithm.ruin.RandomRuinStrategyFactory;
import jsprit.core.algorithm.ruin.RuinStrategy;
import jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import jsprit.core.algorithm.ruin.distance.JobDistance;
import jsprit.core.algorithm.selector.SelectBest;
import jsprit.core.algorithm.selector.SelectRandomly;
import jsprit.core.algorithm.selector.SolutionSelector;
import jsprit.core.algorithm.state.*;
import jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import jsprit.core.algorithm.termination.PrematureAlgorithmTermination;
import jsprit.core.algorithm.termination.TimeTermination;
import jsprit.core.algorithm.termination.VariationCoefficientTermination;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.*;
import jsprit.core.util.ActivityTimeTracker;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VehicleRoutingAlgorithms {
	
	static class TypedMap {
		
		static interface AbstractKey<K> {
			
		    Class<K> getType();
		}
		
		static class AcceptorKey implements AbstractKey<SolutionAcceptor>{

			private ModKey modKey;
			
			public AcceptorKey(ModKey modKey) {
				super();
				this.modKey = modKey;
			}

			
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((modKey == null) ? 0 : modKey.hashCode());
				return result;
			}


			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (!(obj instanceof AcceptorKey))
					return false;
				AcceptorKey other = (AcceptorKey) obj;
				if (modKey == null) {
					if (other.modKey != null)
						return false;
				} else if (!modKey.equals(other.modKey))
					return false;
				return true;
			}


			@Override
			public Class<SolutionAcceptor> getType() {
				return SolutionAcceptor.class;
			}
			
		}
		
		static class SelectorKey implements AbstractKey<SolutionSelector>{

			private ModKey modKey;
			
			public SelectorKey(ModKey modKey) {
				super();
				this.modKey = modKey;
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((modKey == null) ? 0 : modKey.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				SelectorKey other = (SelectorKey) obj;
				if (modKey == null) {
					if (other.modKey != null)
						return false;
				} else if (!modKey.equals(other.modKey))
					return false;
				return true;
			}



			@Override
			public Class<SolutionSelector> getType() {
				return SolutionSelector.class;
			}
			
		}
		
		static class StrategyModuleKey implements AbstractKey<SearchStrategyModule>{

			private ModKey modKey;
			
			public StrategyModuleKey(ModKey modKey) {
				super();
				this.modKey = modKey;
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((modKey == null) ? 0 : modKey.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				StrategyModuleKey other = (StrategyModuleKey) obj;
				if (modKey == null) {
					if (other.modKey != null)
						return false;
				} else if (!modKey.equals(other.modKey))
					return false;
				return true;
			}



			@Override
			public Class<SearchStrategyModule> getType() {
				return SearchStrategyModule.class;
			}
			
		}
		
		static class RuinStrategyKey implements AbstractKey<RuinStrategy>{

			private ModKey modKey;
			
			public RuinStrategyKey(ModKey modKey) {
				super();
				this.modKey = modKey;
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((modKey == null) ? 0 : modKey.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				RuinStrategyKey other = (RuinStrategyKey) obj;
				if (modKey == null) {
					if (other.modKey != null)
						return false;
				} else if (!modKey.equals(other.modKey))
					return false;
				return true;
			}



			@Override
			public Class<RuinStrategy> getType() {
				return RuinStrategy.class;
			}
			
		}

		static class InsertionStrategyKey implements AbstractKey<InsertionStrategy>{

			private ModKey modKey;
			
			public InsertionStrategyKey(ModKey modKey) {
				super();
				this.modKey = modKey;
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((modKey == null) ? 0 : modKey.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				InsertionStrategyKey other = (InsertionStrategyKey) obj;
				if (modKey == null) {
					if (other.modKey != null)
						return false;
				} else if (!modKey.equals(other.modKey))
					return false;
				return true;
			}



			@Override
			public Class<InsertionStrategy> getType() {
				return InsertionStrategy.class;
			}
			
		}
				
		private Map<AbstractKey<?>, Object> map = new HashMap<AbstractKey<?>, Object>();

		public <T> T get(AbstractKey<T> key) {
			if(map.get(key) == null) return null;
	        return key.getType().cast(map.get(key));
	    }

	    public <T> T put(AbstractKey<T> key, T value) {
	        return key.getType().cast(map.put(key, key.getType().cast(value)));
	    }
	    
	    public Set<AbstractKey<?>> keySet(){
	    	return map.keySet();
	    }
	}
	
	static class ModKey {
		private String name;
		private String id;
		
		public ModKey(String name, String id) {
			super();
			this.name = name;
			this.id = id;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ModKey other = (ModKey) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
		
	}
	
	private static Logger log = LogManager.getLogger(VehicleRoutingAlgorithms.class.getName());
	
	private VehicleRoutingAlgorithms(){}
	
	/**
	 * Creates a {@link jsprit.core.algorithm.VehicleRoutingAlgorithm} from a AlgorithConfig based on the input vrp.
	 * 
	 * @param vrp the routing problem
	 * @param algorithmConfig the algorithm config
	 * @return {@link jsprit.core.algorithm.VehicleRoutingAlgorithm}
	 */
	public static VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp, final AlgorithmConfig algorithmConfig){
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(),0, null);
	}
	public static VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp, int nThreads, final AlgorithmConfig algorithmConfig){
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(),nThreads, null);
	}
	
	/**
	 * Read and creates a {@link VehicleRoutingAlgorithm} from an url.
	 * 
	 * @param vrp the routing problem
	 * @param configURL config url
	 * @return {@link jsprit.core.algorithm.VehicleRoutingAlgorithm}
	 */
	public static VehicleRoutingAlgorithm readAndCreateAlgorithm(final VehicleRoutingProblem vrp, final URL configURL){
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		AlgorithmConfigXmlReader xmlReader = new AlgorithmConfigXmlReader(algorithmConfig);
		xmlReader.read(configURL);
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(),0, null);
	}
	public static VehicleRoutingAlgorithm readAndCreateAlgorithm(final VehicleRoutingProblem vrp, int nThreads, final URL configURL){
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		AlgorithmConfigXmlReader xmlReader = new AlgorithmConfigXmlReader(algorithmConfig);
		xmlReader.read(configURL);
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(),nThreads, null);
	}
	
	/**
	 * Read and creates {@link jsprit.core.problem.VehicleRoutingProblem} from config-file.
	 * 
	 * @param vrp the routing problem
	 * @param configFileName the config filename (and location)
	 * @return {@link jsprit.core.algorithm.VehicleRoutingAlgorithm}
	 */
	public static VehicleRoutingAlgorithm readAndCreateAlgorithm(final VehicleRoutingProblem vrp, final String configFileName){
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		AlgorithmConfigXmlReader xmlReader = new AlgorithmConfigXmlReader(algorithmConfig);
		xmlReader.read(configFileName);
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(),0, null);
	}

	public static VehicleRoutingAlgorithm readAndCreateAlgorithm(final VehicleRoutingProblem vrp, final String configFileName, StateManager stateManager){
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		AlgorithmConfigXmlReader xmlReader = new AlgorithmConfigXmlReader(algorithmConfig);
		xmlReader.read(configFileName);
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(),0, stateManager);
	}
	
	public static VehicleRoutingAlgorithm readAndCreateAlgorithm(final VehicleRoutingProblem vrp, int nThreads, final String configFileName, StateManager stateManager){
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		AlgorithmConfigXmlReader xmlReader = new AlgorithmConfigXmlReader(algorithmConfig);
		xmlReader.read(configFileName);
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(), nThreads, stateManager);
	}
	
	public static VehicleRoutingAlgorithm readAndCreateAlgorithm(VehicleRoutingProblem vrp, int nThreads, String configFileName) {
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		AlgorithmConfigXmlReader xmlReader = new AlgorithmConfigXmlReader(algorithmConfig);
		xmlReader.read(configFileName);
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(),nThreads, null);
	}
	
	private static class OpenRouteStateVerifier implements StateUpdater, ReverseActivityVisitor{

		private End end;
		
		private boolean firstAct = true;
		
		private Vehicle vehicle;
		
		@Override
		public void begin(VehicleRoute route) {
			end = route.getEnd();
			vehicle = route.getVehicle();
		}

		@Override
		public void visit(TourActivity activity) {
			if(firstAct){
				firstAct=false;
				if(!vehicle.isReturnToDepot()){
					assert activity.getLocation().getId().equals(end.getLocation().getId()) : "route end and last activity are not equal even route is open. this should not be.";
				}
			}
			
		}

		@Override
		public void finish() {
			firstAct = true;
		}
		
	}

	private static VehicleRoutingAlgorithm createAlgo(final VehicleRoutingProblem vrp, XMLConfiguration config, int nuOfThreads, StateManager stateMan){
		//create state-manager
		final StateManager stateManager;
		if(stateMan!=null) {
			stateManager = stateMan;
		}
		else{
			stateManager = 	new StateManager(vrp);
		}
		stateManager.updateLoadStates();
		stateManager.updateTimeWindowStates();
        stateManager.updateSkillStates();
		stateManager.addStateUpdater(new UpdateEndLocationIfRouteIsOpen());
		stateManager.addStateUpdater(new OpenRouteStateVerifier());
//		stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts()));
//		stateManager.addStateUpdater(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));

		/*
		 * define constraints
		 */
		//constraint manager
		ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
		constraintManager.addTimeWindowConstraint();
		constraintManager.addLoadConstraint();
        constraintManager.addSkillsConstraint();
		
		return readAndCreateAlgorithm(vrp, config, nuOfThreads, null, stateManager, constraintManager, true);	
	}

	public static VehicleRoutingAlgorithm readAndCreateAlgorithm(final VehicleRoutingProblem vrp, AlgorithmConfig config,
			int nuOfThreads, SolutionCostCalculator solutionCostCalculator, final StateManager stateManager, ConstraintManager constraintManager, boolean addDefaultCostCalculators) {
		return readAndCreateAlgorithm(vrp, config.getXMLConfiguration(),nuOfThreads, solutionCostCalculator, stateManager, constraintManager, addDefaultCostCalculators);
	}
	
	private static VehicleRoutingAlgorithm readAndCreateAlgorithm(final VehicleRoutingProblem vrp, XMLConfiguration config,
			int nuOfThreads, final SolutionCostCalculator solutionCostCalculator, final StateManager stateManager, ConstraintManager constraintManager, boolean addDefaultCostCalculators) {
		// map to store constructed modules
		TypedMap definedClasses = new TypedMap();
		
		// algorithm listeners
		Set<PrioritizedVRAListener> algorithmListeners = new HashSet<PrioritizedVRAListener>();
		
		// insertion listeners
		List<InsertionListener> insertionListeners = new ArrayList<InsertionListener>();


		//threading
		final ExecutorService executorService;
		if(nuOfThreads > 0){
			log.debug("setup executor-service with " + nuOfThreads + " threads");
			executorService = Executors.newFixedThreadPool(nuOfThreads);
			algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, new AlgorithmEndsListener() {

				@Override
				public void informAlgorithmEnds(VehicleRoutingProblem problem,Collection<VehicleRoutingProblemSolution> solutions) {
					log.debug("shutdown executor-service");
					executorService.shutdown();
				}
			}));
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread arg0, Throwable arg1) {
					System.err.println(arg1.toString());
					System.exit(0);
				}
			});
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					if(!executorService.isShutdown()){
						System.err.println("shutdowHook shuts down executorService");
						executorService.shutdown();
					}
				}
			});
		}
		else executorService = null; 


		//create fleetmanager
		final VehicleFleetManager vehicleFleetManager = createFleetManager(vrp);

        String switchString = config.getString("construction.insertion.allowVehicleSwitch");
        final boolean switchAllowed;
        if(switchString != null){
            switchAllowed = Boolean.parseBoolean(switchString);
        }
        else switchAllowed = true;
        ActivityTimeTracker.ActivityPolicy activityPolicy;
        if(stateManager.timeWindowUpdateIsActivated()){
            UpdateVehicleDependentPracticalTimeWindows timeWindowUpdater = new UpdateVehicleDependentPracticalTimeWindows(stateManager,vrp.getTransportCosts());
            timeWindowUpdater.setVehiclesToUpdate(new UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate() {
				Map<VehicleTypeKey,Vehicle> uniqueTypes = new HashMap<VehicleTypeKey,Vehicle>();

				@Override
				public Collection<Vehicle> get(VehicleRoute vehicleRoute) {
					if(uniqueTypes.isEmpty()){
						for( Vehicle v : vrp.getVehicles()){
							if(!uniqueTypes.containsKey(v.getVehicleTypeIdentifier())){
								uniqueTypes.put(v.getVehicleTypeIdentifier(),v);
							}
						}
					}
					Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
					vehicles.addAll(uniqueTypes.values());
					return vehicles;
				}
            });
            stateManager.addStateUpdater(timeWindowUpdater);
            activityPolicy = ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS;
        }
        else{
            activityPolicy = ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_ARRIVED;
        }
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(),activityPolicy));
        stateManager.addStateUpdater(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager, activityPolicy));

		final SolutionCostCalculator costCalculator;
		if(solutionCostCalculator==null) costCalculator = getDefaultCostCalculator(stateManager);
		else costCalculator = solutionCostCalculator;

        PrettyAlgorithmBuilder prettyAlgorithmBuilder = PrettyAlgorithmBuilder.newInstance(vrp,vehicleFleetManager,stateManager,constraintManager);
		//construct initial solution creator 
		final InsertionStrategy initialInsertionStrategy = createInitialSolution(config,vrp,vehicleFleetManager,stateManager,algorithmListeners,definedClasses,executorService,nuOfThreads,costCalculator, constraintManager, addDefaultCostCalculators);
        if(initialInsertionStrategy != null) prettyAlgorithmBuilder.constructInitialSolutionWith(initialInsertionStrategy,costCalculator);

		//construct algorithm, i.e. search-strategies and its modules
		int solutionMemory = config.getInt("strategy.memory");
        List<HierarchicalConfiguration> strategyConfigs = config.configurationsAt("strategy.searchStrategies.searchStrategy");
		for(HierarchicalConfiguration strategyConfig : strategyConfigs){
			String name = getName(strategyConfig);
			SolutionAcceptor acceptor = getAcceptor(strategyConfig,vrp,algorithmListeners,definedClasses,solutionMemory);
			SolutionSelector selector = getSelector(strategyConfig,vrp,algorithmListeners,definedClasses);
			
			SearchStrategy strategy = new SearchStrategy(name, selector, acceptor, costCalculator);
			strategy.setName(name);
			List<HierarchicalConfiguration> modulesConfig = strategyConfig.configurationsAt("modules.module");
			for(HierarchicalConfiguration moduleConfig : modulesConfig){
				SearchStrategyModule module = buildModule(moduleConfig,vrp,vehicleFleetManager,stateManager,algorithmListeners,definedClasses,executorService,nuOfThreads, constraintManager, addDefaultCostCalculators);
				strategy.addModule(module);
			}
            prettyAlgorithmBuilder.withStrategy(strategy,strategyConfig.getDouble("probability"));
        }

		//construct algorithm
		VehicleRoutingAlgorithm metaAlgorithm = prettyAlgorithmBuilder.build();
        int maxIterations = getMaxIterations(config);
        if(maxIterations > -1) metaAlgorithm.setMaxIterations(maxIterations);

		//define prematureBreak
		PrematureAlgorithmTermination prematureAlgorithmTermination = getPrematureTermination(config, algorithmListeners);
		if(prematureAlgorithmTermination != null) metaAlgorithm.setPrematureAlgorithmTermination(prematureAlgorithmTermination);
        else{
            List<HierarchicalConfiguration> terminationCriteria = config.configurationsAt("terminationCriteria.termination");
            for(HierarchicalConfiguration terminationConfig : terminationCriteria){
                PrematureAlgorithmTermination termination = getTerminationCriterion(terminationConfig, algorithmListeners);
                if(termination != null) metaAlgorithm.addTerminationCriterion(termination);
            }
        }
        for(PrioritizedVRAListener l : algorithmListeners){
            metaAlgorithm.getAlgorithmListeners().add(l);
        }
        return metaAlgorithm;
	}

    private static int getMaxIterations(XMLConfiguration config) {
        String maxIterationsString = config.getString("iterations");
        if(maxIterationsString == null) maxIterationsString = config.getString("maxIterations");
        if(maxIterationsString != null) return (Integer.parseInt(maxIterationsString));
        return -1;
    }

    private static SolutionCostCalculator getDefaultCostCalculator(final StateManager stateManager) {
		return new VariablePlusFixedSolutionCostCalculatorFactory(stateManager).createCalculator();
    }

	private static VehicleFleetManager createFleetManager(final VehicleRoutingProblem vrp) {
		if(vrp.getFleetSize().equals(FleetSize.INFINITE)){
			return new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

		}
		else if(vrp.getFleetSize().equals(FleetSize.FINITE)){ 
			return new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager(); 
		}
		throw new IllegalStateException("fleet size can only be infinite or finite. " +
				"makes sure your config file contains one of these options");
	}

    private static PrematureAlgorithmTermination getTerminationCriterion(HierarchicalConfiguration config, Set<PrioritizedVRAListener> algorithmListeners) {
        String basedOn = config.getString("[@basedOn]");
        if(basedOn == null){
            log.debug("set default prematureBreak, i.e. no premature break at all.");
            return null;
        }
        if(basedOn.equals("iterations")){
            log.debug("set prematureBreak based on iterations");
            String iter = config.getString("iterations");
            if(iter == null) throw new IllegalStateException("iterations is missing");
            int iterations = Integer.valueOf(iter);
            return new IterationWithoutImprovementTermination(iterations);
        }
        if(basedOn.equals("time")){
            log.debug("set prematureBreak based on time");
            String timeString = config.getString("time");
            if(timeString == null) throw new IllegalStateException("time is missing");
            long time = Long.parseLong(timeString);
            TimeTermination timeBreaker = new TimeTermination(time);
            algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, timeBreaker));
            return timeBreaker;
        }
        if(basedOn.equals("variationCoefficient")){
            log.debug("set prematureBreak based on variation coefficient");
            String thresholdString = config.getString("threshold");
            String iterationsString = config.getString("iterations");
            if(thresholdString == null) throw new IllegalStateException("threshold is missing");
            if(iterationsString == null) throw new IllegalStateException("iterations is missing");
            double threshold = Double.valueOf(thresholdString);
            int iterations = Integer.valueOf(iterationsString);
            VariationCoefficientTermination variationCoefficientBreaker = new VariationCoefficientTermination(iterations, threshold);
            algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, variationCoefficientBreaker));
            return variationCoefficientBreaker;
        }
        throw new IllegalStateException("prematureBreak basedOn " + basedOn + " is not defined");
    }

	private static PrematureAlgorithmTermination getPrematureTermination(XMLConfiguration config, Set<PrioritizedVRAListener> algorithmListeners) {
		String basedOn = config.getString("prematureBreak[@basedOn]");
		if(basedOn == null){
			log.debug("set default prematureBreak, i.e. no premature break at all.");
			return null;
		}
		if(basedOn.equals("iterations")){
			log.debug("set prematureBreak based on iterations");
			String iter = config.getString("prematureBreak.iterations");
			if(iter == null) throw new IllegalStateException("prematureBreak.iterations is missing");
			int iterations = Integer.valueOf(iter);
			return new IterationWithoutImprovementTermination(iterations);
		}
		if(basedOn.equals("time")){
			log.debug("set prematureBreak based on time");
			String timeString = config.getString("prematureBreak.time");
			if(timeString == null) throw new IllegalStateException("prematureBreak.time is missing");
			long time = Long.parseLong(timeString);
			TimeTermination timeBreaker = new TimeTermination(time);
			algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, timeBreaker));
			return timeBreaker;
		}
		if(basedOn.equals("variationCoefficient")){
			log.debug("set prematureBreak based on variation coefficient");
			String thresholdString = config.getString("prematureBreak.threshold");
			String iterationsString = config.getString("prematureBreak.iterations");
			if(thresholdString == null) throw new IllegalStateException("prematureBreak.threshold is missing");
			if(iterationsString == null) throw new IllegalStateException("prematureBreak.iterations is missing");
			double threshold = Double.valueOf(thresholdString);
			int iterations = Integer.valueOf(iterationsString);
			VariationCoefficientTermination variationCoefficientBreaker = new VariationCoefficientTermination(iterations, threshold);
			algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, variationCoefficientBreaker));
			return variationCoefficientBreaker;
		}
		throw new IllegalStateException("prematureBreak basedOn " + basedOn + " is not defined");
	}

    private static String getName(HierarchicalConfiguration strategyConfig) { if(strategyConfig.containsKey("[@name]")){
			return strategyConfig.getString("[@name]");
		}
		return "";
	}	

	private static InsertionStrategy createInitialSolution(XMLConfiguration config, final VehicleRoutingProblem vrp, VehicleFleetManager vehicleFleetManager, final StateManager routeStates, Set<PrioritizedVRAListener> algorithmListeners, TypedMap definedClasses, ExecutorService executorService, int nuOfThreads, final SolutionCostCalculator solutionCostCalculator, ConstraintManager constraintManager, boolean addDefaultCostCalculators) {
		List<HierarchicalConfiguration> modConfigs = config.configurationsAt("construction.insertion");
		if(modConfigs == null) return null;
		if(modConfigs.isEmpty()) return null;
		if(modConfigs.size() != 1) throw new IllegalStateException("#construction.modules != 1. 1 expected");
		HierarchicalConfiguration modConfig = modConfigs.get(0);
		String insertionName = modConfig.getString("[@name]");
		if(insertionName == null) throw new IllegalStateException("insertion[@name] is missing.");
		String insertionId = modConfig.getString("[@id]");
		if(insertionId == null) insertionId = "noId";
		ModKey modKey = makeKey(insertionName,insertionId);
		InsertionStrategyKey insertionStrategyKey = new InsertionStrategyKey(modKey);
		InsertionStrategy insertionStrategy = definedClasses.get(insertionStrategyKey);
		if(insertionStrategy == null){
			List<PrioritizedVRAListener> prioListeners = new ArrayList<PrioritizedVRAListener>();
			insertionStrategy = createInsertionStrategy(modConfig, vrp, vehicleFleetManager, routeStates, prioListeners, executorService, nuOfThreads, constraintManager, addDefaultCostCalculators);
			algorithmListeners.addAll(prioListeners);
			definedClasses.put(insertionStrategyKey,insertionStrategy);
		}
		return insertionStrategy;
    }
	
	private static SolutionSelector getSelector(HierarchicalConfiguration strategyConfig, VehicleRoutingProblem vrp, Set<PrioritizedVRAListener> algorithmListeners, TypedMap definedSelectors) {
		String selectorName = strategyConfig.getString("selector[@name]");
		if(selectorName == null) throw new IllegalStateException("no solutionSelector defined. define either \"selectRandomly\" or \"selectBest\"");
		String selectorId = strategyConfig.getString("selector[@id]");
		if(selectorId == null) selectorId="noId";
		ModKey modKey = makeKey(selectorName,selectorId);
		SelectorKey selectorKey = new SelectorKey(modKey);
		SolutionSelector definedSelector = definedSelectors.get(selectorKey); 
		if(definedSelector != null) {
			return definedSelector;
		}
		if(selectorName.equals("selectRandomly")){
			SelectRandomly selector = SelectRandomly.getInstance();
			definedSelectors.put(selectorKey, selector);
			return selector;
		}
		if(selectorName.equals("selectBest")){
			SelectBest selector = SelectBest.getInstance();
			definedSelectors.put(selectorKey, selector);
			return selector;
		}
		throw new IllegalStateException("solutionSelector is not know. Currently, it only knows \"selectRandomly\" and \"selectBest\"");
	}
	
	private static ModKey makeKey(String name, String id){
		return new ModKey(name, id);
	}
	
	private static SolutionAcceptor getAcceptor(HierarchicalConfiguration strategyConfig, VehicleRoutingProblem vrp, Set<PrioritizedVRAListener> algorithmListeners, TypedMap typedMap, int solutionMemory) {
		String acceptorName = strategyConfig.getString("acceptor[@name]");
		if(acceptorName == null) throw new IllegalStateException("no solution acceptor is defined");
		String acceptorId = strategyConfig.getString("acceptor[@id]");
		if(acceptorId == null) acceptorId = "noId";
		AcceptorKey acceptorKey = new AcceptorKey(makeKey(acceptorName,acceptorId));
		SolutionAcceptor definedAcceptor = typedMap.get(acceptorKey);
		if(definedAcceptor != null) return definedAcceptor; 
		if(acceptorName.equals("acceptNewRemoveWorst")){
			GreedyAcceptance acceptor = new GreedyAcceptance(solutionMemory);
			typedMap.put(acceptorKey, acceptor);
			return acceptor;
		}
		if(acceptorName.equals("acceptNewRemoveFirst")){
			AcceptNewRemoveFirst acceptor = new AcceptNewRemoveFirst(solutionMemory);
			typedMap.put(acceptorKey, acceptor);
			return acceptor;
		}
		if(acceptorName.equals("greedyAcceptance")){
			GreedyAcceptance acceptor = new GreedyAcceptance(solutionMemory);
			typedMap.put(acceptorKey, acceptor);
			return acceptor;
		}
		if(acceptorName.equals("greedyAcceptance_minVehFirst")){
			GreedyAcceptance_minVehFirst acceptor = new GreedyAcceptance_minVehFirst(solutionMemory);
			typedMap.put(acceptorKey, acceptor);
			return acceptor;
		}
		if(acceptorName.equals("schrimpfAcceptance")){
			String nuWarmupIterations = strategyConfig.getString("acceptor.warmup");
			double alpha = strategyConfig.getDouble("acceptor.alpha");
			SchrimpfAcceptance schrimpf = new SchrimpfAcceptance(solutionMemory, alpha);
			if(nuWarmupIterations!=null){
				SchrimpfInitialThresholdGenerator iniThresholdGenerator = new SchrimpfInitialThresholdGenerator(schrimpf, Integer.parseInt(nuWarmupIterations));
				algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, iniThresholdGenerator));
			}
			else{
				double threshold = strategyConfig.getDouble("acceptor.initialThreshold");
				schrimpf.setInitialThreshold(threshold);
			}
			algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, schrimpf));
			typedMap.put(acceptorKey, schrimpf);
			return schrimpf;
		}
		if(acceptorName.equals("experimentalSchrimpfAcceptance")){
			int iterOfSchrimpf = strategyConfig.getInt("acceptor.warmup");
			double alpha = strategyConfig.getDouble("acceptor.alpha");
			ExperimentalSchrimpfAcceptance schrimpf = new ExperimentalSchrimpfAcceptance(solutionMemory, alpha, iterOfSchrimpf);
			algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, schrimpf));
			typedMap.put(acceptorKey, schrimpf);
			return schrimpf;
		}
		else{
			throw new IllegalStateException("solution acceptor " + acceptorName + " is not known");
		}
	}
	
	private static SearchStrategyModule buildModule(HierarchicalConfiguration moduleConfig, final VehicleRoutingProblem vrp, VehicleFleetManager vehicleFleetManager, 
			final StateManager routeStates, Set<PrioritizedVRAListener> algorithmListeners, TypedMap definedClasses, ExecutorService executorService, int nuOfThreads, ConstraintManager constraintManager, boolean addDefaultCostCalculators) {
		String moduleName = moduleConfig.getString("[@name]");
		if(moduleName == null) throw new IllegalStateException("module(-name) is missing.");
		String moduleId = moduleConfig.getString("[@id]");
		if(moduleId == null) moduleId = "noId";
		ModKey modKey = makeKey(moduleName,moduleId);
		StrategyModuleKey strategyModuleKey = new StrategyModuleKey(modKey);
		SearchStrategyModule definedModule = definedClasses.get(strategyModuleKey);
		if(definedModule != null) return definedModule; 
		
		if(moduleName.equals("ruin_and_recreate")){
			String ruin_name = moduleConfig.getString("ruin[@name]");
			if(ruin_name == null) throw new IllegalStateException("module.ruin[@name] is missing.");
			String ruin_id = moduleConfig.getString("ruin[@id]");
			if(ruin_id == null) ruin_id = "noId";
			String shareToRuinString = moduleConfig.getString("ruin.share");
			if(shareToRuinString == null) throw new IllegalStateException("module.ruin.share is missing.");
			double shareToRuin = Double.valueOf(shareToRuinString);
			final RuinStrategy ruin;
			ModKey ruinKey = makeKey(ruin_name,ruin_id);
			if(ruin_name.equals("randomRuin")){
				ruin = getRandomRuin(vrp, routeStates, definedClasses, ruinKey, shareToRuin);
			}
			else if(ruin_name.equals("radialRuin")){
				JobDistance jobDistance = new AvgServiceAndShipmentDistance(vrp.getTransportCosts());
				ruin = getRadialRuin(vrp, routeStates, definedClasses, ruinKey, shareToRuin, jobDistance);
			}
			else throw new IllegalStateException("ruin[@name] " + ruin_name + " is not known. Use either randomRuin or radialRuin.");
			
			String insertionName = moduleConfig.getString("insertion[@name]");
			if(insertionName == null) throw new IllegalStateException("module.insertion[@name] is missing. set it to \"regretInsertion\" or \"bestInsertion\"");
			String insertionId = moduleConfig.getString("insertion[@id]");
			if(insertionId == null) insertionId = "noId";
			ModKey insertionKey = makeKey(insertionName,insertionId);
			InsertionStrategyKey insertionStrategyKey = new InsertionStrategyKey(insertionKey);
			InsertionStrategy insertion = definedClasses.get(insertionStrategyKey);
			if(insertion == null){
				List<HierarchicalConfiguration> insertionConfigs = moduleConfig.configurationsAt("insertion");
				if(insertionConfigs.size() != 1) throw new IllegalStateException("this should be 1");
				List<PrioritizedVRAListener> prioListeners = new ArrayList<PrioritizedVRAListener>();
				insertion = createInsertionStrategy(insertionConfigs.get(0), vrp, vehicleFleetManager, routeStates, prioListeners, executorService, nuOfThreads, constraintManager, addDefaultCostCalculators);
				algorithmListeners.addAll(prioListeners);
			}
			final InsertionStrategy final_insertion = insertion;
		
			RuinAndRecreateModule rrModule =  new RuinAndRecreateModule("ruin_and_recreate", final_insertion, ruin);
			return rrModule;
		}
		throw new NullPointerException("no module found with moduleName=" + moduleName + 
				"\n\tcheck config whether the correct names are used" +
				"\n\tcurrently there are following modules available: " +
				"\n\tbestInsertion" +
				"\n\trandomRuin" +
				"\n\tradialRuin");
	}

	private static RuinStrategy getRadialRuin(final VehicleRoutingProblem vrp, final StateManager routeStates, TypedMap definedClasses, ModKey modKey, double shareToRuin, JobDistance jobDistance) {
		RuinStrategyKey stratKey = new RuinStrategyKey(modKey);
		RuinStrategy ruin = definedClasses.get(stratKey);
		if(ruin == null){
			ruin = new RadialRuinStrategyFactory(shareToRuin, jobDistance).createStrategy(vrp);
			definedClasses.put(stratKey, ruin);
		}
		return ruin;
	}

	private static RuinStrategy getRandomRuin(final VehicleRoutingProblem vrp, final StateManager routeStates, TypedMap definedClasses, ModKey modKey, double shareToRuin) {
		RuinStrategyKey stratKey = new RuinStrategyKey(modKey);
		RuinStrategy ruin = definedClasses.get(stratKey);
		if(ruin == null){
			ruin = new RandomRuinStrategyFactory(shareToRuin).createStrategy(vrp);
			definedClasses.put(stratKey, ruin);
		}
		return ruin;
	}
	
	private static InsertionStrategy createInsertionStrategy(HierarchicalConfiguration moduleConfig, VehicleRoutingProblem vrp,VehicleFleetManager vehicleFleetManager, StateManager routeStates, List<PrioritizedVRAListener> algorithmListeners, ExecutorService executorService, int nuOfThreads, ConstraintManager constraintManager, boolean addDefaultCostCalculators) {
		return InsertionFactory.createInsertion(vrp, moduleConfig, vehicleFleetManager, routeStates, algorithmListeners, executorService, nuOfThreads, constraintManager, addDefaultCostCalculators);
	}

	
	

}
