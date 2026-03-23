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

import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Created by schroeder on 24/05/16.
 */
class Scorer {

    final static double NO_NEW_DEPARTURE_TIME_YET = -12345.12345;

    final static Vehicle NO_NEW_VEHICLE_YET = null;

    final static Driver NO_NEW_DRIVER_YET = null;


    static ScoredJob scoreUnassignedJob(Collection<VehicleRoute> routes, Job unassignedJob, JobInsertionCostsCalculator insertionCostsCalculator, RegretScoringFunction scoringFunction) {
        InsertionData best = null;
        InsertionData secondBest = null;
        VehicleRoute bestRoute = null;
        List<String> failedConstraintNames = new ArrayList<>();
        double benchmark = Double.MAX_VALUE;
        for (VehicleRoute route : routes) {
            if (secondBest != null) {
                benchmark = secondBest.getInsertionCost();
            }
            InsertionData iData = insertionCostsCalculator.getInsertionData(route, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, benchmark);
            if (iData instanceof InsertionData.NoInsertionFound) {
                failedConstraintNames.addAll(iData.getFailedConstraintNames());
                continue;
            }
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
        } else failedConstraintNames.addAll(iData.getFailedConstraintNames());
        if (best == null) {
            ScoredJob.BadJob badJob = new ScoredJob.BadJob(unassignedJob, failedConstraintNames);
            return badJob;
        }
        double score = scoringFunction.score(best, secondBest, unassignedJob);
        ScoredJob scoredJob;
        if (bestRoute == emptyRoute) {
            scoredJob = new ScoredJob(unassignedJob, score, best, bestRoute, true);
        } else scoredJob = new ScoredJob(unassignedJob, score, best, bestRoute, false);
        return scoredJob;
    }

    /**
     * Scores an unassigned job by collecting k-best insertion alternatives.
     *
     * @param routes                   the collection of vehicle routes to consider
     * @param unassignedJob            the job to score
     * @param insertionCostsCalculator calculator for insertion costs
     * @param scoringFunction          the k-best scoring function
     * @param k                        the number of alternatives to consider (-1 or Integer.MAX_VALUE for all)
     * @return a ScoredJob with the regret-k score
     */
    static ScoredJob scoreUnassignedJobWithKBest(Collection<VehicleRoute> routes, Job unassignedJob,
                                                 JobInsertionCostsCalculator insertionCostsCalculator,
                                                 RegretKScoringFunction scoringFunction, int k) {
        List<RegretKAlternatives.Alternative> allAlternatives = new ArrayList<>();
        List<String> failedConstraintNames = new ArrayList<>();

        // Collect all feasible insertions from existing routes
        for (VehicleRoute route : routes) {
            InsertionData iData = insertionCostsCalculator.getInsertionData(route, unassignedJob,
                    NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
            if (iData instanceof InsertionData.NoInsertionFound) {
                failedConstraintNames.addAll(iData.getFailedConstraintNames());
                continue;
            }
            allAlternatives.add(new RegretKAlternatives.Alternative(iData, route));
        }

        // Consider empty route (new vehicle)
        VehicleRoute emptyRoute = VehicleRoute.emptyRoute();
        InsertionData iData = insertionCostsCalculator.getInsertionData(emptyRoute, unassignedJob,
                NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
        if (!(iData instanceof InsertionData.NoInsertionFound)) {
            allAlternatives.add(new RegretKAlternatives.Alternative(iData, emptyRoute));
        } else {
            failedConstraintNames.addAll(iData.getFailedConstraintNames());
        }

        // No feasible insertions found
        if (allAlternatives.isEmpty()) {
            return new ScoredJob.BadJob(unassignedJob, failedConstraintNames);
        }

        // Sort by cost and take top-k
        allAlternatives.sort(Comparator.comparingDouble(RegretKAlternatives.Alternative::getCost));
        int effectiveK = (k <= 0) ? allAlternatives.size() : Math.min(k, allAlternatives.size());
        List<RegretKAlternatives.Alternative> topK = allAlternatives.subList(0, effectiveK);

        RegretKAlternatives alternatives = new RegretKAlternatives(topK);

        // Score using the k-best alternatives
        double score = scoringFunction.score(alternatives, unassignedJob);

        RegretKAlternatives.Alternative best = alternatives.getBest();
        VehicleRoute bestRoute = best.getRoute();

        ScoredJob scoredJob;
        if (bestRoute == emptyRoute) {
            scoredJob = new ScoredJob(unassignedJob, score, best.getInsertionData(), bestRoute, true);
        } else {
            scoredJob = new ScoredJob(unassignedJob, score, best.getInsertionData(), bestRoute, false);
        }
        return scoredJob;
    }
}
