/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import util.RandomNumberGeneration;
import algorithms.InsertionData.NoInsertionFound;
import basics.Job;
import basics.route.VehicleRoute;



/**
 * Simplest recreation strategy. All removed customers are inserted where
 * insertion costs are minimal. I.e. each tour-agent is asked for minimal
 * marginal insertion costs. The tour-agent offering the lowest marginal
 * insertion costs gets the customer/shipment.
 * 
 * @author stefan schroeder
 * 
 */

final class BestInsertionConcurrent extends AbstractInsertionStrategy{
	
	
	static class Batch {
		List<VehicleRoute> routes = new ArrayList<VehicleRoute>();
		
	}
	
	private static Logger logger = Logger.getLogger(BestInsertionConcurrent.class);

	private Random random = RandomNumberGeneration.getRandom();
	
	private RouteAlgorithm routeAlgorithm;
	
//	private ExecutorService executor;
	
	private int nuOfBatches;

	private ExecutorCompletionService<Insertion> completionService;

	public void setRandom(Random random) {
		this.random = random;
	}
	
	public BestInsertionConcurrent(RouteAlgorithm routeAlgorithm, ExecutorService executor, int nuOfThreads) {
		super();
		this.routeAlgorithm = routeAlgorithm;
//		this.executor = executor;
		logger.info("initialise " + this);
		this.nuOfBatches = nuOfThreads;
		completionService = new ExecutorCompletionService<Insertion>(executor);
	}

	@Override
	public String toString() {
		return "[name=concurrentBestInsertion]";
	}

	@Override
	public void run(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs, double result2beat) {
		List<Job> unassignedJobList = new ArrayList<Job>(unassignedJobs);
		Collections.shuffle(unassignedJobList, random);
		informInsertionStarts(vehicleRoutes,unassignedJobs.size());
		
		int inserted = 0;
		for(final Job unassignedJob : unassignedJobList){
			VehicleRoute insertIn = null;
			Insertion bestInsertion = null;
			double bestInsertionCost = Double.MAX_VALUE;
			
			List<Batch> batches = distributeRoutes(vehicleRoutes,nuOfBatches);
			
			for(final Batch batch : batches){
				completionService.submit(new Callable<Insertion>() {
					
					@Override
					public Insertion call() throws Exception {
						return getBestInsertion(batch,unassignedJob);
					}
					
				});
				
			}
			
			try{
				for(int i=0;i<batches.size();i++){
					Future<Insertion> futureIData = completionService.take();
					Insertion insertion = futureIData.get();
					if(insertion == null) continue;
					if(insertion.getInsertionData().getInsertionCost() < bestInsertionCost){
						bestInsertion = insertion;
						bestInsertionCost = insertion.getInsertionData().getInsertionCost();
					}
				}
			}
			catch(InterruptedException e){
				Thread.currentThread().interrupt();
			} 
			catch (ExecutionException e) {
				e.printStackTrace();
				logger.error(e.getCause().toString());
				System.exit(1);
			}	
			
			if(bestInsertion != null){
				informBeforeJobInsertion(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
				insertIn = bestInsertion.getRoute();
//				logger.debug("insert job="+unassignedJob+" at index=" + bestInsertion.getInsertionData().getInsertionIndex() + " delta cost=" + bestInsertion.getInsertionData().getInsertionCost());
				routeAlgorithm.insertJob(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
			} 
			else {
				VehicleRoute newRoute = VehicleRoute.emptyRoute();
				InsertionData bestI = routeAlgorithm.calculateBestInsertion(newRoute, unassignedJob, Double.MAX_VALUE);
				if(bestI instanceof InsertionData.NoInsertionFound) 
					throw new IllegalStateException("given the vehicles, could not create a valid solution.\n\tthe reason might be" +
							" inappropriate vehicle capacity.\n\tthe job that does not fit in any vehicle anymore is \n\t" + unassignedJob);
				insertIn = newRoute;
				informBeforeJobInsertion(unassignedJob,bestI,newRoute);
				routeAlgorithm.insertJob(unassignedJob,bestI,newRoute);
				vehicleRoutes.add(newRoute);
			}
			inserted++;
			informJobInserted((unassignedJobList.size()-inserted), unassignedJob, insertIn);
		}
		
	}
	
	private Insertion getBestInsertion(Batch batch, Job unassignedJob) {
		Insertion bestInsertion = null;
		double bestInsertionCost = Double.MAX_VALUE;
		for(VehicleRoute vehicleRoute : batch.routes){
			InsertionData iData = routeAlgorithm.calculateBestInsertion(vehicleRoute, unassignedJob, bestInsertionCost);
			if(iData instanceof NoInsertionFound) continue;
			if(iData.getInsertionCost() < bestInsertionCost){
				bestInsertion = new Insertion(vehicleRoute,iData);
				bestInsertionCost = iData.getInsertionCost();
			}
		}
		return bestInsertion;
	}

	private List<Batch> distributeRoutes(Collection<VehicleRoute> vehicleRoutes, int nuOfBatches) {
		List<Batch> batches = new ArrayList<Batch>();
		for(int i=0;i<nuOfBatches;i++) batches.add(new Batch()); 
		if(vehicleRoutes.size()<nuOfBatches){
			int nOfNewRoutes = nuOfBatches-vehicleRoutes.size();
			for(int i=0;i<nOfNewRoutes;i++){
				vehicleRoutes.add(VehicleRoute.emptyRoute());
			}
		}
		int count = 0;
		for(VehicleRoute route : vehicleRoutes){
			if(count == nuOfBatches) count=0;
			batches.get(count).routes.add(route);
			count++;
		}
		return batches;
	}

	@Override
	public RouteAlgorithm getRouteAlgorithm() {
		return routeAlgorithm;
	}

}
