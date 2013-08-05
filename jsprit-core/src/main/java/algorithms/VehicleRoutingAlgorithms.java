/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms;


import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import util.RouteUtils;
import algorithms.VehicleRoutingAlgorithms.TypedMap.AbstractInsertionKey;
import algorithms.VehicleRoutingAlgorithms.TypedMap.AbstractKey;
import algorithms.VehicleRoutingAlgorithms.TypedMap.AcceptorKey;
import algorithms.VehicleRoutingAlgorithms.TypedMap.RuinStrategyKey;
import algorithms.VehicleRoutingAlgorithms.TypedMap.SelectorKey;
import algorithms.VehicleRoutingAlgorithms.TypedMap.StrategyModuleKey;
import algorithms.acceptors.AcceptNewIfBetterThanWorst;
import algorithms.acceptors.AcceptNewRemoveFirst;
import algorithms.acceptors.SchrimpfAcceptance;
import algorithms.acceptors.SolutionAcceptor;
import algorithms.selectors.SelectBest;
import algorithms.selectors.SelectRandomly;
import algorithms.selectors.SolutionSelector;
import basics.Job;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.FleetSize;
import basics.VehicleRoutingProblemSolution;
import basics.algo.AlgorithmStartsListener;
import basics.algo.InsertionListener;
import basics.algo.IterationWithoutImprovementBreaker;
import basics.algo.PrematureAlgorithmBreaker;
import basics.algo.SearchStrategy;
import basics.algo.SearchStrategyManager;
import basics.algo.SearchStrategyModule;
import basics.algo.SearchStrategyModuleListener;
import basics.algo.TimeBreaker;
import basics.algo.VariationCoefficientBreaker;
import basics.algo.SearchStrategy.DiscoveredSolution;
import basics.algo.VehicleRoutingAlgorithmListeners.PrioritizedVRAListener;
import basics.algo.VehicleRoutingAlgorithmListeners.Priority;
import basics.io.AlgorithmConfig;
import basics.io.AlgorithmConfigXmlReader;



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

		static class AbstractInsertionKey implements AbstractKey<AbstractInsertionStrategy>{

			private ModKey modKey;
			
			public AbstractInsertionKey(ModKey modKey) {
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
				AbstractInsertionKey other = (AbstractInsertionKey) obj;
				if (modKey == null) {
					if (other.modKey != null)
						return false;
				} else if (!modKey.equals(other.modKey))
					return false;
				return true;
			}



			@Override
			public Class<AbstractInsertionStrategy> getType() {
				return AbstractInsertionStrategy.class;
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
	
	private static Logger log = Logger.getLogger(VehicleRoutingAlgorithms.class);
	
	private VehicleRoutingAlgorithms(){}
	
	/**
	 * Creates a {@link VehicleRoutingAlgorithm} from a AlgorithConfig based on the input vrp.
	 * 
	 * @param vrp
	 * @param algorithmConfig
	 * @return {@link VehicleRoutingAlgorithm}
	 */
	public static VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp, final AlgorithmConfig algorithmConfig){
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(),null,0);
	}
	
	@Deprecated
	public static VehicleRoutingAlgorithm readAndCreateAlgorithm(final VehicleRoutingProblem vrp, final XMLConfiguration config){
		return createAlgo(vrp,config,null,0);
	}
	
	/**
	 * Read and creates a {@link VehicleRoutingAlgorithm} from an url.
	 * 
	 * @param vrp
	 * @param configURL
	 * @return {@link VehicleRoutingProblem}
	 */
	public static VehicleRoutingAlgorithm readAndCreateAlgorithm(final VehicleRoutingProblem vrp, final URL configURL){
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		AlgorithmConfigXmlReader xmlReader = new AlgorithmConfigXmlReader(algorithmConfig);
		xmlReader.read(configURL);
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(),null,0);
	}
	
	/**
	 * Read and creates {@link VehicleRoutingAlgorithm} from config-file.
	 * 
	 * @param vrp
	 * @param configFileName
	 * @return
	 */
	public static VehicleRoutingAlgorithm readAndCreateAlgorithm(final VehicleRoutingProblem vrp, final String configFileName){
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		AlgorithmConfigXmlReader xmlReader = new AlgorithmConfigXmlReader(algorithmConfig);
		xmlReader.read(configFileName);
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(),null, 0);
	}
	
	/**
	 * Read and creates {@link VehicleRoutingAlgorithm} from config-file.
	 * 
	 * @param vrp
	 * @param configFileName
	 * @param nuOfThreads TODO
	 * @param {@link ExecutorService}
	 * @return {@link VehicleRoutingAlgorithm}
	 */
	private static VehicleRoutingAlgorithm readAndCreateConcurrentAlgorithm(final VehicleRoutingProblem vrp, final String configFileName, final ExecutorService executorService, int nuOfThreads){
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		AlgorithmConfigXmlReader xmlReader = new AlgorithmConfigXmlReader(algorithmConfig);
		xmlReader.read(configFileName);
		return createAlgo(vrp,algorithmConfig.getXMLConfiguration(), executorService, nuOfThreads);
	}

	private static VehicleRoutingAlgorithm createAlgo(final VehicleRoutingProblem vrp, XMLConfiguration config, ExecutorService executorService, int nuOfThreads){
			
		//fleetmanager
		final VehicleFleetManager vehicleFleetManager;
		if(vrp.getFleetSize().equals(FleetSize.INFINITE)){
			vehicleFleetManager = new InfiniteVehicles(vrp.getVehicles());

		}
		else if(vrp.getFleetSize().equals(FleetSize.FINITE)){ 
			vehicleFleetManager = new VehicleFleetManagerImpl(vrp.getVehicles()); 
		}
		else{
			throw new IllegalStateException("fleet size can only be infinite or finite. " +
					"makes sure your config file contains one of these options");
		}

		Set<PrioritizedVRAListener> algorithmListeners = new HashSet<PrioritizedVRAListener>();
		List<InsertionListener> insertionListeners = new ArrayList<InsertionListener>();

		algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, new SolutionVerifier()));

		RouteStates routeStates = new RouteStates();
		routeStates.initialiseStateOfJobs(vrp.getJobs().values());
		algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, routeStates));
		
		TypedMap definedClasses = new TypedMap();

		/*
		 * initial solution - construction
		 */
		AlgorithmStartsListener createInitialSolution = createInitialSolution(config,vrp,vehicleFleetManager,routeStates,algorithmListeners,definedClasses,executorService,nuOfThreads);
		if(createInitialSolution != null) algorithmListeners.add(new PrioritizedVRAListener(Priority.MEDIUM, createInitialSolution));

		int solutionMemory = config.getInt("strategy.memory");
		SearchStrategyManager searchStratManager = new SearchStrategyManager();
		List<HierarchicalConfiguration> strategyConfigs = config.configurationsAt("strategy.searchStrategies.searchStrategy");
		for(HierarchicalConfiguration strategyConfig : strategyConfigs){
			String name = getName(strategyConfig);
			SolutionAcceptor acceptor = getAcceptor(strategyConfig,vrp,algorithmListeners,definedClasses,solutionMemory);
			SolutionSelector selector = getSelector(strategyConfig,vrp,algorithmListeners,definedClasses);
			SearchStrategy strategy = new SearchStrategy(selector, acceptor);
			strategy.setName(name);
			List<HierarchicalConfiguration> modulesConfig = strategyConfig.configurationsAt("modules.module");
			for(HierarchicalConfiguration moduleConfig : modulesConfig){
				SearchStrategyModule module = buildModule(moduleConfig,vrp,vehicleFleetManager,routeStates,algorithmListeners,definedClasses,executorService,nuOfThreads);
				strategy.addModule(module);
			}
			searchStratManager.addStrategy(strategy, strategyConfig.getDouble("probability"));
		}
		VehicleRoutingAlgorithm metaAlgorithm = new VehicleRoutingAlgorithm(vrp, searchStratManager);
		if(config.containsKey("iterations")){
			int iter = config.getInt("iterations");
			metaAlgorithm.setNuOfIterations(iter);
			log.info("set nuOfIterations to " + iter);
		}
		//prematureBreak
		PrematureAlgorithmBreaker prematureAlgoBreaker = getPrematureBreaker(config,algorithmListeners);
		metaAlgorithm.setPrematureAlgorithmBreaker(prematureAlgoBreaker);
		
		registerListeners(metaAlgorithm,algorithmListeners);
		registerInsertionListeners(definedClasses,insertionListeners);
		return metaAlgorithm;	
	}

	private static PrematureAlgorithmBreaker getPrematureBreaker(XMLConfiguration config, Set<PrioritizedVRAListener> algorithmListeners) {
		String basedOn = config.getString("prematureBreak[@basedOn]");
		if(basedOn == null){
			log.info("set default prematureBreak, i.e. no premature break at all.");
			return new PrematureAlgorithmBreaker() {

				@Override
				public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
					return false;
				}
			};
		}
		if(basedOn.equals("iterations")){
			log.info("set prematureBreak based on iterations");
			String iter = config.getString("prematureBreak.iterations");
			if(iter == null) throw new IllegalStateException("prematureBreak.iterations is missing");
			int iterations = Integer.valueOf(iter);
			return new IterationWithoutImprovementBreaker(iterations);
		}
		if(basedOn.equals("time")){
			log.info("set prematureBreak based on time");
			String timeString = config.getString("prematureBreak.time");
			if(timeString == null) throw new IllegalStateException("prematureBreak.time is missing");
			double time = Double.valueOf(timeString);
			TimeBreaker timeBreaker = new TimeBreaker(time);
			algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, timeBreaker));
			return timeBreaker;
		}
		if(basedOn.equals("variationCoefficient")){
			log.info("set prematureBreak based on variation coefficient");
			String thresholdString = config.getString("prematureBreak.threshold");
			String iterationsString = config.getString("prematureBreak.iterations");
			if(thresholdString == null) throw new IllegalStateException("prematureBreak.threshold is missing");
			if(iterationsString == null) throw new IllegalStateException("prematureBreak.iterations is missing");
			double threshold = Double.valueOf(thresholdString);
			int iterations = Integer.valueOf(iterationsString);
			VariationCoefficientBreaker variationCoefficientBreaker = new VariationCoefficientBreaker(iterations, threshold);
			algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, variationCoefficientBreaker));
			return variationCoefficientBreaker;
		}
		throw new IllegalStateException("prematureBreak basedOn " + basedOn + " is not defined");
	}

	private static void registerInsertionListeners(TypedMap definedClasses, List<InsertionListener> insertionListeners) {
		for(AbstractKey<?> key : definedClasses.keySet()){
			if(key instanceof AbstractInsertionKey){
				AbstractInsertionKey insertionKey = (AbstractInsertionKey) key;
				AbstractInsertionStrategy insertionStrategy = definedClasses.get(insertionKey);
				for(InsertionListener l : insertionListeners){
					//							log.info("add insertionListener " + l + " to " + insertionStrategy);
					insertionStrategy.addListener(l);
				}
			}
		}
		//				log.warn("cannot register insertion listeners yet");
	}

	private static String getName(HierarchicalConfiguration strategyConfig) {
		if(strategyConfig.containsKey("[@name]")){
			return strategyConfig.getString("[@name]");
		}
		return "";
	}	

	
	private static void registerListeners(VehicleRoutingAlgorithm metaAlgorithm, Set<PrioritizedVRAListener> algorithmListeners) {
		metaAlgorithm.getAlgorithmListeners().addAll(algorithmListeners);
	}
	
	private static AlgorithmStartsListener createInitialSolution(XMLConfiguration config, final VehicleRoutingProblem vrp, VehicleFleetManager vehicleFleetManager, RouteStates activityStates, Set<PrioritizedVRAListener> algorithmListeners, TypedMap definedClasses, ExecutorService executorService, int nuOfThreads) {
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
		AbstractInsertionKey insertionStrategyKey = new AbstractInsertionKey(modKey);
		AbstractInsertionStrategy insertionStrategy = definedClasses.get(insertionStrategyKey);
		if(insertionStrategy == null){
			List<PrioritizedVRAListener> prioListeners = new ArrayList<PrioritizedVRAListener>();
			insertionStrategy = createInsertionStrategy(modConfig, vrp, vehicleFleetManager, activityStates, prioListeners, executorService, nuOfThreads);
			algorithmListeners.addAll(prioListeners);
			definedClasses.put(insertionStrategyKey,insertionStrategy);
		}
		final AbstractInsertionStrategy finalInsertionStrategy = insertionStrategy;

		return new AlgorithmStartsListener() {

			@Override
			public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {

				CreateInitialSolution createInitialSolution = new CreateInitialSolution(finalInsertionStrategy);
				createInitialSolution.setGenerateAsMuchAsRoutesAsVehiclesExist(false);
				VehicleRoutingProblemSolution vrpSol = createInitialSolution.createInitialSolution(vrp);
				solutions.add(vrpSol);

			}
		};


	}
	
	private static SolutionSelector getSelector(HierarchicalConfiguration strategyConfig, VehicleRoutingProblem vrp, Set<PrioritizedVRAListener> algorithmListeners, TypedMap definedSelectors) {
		String selectorName = strategyConfig.getString("selector[@name]");
		if(selectorName == null) throw new IllegalStateException("no solutionSelector defined. define either \"selectRandom\" or \"selectBest\"");
		String selectorId = strategyConfig.getString("selector[@id]");
		if(selectorId == null) selectorId="noId";
		ModKey modKey = makeKey(selectorName,selectorId);
		SelectorKey selectorKey = new SelectorKey(modKey);
		SolutionSelector definedSelector = definedSelectors.get(selectorKey); 
		if(definedSelector != null) {
			return definedSelector;
		}
		if(selectorName.equals("selectRandom")){
			SelectRandomly selector = SelectRandomly.getInstance();
			definedSelectors.put(selectorKey, selector);
			return selector;
		}
		if(selectorName.equals("selectBest")){
			SelectBest selector = SelectBest.getInstance();
			definedSelectors.put(selectorKey, selector);
			return selector;
		}
		throw new IllegalStateException("solutionSelector is not know. Currently, it only knows \"selectRandom\" and \"selectBest\"");
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
			AcceptNewIfBetterThanWorst acceptor = new AcceptNewIfBetterThanWorst(solutionMemory);
			typedMap.put(acceptorKey, acceptor);
			return acceptor;
		}
		if(acceptorName.equals("acceptNewRemoveFirst")){
			AcceptNewRemoveFirst acceptor = new AcceptNewRemoveFirst(solutionMemory);
			typedMap.put(acceptorKey, acceptor);
			return acceptor;
		}
		if(acceptorName.equals("schrimpfAcceptance")){
			int iterOfSchrimpf = strategyConfig.getInt("acceptor.warmup");
			double alpha = strategyConfig.getDouble("acceptor.alpha");
			SchrimpfAcceptance schrimpf = new SchrimpfAcceptance(solutionMemory, alpha, iterOfSchrimpf);
			algorithmListeners.add(new PrioritizedVRAListener(Priority.LOW, schrimpf));
			typedMap.put(acceptorKey, schrimpf);
			return schrimpf;
		}
		else{
			throw new IllegalStateException("solution acceptor " + acceptorName + " is not known");
		}
	}
	
	private static SearchStrategyModule buildModule(HierarchicalConfiguration moduleConfig, VehicleRoutingProblem vrp, VehicleFleetManager vehicleFleetManager, 
			RouteStates activityStates, Set<PrioritizedVRAListener> algorithmListeners, TypedMap definedClasses, ExecutorService executorService, int nuOfThreads) {
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
				ruin = getRandomRuin(vrp, activityStates, definedClasses, ruinKey, shareToRuin);
			}
			else if(ruin_name.equals("radialRuin")){
				String ruin_distance = moduleConfig.getString("ruin.distance");
				JobDistance jobDistance;
				if(ruin_distance == null) jobDistance = new JobDistanceAvgCosts(vrp.getTransportCosts());
				else {
					if(ruin_distance.equals("euclidean")){
						jobDistance = new EuclideanServiceDistance();
					}
					else throw new IllegalStateException("does not know ruin.distance " + ruin_distance + ". either ommit ruin.distance then the "
							+ "default is used or use 'euclidean'");
				}
				ruin = getRadialRuin(vrp, activityStates, definedClasses, ruinKey, shareToRuin, jobDistance);
			}
			else throw new IllegalStateException("ruin[@name] " + ruin_name + " is not known. Use either randomRuin or radialRuin.");
			
			String insertionName = moduleConfig.getString("insertion[@name]");
			if(insertionName == null) throw new IllegalStateException("module.insertion[@name] is missing. set it to \"regretInsertion\" or \"bestInsertion\"");
			String insertionId = moduleConfig.getString("insertion[@id]");
			if(insertionId == null) insertionId = "noId";
			ModKey insertionKey = makeKey(insertionName,insertionId);
			AbstractInsertionKey insertionStrategyKey = new AbstractInsertionKey(insertionKey);
			AbstractInsertionStrategy insertion = definedClasses.get(insertionStrategyKey);
			if(insertion == null){
				List<HierarchicalConfiguration> insertionConfigs = moduleConfig.configurationsAt("insertion");
				if(insertionConfigs.size() != 1) throw new IllegalStateException("this should be 1");
				List<PrioritizedVRAListener> prioListeners = new ArrayList<PrioritizedVRAListener>();
				insertion = createInsertionStrategy(insertionConfigs.get(0), vrp, vehicleFleetManager, activityStates, prioListeners, executorService, nuOfThreads);
				algorithmListeners.addAll(prioListeners);
			}
			final AbstractInsertionStrategy final_insertion = insertion;
			SearchStrategyModule module = new SearchStrategyModule() {
				
				private Logger logger = Logger.getLogger(SearchStrategyModule.class);
				
				@Override
				public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
					Collection<Job> ruinedJobs = ruin.ruin(vrpSolution.getRoutes());
					final_insertion.run(vrpSolution.getRoutes(), ruinedJobs, Double.MAX_VALUE);
					double totalCost = RouteUtils.getTotalCost(vrpSolution.getRoutes());
					vrpSolution.setCost(totalCost);
					return vrpSolution;
				}
				
				@Override
				public String toString() {
					return getName();
				}
				
				@Override
				public String getName() {
					return "[name=ruin_and_recreate][ruin="+ruin+"][recreate="+final_insertion+"]";
				}
				
				@Override
				public void addModuleListener(SearchStrategyModuleListener moduleListener) {
					if(moduleListener instanceof InsertionListener){
						InsertionListener iListener = (InsertionListener) moduleListener; 
						if(!final_insertion.getListener().contains(iListener)){
							logger.info("register moduleListener " + moduleListener);
							final_insertion.addListener(iListener);
						}
						
					}
					
				}
			};
			return module;
		}
	
		if(moduleName.equals("gendreau")){
			int iterations = moduleConfig.getInt("iterations");
			double share = moduleConfig.getDouble("share");
			String ruinName = moduleConfig.getString("ruin[@name]");
			if(ruinName == null) throw new IllegalStateException("gendreau.ruin[@name] is missing. set it to \"radialRuin\" or \"randomRuin\"");
			String ruinId = moduleConfig.getString("ruin[@id]");
			if(ruinId == null) ruinId = "noId";
			ModKey ruinKey = makeKey(ruinName,ruinId);
			RuinStrategyKey stratKey = new RuinStrategyKey(ruinKey);
			RuinStrategy ruin = definedClasses.get(stratKey);
			if(ruin == null){
				ruin = RuinRadial.newInstance(vrp, 0.3, new JobDistanceAvgCosts(vrp.getTransportCosts()), new JobRemoverImpl(), new TourStateUpdater(activityStates, vrp.getTransportCosts(), vrp.getActivityCosts()));
				definedClasses.put(stratKey, ruin);
			}
			
			String insertionName = moduleConfig.getString("insertion[@name]");
			if(insertionName == null) throw new IllegalStateException("gendreau.insertion[@name] is missing. set it to \"regretInsertion\" or \"bestInsertion\"");
			String insertionId = moduleConfig.getString("insertion[@id]");
			if(insertionId == null) insertionId = "noId";
			ModKey insertionKey = makeKey(insertionName,insertionId);
			AbstractInsertionKey insertionStrategyKey = new AbstractInsertionKey(insertionKey);
			AbstractInsertionStrategy insertion = definedClasses.get(insertionStrategyKey);
			if(insertion == null){
				List<HierarchicalConfiguration> insertionConfigs = moduleConfig.configurationsAt("insertion");
				if(insertionConfigs.size() != 1) throw new IllegalStateException("this should be 1");
				List<PrioritizedVRAListener> prioListeners = new ArrayList<PrioritizedVRAListener>();
				insertion = createInsertionStrategy(insertionConfigs.get(0), vrp, vehicleFleetManager, activityStates, prioListeners, executorService, nuOfThreads);
				algorithmListeners.addAll(prioListeners);
			}
			Gendreau gendreau = new Gendreau(vrp, ruin, insertion);
			gendreau.setShareOfJobsToRuin(share);
			gendreau.setNuOfIterations(iterations);
			gendreau.setFleetManager(vehicleFleetManager);
			definedClasses.put(strategyModuleKey, gendreau);
			return gendreau;
		}
		throw new NullPointerException("no module found with moduleName=" + moduleName + 
				"\n\tcheck config whether the correct names are used" +
				"\n\tcurrently there are following modules available: " +
				"\n\tbestInsertion" +
				"\n\trandomRuin" +
				"\n\tradialRuin" + 
				"\n\tgendreauPostOpt");
	}

	private static RuinStrategy getRadialRuin(VehicleRoutingProblem vrp, RouteStates activityStates, TypedMap definedClasses, ModKey modKey, double shareToRuin, JobDistance jobDistance) {
		RuinStrategyKey stratKey = new RuinStrategyKey(modKey);
		RuinStrategy ruin = definedClasses.get(stratKey);
		if(ruin == null){
			ruin = RuinRadial.newInstance(vrp, shareToRuin, jobDistance, new JobRemoverImpl(), new TourStateUpdater(activityStates, vrp.getTransportCosts(), vrp.getActivityCosts()));
			definedClasses.put(stratKey, ruin);
		}
		return ruin;
	}

	private static RuinStrategy getRandomRuin(VehicleRoutingProblem vrp,
			RouteStates activityStates, TypedMap definedClasses,
			ModKey modKey, double shareToRuin) {
		RuinStrategyKey stratKey = new RuinStrategyKey(modKey);
		RuinStrategy ruin = definedClasses.get(stratKey);
		if(ruin == null){
			ruin = RuinRandom.newInstance(vrp, shareToRuin, new JobRemoverImpl(), new TourStateUpdater(activityStates, vrp.getTransportCosts(), vrp.getActivityCosts()));
			definedClasses.put(stratKey, ruin);
		}
		return ruin;
	}
	
	private static AbstractInsertionStrategy createInsertionStrategy(HierarchicalConfiguration moduleConfig, VehicleRoutingProblem vrp,VehicleFleetManager vehicleFleetManager, RouteStates activityStates, List<PrioritizedVRAListener> algorithmListeners, ExecutorService executorService, int nuOfThreads) {
		AbstractInsertionStrategy insertion = InsertionFactory.createInsertion(vrp, moduleConfig, vehicleFleetManager, activityStates, algorithmListeners, executorService, nuOfThreads);
		return insertion;
	}
	

}
