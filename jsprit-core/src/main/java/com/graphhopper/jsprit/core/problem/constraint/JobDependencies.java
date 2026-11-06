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
package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;

/**
 * Which jobs the regret insertion treats as dependent on others (inserted together, see
 * {@link DependencyType}), indexed by job index. Lived inside ConstraintManager although it is
 * not a constraint; ConstraintManager keeps its accessors and delegates here.
 */
public class JobDependencies {

    private final VehicleRoutingProblem vrp;

    private final DependencyType[] dependencyTypes;

    public JobDependencies(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
        this.dependencyTypes = new DependencyType[vrp.getJobs().size() + 1];
    }

    public DependencyType[] getDependencyTypes() {
        return dependencyTypes;
    }

    public void setDependencyType(String jobId, DependencyType dependencyType) {
        Job job = vrp.getJobs().get(jobId);
        if (job != null) {
            dependencyTypes[vrp.getJobIndex(job)] = dependencyType;
        }
    }

    public DependencyType getDependencyType(String jobId) {
        Job job = vrp.getJobs().get(jobId);
        if (job != null) {
            return dependencyTypes[vrp.getJobIndex(job)];
        }
        return DependencyType.NO_TYPE;
    }
}
