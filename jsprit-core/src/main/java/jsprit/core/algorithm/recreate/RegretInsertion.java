/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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

package jsprit.core.algorithm.recreate;

import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.VehicleRoute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
* Insertion based on regret approach.
*
* <p>Basically calculates the insertion cost of the firstBest and the secondBest alternative. The score is then calculated as difference
* between secondBest and firstBest, plus additional scoring variables that can defined in this.ScoringFunction.
* The idea is that if the cost of the secondBest alternative is way higher than the first best, it seems to be important to insert this
* customer immediatedly. If difference is not that high, it might not impact solution if this customer is inserted later.
*
* @author stefan schroeder
*
*/
public class RegretInsertion extends AbstractInsertionStrategy {

    static class ScoredJob {

        private Job job;

        private double score;

        private InsertionData insertionData;

        private VehicleRoute route;

        private boolean newRoute;


        ScoredJob(Job job, double score, InsertionData insertionData, VehicleRoute route, boolean isNewRoute) {
            this.job = job;
            this.score = score;
            this.insertionData = insertionData;
            this.route = route;
            this.newRoute = isNewRoute;
        }

        public boolean isNewRoute() {
            return newRoute;
        }

        public Job getJob() {
            return job;
        }

        public double getScore() {
            return score;
        }

        public InsertionData getInsertionData() {
            return insertionData;
        }

        public VehicleRoute getRoute() {
            return route;
        }
    }

    static class BadJob extends ScoredJob {

        BadJob(Job job) {
            super(job, 0., null, null, false);
        }
    }

	/**
	 * Scorer to include other impacts on score such as time-window length or distance to depot.
	 *
	 * @author schroeder
	 *
	 */
	static interface ScoringFunction {

		public double score(InsertionData best, Job job);

	}

	/**
	 * Scorer that includes the length of the time-window when scoring a job. The wider the time-window, the lower the score.
	 *
	 * <p>This is the default scorer, i.e.: score = (secondBest - firstBest) + this.TimeWindowScorer.score(job)
	 *
	 * @author schroeder
	 *
	 */
	public static class DefaultScorer implements ScoringFunction {

        private VehicleRoutingProblem vrp;

        private double tw_param = - 0.5;

        private double depotDistance_param = + 0.1;

        private double minTimeWindowScore = - 100000;

        public DefaultScorer(VehicleRoutingProblem vrp) {
            this.vrp = vrp;
        }

        public void setTimeWindowParam(double tw_param){ this.tw_param = tw_param; }

        public void setDepotDistanceParam(double depotDistance_param){ this.depotDistance_param = depotDistance_param; }

        @Override
        public double score(InsertionData best, Job job) {
            double score;
            if(job instanceof Service){
                score = scoreService(best, job);
            }
            else if(job instanceof Shipment){
                score = scoreShipment(best,job);
            }
            else throw new IllegalStateException("not supported");
            return score;
        }

        private double scoreShipment(InsertionData best, Job job) {
            Shipment shipment = (Shipment)job;
            double maxDepotDistance_1 = Math.max(
                    getDistance(best.getSelectedVehicle().getStartLocation(),shipment.getPickupLocation()),
                    getDistance(best.getSelectedVehicle().getStartLocation(),shipment.getDeliveryLocation())
            );
            double maxDepotDistance_2 = Math.max(
                    getDistance(best.getSelectedVehicle().getEndLocation(),shipment.getPickupLocation()),
                    getDistance(best.getSelectedVehicle().getEndLocation(),shipment.getDeliveryLocation())
            );
            double maxDepotDistance = Math.max(maxDepotDistance_1,maxDepotDistance_2);
            double minTimeToOperate = Math.min(shipment.getPickupTimeWindow().getEnd()-shipment.getPickupTimeWindow().getStart(),
                    shipment.getDeliveryTimeWindow().getEnd()-shipment.getDeliveryTimeWindow().getStart());
            return Math.max(tw_param * minTimeToOperate,minTimeWindowScore) + depotDistance_param * maxDepotDistance;
        }

        private double scoreService(InsertionData best, Job job) {
            double maxDepotDistance = Math.max(
                    getDistance(best.getSelectedVehicle().getStartLocation(), ((Service) job).getLocation()),
                    getDistance(best.getSelectedVehicle().getEndLocation(), ((Service) job).getLocation())
            );
            return Math.max(tw_param * (((Service)job).getTimeWindow().getEnd() - ((Service)job).getTimeWindow().getStart()),minTimeWindowScore) +
                    depotDistance_param * maxDepotDistance;
        }


        private double getDistance(Location loc1, Location loc2) {
            return vrp.getTransportCosts().getTransportCost(loc1,loc2,0.,null,null);
        }

		@Override
		public String toString() {
			return "[name=defaultScorer][twParam="+tw_param+"][depotDistanceParam=" + depotDistance_param + "]";
		}

	}

    private static Logger logger = LogManager.getLogger(RegretInsertion.class);

	private ScoringFunction scoringFunction;

    private JobInsertionCostsCalculator insertionCostsCalculator;


    /**
	 * Sets the scoring function.
	 *
	 * <p>By default, the this.TimeWindowScorer is used.
	 *
	 * @param scoringFunction to score
	 */
	public void setScoringFunction(ScoringFunction scoringFunction) {
		this.scoringFunction = scoringFunction;
	}

	public RegretInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem) {
		super(vehicleRoutingProblem);
        this.scoringFunction = new DefaultScorer(vehicleRoutingProblem);
		this.insertionCostsCalculator = jobInsertionCalculator;
        this.vrp = vehicleRoutingProblem;
		logger.debug("initialise {}", this);
	}

	@Override
	public String toString() {
		return "[name=regretInsertion][additionalScorer="+scoringFunction+"]";
	}


	/**
	 * Runs insertion.
	 *
	 * <p>Before inserting a job, all unassigned jobs are scored according to its best- and secondBest-insertion plus additional scoring variables.
	 *
	 */
	@Override
	public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<Job>(unassignedJobs.size());
        List<Job> jobs = new ArrayList<Job>(unassignedJobs);

        while (!jobs.isEmpty()) {
            List<Job> unassignedJobList = new ArrayList<Job>(jobs);
            List<Job> badJobList = new ArrayList<Job>();
            ScoredJob bestScoredJob = nextJob(routes, unassignedJobList, badJobList);
            if(bestScoredJob != null){
                if(bestScoredJob.isNewRoute()){
                    routes.add(bestScoredJob.getRoute());
                }
                insertJob(bestScoredJob.getJob(),bestScoredJob.getInsertionData(),bestScoredJob.getRoute());
                jobs.remove(bestScoredJob.getJob());
            }
            for(Job bad : badJobList) {
                jobs.remove(bad);
                badJobs.add(bad);
            }
        }
        return badJobs;
    }

    private ScoredJob nextJob(Collection<VehicleRoute> routes, List<Job> unassignedJobList, List<Job> badJobs) {
        ScoredJob bestScoredJob = null;
        for (Job unassignedJob : unassignedJobList) {
            ScoredJob scoredJob = getScoredJob(routes,unassignedJob,insertionCostsCalculator,scoringFunction);
            if(scoredJob instanceof BadJob){
                badJobs.add(unassignedJob);
                continue;
            }
            if(bestScoredJob == null) bestScoredJob = scoredJob;
            else{
                if(scoredJob.getScore() > bestScoredJob.getScore()){
                    bestScoredJob = scoredJob;
                }
            }
        }
        return bestScoredJob;
    }

    static ScoredJob getScoredJob(Collection<VehicleRoute> routes, Job unassignedJob, JobInsertionCostsCalculator insertionCostsCalculator, ScoringFunction scoringFunction) {
        InsertionData best = null;
        InsertionData secondBest = null;
        VehicleRoute bestRoute = null;

        double benchmark = Double.MAX_VALUE;
        for (VehicleRoute route : routes) {
            if (secondBest != null) {
                benchmark = secondBest.getInsertionCost();
            }
            InsertionData iData = insertionCostsCalculator.getInsertionData(route, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, benchmark);
            if (iData instanceof InsertionData.NoInsertionFound) continue;
            if (best == null) {
                best = iData;
                bestRoute = route;
            } else if (iData.getInsertionCost() < best.getInsertionCost()) {
                secondBest = best;
                best = iData;
                bestRoute = route;
            } else if (secondBest == null || (iData.getInsertionCost() < secondBest.getInsertionCost())) {
                secondBest = iData;
            }
        }

        VehicleRoute emptyRoute = VehicleRoute.emptyRoute();
        InsertionData iData = insertionCostsCalculator.getInsertionData(emptyRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, benchmark);
        if (!(iData instanceof InsertionData.NoInsertionFound)) {
            if (best == null) {
                best = iData;
                bestRoute = emptyRoute;
            } else if (iData.getInsertionCost() < best.getInsertionCost()) {
                secondBest = best;
                best = iData;
                bestRoute = emptyRoute;
            } else if (secondBest == null || (iData.getInsertionCost() < secondBest.getInsertionCost())) {
                secondBest = iData;
            }
        }
        if(best == null){
            return new RegretInsertion.BadJob(unassignedJob);
        }
        double score = score(unassignedJob, best, secondBest, scoringFunction);
        ScoredJob scoredJob;
        if(bestRoute == emptyRoute){
            scoredJob = new ScoredJob(unassignedJob, score, best, bestRoute, true);
        }
        else scoredJob = new ScoredJob(unassignedJob, score, best, bestRoute, false);
        return scoredJob;
    }


    static double score(Job unassignedJob, InsertionData best, InsertionData secondBest, ScoringFunction scoringFunction) {
        if(best == null){
            throw new IllegalStateException("cannot insert job " +  unassignedJob.getId());
        }
        double score;
        if(secondBest == null){ //either there is only one vehicle or there are more vehicles, but they cannot load unassignedJob
            //if only one vehicle, I want the job to be inserted with min iCosts
            //if there are more vehicles, I want this job to be prioritized since there are no alternatives
            score = Integer.MAX_VALUE - best.getInsertionCost() + scoringFunction.score(best, unassignedJob);
        }
        else{
            score = (secondBest.getInsertionCost()-best.getInsertionCost()) + scoringFunction.score(best, unassignedJob);
        }
        return score;
    }


}
