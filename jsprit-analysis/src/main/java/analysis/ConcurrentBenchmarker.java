package analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import util.Solutions;
import algorithms.VehicleRoutingAlgorithms;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.VehicleRoutingAlgorithmListeners.Priority;

public class ConcurrentBenchmarker {

	static class Problem {
		public final String name;
		public final VehicleRoutingProblem vrp;
		public final Double bestKnown;
		public Problem(String name, VehicleRoutingProblem vrp, Double bestKnown) {
			super();
			this.name = name;
			this.vrp = vrp;
			this.bestKnown = bestKnown;
		}
	}
	
	static class Result {
		public final double result;
		public final double time;
		public final Problem problem;
		public Double delta = null;
		public Result(Problem p, double result, double time) {
			super();
			this.result = result;
			this.time = time;
			this.problem = p;
		}
		void setBestKnownDelta(double delta){
			this.delta = delta;
		}
	}
	
	private String algorithmConfig;
	
	private List<Problem> problems = new ArrayList<Problem>();
	
	public ConcurrentBenchmarker(String algorithmConfig) {
		super();
		this.algorithmConfig = algorithmConfig;
	}

	public void addProblem(String name, VehicleRoutingProblem problem){
		problems.add(new Problem(name,problem,null));
	}
	
	public void addProblem(String name, VehicleRoutingProblem problem, double bestKnown){
		problems.add(new Problem(name,problem,bestKnown));
	}
	
	public void run(){
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
		List<Future<Result>> futures = new ArrayList<Future<Result>>();
		List<Result> results = new ArrayList<ConcurrentBenchmarker.Result>();
		for(final Problem p : problems){
			Future<Result> futureResult = executor.submit(new Callable<Result>(){

				@Override
				public Result call() throws Exception {
					return runAlgoAndGetResult(p);
				}
				
			});
			futures.add(futureResult);
		}
		try {
			for(Future<Result> f : futures){
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
		print(results);
	}

	private Result runAlgoAndGetResult(Problem p) {
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(p.vrp, algorithmConfig);
		StopWatch stopwatch = new StopWatch();
		vra.getAlgorithmListeners().addListener(stopwatch,Priority.HIGH);
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		VehicleRoutingProblemSolution best = Solutions.getBest(solutions);
		Result result = new Result(p,best.getCost(),stopwatch.getCompTimeInSeconds());
		if(p.bestKnown != null) result.setBestKnownDelta((best.getCost()/p.bestKnown-1));
		return result;
	}

	private void print(List<Result> results) {
		System.out.println("instance,time [in sec],result,delta [in percent to bestKnown]");
		double sumTime=0.0;
		double sumResult=0.0;
		for(Result r : results){
			sumTime+=r.time;
			sumResult+=r.result;
			System.out.println("[instance="+r.problem.name+"][time="+round(r.time,2)+"][result="+round(r.result,2)+"][delta="+round(r.delta,3)+"]");
		}
		System.out.println("[avgTime="+round(sumTime/(double)results.size(),2)+"][avgResult="+round(sumResult/(double)results.size(),2)+"]");
	}

	private double round(Double delta, int i) {
		long roundedVal = Math.round(delta*Math.pow(10, i));
		return (double)roundedVal/(double)(Math.pow(10, i));
	}

}
