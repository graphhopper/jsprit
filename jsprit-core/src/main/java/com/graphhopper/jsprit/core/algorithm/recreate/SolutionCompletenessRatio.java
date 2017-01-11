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

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;

/**
 * Created by schroeder on 11/01/17.
 */
class SolutionCompletenessRatio implements InsertionStartsListener, JobInsertedListener {

    protected double solutionCompletenessRatio = 0.5;

    private final int nuOfJobs;

    private int nuOfJobsToRecreate;

    public SolutionCompletenessRatio(int nuOfJobs) {
        this.nuOfJobs = nuOfJobs;
    }

    public void setSolutionCompletenessRatio(double ratio) {
        solutionCompletenessRatio = ratio;
    }

    public double getSolutionCompletenessRatio() {
        return solutionCompletenessRatio;
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        this.nuOfJobsToRecreate = unassignedJobs.size();
        solutionCompletenessRatio = (1 - ((double) nuOfJobsToRecreate / (double) nuOfJobs));
    }

    @Override
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
        nuOfJobsToRecreate--;
        solutionCompletenessRatio = (1 - ((double) nuOfJobsToRecreate / (double) nuOfJobs));
    }
}
