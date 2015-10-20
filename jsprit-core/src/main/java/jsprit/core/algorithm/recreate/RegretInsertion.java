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

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Break;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Insertion based on regret approach.
 * <p/>
 * <p>Basically calculates the insertion cost of the firstBest and the secondBest alternative. The score is then calculated as difference
 * between secondBest and firstBest, plus additional scoring variables that can defined in this.ScoringFunction.
 * The idea is that if the cost of the secondBest alternative is way higher than the first best, it seems to be important to insert this
 * customer immediatedly. If difference is not that high, it might not impact solution if this customer is inserted later.
 *
 * @author stefan schroeder
 */
public class RegretInsertion extends AbstractInsertionStrategy {



    private static Logger logger = LogManager.getLogger(RegretInsertionFast.class);

    private ScoringFunction scoringFunction;

    private JobInsertionCostsCalculator insertionCostsCalculator;


    /**
     * Sets the scoring function.
     * <p/>
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
        return "[name=regretInsertion][additionalScorer=" + scoringFunction + "]";
    }


    /**
     * Runs insertion.
     * <p/>
     * <p>Before inserting a job, all unassigned jobs are scored according to its best- and secondBest-insertion plus additional scoring variables.
     */
    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<Job>(unassignedJobs.size());

        Iterator<Job> jobIterator = unassignedJobs.iterator();
        while (jobIterator.hasNext()){
            Job job = jobIterator.next();
            if(job instanceof Break){
                VehicleRoute route = findRoute(routes,job);
                if(route == null){
                    badJobs.add(job);
                }
                else {
                    InsertionData iData = insertionCostsCalculator.getInsertionData(route, job, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
                    if (iData instanceof InsertionData.NoInsertionFound) {
                        badJobs.add(job);
                    } else {
                        insertJob(job, iData, route);
                    }
                }
                jobIterator.remove();
            }
        }

        List<Job> jobs = new ArrayList<Job>(unassignedJobs);
        while (!jobs.isEmpty()) {
            List<Job> unassignedJobList = new ArrayList<Job>(jobs);
            List<Job> badJobList = new ArrayList<Job>();
            ScoredJob bestScoredJob = nextJob(routes, unassignedJobList, badJobList);
            if (bestScoredJob != null) {
                if (bestScoredJob.isNewRoute()) {
                    routes.add(bestScoredJob.getRoute());
                }
                insertJob(bestScoredJob.getJob(), bestScoredJob.getInsertionData(), bestScoredJob.getRoute());
                jobs.remove(bestScoredJob.getJob());
            }
            for (Job bad : badJobList) {
                jobs.remove(bad);
                badJobs.add(bad);
            }
        }
        return badJobs;
    }

    private VehicleRoute findRoute(Collection<VehicleRoute> routes, Job job) {
        for(VehicleRoute r : routes){
            if(r.getVehicle().getBreak() == job) return r;
        }
        return null;
    }

    private ScoredJob nextJob(Collection<VehicleRoute> routes, Collection<Job> unassignedJobList, List<Job> badJobs) {
        ScoredJob bestScoredJob = null;
        for (Job unassignedJob : unassignedJobList) {
            ScoredJob scoredJob = getScoredJob(routes, unassignedJob, insertionCostsCalculator, scoringFunction);
            if (scoredJob instanceof ScoredJob.BadJob) {
                badJobs.add(unassignedJob);
                continue;
            }
            if (bestScoredJob == null) bestScoredJob = scoredJob;
            else {
                if (scoredJob.getScore() > bestScoredJob.getScore()) {
                    bestScoredJob = scoredJob;
                } else if (scoredJob.getScore() == bestScoredJob.getScore()) {
                    if (scoredJob.getJob().getId().compareTo(bestScoredJob.getJob().getId()) <= 0) {
                        bestScoredJob = scoredJob;
                    }
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
        if (best == null) {
            return new ScoredJob.BadJob(unassignedJob);
        }
        double score = score(unassignedJob, best, secondBest, scoringFunction);
        ScoredJob scoredJob;
        if (bestRoute == emptyRoute) {
            scoredJob = new ScoredJob(unassignedJob, score, best, bestRoute, true);
        } else scoredJob = new ScoredJob(unassignedJob, score, best, bestRoute, false);
        return scoredJob;
    }


    static double score(Job unassignedJob, InsertionData best, InsertionData secondBest, ScoringFunction scoringFunction) {
        if (best == null) {
            throw new IllegalStateException("cannot insert job " + unassignedJob.getId());
        }
        double score;
        if (secondBest == null) { //either there is only one vehicle or there are more vehicles, but they cannot load unassignedJob
            //if only one vehicle, I want the job to be inserted with min iCosts
            //if there are more vehicles, I want this job to be prioritized since there are no alternatives
            score = Integer.MAX_VALUE - best.getInsertionCost() + scoringFunction.score(best, unassignedJob);
        } else {
            score = (secondBest.getInsertionCost() - best.getInsertionCost()) + scoringFunction.score(best, unassignedJob);
        }
        return score;
    }


}
