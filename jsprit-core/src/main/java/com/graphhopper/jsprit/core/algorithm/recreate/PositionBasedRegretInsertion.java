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
 * Position-based regret insertion strategy.
 *
 * <p>Unlike route-based regret which considers only the best insertion per route,
 * position-based regret considers ALL feasible insertion positions across all routes.
 * This approach was ranked #1 in Voigt et al. 2025 meta-analysis of LNS operators.</p>
 *
 * <p>The key difference from {@link RegretInsertionFast}:</p>
 * <ul>
 *   <li>Route-based: compares best positions across routes (one position per route)</li>
 *   <li>Position-based: compares individual positions across all routes (many positions per route)</li>
 * </ul>
 *
 * <p>This typically yields better solution quality at the cost of more computation,
 * as it has finer granularity for the regret calculation.</p>
 *
 * @author schroeder
 */
public class PositionBasedRegretInsertion extends AbstractInsertionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PositionBasedRegretInsertion.class);

    private RegretKScoringFunction scoringFunction;

    private int k = 2;

    private final JobInsertionCostsCalculator insertionCostsCalculator;

    public PositionBasedRegretInsertion(JobInsertionCostsCalculator jobInsertionCalculator,
                                        VehicleRoutingProblem vehicleRoutingProblem) {
        super(vehicleRoutingProblem);
        this.insertionCostsCalculator = jobInsertionCalculator;
        this.scoringFunction = new RegretKScoringFunctionAdapter(
                new DefaultRegretScoringFunction(new DefaultScorer(vehicleRoutingProblem)));
        logger.debug("initialise {}", this);
    }

    /**
     * Sets the number of positions to consider for regret calculation.
     *
     * @param k number of positions (-1 or Integer.MAX_VALUE for all)
     */
    public void setK(int k) {
        this.k = k;
    }

    /**
     * Sets the scoring function for regret calculation.
     *
     * @param scoringFunction the scoring function
     */
    public void setScoringFunction(RegretKScoringFunction scoringFunction) {
        this.scoringFunction = scoringFunction;
    }

    @Override
    public String toString() {
        return "[name=positionBasedRegretInsertion][k=" + k + "]";
    }

    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<>(unassignedJobs.size());
        Set<Job> jobs = new LinkedHashSet<>(unassignedJobs);

        while (!jobs.isEmpty()) {
            ScoredJob bestScoredJob = findBestJobToInsert(routes, jobs);

            if (bestScoredJob == null) {
                break;
            }

            if (bestScoredJob instanceof ScoredJob.BadJob) {
                Job unassigned = bestScoredJob.getJob();
                jobs.remove(unassigned);
                badJobs.add(unassigned);
                markUnassigned(unassigned, bestScoredJob.getInsertionData().getFailedConstraintNames());
            } else {
                if (bestScoredJob.isNewRoute()) {
                    routes.add(bestScoredJob.getRoute());
                }
                insertJob(bestScoredJob.getJob(), bestScoredJob.getInsertionData(), bestScoredJob.getRoute());
                jobs.remove(bestScoredJob.getJob());
            }
        }

        return badJobs;
    }

    private ScoredJob findBestJobToInsert(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        ScoredJob bestScoredJob = null;
        List<ScoredJob> badJobs = new ArrayList<>();

        for (Job job : unassignedJobs) {
            ScoredJob scoredJob = Scorer.scoreUnassignedJobPositionBased(
                    routes, job, insertionCostsCalculator, scoringFunction, k);

            if (scoredJob instanceof ScoredJob.BadJob) {
                badJobs.add(scoredJob);
                continue;
            }

            if (bestScoredJob == null || scoredJob.getScore() > bestScoredJob.getScore()) {
                bestScoredJob = scoredJob;
            }
        }

        // If no valid insertion found, return the first bad job (if any)
        if (bestScoredJob == null && !badJobs.isEmpty()) {
            return badJobs.get(0);
        }

        return bestScoredJob;
    }
}
