package jsprit.analysis.toolbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmFactory;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.BenchmarkInstance;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ComputationalLaboratory {
	
	/**
	 * Listener-interface to listen to calculation.
	 * 
	 * <p>Note that calculations are run concurrently, i.e. a unique task that is distributed to an available thread is
	 * {algorithm, instance, run}.
	 * 
	 * @author schroeder
	 *
	 */
	public static interface CalculationListener {
		
		public void calculationStarts(final BenchmarkInstance p, final String algorithmName, final VehicleRoutingAlgorithm algorithm, final int run);
		
		public void calculationEnds(final BenchmarkInstance p, final String algorithmName, final VehicleRoutingAlgorithm algorithm, final int run, final Collection<VehicleRoutingProblemSolution> solutions);
		
	}
	
	/**
	 * Collects whatever indicators you require by algorithmName, instanceName, run and indicator.
	 * 
	 * @author schroeder
	 *
	 */
	public static class DataCollector {
		
		public static class Key {
			private String instanceName;
			private String algorithmName;
			private int run;
			private String indicatorName;
			
			public Key(String instanceName, String algorithmName, int run,String indicatorName) {
				super();
				this.instanceName = instanceName;
				this.algorithmName = algorithmName;
				this.run = run;
				this.indicatorName = indicatorName;
			}
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime
						* result
						+ ((algorithmName == null) ? 0 : algorithmName
								.hashCode());
				result = prime
						* result
						+ ((indicatorName == null) ? 0 : indicatorName
								.hashCode());
				result = prime
						* result
						+ ((instanceName == null) ? 0 : instanceName.hashCode());
				result = prime * result + run;
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
				Key other = (Key) obj;
				if (algorithmName == null) {
					if (other.algorithmName != null)
						return false;
				} else if (!algorithmName.equals(other.algorithmName))
					return false;
				if (indicatorName == null) {
					if (other.indicatorName != null)
						return false;
				} else if (!indicatorName.equals(other.indicatorName))
					return false;
				if (instanceName == null) {
					if (other.instanceName != null)
						return false;
				} else if (!instanceName.equals(other.instanceName))
					return false;
				if (run != other.run)
					return false;
				return true;
			}
			public String getInstanceName() {
				return instanceName;
			}
			public String getAlgorithmName() {
				return algorithmName;
			}
			public int getRun() {
				return run;
			}
			public String getIndicatorName() {
				return indicatorName;
			}
			
			@Override
			public String toString() {
				return "[algorithm="+algorithmName+"][instance="+instanceName+"][run="+run+"][indicator="+indicatorName+"]";
			}
			
		}
		
		private ConcurrentHashMap<Key, Double> data = new ConcurrentHashMap<ComputationalLaboratory.DataCollector.Key, Double>();
		
		/**
		 * Adds a single date by instanceName, algorithmName, run and indicatorName. 
		 * <p>If there is already an entry for this instance, algorithm, run and indicatorName, it is overwritten.
		 * 
		 * @param instanceName
		 * @param algorithmName
		 * @param run
		 * @param indicatorName
		 * @param value
		 */
		public void addDate(String instanceName, String algorithmName, int run, String indicatorName, double value){
			Key key = new Key(instanceName,algorithmName,run,indicatorName);
			data.put(key, value);
		}
		
		/**
		 * Returns a collections of indicator values representing the calculated values of individual runs.
		 * 
		 * @param instanceName
		 * @param algorithmName
		 * @param indicator
		 * @return
		 */
		public Collection<Double> getData(String instanceName, String algorithmName, String indicator){
			List<Double> values = new ArrayList<Double>();
			for(Key key : data.keySet()){
				if(key.getAlgorithmName().equals(algorithmName) && key.getInstanceName().equals(instanceName) && key.getIndicatorName().equals(indicator)){
					values.add(data.get(key));
				}
			}
			return values;
		}
		
		/**
		 * Returns indicator value.
		 * 
		 * @param instanceName
		 * @param algorithmName
		 * @param run
		 * @param indicator
		 * @return
		 */
		public Double getDate(String instanceName, String algorithmName, int run, String indicator){
			return data.get(new Key(instanceName,algorithmName,run,indicator));
		}

		/**
		 * Returns all keys that have been created. A key is a unique combination of algorithmName, instanceName, run and indicator.
		 * 
		 * @return
		 */
		public Set<Key> keySet(){
			return data.keySet();
		}
		
		/**
		 * Returns date associated to specified key.
		 * 
		 * @param key
		 * @return
		 */
		public Double getData(Key key){
			return data.get(key);
		}
		
	}
	
	
	private static class Algorithm {
		
		private String name;
		
		private VehicleRoutingAlgorithmFactory factory;

		public Algorithm(String name, VehicleRoutingAlgorithmFactory factory) {
			super();
			this.name = name;
			this.factory = factory;
		}
		
	}
	
	private List<BenchmarkInstance> benchmarkInstances = new ArrayList<BenchmarkInstance>();

	private int runs = 1;
	
	private Collection<CalculationListener> listeners = new ArrayList<ComputationalLaboratory.CalculationListener>();
	
	private List<Algorithm> algorithms = new ArrayList<ComputationalLaboratory.Algorithm>();
	
	private Set<String> algorithmNames = new HashSet<String>();
	
	private Set<String> instanceNames = new HashSet<String>();
	
	private int threads = Runtime.getRuntime().availableProcessors()+1;
	
	public ComputationalLaboratory() {
		Logger.getRootLogger().setLevel(Level.ERROR);
	}
	
	/**
	 * Adds algorithmFactory by name.
	 * 
	 * @param name
	 * @param factory
	 * @throws IllegalStateException if there is already an algorithmFactory with the same name
	 */
	public void addAlgorithmFactory(String name, VehicleRoutingAlgorithmFactory factory){
		if(algorithmNames.contains(name)) throw new IllegalStateException("there is already a algorithmFactory with the same name (algorithmName="+name+"). unique names are required.");
		algorithms.add(new Algorithm(name,factory));
		algorithmNames.add(name);
	}
	
	public Collection<String> getAlgorithmNames() {
		return algorithmNames;
	}
	
	public Collection<String> getInstanceNames(){
		return instanceNames;
	}

	/**
	 * Adds instance by name.
	 * 
	 * @param name
	 * @param problem
	 * @throws IllegalStateException if there is already an instance with the same name.
	 */
	public void addInstance(String name, VehicleRoutingProblem problem){
		if(benchmarkInstances.contains(name)) throw new IllegalStateException("there is already an instance with the same name (instanceName="+name+"). unique names are required.");
		benchmarkInstances.add(new BenchmarkInstance(name,problem,null,null));
		instanceNames.add(name);
	}
	
	/**
	 * Adds instance.
	 * 
	 * @param name
	 * @param problem
	 * @throws IllegalStateException if there is already an instance with the same name.
	 */
	public void addInstance(BenchmarkInstance instance){
		if(benchmarkInstances.contains(instance.name)) throw new IllegalStateException("there is already an instance with the same name (instanceName="+instance.name+"). unique names are required.");
		benchmarkInstances.add(instance);
		instanceNames.add(instance.name);
	}
	
	/**
	 * Adds collection of instances.
	 * 
	 * @param name
	 * @param problem
	 * @throws IllegalStateException if there is already an instance with the same name.
	 */
	public void addAllInstances(Collection<BenchmarkInstance> instances){
		for(BenchmarkInstance i : instances){
			addInstance(i);
		}
	}
	
	/**
	 * Adds instance by name, and with best known results.
	 * 
	 * @param name
	 * @param problem
	 * @throws IllegalStateException if there is already an instance with the same name.
	 */
	public void addInstance(String name, VehicleRoutingProblem problem, Double bestKnownResult, Double bestKnownVehicles){
		addInstance(new BenchmarkInstance(name,problem,bestKnownResult,bestKnownVehicles));
	}
	
	/**
	 * Adds listener to listen computational experiments.
	 * 
	 * @param listener
	 */
	public void addListener(CalculationListener listener){
		listeners.add(listener);
	}
	
	/**
	 * Sets nuOfRuns with same algorithm on same instance.
	 * <p>Default is 1
	 * 
	 * @param runs
	 */
	public void setNuOfRuns(int runs){
		this.runs = runs;
	}
	
	/**
	 * Runs experiments.
	 * 
	 * <p>If nuThreads > 1 it runs them concurrently, i.e. individual runs are distributed to available threads. Therefore 
	 * a unique task is defined by its algorithmName, instanceName and its runNumber.
	 * <p>If you have one algorithm called "myAlgorithm" and one instance called "myInstance", and you need to run "myAlgorithm" on "myInstance" three times
	 * with three threads then "myAlgorithm","myInstance",run1 runs on the first thread, "myAlgorithm", "myInstance", run2 on the second etc.
	 * <p>You can register whatever analysisTool you require by implementing and registering CalculationListener. Then your tool is informed just
	 * before a calculation starts as well as just after a calculation has been finished.
	 * 
	 * @see CalculationListener
	 * @throws IllegalStateException if either no algorithm or no instance has been specified 
	 */
	public void run(){
		if(algorithms.isEmpty()){
			throw new IllegalStateException("no algorithm specified. at least one algorithm needs to be specified.");
		}
		if(benchmarkInstances.isEmpty()){
			throw new IllegalStateException("no instance specified. at least one instance needs to be specified.");
		}
		System.out.println("start benchmarking [nuAlgorithms="+algorithms.size()+"][nuInstances=" + benchmarkInstances.size() + "][runsPerInstance=" + runs + "]");
		double startTime = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for(final Algorithm algorithm : algorithms){
			for(final BenchmarkInstance p : benchmarkInstances){
				for(int run=0;run<runs;run++){
					final int r = run;
					executor.submit(new Runnable(){

						@Override
						public void run() {
							runAlgorithm(p, algorithm, r+1);
						}
						
					});
				}
			}
		}
		try {
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("benchmarking done [time="+(System.currentTimeMillis()-startTime)/1000 + "sec]");
	}

	/**
	 * Sets number of threads.
	 * <p>By default: <code>nuThreads = Runtime.getRuntime().availableProcessors()+1</code>
	 * 
	 * @param threads
	 */
	public void setThreads(int threads) {
		this.threads = threads;
	}

	private void runAlgorithm(BenchmarkInstance p, Algorithm algorithm, int run) {
		System.out.println("[algorithm=" + algorithm.name + "][instance="+p.name+"][run="+run+"][status=start]");
		VehicleRoutingAlgorithm vra = algorithm.factory.createAlgorithm(p.vrp);
		informCalculationStarts(p, algorithm.name, vra, run);
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		System.out.println("[algorithm=" + algorithm.name + "][instance="+p.name+"][run="+run+"][status=finished]");
		informCalculationsEnds(p, algorithm.name, vra, run, solutions);
	}

	private void informCalculationStarts(BenchmarkInstance p, String name, VehicleRoutingAlgorithm vra, int run) {
		for(CalculationListener l : listeners) l.calculationStarts(p, name, vra, run);
	}

	private void informCalculationsEnds(BenchmarkInstance p, String name, VehicleRoutingAlgorithm vra, int run,
			Collection<VehicleRoutingProblemSolution> solutions) {
		for(CalculationListener l : listeners) l.calculationEnds(p, name, vra, run, solutions);
	}

}
