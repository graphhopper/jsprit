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
package com.graphhopper.jsprit.core.algorithm.ruin.listener;

import com.graphhopper.jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;


/**
 * Listener that listens to the ruin-process. It informs whoever is interested about start, end and about a removal of a job.
 *
 * @author schroeder
 */
public interface RuinListener extends SearchStrategyModuleListener {

    /**
     * informs about ruin-start.
     *
     * @param routes
     */
    public void ruinStarts(Collection<VehicleRoute> routes);

    /**
     * informs about ruin-end.
     *
     * @param routes
     * @param unassignedJobs
     */
    public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs);

    /**
     * informs if a {@link Job} has been removed from a {@link VehicleRoute}.
     *
     * @param job
     * @param fromRoute
     */
    public void removed(Job job, VehicleRoute fromRoute);

}
