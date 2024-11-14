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
}
