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

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.List;

/**
 * Created by schroeder on 15/10/15.
 */
class ScoredJob {

    static class BadJob extends ScoredJob {

        BadJob(Job job, List<String> failedConstraintNames) {
            super(job, 0., getEmptyInsertion(failedConstraintNames), null, false);
        }

        private static InsertionData getEmptyInsertion(List<String> failedConstraintNames) {
            InsertionData empty = new InsertionData.NoInsertionFound();
            empty.getFailedConstraintNames().addAll(failedConstraintNames);
            return empty;
        }
    }

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
