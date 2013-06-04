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
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import algorithms.InsertionData.NoInsertionFound;
import basics.Job;
import basics.Service;
import basics.route.VehicleRoute;


/**
 * Insertion based an regret approach. 
 * 
 * <p>Basically calculates the insertion cost of the firstBest and the secondBest alternative. The score is then calculated as difference 
 * between secondBest and firstBest, plus additional scoring variables that can defined in this.ScoringFunction.
 * The idea is that if the cost of the secondBest alternative is way higher than the first best, it seems to be important to insert this
 * customer immediatedly. If difference is not that high, it might not impact solution if this customer is inserted later.
 * 
 * @author stefan schroeder
 *
 */
final class RegretInsertion extends AbstractInsertionStrategy{
		
	/**
	 * Scorer to include other impacts on score such as time-window length or distance to depot.
	 * 
	 * @author schroeder
	 *
	 */
	static interface ScoringFunction {
		
		public double score(Job job);
		
	}
	
	/**
	 * Scorer that includes the length of the time-window when scoring a job. The wider the time-window, the lower the score.
	 * 
	 * <p>This is the default scorer, i.e.: score = (secondBest - firstBest) + this.TimeWindowScorer.score(job)
	 *  
	 * @author schroeder
	 *
	 */
	static class TimeWindowScorer implements ScoringFunction {

		private double tw_scoringParam = - 0.1;
		
		@Override
		public double score(Job job) {
			double twStart = 0.0;
			double twEnd = 0.0;
//			if(job instanceof Shipment){
//				twStart = ((Shipment) job).getDeliveryTW().getStart();
//				twEnd  = ((Shipment) job).getDeliveryTW().getEnd();
//			}
//			else 
			if(job instanceof Service){
				twStart = ((Service) job).getTimeWindow().getStart();
				twEnd  = ((Service) job).getTimeWindow().getEnd();
			}
			return (twEnd-twStart)*tw_scoringParam;
		}
		
		@Override
		public String toString() {
			return "[name=timeWindowScorer][scoringParam="+tw_scoringParam+"]";
		}
		
	}
	
	public static RegretInsertion newInstance(RouteAlgorithm routeAlgorithm) {
		return new RegretInsertion(routeAlgorithm);
	}

	private Logger logger = Logger.getLogger(RegretInsertion.class);
	
	private RouteAlgorithm routeAlgorithm;
	
	private ScoringFunction scoringFunction = new TimeWindowScorer();

	/**
	 * Sets the scoring function.
	 * 
	 * <p>By default, the this.TimeWindowScorer is used.
	 * 
	 * @param scoringFunction
	 */
	public void setScoringFunction(ScoringFunction scoringFunction) {
		this.scoringFunction = scoringFunction;
	}

	public RegretInsertion(RouteAlgorithm routeAlgorithm) {
		super();
		this.routeAlgorithm = routeAlgorithm;
		logger.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[name=regretInsertion][additionalScorer="+scoringFunction+"]";
	}
	
	public RouteAlgorithm getRouteAlgorithm(){
		return routeAlgorithm;
	}

	/**
	 * Runs insertion.
	 * 
	 * <p>Before inserting a job, all unassigned jobs are scored according to its best- and secondBest-insertion plus additional scoring variables.
	 * 
	 */
	@Override
	public void run(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs, double resultToBeat) {
		List<Job> jobs = new ArrayList<Job>(unassignedJobs);
		informInsertionStarts(routes,unassignedJobs.size());
		int inserted = 0;
		while(!jobs.isEmpty()){
			List<Job> unassignedJobList = new ArrayList<Job>(jobs);
			ScoredJob bestScoredJob = null;
			double bestScore = -1*Double.MAX_VALUE;
			VehicleRoute insertIn = null;
			
			for(Job unassignedJob : unassignedJobList){
				InsertionData best = null;
				InsertionData secondBest = null;
				VehicleRoute bestRoute = null;
				
				double benchmark = Double.MAX_VALUE;
				for(VehicleRoute route : routes){
					if(secondBest != null){
						benchmark = secondBest.getInsertionCost();
					}
					InsertionData iData = routeAlgorithm.calculateBestInsertion(route, unassignedJob, benchmark);
					if(iData instanceof NoInsertionFound) continue;
					if(best == null){
						best = iData;
						bestRoute = route;
					}
					else if(iData.getInsertionCost() < best.getInsertionCost()){
						secondBest = best;
						best = iData;
						bestRoute = route;
					}
					else if(secondBest == null || (iData.getInsertionCost() < secondBest.getInsertionCost())){
						secondBest = iData;
					}
				}
				if(best == null){
					break;
				}
				double score = score(unassignedJob,best,secondBest);
				if(score > bestScore){
					bestScoredJob = new ScoredJob(unassignedJob,score,best,bestRoute);
					bestScore = score;
				}
			}
			Job assignedJob;
			if(bestScoredJob == null){
				Job job = unassignedJobList.get(0);
				VehicleRoute newRoute = VehicleRoute.emptyRoute();	
				InsertionData bestI = routeAlgorithm.calculateBestInsertion(newRoute, job, Double.MAX_VALUE);
				if(bestI instanceof InsertionData.NoInsertionFound) throw new IllegalStateException("given the vehicles, could not create a valid solution");
				insertIn=newRoute;
				assignedJob=job;
				routeAlgorithm.insertJob(job,bestI,newRoute);
				routes.add(newRoute);
				jobs.remove(job);
				
			}
			else{
				routeAlgorithm.insertJob(bestScoredJob.getJob(),bestScoredJob.getInsertionData(), bestScoredJob.getRoute());
				insertIn=bestScoredJob.getRoute();
				assignedJob=bestScoredJob.getJob();
				jobs.remove(bestScoredJob.getJob());
			}
			inserted++;
			informJobInserted((unassignedJobList.size()-inserted), assignedJob, insertIn);
			
		}
	}

	private double score(Job unassignedJob, InsertionData best, InsertionData secondBest) {
		if(best == null){
			throw new IllegalStateException("cannot insert job " +  unassignedJob.getId());
		}
		if(secondBest == null){
			return Double.MAX_VALUE;
		}
		return (secondBest.getInsertionCost()-best.getInsertionCost()) + scoringFunction.score(unassignedJob);

	}
		
}
