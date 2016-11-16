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

package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by schroeder on 04/08/15.
 */
public class RuinBreaks implements RuinListener {

    private final static Logger logger = LoggerFactory.getLogger(RuinBreaks.class);

    @Override
    public void ruinStarts(Collection<VehicleRoute> routes) {
    }

    @Override
    public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        for (VehicleRoute r : routes) {
            Break aBreak = r.getVehicle().getBreak();
            if (aBreak != null) {
                r.getTourActivities().removeJob(aBreak);
                logger.trace("ruin: {}", aBreak.getId());
                unassignedJobs.add(aBreak);
            }
        }
    }

    @Override
    public void removed(Job job, VehicleRoute fromRoute) {
    }
}
