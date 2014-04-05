package jsprit.analysis.toolbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmFactory;
import jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.Priority;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.BenchmarkInstance;
import jsprit.core.util.Solutions;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ComputationalLaboratory {
	
	public abstract static interface Result {
		
		public abstract String getIndicatorName();
		
		public abstract double getResult();
	}
	
	public abstract static interface Indicator {
				
		public abstract void calculationStarts();
		
		public abstract void runStarts(VehicleRoutingAlgorithm vra);
		
		public abstract void runEnds(VehicleRoutingProblemSolution vrs);
		
		public abstract double getResult();
		
		public abstract String getIndicatorName();
		
	}
	
	public static class ResultContainer {
		
		private List<Result> results = new ArrayList<Result>();
		
		private String instanceName;
		
		private String algorithmName;
		
		public ResultContainer(String instanceName, String algorithmName) {
			this.instanceName = instanceName;
			this.algorithmName = algorithmName;
		}

		public void addResult(Result result){
			results.add(result);
		}

		public List<Result> getResults() {
			return results;
		}

		public String getInstanceName() { return instanceName; }
		
		public String getAlgorithmName() { return algorithmName; }
		
	}
	
	public abstract static interface IndicatorFactory {
		public abstract Indicator createIndicator(VehicleRoutingProblem vrp);
	}
	
	public static interface ResultWriter {
		public void writeResults(Collection<ResultContainer> results);
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
	
	private Collection<ResultWriter> writers = new ArrayList<ResultWriter>();
	
	private Collection<ResultContainer> results = new ArrayList<ResultContainer>();
	
	private Collection<IndicatorFactory> indicatorFactories = new ArrayList<IndicatorFactory>();
	
	private List<Algorithm> algorithms = new ArrayList<ComputationalLaboratory.Algorithm>();
	
	private int threads = Runtime.getRuntime().availableProcessors()+1;
	
	public ComputationalLaboratory() {
		Logger.getRootLogger().setLevel(Level.ERROR);
	}
	
	public void addResultWriter(ResultWriter writer){
		writers.add(writer);
	}
	
	public void addAlgorithmFactory(String name, VehicleRoutingAlgorithmFactory factory){
		algorithms.add(new Algorithm(name,factory));
	}
	
	public void addIndicatorFactory(IndicatorFactory indicatorFactory){
		indicatorFactories.add(indicatorFactory);
	}

	public void addInstance(String name, VehicleRoutingProblem problem){
		benchmarkInstances.add(new BenchmarkInstance(name,problem,null,null));
	}
	
	public void addInstance(BenchmarkInstance instance){
		benchmarkInstances.add(instance);
	}
	
	public void addAllInstances(Collection<BenchmarkInstance> instances){
		benchmarkInstances.addAll(instances);
	}
	
	public void addInstance(String name, VehicleRoutingProblem problem, Double bestKnownResult, Double bestKnownVehicles){
		benchmarkInstances.add(new BenchmarkInstance(name,problem,bestKnownResult,bestKnownVehicles));
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
	
	public void run(){
		if(algorithms.isEmpty()){
			throw new IllegalStateException("no algorithm specified. at least one algorithm needs to be specified.");
		}
		if(benchmarkInstances.isEmpty()){
			throw new IllegalStateException("no instance specified. at least one instance needs to be specified.");
		}
		if(indicatorFactories.isEmpty()){
			throw new IllegalStateException("no indicator specified. at least one indicator needs to be specified.");
		}
		System.out.println("start benchmarking [nuAlgorithms="+algorithms.size()+"][nuInstances=" + benchmarkInstances.size() + "][runsPerInstance=" + runs + "]");
		double startTime = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		List<Future<ResultContainer>> futures = new ArrayList<Future<ResultContainer>>();
		for(final Algorithm algorithm : algorithms){
			for(final BenchmarkInstance p : benchmarkInstances){

				Future<ResultContainer> futureResult = executor.submit(new Callable<ResultContainer>(){

					@Override
					public ResultContainer call() throws Exception {
						return runAlgoAndGetResult(p, algorithm);
					}

				});
				futures.add(futureResult);
			}
		}
		try {
			for(Future<ResultContainer> f : futures){
				results.add(f.get());
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		executor.shutdown();
		write(results);
		System.out.println("done [time="+(System.currentTimeMillis()-startTime)/1000 + "sec]");
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	private ResultContainer runAlgoAndGetResult(BenchmarkInstance p, Algorithm algorithm) {
		System.out.println("run " + algorithm.name + " on " + p.name);
		List<Indicator> indicators = createIndicators(p.vrp);
		informCalculationStarts(indicators);
		for(int run=0;run<runs;run++){
			System.out.println("algorithmName=" + algorithm.name + "; instanceName=" + p.name + "; run=" + (run+1));
			VehicleRoutingAlgorithm vra = algorithm.factory.createAlgorithm(p.vrp);
			informRunStarts(indicators,vra);
			StopWatch stopwatch = new StopWatch();
			vra.getAlgorithmListeners().addListener(stopwatch,Priority.HIGH);
			Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
			VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);
			informRunEnds(indicators,best);
		}
		System.out.println("finished runs of " + algorithm.name + " on " + p.name);
		ResultContainer resultContainer = getResultContainer(indicators, p.name, algorithm.name);
		return resultContainer;
	}

	private ResultContainer getResultContainer(List<Indicator> indicators, String instanceName, String algorithmName) {
		ResultContainer rc = new ResultContainer(instanceName, algorithmName);
		for(final Indicator i : indicators) {
			Result result = new Result(){

				@Override
				public String getIndicatorName() {
					return i.getIndicatorName();
				}

				@Override
				public double getResult() {
					return i.getResult();
				}
				
			};
			rc.addResult(result);
		}
		return rc;
	}

	private void informRunEnds(List<Indicator> indicators,VehicleRoutingProblemSolution best) {
		for(Indicator i : indicators) i.runEnds(best);
	}

	private void informRunStarts(List<Indicator> indicators,VehicleRoutingAlgorithm vra) {
		for(Indicator i : indicators) i.runStarts(vra);
	}

	private void informCalculationStarts(List<Indicator> indicators) {
		for(Indicator i : indicators) i.calculationStarts();
	}

	private List<Indicator> createIndicators(VehicleRoutingProblem vrp) {
		List<Indicator> indicators = new ArrayList<ComputationalLaboratory.Indicator>();
		for(IndicatorFactory iFactory : indicatorFactories) indicators.add(iFactory.createIndicator(vrp));
		return indicators;
	}

	private void write(Collection<ResultContainer> results) {
		for(ResultWriter writer : writers){
			writer.writeResults(results);
		}
	}

}
