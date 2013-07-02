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

package analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import util.BenchmarkWriter;
import util.Solutions;
import algorithms.VehicleRoutingAlgorithms;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.VehicleRoutingAlgorithmListeners.Priority;

public class ConcurrentBenchmarker {
	
	public static class BenchmarkInstance {
		public final String name;
		public final VehicleRoutingProblem vrp;
		public final Double bestKnown;
		public BenchmarkInstance(String name, VehicleRoutingProblem vrp, Double bestKnown) {
			super();
			this.name = name;
			this.vrp = vrp;
			this.bestKnown = bestKnown;
		}
	}
	
	public static class BenchmarkResult {
		public final double result;
		public final double time;
		public final int nuOfVehicles;
		public final BenchmarkInstance instance;
		public Double delta = null;
		public BenchmarkResult(BenchmarkInstance p, double result, double time, int nuOfVehicles) {
			super();
			this.result = result;
			this.time = time;
			this.instance = p;
			this.nuOfVehicles = nuOfVehicles;
		}
		void setBestKnownDelta(double delta){
			this.delta = delta;
		}
	}
	
	private String algorithmConfig;
	
	private List<BenchmarkInstance> problems = new ArrayList<BenchmarkInstance>();

	private int runs = 1;
	
	private Collection<BenchmarkWriter> writers = new ArrayList<BenchmarkWriter>();
	
	private Collection<BenchmarkResult> results = new ArrayList<ConcurrentBenchmarker.BenchmarkResult>();
	
	public ConcurrentBenchmarker(String algorithmConfig) {
		super();
		this.algorithmConfig = algorithmConfig;
		Logger.getRootLogger().setLevel(Level.ERROR);
	}
	
	public void addBenchmarkWriter(BenchmarkWriter writer){
		writers.add(writer);
	}

	public void addProblem(String name, VehicleRoutingProblem problem){
		problems.add(new BenchmarkInstance(name,problem,null));
	}
	
	public void addProblem(String name, VehicleRoutingProblem problem, double bestKnown){
		problems.add(new BenchmarkInstance(name,problem,bestKnown));
	}
	
	public void setNuOfRuns(int runs){
		this.runs = runs;
	}
	
	public void run(){
		System.out.println("start benchmarking [nuOfInstances=" + problems.size() + "]");
		double startTime = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
		List<Future<BenchmarkResult>> futures = new ArrayList<Future<BenchmarkResult>>();
//		List<BenchmarkResult> results = new ArrayList<ConcurrentBenchmarker.BenchmarkResult>();
		for(final BenchmarkInstance p : problems){
			for(int run=0;run<runs;run++){
				Future<BenchmarkResult> futureResult = executor.submit(new Callable<BenchmarkResult>(){

					@Override
					public BenchmarkResult call() throws Exception {
						return runAlgoAndGetResult(p);
					}

				});
				futures.add(futureResult);
			}
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
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(p.vrp, algorithmConfig);
		StopWatch stopwatch = new StopWatch();
		vra.getAlgorithmListeners().addListener(stopwatch,Priority.HIGH);
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		VehicleRoutingProblemSolution best = Solutions.getBest(solutions);
		BenchmarkResult result = new BenchmarkResult(p,best.getCost(),stopwatch.getCompTimeInSeconds(), best.getRoutes().size());
		if(p.bestKnown != null) result.setBestKnownDelta((best.getCost()/p.bestKnown-1));
		return result;
	}

	private void print(Collection<BenchmarkResult> results) {
		double sumTime=0.0;
		double sumResult=0.0;
		for(BenchmarkResult r : results){
			sumTime+=r.time;
			sumResult+=r.result;
//			print(r);
		}
		System.out.println("[avgTime="+round(sumTime/(double)results.size(),2)+"][avgResult="+round(sumResult/(double)results.size(),2)+"]");
		for(BenchmarkWriter writer : writers){
			writer.write(results);
		}
	}

	private void print(BenchmarkResult r, int count) {
		System.out.println("("+count+"/"+problems.size() +")"+ "\t[instance="+r.instance.name+"][time="+round(r.time,2)+"][result="+round(r.result,2)+
				"][delta="+round(r.delta,3)+"][nuOfVehicles="+r.nuOfVehicles+"]");
		
	}

	private Double round(Double value, int i) {
		if(value==null) return null;
		long roundedVal = Math.round(value*Math.pow(10, i));
		return (double)roundedVal/(double)(Math.pow(10, i));
	}

}
