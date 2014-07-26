/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package jsprit.analysis.toolbox;

import jsprit.analysis.util.BenchmarkWriter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmFactory;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.Priority;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.BenchmarkInstance;
import jsprit.core.util.BenchmarkResult;
import jsprit.core.util.Solutions;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;


public class ConcurrentBenchmarker {
	
	public static interface Cost {
		public double getCost(VehicleRoutingProblemSolution sol);
	}
	
	
	
	private String algorithmConfig = null;
	
	private List<BenchmarkInstance> benchmarkInstances = new ArrayList<BenchmarkInstance>();

	private int runs = 1;
	
	private Collection<BenchmarkWriter> writers = new ArrayList<BenchmarkWriter>();
	
	private Collection<BenchmarkResult> results = new ArrayList<BenchmarkResult>();
	
	private Cost cost = new Cost(){

		@Override
		public double getCost(VehicleRoutingProblemSolution sol) {
			return sol.getCost();
		}
		
	};

	private VehicleRoutingAlgorithmFactory algorithmFactory;
	
	public void setCost(Cost cost){ this.cost = cost; }
	
	public ConcurrentBenchmarker(String algorithmConfig) {
		super();
		this.algorithmConfig = algorithmConfig;
//		LogManager.getRootLogger().setLevel(Level.ERROR);
	}
	
	public ConcurrentBenchmarker(VehicleRoutingAlgorithmFactory algorithmFactory){
		this.algorithmFactory = algorithmFactory;
	}
	
	public void addBenchmarkWriter(BenchmarkWriter writer){
		writers.add(writer);
	}

	public void addInstance(String name, VehicleRoutingProblem problem){
		benchmarkInstances.add(new BenchmarkInstance(name,problem,null,null));
	}
	
	public void addInstane(BenchmarkInstance instance){
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
		System.out.println("start benchmarking [nuOfInstances=" + benchmarkInstances.size() + "][runsPerInstance=" + runs + "]");
		double startTime = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
		List<Future<BenchmarkResult>> futures = new ArrayList<Future<BenchmarkResult>>();
		for(final BenchmarkInstance p : benchmarkInstances){
			
				Future<BenchmarkResult> futureResult = executor.submit(new Callable<BenchmarkResult>(){

					@Override
					public BenchmarkResult call() throws Exception {
						return runAlgoAndGetResult(p);
					}

				});
				futures.add(futureResult);
			
		}
		try {
			int count = 1;
			for(Future<BenchmarkResult> f : futures){
				BenchmarkResult r = f.get();
				print(r,count);
				results.add(f.get());
				count++;
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
		System.out.println("done [time="+(System.currentTimeMillis()-startTime)/1000 + "sec]");
	}

	private BenchmarkResult runAlgoAndGetResult(BenchmarkInstance p) {
		double[] vehicles = new double[runs];
		double[] results = new double[runs];
		double[] times = new double[runs];
		
		for(int run=0;run<runs;run++){
			VehicleRoutingAlgorithm vra = createAlgorithm(p);
			StopWatch stopwatch = new StopWatch();
			vra.getAlgorithmListeners().addListener(stopwatch,Priority.HIGH);
			Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
			VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);
			vehicles[run] = best.getRoutes().size();
			results[run] = cost.getCost(best);
			times[run] = stopwatch.getCompTimeInSeconds();
		}
		
		return new BenchmarkResult(p, runs, results, times, vehicles);
	}

	private VehicleRoutingAlgorithm createAlgorithm(BenchmarkInstance p) {
		if(algorithmConfig != null){
			return VehicleRoutingAlgorithms.readAndCreateAlgorithm(p.vrp, algorithmConfig);
		}
		else{
			return algorithmFactory.createAlgorithm(p.vrp);
		}
		
	}

	private void print(Collection<BenchmarkResult> results) {
		double sumTime=0.0;
		double sumResult=0.0;
		for(BenchmarkResult r : results){
			sumTime+=r.getTimesStats().getMean();
			sumResult+=r.getResultStats().getMean();
//			print(r);
		}
		System.out.println("[avgTime="+round(sumTime/(double)results.size(),2)+"][avgResult="+round(sumResult/(double)results.size(),2)+"]");
		for(BenchmarkWriter writer : writers){
			writer.write(results);
		}
	}

	private void print(BenchmarkResult r, int count) {
		Double avgDelta = null;
		Double bestDelta = null;
		Double worstDelta = null;
		if(r.instance.bestKnownResult != null){
			avgDelta = (r.getResultStats().getMean() / r.instance.bestKnownResult - 1) * 100;
			bestDelta = (r.getResultStats().getMin() / r.instance.bestKnownResult - 1) * 100;
			worstDelta = (r.getResultStats().getMax() / r.instance.bestKnownResult - 1) * 100;
		}
		System.out.println("("+count+"/"+benchmarkInstances.size() +")"+ "\t[instance="+r.instance.name+
				"][avgTime="+round(r.getTimesStats().getMean(),2)+"]" +
				"[Result=" + getString(r.getResultStats()) + "]" +
				"[Vehicles=" + getString(r.getVehicleStats()) + "]" +
				"[Delta[%]=" + getString(bestDelta,avgDelta,worstDelta) + "]");
	}
	
	private String getString(Double bestDelta, Double avgDelta,Double worstDelta) {
		return "[best="+round(bestDelta,2)+"][avg="+round(avgDelta,2)+"][worst="+round(worstDelta,2)+"]";
	}

	private String getString(DescriptiveStatistics stats){
		return "[best="+round(stats.getMin(),2)+"][avg="+round(stats.getMean(),2)+"][worst="+round(stats.getMax(),2)+"][stdDev=" + round(stats.getStandardDeviation(),2)+"]";
	}

	private Double round(Double value, int i) {
		if(value==null) return null;
		long roundedVal = Math.round(value*Math.pow(10, i));
		return (double)roundedVal/(double)(Math.pow(10, i));
	}

}
