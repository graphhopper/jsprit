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
package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmEndsListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class SolutionVerifier implements AlgorithmEndsListener {

    @Override
    public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {

        for (VehicleRoutingProblemSolution solution : solutions) {
            Set<Job> jobsInSolution = new HashSet<Job>();
            for (VehicleRoute route : solution.getRoutes()) {
                jobsInSolution.addAll(route.getTourActivities().getJobs());
            }
            if (jobsInSolution.size() != problem.getJobs().size()) {
                throw new IllegalStateException("we are at the end of the algorithm and still have not found a valid solution." +
                    "This cannot be.");
            }
        }

    }

}
