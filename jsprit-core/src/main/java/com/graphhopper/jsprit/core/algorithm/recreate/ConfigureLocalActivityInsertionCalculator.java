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
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;

/**
 * Created by schroeder on 22/07/15.
 */
public class ConfigureLocalActivityInsertionCalculator implements InsertionStartsListener, JobInsertedListener {

    private VehicleRoutingProblem vrp;

    private LocalActivityInsertionCostsCalculator localActivityInsertionCostsCalculator;

    private int nuOfJobsToRecreate;

    public ConfigureLocalActivityInsertionCalculator(VehicleRoutingProblem vrp, LocalActivityInsertionCostsCalculator localActivityInsertionCostsCalculator) {
        this.vrp = vrp;
        this.localActivityInsertionCostsCalculator = localActivityInsertionCostsCalculator;
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        this.nuOfJobsToRecreate = unassignedJobs.size();
        double completenessRatio = (1 - ((double) nuOfJobsToRecreate / (double) vrp.getJobs().values().size()));
        localActivityInsertionCostsCalculator.setSolutionCompletenessRatio(Math.max(0.5, completenessRatio));
    }

    @Override
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
        nuOfJobsToRecreate--;
        double completenessRatio = (1 - ((double) nuOfJobsToRecreate / (double) vrp.getJobs().values().size()));
        localActivityInsertionCostsCalculator.setSolutionCompletenessRatio(Math.max(0.5, completenessRatio));
    }
}
