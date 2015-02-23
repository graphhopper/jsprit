/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.analysis.toolbox;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmFactory;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.BenchmarkInstance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ComputationalLaboratory {

	public static interface LabListener {

	}

	/**
	 * Listener-interface to listen to calculation.
	 * 
	 * <p>Note that calculations are run concurrently, i.e. a unique task that is distributed to an available thread is
	 * {algorithm, instance, run}.
	 * 
	 * @author schroeder
	 *
	 */
	public static interface CalculationListener extends LabListener{
		
		public void calculationStarts(final BenchmarkInstance p, final String algorithmName, final VehicleRoutingAlgorithm algorithm, final int run);
		
		public void calculationEnds(final BenchmarkInstance p, final String algorithmName, final VehicleRoutingAlgorithm algorithm, final int run, final Collection<VehicleRoutingProblemSolution> solutions);
		
	}

	public static interface LabStartsAndEndsListener extends LabListener {

		public void labStarts(List<BenchmarkInstance> instances, int noAlgorithms, int runs);

		public void labEnds();
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
		
		private final static String SOLUTION_INDICATOR_NAME = "vehicle-routing-problem-solution"; 
		
		private ConcurrentHashMap<Key, Double> data = new ConcurrentHashMap<ComputationalLaboratory.DataCollector.Key, Double>();
		
		private ConcurrentHashMap<Key, VehicleRoutingProblemSolution> solutions = new ConcurrentHashMap<ComputationalLaboratory.DataCollector.Key, VehicleRoutingProblemSolution>();
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
			if(indicatorName.equals(SOLUTION_INDICATOR_NAME)) throw new IllegalArgumentException(indicatorName + " is already used internally. please choose another indicator-name.");
			Key key = new Key(instanceName,algorithmName,run,indicatorName);
			data.put(key, value);
		}
		
		public void addSolution(String instanceName, String algorithmName, int run, VehicleRoutingProblemSolution solution){
			Key key = new Key(instanceName,algorithmName,run,SOLUTION_INDICATOR_NAME);
			solutions.put(key, solution);
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
		
		public VehicleRoutingProblemSolution getSolution(String instanceName, String algorithmName, int run){
			return solutions.get(new Key(instanceName,algorithmName,run,"solution"));
		}

		/**
		 * Returns all keys that have been created. A key is a unique combination of algorithmName, instanceName, run and indicator.
		 * 
		 * @return
		 */
		public Set<Key> getDataKeySet(){
			return data.keySet();
		}
		
		public Set<Key> getSolutionKeySet(){
			return solutions.keySet();
		}
		
		public VehicleRoutingProblemSolution getSolution(Key solutionKey){
			return solutions.get(solutionKey);
		}
		
		public Collection<VehicleRoutingProblemSolution> getSolutions(){
			return solutions.values();
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

	private Collection<LabStartsAndEndsListener> startsAndEndslisteners = new ArrayList<LabStartsAndEndsListener>();
	
	private List<Algorithm> algorithms = new ArrayList<ComputationalLaboratory.Algorithm>();
	
	private Set<String> algorithmNames = new HashSet<String>();
	
	private Set<String> instanceNames = new HashSet<String>();
	
	private int threads = 1;
	
	public ComputationalLaboratory() {

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
	 * @param instance the instance to be added
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
	 * @param instances collection of instances to be added
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
	public void addListener(LabListener listener){
		if(listener instanceof CalculationListener) {
			listeners.add((CalculationListener) listener);
		}
		if(listener instanceof LabStartsAndEndsListener){
			startsAndEndslisteners.add((LabStartsAndEndsListener) listener);
		}
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
		informStart();
		System.out.println("start benchmarking [nuAlgorithms="+algorithms.size()+"][nuInstances=" + benchmarkInstances.size() + "][runsPerInstance=" + runs + "]");
		double startTime = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for(final Algorithm algorithm : algorithms){
			for(final BenchmarkInstance p : benchmarkInstances){
				for(int run=0;run<runs;run++){
					final int r = run;
//					runAlgorithm(p, algorithm, r+1);

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
		informEnd();
	}

	private void informEnd() {
		for(LabStartsAndEndsListener l : startsAndEndslisteners){
			l.labEnds();
		}
	}

	private void informStart() {
		for(LabStartsAndEndsListener l : startsAndEndslisteners){
			l.labStarts(benchmarkInstances, algorithms.size(),runs);
		}
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
