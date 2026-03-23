/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Insertion based on regret approach.
 * <p>
 * <p>Basically calculates the insertion cost of the firstBest and the secondBest alternative. The score is then calculated as difference
 * between secondBest and firstBest, plus additional scoring variables that can defined in this.ScoringFunction.
 * The idea is that if the cost of the secondBest alternative is way higher than the first best, it seems to be important to insert this
 * customer immediatedly. If difference is not that high, it might not impact solution if this customer is inserted later.
 *
 * @author stefan schroeder
 */
public class RegretInsertion extends AbstractInsertionStrategy {


    private static final Logger logger = LoggerFactory.getLogger(RegretInsertion.class);

    private final JobInsertionCostsCalculator insertionCostsCalculator;

    private RegretScoringFunction regretScoringFunction;


    public void setRegretScoringFunction(RegretScoringFunction regretScoringFunction) {
        this.regretScoringFunction = regretScoringFunction;
    }

    /**
     * Sets the scoring function.
     * <p>
     * <p>By default, the this.TimeWindowScorer is used.
     *
     * @param scoringFunction to score
     */
    public void setScoringFunction(ScoringFunction scoringFunction) {
        this.regretScoringFunction = new DefaultRegretScoringFunction(scoringFunction);
    }

    public RegretInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem) {
        super(vehicleRoutingProblem);
        this.regretScoringFunction = new DefaultRegretScoringFunction(new DefaultScorer(vehicleRoutingProblem));
        this.insertionCostsCalculator = jobInsertionCalculator;
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=regretInsertion][additionalScorer=" + regretScoringFunction + "]";
    }


    /**
     * Runs insertion.
     * <p>
     * <p>Before inserting a job, all unassigned jobs are scored according to its best- and secondBest-insertion plus additional scoring variables.
     */
    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<>(unassignedJobs.size());

        // Use LinkedHashSet for O(1) removal while preserving insertion order
        Set<Job> jobs = new LinkedHashSet<>(unassignedJobs);

        // Handle breaks first (without modifying the input collection)
        for (Job job : unassignedJobs) {
            if (job.getJobType().isBreak()) {
                VehicleRoute route = findRoute(routes, job);
                if (route == null) {
                    badJobs.add(job);
                } else {
                    InsertionData iData = insertionCostsCalculator.getInsertionData(route, job, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
                    if (iData instanceof InsertionData.NoInsertionFound) {
                        badJobs.add(job);
                    } else {
                        insertJob(job, iData, route);
                    }
                }
                jobs.remove(job);
            }
        }

        while (!jobs.isEmpty()) {
            List<ScoredJob> badJobList = new ArrayList<>();
            ScoredJob bestScoredJob = getBestScoredUnassignedJob(routes, jobs, badJobList);
            if (bestScoredJob != null) {
                if (bestScoredJob.isNewRoute()) {
                    routes.add(bestScoredJob.getRoute());
                }
                insertJob(bestScoredJob.getJob(), bestScoredJob.getInsertionData(), bestScoredJob.getRoute());
                jobs.remove(bestScoredJob.getJob());
            }
            for (ScoredJob bad : badJobList) {
                Job unassigned = bad.getJob();
                jobs.remove(unassigned);
                badJobs.add(unassigned);
                markUnassigned(unassigned, bad.getInsertionData().getFailedConstraintNames());
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

    private ScoredJob getBestScoredUnassignedJob(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs, List<ScoredJob> badJobs) {
        ScoredJob bestScoredJob = null;
        for (Job unassignedJob : unassignedJobs) {
            ScoredJob scoredJob = Scorer.scoreUnassignedJob(routes, unassignedJob, insertionCostsCalculator, regretScoringFunction);
            if (scoredJob instanceof ScoredJob.BadJob) {
                badJobs.add(scoredJob);
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


}
