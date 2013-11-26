package jsprit.core.algorithm.recreate;
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
///*******************************************************************************
// * Copyright (c) 2011 Stefan Schroeder.
// * eMail: stefan.schroeder@kit.edu
// * 
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the GNU Public License v2.0
// * which accompanies this distribution, and is available at
// * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
// * 
// * Contributors:
// *     Stefan Schroeder - initial API and implementation
// ******************************************************************************/
//package algorithms;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.concurrent.Callable;
//import java.util.concurrent.CompletionService;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorCompletionService;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Future;
//
//import org.apache.log4j.Logger;
//
//import basics.InsertionData;
//import basics.Job;
//import basics.Service;
//import basics.Shipment;
//import basics.VehicleRoute;
//import basics.InsertionData.NoInsertionFound;
//
//
//
//
//
//
//
///**
// * Simplest recreation strategy. All removed customers are inserted where insertion costs are minimal. I.e. each tour-agent is asked for
// * minimal marginal insertion costs. The tour-agent offering the lowest marginal insertion costs gets the customer/shipment.
// * 
// * @author stefan schroeder
// *
// */
//
//final class ParRegretInsertion extends AbstractRecreationStrategy{
//	
//		
//	private Logger logger = Logger.getLogger(ParRegretInsertion.class);
//	
//
//	public static double scoreParam_of_timeWindowLegth = 0.0;
//	
//	private ExecutorService executor;
//
//	public static double scoreParam_of_distance = 0.5;
//	
//	private DepotDistance depotDistance;
//	
//	private RouteAlgorithm routeAlgorithm;
//	
//	private VehicleRouteFactory vehicleRouteFactory;
//
//	public ParRegretInsertion(ExecutorService executor, RouteAlgorithm routeAlgorithm, VehicleRouteFactory routeFactory, DepotDistance depotDistance) {
//		super();
//		this.executor = executor;
//		this.routeAlgorithm = routeAlgorithm;
//		this.depotDistance = depotDistance;
//		this.vehicleRouteFactory = routeFactory;
//	}
//
//
//	@Override
//	public void recreate(final Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs, double result2beat) {
//		List<Job> jobs = new ArrayList<Job>(unassignedJobs);
//		informRecreationStart(unassignedJobs.size());
//		
//		while(!jobs.isEmpty()){
//			List<Job> unassignedJobList = new ArrayList<Job>(jobs);
//			ScoredJob bestScoredJob = null;
//			double bestScore = -1*Double.MAX_VALUE;
//			CompletionService<ScoredJob> completionService = new ExecutorCompletionService<ScoredJob>(executor);
//			
//			for(final Job unassignedJob : unassignedJobList){
//				completionService.submit(new Callable<ScoredJob>(){
//
//					@Override
//					public ScoredJob call() throws Exception {
//						return getScoredJob(vehicleRoutes, unassignedJob);
//					}
//					
//				});
//				
//			}
//			try{
//				for(int i=0;i<unassignedJobList.size();i++){
//					Future<ScoredJob> fsj = completionService.take();
//					ScoredJob scoredJob = fsj.get();
//					if(scoredJob == null){
//						continue;
//					}
//					if(scoredJob.getScore() > bestScore){
//						bestScoredJob = scoredJob;
//						bestScore = scoredJob.getScore();
//					}
//				}
//			}
//			catch(InterruptedException e){
//				Thread.currentThread().interrupt();
//			} 
//			catch (ExecutionException e) {
//				e.printStackTrace();
//				logger.error(e.getCause().toString());
//				System.exit(1);
//			}	
//			if(bestScoredJob == null){
//				Job job = unassignedJobList.get(0);
//				VehicleRoute newRoute = vehicleRouteFactory.createVehicleRoute();	
//				InsertionData bestI = routeAlgorithm.calculateBestInsertion(newRoute, job, Double.MAX_VALUE);
//				if(bestI instanceof InsertionData.NoInsertionFound) throw new IllegalStateException("given the vehicles, could not create a valid solution");
//				routeAlgorithm.insertJob(job,bestI,newRoute);
//				vehicleRoutes.add(newRoute);
//				jobs.remove(job);
//				
//			}
//			else{
//				routeAlgorithm.insertJob(bestScoredJob.getJob(),bestScoredJob.getInsertionData(),bestScoredJob.getRoute());
//				jobs.remove(bestScoredJob.getJob());
//			}
//			informJobInsertion(null, (unassignedJobList.size()-1), null);
//		}
//	}
//
//	private ScoredJob getScoredJob(Collection<VehicleRoute> vehicleRoutes, Job job){
//		InsertionData best = null;
//		InsertionData secondBest = null;
//		VehicleRoute bestRoute = null;
//		double benchmark = Double.MAX_VALUE;
//		for(VehicleRoute route : vehicleRoutes){
//			if(secondBest != null){
//				benchmark = secondBest.getInsertionCost();
//			}
//			InsertionData iData = routeAlgorithm.calculateBestInsertion(route, job, benchmark);
//			if(iData instanceof NoInsertionFound) continue;
//			if(best == null) {
//				best = iData;
//				bestRoute = route;
//			}
//			else if(iData.getInsertionCost() < best.getInsertionCost()){
//				secondBest = best;
//				best = iData;
//				bestRoute = route;
//			}
//			else if(secondBest == null) secondBest = iData;
//			else if(iData.getInsertionCost() < secondBest.getInsertionCost()) secondBest = iData;
//		}
//		if(best == null){
//			return null;
//		}
//		double score = score(job,best,secondBest);
//		return new ScoredJob(job, score, best, bestRoute);
//	}
//
//	private double score(Job unassignedJob, InsertionData best, InsertionData secondBest) {
//		/*
//		 * wieder so eine bescheuerte fallunterscheidung. hier will ich
//		 * doch einfach nur das maßgebende zeitfenster des jobs
//		 * job.getTimeWindow()
//		 * Problem: eine Shipment hat zwei TWs, sowohl ein PickupTW als auch
//		 * ein DeliveryTW
//		 */
//		double twStart = 0.0;
//		double twEnd = 0.0;
//		if(unassignedJob instanceof Shipment){
//			twStart = ((Shipment) unassignedJob).getDeliveryTW().getStart();
//			twEnd  = ((Shipment) unassignedJob).getDeliveryTW().getEnd();
//		}
//		else if(unassignedJob instanceof Service){
//			twStart = ((Service) unassignedJob).getTimeWindow().getStart();
//			twEnd  = ((Service) unassignedJob).getTimeWindow().getEnd();
//		}
//		if(best == null){
//			throw new IllegalStateException("cannot insert job " +  unassignedJob.getId());
//		}
//		if(secondBest == null){
//			return Double.MAX_VALUE;
//		} 
////		double score = (secondBest.getInsertionCost()-best.getInsertionCost()) + scoreParam_of_distance*getDistance(unassignedJob) - scoreParam_of_timeWindowLegth*(twEnd-twStart);
////		logger.info("priceDiff="+ (secondBest.getPrice()-best.getPrice()) + "; param*dist=" 
//		double timeWindowInfluence = scoreParam_of_timeWindowLegth*(twEnd-twStart);
//		double distanceInfluence = scoreParam_of_distance*getDistance(unassignedJob);
//		double score = (secondBest.getInsertionCost()-best.getInsertionCost()) - timeWindowInfluence 
//			+ distanceInfluence;
//		return score;
//	}
//
//	private double getDistance(Job unassignedJob) {
//		return depotDistance.calcDistance(unassignedJob);
//	}
//
//
//	public double getTimeParam() {
//		return scoreParam_of_timeWindowLegth;
//	}
//
//	
//}
